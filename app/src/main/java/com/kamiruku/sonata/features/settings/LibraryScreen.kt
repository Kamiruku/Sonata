package com.kamiruku.sonata.features.settings

import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LibraryScreen() {
    val context = LocalContext.current
    val paths = mutableStateSetOf<String>()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val path = uri.getRealPath()

        val parentFolder = paths.filter { path.startsWith(it) }
        if (parentFolder.isNotEmpty()) {
            Toast.makeText(context, "Folder/parent folder already included.", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }

        val childrenFolder = paths.filter { it.startsWith(path) }
        if (childrenFolder.isNotEmpty()) {
            Toast.makeText(context, "${childrenFolder.size} ${if (childrenFolder.size > 1) "entries" else "entry"} are children of the new folder and will be removed.", Toast.LENGTH_SHORT).show()
            paths.removeAll(childrenFolder)
        }

        paths.add(path)
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
                    fontSize = 18.sp
                )

                IconButton(
                    onClick = { launcher.launch(null) },
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
}
private fun Uri.getAbsolutePath(): String {
    val uriPath = this.getPath()
    var filePath = Environment.getExternalStorageDirectory().absolutePath
    if (uriPath != null) {
        val x = uriPath.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (x.size > 1) filePath = filePath + "/" + x[1]
    }
    return filePath
}

private fun Uri.getRealPath(): String {
    val path = this.path ?: ""
    val pathSplit = path.replace("/tree", "").removePrefix("/").split('/')
    if (pathSplit[0].take(7) == "primary") {
        return this.getAbsolutePath()
    } else {
        val new = "/storage" + path.replace("/tree", "").replace(":", "/")
        return new
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