package com.kamiruku.sonata.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kamiruku.sonata.FileNode
import com.kamiruku.sonata.utils.toTime

@Composable
fun SongDetailsDialog(
    file: FileNode?,
    onDismiss: () -> Unit
) {
    if (file == null)  return
    val song = file.song ?: return

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(20.dp)
                ) {
                    Text(text = "Info/Tags", style = MaterialTheme.typography.titleLarge)

                    Text(text = song.path, style = MaterialTheme.typography.bodyLarge)

                    Text(
                        text = "${song.duration.div(1000)} secs (${song.duration.toTime()})",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    InfoLabel("Title", song.title)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 25.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoLabel("Track", "${song.track}")
                        InfoLabel("Disc", "${song.disc}")
                        InfoLabel("Year", song.year)
                    }

                    InfoLabel("Album", song.album)

                    InfoLabel("Artist", song.artist)

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun InfoLabel(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}