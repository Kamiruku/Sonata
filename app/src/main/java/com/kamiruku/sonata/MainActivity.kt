package com.kamiruku.sonata

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : ComponentActivity() {
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

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

        val reycView = findViewById<RecyclerView>(R.id.recycView)
        reycView.layoutManager = LinearLayoutManager(this)

        val audioList = getAudioFilesViaMediaStore()
        Log.d("FILE AMOUNT", audioList.size.toString())
        val rootNode = FileTreeBuilder.buildTree(audioList)

        val adapter = FileRecyclerAdapter(mutableListOf(rootNode))
        reycView.adapter = adapter
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
        val audioCursor = contentResolver.query(
            musicUri,
            null,
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
                            duration = audioCursor.getLong(durationColumn)
                        )
                    }
                }
            }
        }
        audioCursor!!.close()

        return audioList
    }
}