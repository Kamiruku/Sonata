package com.kamiruku.sonata.features.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kamiruku.sonata.FileNode
import com.kamiruku.sonata.Song
import com.kamiruku.sonata.features.library.components.FileListItem
import com.kamiruku.sonata.features.library.components.FolderHeader
import com.kamiruku.sonata.utils.getAllSongPaths

@Composable
fun FolderScreen(
    listState: LazyListState,
    node: FileNode,
    allPaths: List<String>,
    selectedItems: Set<String>,
    inSelectionMode: Boolean,
    onToggleSelect: (String) -> Unit,
    onToggleSelectFolder: (List<String>) -> Unit,
    onOpen: (FileNode) -> Unit,
    onPlay: (Song) -> Unit
) {
    Box {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 50.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = node.absolutePath) {
                FolderHeader(node)
            }

            items(
                node.children.values.toList(),
                key = { it.absolutePath }
            ) { child ->
                val flat = remember(child.absolutePath, allPaths) {
                    if (child.isFolder) child.getAllSongPaths(allPaths)
                    else emptyList()
                }

                val isSelected = if (child.isFolder) {
                    flat.isNotEmpty() && selectedItems.containsAll(flat)
                } else {
                    child.song?.path in selectedItems
                }

                val onClick: () -> Unit = {
                    if (child.isFolder) {
                        if (inSelectionMode) onToggleSelectFolder(flat)
                        else onOpen(child)
                    } else {
                        if (inSelectionMode) child.song?.let { onToggleSelect(it.path) }
                        else child.song?.let(onPlay)
                    }
                }

                val onLongClick: () -> Unit = {
                    if (child.isFolder) {
                        if (!inSelectionMode) onToggleSelectFolder(flat)
                    } else {
                        if (!inSelectionMode) child.song?.let { onToggleSelect(it.path) }
                    }
                }

                FileListItem(
                    isSelected = isSelected,
                    inSelectionMode = inSelectionMode,
                    node = child,
                    onClick = onClick,
                    onLongClick = onLongClick
                )
            }

            item {
                AnimatedVisibility(inSelectionMode) {
                    Spacer(Modifier.padding(75.dp))
                }
            }
        }
    }
}