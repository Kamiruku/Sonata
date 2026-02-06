package com.kamiruku.sonata

import android.content.ContentResolver
import android.content.ContentUris
import android.provider.MediaStore
import android.util.Log
import com.kamiruku.sonata.db.SongEntity
import com.kamiruku.sonata.db.SongRepository
import com.kamiruku.sonata.taglib.TagLib
import com.kamiruku.sonata.taglib.TagLibObject

class MediaStoreSource(private val contentResolver: ContentResolver) {
    suspend fun syncLibrary(repository: SongRepository, pathSrcs: Set<String>): Boolean {
        val mediaStoreFiles = getMediaStoreFiles(pathSrcs)

        Log.d("SongSync", "Found ${mediaStoreFiles.size} songs in MediaStore")
        Log.d("SongSync", "DB has ${repository.getSongCount()} songs")

        val existingFiles = repository.getDatePathMap()
        val newFiles = mediaStoreFiles.filter { file ->
            val knownDate = existingFiles[file.path]
            knownDate == null || knownDate != file.dateModified
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
        if (newSongs.isNotEmpty()) {
            repository.insertSongs(newSongs)
            Log.d("SongSync", "${newSongs.size} songs has been added to the db")
        }

        val mediaStorePaths = mediaStoreFiles.map { it.path }.toSet()
        val deletedPaths = existingFiles.keys - mediaStorePaths
        if (deletedPaths.isNotEmpty()) {
            repository.deleteByPaths(deletedPaths)
            Log.d("SongSync", "${deletedPaths.size} songs has been removed from the db")
        }

        return newSongs.isNotEmpty() || deletedPaths.isNotEmpty()
    }

    fun getMediaStoreFiles(pathSrcs: Set<String>): List<MediaStoreFile> {
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

        for (path in pathSrcs) {
            val audioCursor = contentResolver.query(
                musicUri,
                projection,
                "${MediaStore.Audio.Media.DATA} LIKE ?",
                arrayOf("%$path%"),
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
        }
        return fileList
    }
    fun getSongDetailsTagLib(id: Long, albumId: Long, relativePath: String, fileName: String, dateModified: Long, size: Long): SongEntity? {
        val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
        contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
            val fd = pfd.detachFd()

            val audioDetails = TagLib.getDetails(fd, fileName)
                ?: TagLibObject(-1,-1,-1,-1, -1, HashMap())
            val metadata = audioDetails.propertyMap

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

            val duration = audioDetails.lengthInMilliseconds.toLong()
            val bitrate = audioDetails.bitrate
            val sampleRate = audioDetails.sampleRate
            val channels = audioDetails.channels
            val bitsPerSample = audioDetails.bitsPerSample

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