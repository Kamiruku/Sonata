package com.kamiruku.sonata.features.library

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.samples.apps.nowinandroid.core.designsystem.component.scrollbar.DraggableScrollbar
import com.google.samples.apps.nowinandroid.core.designsystem.component.scrollbar.rememberDraggableScroller
import com.google.samples.apps.nowinandroid.core.designsystem.component.scrollbar.scrollbarState
import com.kamiruku.sonata.features.library.components.FileListItem
import com.kamiruku.sonata.FileNode
import com.kamiruku.sonata.features.library.components.FolderHeader
import com.kamiruku.sonata.rememberDirectionalLazyListState
import com.kamiruku.sonata.state.ScrollDirection

@Composable
fun FolderScreen(
    node: FileNode,
    onOpen: (FileNode) -> Unit,
    onBack: () -> Unit,
    onPlay: (FileNode) -> Unit,
    openDetails: (FileNode) -> Unit,
    onScrollDirectionChanged: (Boolean) -> Unit
) {
    val listState = rememberLazyListState()
    val directionState = rememberDirectionalLazyListState(listState)

    val atTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
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
            contentPadding = PaddingValues(
                top = 50.dp,
                bottom = 50.dp,
                start = 25.dp,
                end = 25.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = node.sortId) {
                FolderHeader(node, onBack)
            }

            items(
                node.children.values.toList(),
                key = { it.sortId }
            ) { child ->
                if (child.isFolder) {
                    FileListItem(
                        node = child,
                        onClick = { onOpen(child) },
                        onLongClick = {}
                    )
                } else {
                    FileListItem(
                        node = child,
                        onClick = { onPlay(child) },
                        onLongClick = { openDetails(child) }
                    )
                }
            }
        }

        //... some arbitrary size
        if (node.children.size > 25) {
            val scrollBarState = listState.scrollbarState(node.children.size)
            val onDrag = listState.rememberDraggableScroller(node.children.size)

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