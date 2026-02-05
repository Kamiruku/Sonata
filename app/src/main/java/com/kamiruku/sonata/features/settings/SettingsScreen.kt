package com.kamiruku.sonata.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kamiruku.sonata.FileNode
import com.kamiruku.sonata.Song

@Composable
fun SettingsScreen(
    onGeneralClick: () -> Unit,
    onLibraryClick: () -> Unit,
    onAudioClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(25.dp)
    ) {
        item {
            Text(
                text = "Settings",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 30.dp),
                fontSize = 22.sp
            )
        }

        item {
            Text(
                text = "General",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onGeneralClick() }
                    .padding(vertical = 22.dp),
                fontSize = 18.sp
            )
        }

        item {
            Text(
                text = "Library",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLibraryClick() }
                    .padding(vertical = 22.dp),
                fontSize = 18.sp
            )
        }

        item {
            Text(
                text = "Audio & Playback",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAudioClick() }
                    .padding(vertical = 22.dp),
                fontSize = 18.sp
            )
        }

        item {
            Text(
                text = "About",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAboutClick() }
                    .padding(vertical = 22.dp),
                fontSize = 18.sp
            )
        }
    }
}