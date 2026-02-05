package com.kamiruku.sonata.features.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.kamiruku.sonata.SharedViewModel

@Composable
fun LibraryScreen(viewModel: SharedViewModel) {
    val pathSrcs by viewModel.pathSrcs.collectAsState()

    val context = LocalContext.current
    val paths = remember { mutableStateSetOf<String>() }
    val showDialog = remember { mutableStateOf(false) }

    //Should perform overlapping check here...?
    LaunchedEffect(pathSrcs) {
        paths.addAll(pathSrcs)
    }

    if (showDialog.value) {
        FolderPickerDialog(
            context = context,
            callback = { path ->
                val parentFolder = paths.filter { path.startsWith(it) }
                if (parentFolder.isNotEmpty()) {
                    Toast.makeText(context, "Folder/parent folder already included.", Toast.LENGTH_SHORT).show()
                    return@FolderPickerDialog
                }

                val childrenFolder = paths.filter { it.startsWith(path) }
                if (childrenFolder.isNotEmpty()) {
                    Toast.makeText(context, "${childrenFolder.size} ${if (childrenFolder.size > 1) "entries" else "entry"} are children of the new folder and will be removed.", Toast.LENGTH_SHORT).show()
                    paths.removeAll(childrenFolder)
                }
                paths.add(path)
                showDialog.value = false
            },
            onDismiss = { showDialog.value = false }
        )
    }

    Box(Modifier.fillMaxSize()) {
        Column {
            Text(
                text = "Library Settings",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 25.dp, top = 55.dp),
                fontSize = 22.sp
            )

            Row(
                Modifier
                    .padding(top = 27.dp)
                    .padding(horizontal = 25.dp)
            ) {
                Text(
                    text = "Selected Folders",
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.secondary
                )

                IconButton(
                    onClick = { showDialog.value = true },
                    Modifier.align(Alignment.CenterVertically)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "add")
                }
            }

            LazyColumn(Modifier.padding(start = 30.dp, end = 25.dp, top = 20.dp)) {
                items(paths.toList()) { path ->
                    PathLabels(path) { paths.remove(path) }
                }
            }
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        viewModel.savePathSrcs(paths)
    }
}

@Composable
fun PathLabels(path: String, onRemove: () -> Unit) {
    Row {
        Text(
            text = path,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f)
        )

        IconButton(
            onClick = { onRemove() },
            Modifier.align(Alignment.CenterVertically)
        ) {
            Icon(Icons.Outlined.Remove, contentDescription = "remove")
        }
    }
}