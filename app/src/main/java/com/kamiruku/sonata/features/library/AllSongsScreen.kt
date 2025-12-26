package com.kamiruku.sonata.features.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kamiruku.sonata.FileNode
import com.kamiruku.sonata.features.library.components.FileListItem
import com.kamiruku.sonata.rememberDirectionalLazyListState
import com.kamiruku.sonata.state.ScrollDirection

@Composable
fun AllSongsScreen(
    songList: List<FileNode>,
    onScrollDirectionChanged: (Boolean) -> Unit,
    onPlay: (FileNode) -> Unit,
    openDetails: (FileNode) -> Unit,
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

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(25.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "All Songs",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 30.dp),
                fontSize = 22.sp
            )
        }

        items(
            items = songList,
            key = { it.sortId }
        ) { node ->
            FileListItem(
                node = node,
                onClick = { onPlay(node) },
                onLongClick = { openDetails(node) }
            )
        }
    }
}