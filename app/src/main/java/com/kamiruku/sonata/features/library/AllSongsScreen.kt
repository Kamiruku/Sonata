package com.kamiruku.sonata.features.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kamiruku.sonata.FileNode
import com.kamiruku.sonata.Song
import com.kamiruku.sonata.features.library.components.FileListItem

@Composable
fun AllSongsScreen(
    listState: LazyListState,
    songList: List<FileNode>,
    selectedItems: Set<String>,
    inSelectionMode: Boolean,
    onToggleSelect: (String) -> Unit,
    onPlay: (Song) -> Unit,
) {
    Box {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 25.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "All Songs",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp)
                        .padding(vertical = 30.dp),
                    fontSize = 22.sp
                )
            }

            items(
                items = songList,
                key = { it.absolutePath }
            ) { node ->
                val isSelected = node.song?.path in selectedItems

                FileListItem(
                    isSelected = isSelected,
                    inSelectionMode = inSelectionMode,
                    node = node,
                    onClick = {
                        if (inSelectionMode) {
                            node.song?.let { onToggleSelect(it.path) }
                        } else {
                            node.song?.let(onPlay)
                        }
                    },
                    onLongClick = {
                        if (!inSelectionMode) {
                            node.song?.let { onToggleSelect(it.path) }
                        }
                    }
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