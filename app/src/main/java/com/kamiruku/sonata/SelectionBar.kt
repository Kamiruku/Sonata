package com.kamiruku.sonata

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PhotoAlbum
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.kamiruku.sonata.navigation.Navigator
import com.kamiruku.sonata.navigation.SonataRoute
import com.kamiruku.sonata.ui.components.SongDetailsDialog
import com.kamiruku.sonata.utils.findFirstIndex

@Composable
fun SelectionBar(
    viewModel: SharedViewModel,
    state: NavigationState,
    navigator: Navigator
) {
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    val selectedItems = viewModel.selectedItems

    val songList by viewModel.songList.collectAsState()
    val allPaths = songList.mapNotNull { it.song?.path }

    val filteredSongs = viewModel.filteredSongs

    val currentStack = state.backStacks[state.topLevelRoute]
    val currentRoute = currentStack?.last()
    val flat = remember(currentRoute) {
        when (val currentRoute = currentStack?.last()) {
            is SonataRoute.Folder -> {
                val curPath = currentRoute.path
                val curNode = viewModel.findNode(curPath) ?: return@remember emptyList()
                val startIndex = allPaths.findFirstIndex(curPath)
                allPaths.subList(startIndex, startIndex + curNode.musicTotal)
            }
            is SonataRoute.AllSongs -> {
                allPaths
            }
            is SonataRoute.Search -> {
                filteredSongs.map { it.path }
            }
            else -> emptyList()
        }
    }

    val BUTTON_SIZE = 60.dp

    val inSelectionMode by viewModel.inSelectionMode.collectAsState()

    BackHandler(enabled = inSelectionMode) {
        viewModel.clearSelected()
    }

    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary.copy(0.8f), RoundedCornerShape(8.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = { }
            )
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background.copy(0.5f))
                .padding(horizontal = 25.dp)
        ) {
            Text(
                text =
                    if (selectedItems.containsAll(flat)) "Remove All"
                    else "Select All",
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterStart)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        if (selectedItems.containsAll(flat)) {
                            viewModel.clearSelected(true)
                        } else {
                            viewModel.setSelected(flat)
                        }
                    }
            )

            Text(
                text = selectedItems.size.toString(),
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )

            IconButton(
                onClick = {
                    viewModel.clearSelected()
                },
                modifier = Modifier.align(Alignment.CenterEnd).scale(0.8f)
            ) {
                Icon(
                    Icons.Outlined.Close,
                    "close"
                )
            }
        }

        Box(
            Modifier.background(MaterialTheme.colorScheme.background.copy(0.8f))
        ) {
            Column {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp)
                        .padding(top = 5.dp),
                    horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        enabled = viewModel.selectedItems.isNotEmpty(),
                        onClick = { },
                        modifier = Modifier.size(BUTTON_SIZE)
                    ) {
                        IconLabel(Icons.AutoMirrored.Outlined.PlaylistAdd, "Playlist")
                    }

                    IconButton(
                        enabled = viewModel.selectedItems.isNotEmpty(),
                        onClick = { },
                        modifier = Modifier.size(BUTTON_SIZE)
                    ) {
                        IconLabel(Icons.AutoMirrored.Outlined.QueueMusic, "Queue")
                    }

                    IconButton(
                        enabled = viewModel.selectedItems.isNotEmpty(),
                        onClick = { },
                        modifier = Modifier.size(BUTTON_SIZE)
                    ) {
                        IconLabel(Icons.Outlined.PlayArrow, "Play Next")
                    }
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp)
                        .padding(top = 5.dp, bottom = 35.dp),
                    horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        enabled = viewModel.selectedItems.size == 1,
                        onClick = {
                            selectedSong = viewModel.findNode(viewModel.selectedItems.single())?.song
                        },
                        modifier = Modifier.size(BUTTON_SIZE)
                    ) {
                        IconLabel(Icons.Outlined.Info, "Info")
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
                        },
                        modifier = Modifier.size(BUTTON_SIZE)
                    ) {
                        IconLabel(Icons.Outlined.Folder, "Folder")
                    }

                    IconButton(
                        enabled = viewModel.selectedItems.size == 1,
                        onClick = { },
                        modifier = Modifier.size(BUTTON_SIZE)
                    ) {
                        IconLabel(Icons.Outlined.PhotoAlbum, "Album")
                    }
                }
            }
        }
    }

    SongDetailsDialog(
        song = selectedSong,
        onDismiss = { selectedSong = null }
    )
}

@Composable
fun IconLabel(icon: ImageVector, text: String) {
    Column {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = text,
            fontSize = 9.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}