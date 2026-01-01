package com.kamiruku.sonata.ui.components

import android.content.ContentUris
import android.provider.MediaStore
import android.text.format.Formatter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kamiruku.sonata.FileNode
import com.kamiruku.sonata.Song
import com.kamiruku.sonata.taglib.TagLib
import com.kamiruku.sonata.utils.toTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SongDetailsDialog(
    file: FileNode?,
    onDismiss: () -> Unit
) {
    if (file == null)  return
    val song = file.song ?: return

    val context = LocalContext.current
    var metadata by remember { mutableStateOf<Map<String, Array<String>>?>(null) }

    LaunchedEffect(song.iD) {
        launch(Dispatchers.IO) {
            val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.iD)
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                val fd = pfd.detachFd()

                val rawMetadata = TagLib.getMetadata(fd)
                withContext(Dispatchers.Main) {
                    metadata = rawMetadata.filter { it.value.isNotEmpty() }
                }
            }
        }
    }

    val preferredTags = listOf(
        "Artist" to listOf("ARTIST"),
        "Title" to listOf("TITLE"),
        "Album" to listOf("ALBUM"),
        "Date" to listOf("DATE", "YEAR"),
        "Genre" to listOf("GENRE"),
        "Composer" to listOf("COMPOSER"),
        "Performer" to listOf("PERFORMER"),
        "Album Artist" to listOf("ALBUMARTIST", "ALBUM ARTIST"),
        "Track" to listOf("TRACKNUMBER", "TRACK"),
        "Total Tracks" to listOf("TRACKTOTAL", "TOTALTRACKS", "TRACKCOUNT"),
        "Disc" to listOf("DISCNUMBER", "DISC"),
        "Total Discs" to listOf("DISCTOTAL", "TOTALDISCS"),
        "Comment" to listOf("COMMENT")
    )

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
                    .fillMaxHeight(0.75f)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Info/Tags",
                        style = MaterialTheme.typography.titleLarge
                    )

                    ShowLocation(song)

                    Spacer(modifier = Modifier.height(8.dp))

                    ShowGeneral(song)

                    Spacer(modifier = Modifier.height(8.dp))

                    val priorityTags = mutableListOf<Pair<String, Array<String>>>()
                    var lessImportant: MutableMap<String, Array<String>> = metadata?.toMutableMap() ?: mutableMapOf()

                    metadata?.also { map ->
                        for ((label, keys) in preferredTags) {
                            val keyFound = keys.firstOrNull { it in map } ?: continue
                            val value = map[keyFound]
                            if (!value.isNullOrEmpty()) {
                                priorityTags += label to value
                                lessImportant.remove(keyFound)
                            }
                        }
                    }
                    lessImportant = lessImportant.toSortedMap(String.CASE_INSENSITIVE_ORDER)

                    ShowMetadata(priorityTags, lessImportant)

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun ShowLocation(song: Song) {
    Text(
        text = "Location",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
    ShowLabel("Path", song.path)
    ShowLabel("Size", song.size.bytesToMB())
    ShowLabel("Modified", song.dateModified.toDateString())
}

@Composable
fun ShowGeneral(song: Song) {
    Text(
        text = "General",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
    ShowLabel("Duration", "${song.duration.toTime()} (${song.duration / 1000} s)")
    ShowLabel("Sample rate", "${song.sampleRate} Hz")
    ShowLabel("Channels", "${song.channels}")
    ShowLabel("Bits per sample", "${song.bitsPerSample}")
    ShowLabel("Bitrate", "${song.bitrate} kbps")
}

@Composable
fun ShowMetadata(priority: List<Pair<String, Array<String>>>, leftover: Map<String, Array<String>>) {
    Text(
        text = "Metadata",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )

    priority.forEach { (key, value) ->
        ShowLabel(key, value.joinToString(", "))
    }

    //the... uh unconventional tags
    leftover.forEach { (key, value) ->
        ShowLabel("<$key>", value.joinToString(", "))
    }
}

@Composable
fun ShowLabel(label: String, value: String) {
    Row {
        Text(label, modifier = Modifier.width(115.dp))
        Text(value)
    }
}

@Composable
private fun Long.bytesToMB(): String {
    return Formatter.formatFileSize(LocalContext.current, this)
}

private fun Long.toDateString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return sdf.format(Date(this))
}