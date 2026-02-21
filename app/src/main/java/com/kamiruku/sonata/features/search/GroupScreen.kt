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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.request.ImageRequest
import com.kamiruku.sonata.Song
import com.kamiruku.sonata.features.search.components.SongListItem
import com.kamiruku.sonata.ui.components.ContentHeader
import com.kamiruku.sonata.utils.getAlbumArt
import com.kamiruku.sonata.utils.toTime

@Composable fun GroupScreen(
    listState: LazyListState,
    title: String,
    list: List<Song>,
    selectedItems: Set<String>,
    inSelectionMode: Boolean,
    onToggleSelect: (String) -> Unit,
    onPlay: (Song) -> Unit
) {
    val context = LocalContext.current

    val firstSong = list.first()
    val imageRequest = remember(firstSong.albumId, context) {
        ImageRequest.Builder(context)
            .data(getAlbumArt(albumId = firstSong.albumId))
            .size(1200)
            .crossfade(true)
            .build()
    }

    val duration = remember(title, list) {
        list.sumOf { it.duration }.toTime()
    }

    val subText = remember(title, list) {
        "${list.size} | ${duration}"
    }

    Box {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 50.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                ContentHeader(imageRequest, title, subText)
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