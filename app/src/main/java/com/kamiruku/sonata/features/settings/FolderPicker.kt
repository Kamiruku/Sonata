package com.kamiruku.sonata.features.settings

import android.content.Context
import android.os.Environment
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun FolderPickerDialog(
    context: Context,
    callback: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val currentPath = remember { mutableStateOf<File>(Environment.getExternalStorageDirectory()) }
    val parentPath = remember { mutableStateOf(currentPath.value.parentFile) }
    val foldersList = remember { mutableStateOf<List<File>>(emptyList()) }
    val showRootSelector = remember { mutableStateOf(false) }

    LaunchedEffect(currentPath.value) {
        val loadedFolders = withContext(Dispatchers.IO) {
            currentPath.value.listFiles()
                ?.filter { it.isDirectory }
                ?.sortedBy { it.name.lowercase() }
                ?: emptyList()
        }
        foldersList.value = loadedFolders
        parentPath.value = withContext(Dispatchers.IO) {
            currentPath.value.parentFile
        }
    }

    val rootPaths = remember {
        val dirs = context.getExternalFilesDirs(null)

        val storages = mutableSetOf<File>()
        storages.add(Environment.getExternalStorageDirectory())

        dirs.filterNotNull().forEach { dir ->
            val root = dir.absolutePath.split("/Android/")[0]
            val file = File(root)
            if (file.exists()) {
                storages.add(file)
            }
        }
        storages.toList()
    }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            //dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
        ) {
            Column {
                Text(
                    "Select a folder",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(20.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 20.dp)
                ) {
                    val root = rootPaths.find { currentPath.value.startsWith(it.absolutePath) }

                    val displayPath = if (root != null) {
                        currentPath.value.absolutePath.replace(
                            root.absolutePath,
                            if (root.absolutePath.contains("emulated")) "Internal" else "SD Card"
                        )
                    } else {
                        currentPath.value.absolutePath
                    }
                    val split = displayPath.split('/')

                    Text(
                        text = split[0],
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = rootPaths.size > 1
                        ) {
                            showRootSelector.value = true
                        }
                    )

                    if (split.size > 1) {
                        val style = SpanStyle(
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )

                        val text = buildAnnotatedString {
                            withStyle(style) {
                                append(" > ")
                            }

                            for (i in 1 until split.size) {
                                append(split[i])

                                if (i != split.lastIndex) {
                                    withStyle(style) {
                                        append(" > ")
                                    }
                                }
                            }
                        }

                        Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    /*
                    if (currentPath.value.parentFile?.exists() == true && currentPath.value !in rootPaths) {
                        item {
                            Text(
                                "...",
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        currentPath.value = parentPath.value ?: return@clickable
                                    }
                                    .padding(horizontal = 20.dp)
                                    .padding(vertical = 12.dp)
                            )
                        }
                    }
                     */

                    items(foldersList.value, key = { it.path }) { file ->
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentPath.value = file
                                }
                                .padding(vertical = 10.dp)
                                .padding(horizontal = 20.dp)
                        ) {
                            Text(file.name)
                        }
                    }
                }

                Row(
                    Modifier.padding(25.dp)
                ) {
                    Spacer(Modifier.weight(1f))

                    TextButton(onClick = { onDismiss() }) {
                        Text("Cancel")
                    }

                    TextButton(onClick = { callback(currentPath.value.absolutePath) }) {
                        Text("OK")
                    }
                }
            }
        }

        BackHandler(enabled = !showRootSelector.value) {
            if (currentPath.value.parentFile?.exists() == true && currentPath.value !in rootPaths) {
                currentPath.value = parentPath.value ?: return@BackHandler
            } else if (currentPath.value in rootPaths) {
                onDismiss()
            }
        }
    }

    if (showRootSelector.value) {
        AlertDialog(
            onDismissRequest = { showRootSelector.value = false },
            title = { Text("Select Storage") },
            text = {
                Column {
                    rootPaths.forEach { root ->
                        val isSelected = currentPath.value.absolutePath.startsWith(root.absolutePath)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentPath.value = root
                                    showRootSelector.value = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(text = if (root.absolutePath.contains("emulated")) "Internal Storage" else "SD Card")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRootSelector.value = false }) { Text("Cancel") }
            }
        )
    }
}