package com.kamiruku.sonata

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.kamiruku.sonata.ui.theme.SonataTheme


class MainActivity : FragmentActivity() {
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private val viewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                loadMusic()
            } else {
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
        val audioList = getAudioFilesViaMediaStore()
        Log.d("FILE AMOUNT", audioList.size.toString())
        val rootNode = FileTreeBuilder.buildTree(audioList)
        viewModel.setList(rootNode)
    }

    fun getAudioFilesViaMediaStore(): List<Song> {
        //.nomedia affected
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM,
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
            val titleColumn = audioCursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE)
            val artistColumn: Int = audioCursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST)
            val relativePathColumn: Int = audioCursor.getColumnIndex(MediaStore.Audio.AudioColumns.RELATIVE_PATH)
            val albumColumn: Int = audioCursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM)
            val durationColumn: Int = audioCursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION)
            val albumIdColumn: Int = audioCursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID)
            val displayNameColumn: Int = audioCursor.getColumnIndex(MediaStore.Audio.AudioColumns.DISPLAY_NAME)

            cursor.apply {
                if (count == 0) Log.d("Cursor", "get cursor data: Cursor is empty.")
                else {
                    while (cursor.moveToNext()) {
                        try {
                            val iD = cursor.getLong(idColumn)
                            val title = cursor.getString(titleColumn)
                            val artist = cursor.getString(artistColumn)
                            val path = cursor.getString(relativePathColumn) + cursor.getString(displayNameColumn)
                            val album = cursor.getString(albumColumn)
                            val duration = cursor.getLong(durationColumn)
                            val albumId = cursor.getLong(albumIdColumn)

                            audioList += Song(
                                iD = iD,
                                title = title,
                                artist = artist,
                                path = path,
                                album = album,
                                duration = duration,
                                albumId = albumId,
                                track = -1,
                                disc = -1,
                                year = "-1"
                            )
                        } catch (e: Exception) {
                            Log.e("Cursor read", "ERR", e)
                        }

                    }
                }
            }
        }
        audioCursor.close()


        return audioList
    }
}