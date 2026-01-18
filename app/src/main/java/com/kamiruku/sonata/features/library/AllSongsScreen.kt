package com.kamiruku.sonata.features.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.samples.apps.nowinandroid.core.designsystem.component.scrollbar.DraggableScrollbar
import com.google.samples.apps.nowinandroid.core.designsystem.component.scrollbar.rememberDraggableScroller
import com.google.samples.apps.nowinandroid.core.designsystem.component.scrollbar.scrollbarState
import com.kamiruku.sonata.FileNode
import com.kamiruku.sonata.Song
import com.kamiruku.sonata.features.library.components.FileListItem
import com.kamiruku.sonata.rememberDirectionalLazyListState
import com.kamiruku.sonata.state.ScrollDirection

@Composable
fun AllSongsScreen(
    selectedItems: Set<String>,
    inSelectionMode: Boolean,
    onToggleSelect: (String) -> Unit,
    songList: List<FileNode>,
    onScrollDirectionChanged: (Boolean) -> Unit,
    onPlay: (Song) -> Unit,
) {
    val listState = rememberLazyListState()
    val directionState = rememberDirectionalLazyListState(listState)

    val atTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
    }

    val isBiggerThanScreen by remember {
        derivedStateOf {
            listState.canScrollForward || listState.canScrollBackward
        }
    }

    LaunchedEffect(directionState.scrollDirection, atTop) {
        val shouldShow = when {
            atTop -> true
            directionState.scrollDirection == ScrollDirection.Up -> true
            directionState.scrollDirection == ScrollDirection.Down -> false
            else -> return@LaunchedEffect
        }
        onScrollDirectionChanged(shouldShow)
    }

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
                key = { it.sortId }
            ) { node ->
                val isSelected = node.song?.path in selectedItems

                FileListItem(
                    isSelected = isSelected,
                    inSelectionMode = inSelectionMode,
                    node = node,
                    onClick = {
                        if (inSelectionMode) {
                            onToggleSelect(node.song?.path ?: "")
                        } else {
                            node.song?.let(onPlay)
                        }
                    },
                    onLongClick = {
                        if (!inSelectionMode) {
                            onToggleSelect(node.song?.path ?: "")
                        }
                    }
                )
            }

            if (isBiggerThanScreen) {
                item {
                    AnimatedVisibility(inSelectionMode) {
                        Spacer(Modifier.padding(75.dp))
                    }
                }
            }
        }

        //... some arbitrary size
        if (songList.size > 25) {
            val scrollBarState = listState.scrollbarState(songList.size)
            val onDrag = listState.rememberDraggableScroller(songList.size)

            listState.DraggableScrollbar(
                state = scrollBarState,
                orientation = Orientation.Vertical,
                onThumbMoved = { percent -> onDrag(percent) },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(top = 100.dp, end = 4.dp, bottom = 150.dp)
            )
        }
    }
}