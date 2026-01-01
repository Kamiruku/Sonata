package com.kamiruku.sonata

import android.content.ContentResolver
import android.content.ContentUris
import android.provider.MediaStore
import android.util.Log
import com.kamiruku.sonata.db.SongEntity
import com.kamiruku.sonata.db.SongRepository
import com.kamiruku.sonata.taglib.TagLib

class MediaStoreSource(private val contentResolver: ContentResolver) {
    suspend fun syncLibrary(repository: SongRepository) {
        val mediaStoreFiles = getMediaStoreFiles()

        Log.d("SongSync", "Found ${mediaStoreFiles.size} songs in MediaStore")
        Log.d("SongSync", "DB has ${repository.getSongCount()} songs")

        val newFiles = mediaStoreFiles.filter { file ->
            !repository.songExists(file.path, file.dateModified)
        }

        Log.d("SongSync", "Found ${newFiles.size} new songs, getting details from TagLib")

        val newSongs = newFiles.mapNotNull { file ->
            getSongDetailsTagLib(
                file.id,
                file.albumId,
                file.relativePath,
                file.fileName,
                file.dateModified,
                file.size
            )
        }
        Log.d("SongSync", "TagLib has gotten ${newSongs.size} details.")
        if (newSongs.isNotEmpty()) repository.insertSongs(newSongs)
    }

    fun getMediaStoreFiles(): List<MediaStoreFile> {
        //.nomedia affected
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.RELATIVE_PATH,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.SIZE
        )

        val fileList = mutableListOf<MediaStoreFile>()

        val audioCursor = contentResolver.query(
            musicUri,
            projection,
            "${MediaStore.Audio.Media.RELATIVE_PATH} LIKE ?",
            arrayOf("%Music%"),
            null
        ) ?: return emptyList()

        audioCursor.use { cursor ->
            val idColumn = cursor.getColumnIndex(MediaStore.Audio.AudioColumns._ID)
            val albumIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
            val relativePathColumn = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.RELATIVE_PATH)
            val displayNameColumn = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DISPLAY_NAME)
            val dateModifiedColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)
            val sizeColumn = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)

            cursor.apply {
                if (count == 0) Log.d("Cursor", "get cursor data: Cursor is empty.")
                else {
                    while (cursor.moveToNext()) {
                        try {
                            val iD = cursor.getLong(idColumn)
                            val albumId = cursor.getLong(albumIdColumn)
                            val relativePath = cursor.getString(relativePathColumn)
                            val displayName = cursor.getString(displayNameColumn)
                            val dateModified = cursor.getLong(dateModifiedColumn) * 1000L
                            val size = cursor.getLong(sizeColumn)

                            fileList += MediaStoreFile(iD, albumId, relativePath, displayName, dateModified, size)
                        } catch (e: Exception) {
                            Log.e("Cursor read", "ERR", e)
                        }
                    }
                }
            }
        }
        return fileList
    }
    fun getSongDetailsTagLib(id: Long, albumId: Long, relativePath: String, fileName: String, dateModified: Long, size: Long): SongEntity? {
        val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
        contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
            val fd = pfd.detachFd()

            val prop = TagLib.getAudioProperties(fd)
            val metadata = TagLib.getMetadata(fd)

            //be as faithful to the tags as possible.
            val artists = metadata["ARTIST"] ?: emptyArray()
            val title = metadata["TITLE"]?.firstOrNull() ?: ""
            val album = metadata["ALBUM"]?.firstOrNull() ?: ""
            val date = metadata["DATE"]?.firstOrNull()?.takeIf { it.isNotBlank() }
                ?: metadata["YEAR"]?.firstOrNull()?.takeIf { it.isNotBlank() } ?: ""
            val albumArtist = metadata["ALBUMARTIST"]?.firstOrNull()?.takeIf { it.isNotBlank() }
                ?: metadata["ALBUM ARTIST"]?.firstOrNull()?.takeIf { it.isNotBlank() } ?: ""
            val trackString = (metadata["TRACKNUMBER"]?.firstOrNull()?.takeIf { it.isNotBlank() }
                ?: metadata["TRACK"]?.firstOrNull()?.takeIf { it.isNotBlank() }) ?: ""
            val discString = (metadata["DISCNUMBER"]?.firstOrNull()?.takeIf { it.isNotBlank() }
                ?: metadata["TPOS"]?.firstOrNull()?.takeIf { it.isNotBlank() }) ?: ""

            val duration = prop[0].toLong()
            val bitrate = prop[1]
            val sampleRate = prop[2]
            val channels = prop[3]
            val bitsPerSample = prop[4]

            return SongEntity(
                mediaStoreId = id,
                mediaStoreAlbumId = albumId,
                artists = artists,
                title = title,
                album = album,
                date = date,
                albumArtist = albumArtist,
                track = trackString,
                disc = discString,
                bitrate = bitrate,
                sampleRate = sampleRate,
                channels = channels,
                bitsPerSample = bitsPerSample,
                duration = duration,
                dateModified = dateModified,
                size = size,
                path = relativePath + fileName,
                folder = relativePath,
                fileName = fileName
            )
        }
        return null
    }
}

data class MediaStoreFile(
    val id: Long,
    val albumId: Long,
    val relativePath: String,
    val fileName: String,
    val dateModified: Long,
    val size: Long,
    val path: String = relativePath + fileName
)