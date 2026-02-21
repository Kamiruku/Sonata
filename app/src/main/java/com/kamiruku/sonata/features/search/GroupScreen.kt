package com.kamiruku.sonata.features.search

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kamiruku.sonata.Song
import com.kamiruku.sonata.features.library.components.GroupHeader
import com.kamiruku.sonata.features.search.components.SongListItem

@Composable fun GroupScreen(
    listState: LazyListState,
    title: String,
    list: List<Song>,
    selectedItems: Set<String>,
    inSelectionMode: Boolean,
    onToggleSelect: (String) -> Unit,
    onPlay: (Song) -> Unit
) {
    Box {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 50.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                GroupHeader(title, list)
            }

            items(
                list,
                key = { it.iD }
            ) { song ->
                val isSelected = song.path in selectedItems

                SongListItem(
                    isSelected = isSelected,
                    inSelectionMode = inSelectionMode,
                    song = song,
                    onClick = {
                        if (inSelectionMode) onToggleSelect(song.path)
                        else onPlay(song)
                    },
                    onLongClick = {
                        if (!inSelectionMode) onToggleSelect(song.path)
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