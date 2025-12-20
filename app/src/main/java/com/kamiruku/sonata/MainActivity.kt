package com.kamiruku.sonata

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity


class MainActivity : FragmentActivity() {
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private val viewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

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

        val composeView = findViewById<ComposeView>(R.id.compose_view)
        composeView.setContent {
            LazyColumn(Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp) ) {
                items(mutableListOf(rootNode)) {
                    ListItem(it)
                }
            }
        }
    }

    @Composable
    fun ListItem(node: FileNode, modifier: Modifier = Modifier) {
        Row(
            modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        handleClick(node)
                    }
            ) {
                Text(
                    //Root will always be folder.
                    /*
                    text = if (!node.isFolder) {
                        node.song?.title ?: "Unknown"
                    } else {
                        "📁 ${node.name}"
                    },
                    */
                    modifier = Modifier
                        .fillMaxWidth()
                        .basicMarquee(animationMode = androidx.compose.foundation.MarqueeAnimationMode.Immediately),
                    text = node.name,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    //TODO remove hardcoded color
                    color = Color.White
                )

                Text(
                    /*
                    text = if (!node.isFolder) {
                        val extension = node.song?.path?.substring(
                            node.song.path?.lastIndexOf('.')?.plus(1) ?: 0
                        )
                        "${extension?.uppercase()} | ${node.song?.duration?.toTime()}"
                    } else {
                        "${node.musicTotal} | ${node.durationTotal.toTime()}"
                    },
                    */
                    text = "${node.musicTotal} | ${node.durationTotal.toTime()}",
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    //TODO remove hardcoded color
                    color = Color.White
                )
            }
        }
    }

    fun handleClick(node: FileNode) {
        if (node.isFolder && node.children.isNotEmpty()) {
            val fragment = HandleFragment().apply {
                arguments = Bundle().apply {
                    putInt("nodeSortId", node.sortId)
                }
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, fragment)
                .addToBackStack(null)
                .commit()
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