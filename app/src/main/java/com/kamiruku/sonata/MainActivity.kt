package com.kamiruku.sonata

import android.Manifest
import android.app.Application
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kamiruku.sonata.db.SongRepository
import com.kamiruku.sonata.ui.theme.SonataTheme


class MainActivity : FragmentActivity() {
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

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

        val songRepository = SongRepository(this@MainActivity)

        val viewModel: SharedViewModel by viewModels {
            SharedViewModelFactory(this@MainActivity.application, songRepository)
        }

        checkPermission({
            viewModel.loadCachedSongs()
            viewModel.syncMusic()
        })

        setContent {
            SonataTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    SonataApp(
                        viewModel = viewModel
                    )
                }

            }
        }
    }

    fun checkPermission(loadMusic: () -> Unit) {
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
}

class SharedViewModelFactory(
    private val application: Application,
    private val songRepository: SongRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SharedViewModel(application, songRepository) as T
    }
}