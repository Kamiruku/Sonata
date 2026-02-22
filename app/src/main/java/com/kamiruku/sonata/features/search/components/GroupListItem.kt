package com.kamiruku.sonata.features.search.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kamiruku.sonata.Song
import com.kamiruku.sonata.ui.components.ListAlbumArt
import com.kamiruku.sonata.ui.components.SelectionOverlay
import com.kamiruku.sonata.utils.toTime

@Composable
fun GroupListItem(
    isSelected: Boolean = false,
    inSelectionMode: Boolean = false,
    title: String,
    list: List<Song>,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val firstSong = list.first()

    Box(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .padding(horizontal = 25.dp)
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ListAlbumArt(firstSong.albumId)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterVertically),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = title,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .basicMarquee(),
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )

                val subText = remember(title, list) {
                    val duration = list.sumOf { it.duration }.toTime()
                    "${list.size} | ${duration}"
                }

                Text(
                    text = subText,
                    modifier = Modifier.weight(1f),
                    fontSize = 16.sp,
                    maxLines = 1
                )
            }
        }

        if (inSelectionMode) {
            SelectionOverlay(isSelected)
        }
    }
}