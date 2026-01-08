package com.kamiruku.sonata

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
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
import com.kamiruku.sonata.db.SongEntity
import com.kamiruku.sonata.db.SongRepository
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
            val repository = SongRepository(this@MainActivity.applicationContext)
            val mediaStoreSource = MediaStoreSource(contentResolver)

            mediaStoreSource.syncLibrary(repository)
            val songList = repository.getAllSongs().map {
                it.toUiModel()
            }
            val rootNode = FileTreeBuilder.buildTree(songList)

            withContext(Dispatchers.Main) {
                viewModel.setList(rootNode)
            }
        }
    }

    fun SongEntity.toUiModel(): Song {
        return Song(
            iD = this.mediaStoreId,
            albumId = this.mediaStoreAlbumId,
            artists = this.artists,
            title = this.title,
            album = this.album,
            date = this.date,
            albumArtist = this.albumArtist,
            track = this.track,
            disc = this.disc,
            bitrate = this.bitrate,
            sampleRate = this.sampleRate,
            channels = this.channels,
            bitsPerSample = this.bitsPerSample,
            duration = this.duration,
            dateModified = this.dateModified,
            size = this.size,
            path = this.path
        )
    }
}