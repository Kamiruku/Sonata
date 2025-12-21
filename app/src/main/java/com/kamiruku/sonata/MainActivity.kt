package com.kamiruku.sonata

import android.Manifest
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
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.kamiruku.sonata.ui.theme.SonataTheme


class MainActivity : FragmentActivity() {
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private val viewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permissions.forEach { (permission, isGranted) ->
                if (!isGranted) {
                    Toast.makeText(this, "Permission denied: $permission", Toast.LENGTH_SHORT).show()
                }
            }
        }

        checkPermission()

        val audioList = getAudioFilesViaMediaStore()
        Log.d("FILE AMOUNT", audioList.size.toString())
        val rootNode = FileTreeBuilder.buildTree(audioList)

        viewModel.setList(rootNode)

        setContent {
            SonataTheme {
                SonataNavHost(
                    navController = rememberNavController(),
                    viewModel = viewModel)
            }
        }
    }

    fun checkPermission() {
        val permissionsNeeded = mutableListOf<String>()

        if (ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.READ_MEDIA_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.READ_MEDIA_AUDIO)
        }
        /*
        if (ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
             permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        */

        if (permissionsNeeded.isNotEmpty()) {
            permissionLauncher.launch(permissionsNeeded.toTypedArray()  )
        }
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
            MediaStore.Audio.Media.DATA
        )

        //TODO replace placeholder path
        val audioCursor = contentResolver.query(
            musicUri,
            projection,
            "${MediaStore.Audio.Media.DATA} LIKE ?",
            arrayOf("%E58E-9E76/Music%"),
            null)

        val audioList = mutableListOf<Song>()

        audioCursor?.use { cursor ->
            val idColumn: Int = audioCursor.getColumnIndex(MediaStore.Audio.AudioColumns._ID)
            val titleColumn = audioCursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE)
            val artistColumn: Int = audioCursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST)
            val dataColumn: Int = audioCursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA)
            val albumColumn: Int = audioCursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM)
            val durationColumn: Int = audioCursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION)
            val albumIdColumn: Int = audioCursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID)

            cursor.apply {
                if (count == 0) Log.d("Cursor", "get cursor data: Cursor is empty.")
                else {
                    while (audioCursor.moveToNext()) {
                        audioList += Song(
                            iD = audioCursor.getLong(idColumn),
                            title = audioCursor.getString(titleColumn),
                            artist = audioCursor.getString(artistColumn),
                            path = audioCursor.getString(dataColumn),
                            album = audioCursor.getString(albumColumn),
                            duration = audioCursor.getLong(durationColumn),
                            albumId = audioCursor.getLong(albumIdColumn)
                        )
                    }
                }
            }
        }
        audioCursor!!.close()

        return audioList
    }
}