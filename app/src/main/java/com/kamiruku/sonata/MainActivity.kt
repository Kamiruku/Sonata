package com.kamiruku.sonata

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.kamiruku.sonata.taglib.TagLib
import com.kamiruku.sonata.ui.theme.SonataTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : FragmentActivity() {
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private val viewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "Permission denied: READ_MEDIA_AUDIO", Toast.LENGTH_SHORT).show()

            }
        }

        checkPermission()

        setContent {
            SonataTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    SonataApp(
                        navController = rememberNavController(),
                        viewModel = viewModel
                    )
                }

            }
        }
    }

    fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            loadMusic()
        } else {
            permissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
        }
    }

    private fun loadMusic() {
        lifecycleScope.launch(Dispatchers.IO) {
            val audioList = getAudioFilesViaMediaStore()
            val rootNode = FileTreeBuilder.buildTree(audioList)

            withContext(Dispatchers.Main) {
                viewModel.setList(rootNode)
            }
        }
    }

    fun getAudioFilesViaMediaStore(): List<Song> {
        //.nomedia affected
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.RELATIVE_PATH,
            MediaStore.Audio.Media.DISPLAY_NAME
        )

        val audioList = mutableListOf<Song>()

        //TODO replace placeholder path
        val audioCursor = contentResolver.query(
            musicUri,
            projection,
            "${MediaStore.Audio.Media.RELATIVE_PATH} LIKE ?",
            arrayOf("%Music%"),
            null
        ) ?: return emptyList()

        audioCursor.use { cursor ->
            val idColumn: Int = audioCursor.getColumnIndex(MediaStore.Audio.AudioColumns._ID)
            val relativePathColumn: Int = audioCursor.getColumnIndex(MediaStore.Audio.AudioColumns.RELATIVE_PATH)
            val albumIdColumn: Int = audioCursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID)
            val displayNameColumn: Int = audioCursor.getColumnIndex(MediaStore.Audio.AudioColumns.DISPLAY_NAME)

            cursor.apply {
                println(count)
                if (count == 0) Log.d("Cursor", "get cursor data: Cursor is empty.")
                else {
                    while (cursor.moveToNext()) {
                        try {
                            val iD = cursor.getLong(idColumn)
                            val path = cursor.getString(relativePathColumn) + cursor.getString(displayNameColumn)
                            val albumId = cursor.getLong(albumIdColumn)

                            val song = getSongDetailsTagLib(iD, albumId, path)

                            if (song != null) audioList += song
                        } catch (e: Exception) {
                            Log.e("Cursor read", "ERR", e)
                        }
                    }
                }
            }
        }
        return audioList
    }

    fun getSongDetailsTagLib(id: Long, albumId: Long, path: String): Song? {
        val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
        contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
            val fd = pfd.detachFd()

            val prop = TagLib.getAudioProperties(fd)
            val metadata = TagLib.getMetadata(fd)

            //be as faithful to the tags as possible.
            val title = metadata["TITLE"]?.firstOrNull()?.takeIf { it.isNotBlank() } ?: ""
            val artist = metadata["ARTIST"]?.firstOrNull()?.takeIf { it.isNotBlank() } ?: ""
            val album = metadata["ALBUM"]?.firstOrNull()?.takeIf { it.isNotBlank() } ?: ""

            val date = metadata["DATE"]?.firstOrNull()?.takeIf { it.isNotBlank() }
                ?: metadata["YEAR"]?.firstOrNull()?.takeIf { it.isNotBlank() } ?: ""
            val trackString = (metadata["TRACKNUMBER"]?.firstOrNull()?.takeIf { it.isNotBlank() }
                ?: metadata["TRACK"]?.firstOrNull()?.takeIf { it.isNotBlank() }) ?: ""
            val discString = (metadata["DISCNUMBER"]?.firstOrNull()?.takeIf { it.isNotBlank() }
                ?: metadata["TPOS"]?.firstOrNull()?.takeIf { it.isNotBlank() }) ?: ""

            val duration = prop[0].toLong()
            val bitrate = prop[1]
            val sampleRate = prop[2]
            val channels = prop[3]
            val bitsPerSample = prop[4]

            println(bitsPerSample)

            return Song(
                iD = id,
                title = title,
                artist = artist,
                album = album,
                duration = duration,
                albumId = albumId,
                disc = discString,
                track = trackString,
                date = date,
                path = path,
                bitrate = bitrate,
                sampleRate = sampleRate,
                channels = channels
            )
        }
        return null
    }
}