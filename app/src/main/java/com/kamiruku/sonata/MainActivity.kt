package com.kamiruku.sonata

import Song
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val locationPermissionRequest =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) {}

        if (ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.READ_MEDIA_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(Manifest.permission.READ_MEDIA_AUDIO)
            return
        }

        val filePaths = getAudioFiles()
        FileTreeBuilder.buildTree(filePaths)
    }

    fun getAudioFiles(): List<String> {
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val audioCursor = contentResolver.query(musicUri, null, null, null, null)

        val audioList = mutableListOf<Song>()
        val filePaths = mutableListOf<String>()

        if (audioCursor != null && audioCursor.moveToFirst()) {
            val idColumn: Int = audioCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val titleColumn = audioCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistColumn: Int = audioCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val dataColumn: Int = audioCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val albumColumn: Int = audioCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)

            do {
                audioList.add(
                    Song(
                        songID = audioCursor.getLong(idColumn),
                        songTitle = audioCursor.getString(titleColumn),
                        songArtist = audioCursor.getString(artistColumn),
                        songData = audioCursor.getString(dataColumn),
                        songAlbum = audioCursor.getString(albumColumn)
                    )
                )
                filePaths.add(audioCursor.getString(dataColumn))
            } while (audioCursor.moveToNext())

        }
        audioCursor!!.close()

        return filePaths
    }
}