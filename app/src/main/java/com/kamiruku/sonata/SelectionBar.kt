package com.kamiruku.sonata

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.kamiruku.sonata.navigation.Navigator
import com.kamiruku.sonata.navigation.SonataRoute
import com.kamiruku.sonata.ui.components.SongDetailsDialog

@Composable
fun SelectionBar(
    viewModel: SharedViewModel,
    navigator: Navigator
) {
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    val selectedItems = viewModel.selectedItems

    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary.copy(0.5f))
    ) {
        Text(
            text = selectedItems.size.toString(),
            Modifier.padding(15.dp)
        )

        Row(
            Modifier
                .fillMaxWidth()
                .padding(25.dp)
                .padding(vertical = 25.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {
            IconButton(
                enabled = viewModel.selectedItems.size == 1,
                onClick = {
                    selectedSong = viewModel.findNode(viewModel.selectedItems.single())?.song
                }
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = "info"
                )
            }

            IconButton(
                enabled = viewModel.selectedItems.size == 1,
                onClick = {
                    val curPath = viewModel.selectedItems.single()
                    val splitPath = curPath.split('/').filter { it.isNotEmpty() }
                    val routes = mutableListOf<NavKey>()
                    routes.add(SonataRoute.LibraryHome)
                    routes.add(SonataRoute.FolderRoot)
                    for (i in 1 until splitPath.size) {
                        val subPath = splitPath.take(i)
                        val path = subPath.joinToString("/")
                        routes.add(SonataRoute.Folder(path))
                    }
                    navigator.navigateList(routes)
                }
            ) {
                Icon(
                    Icons.Outlined.Folder,
                    contentDescription = "folder"
                )
            }
        }
    }

    SongDetailsDialog(
        song = selectedSong,
        onDismiss = { selectedSong = null }
    )
}