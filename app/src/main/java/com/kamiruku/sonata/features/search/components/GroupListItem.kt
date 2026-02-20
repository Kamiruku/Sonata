package com.kamiruku.sonata.features.search.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kamiruku.sonata.Song
import com.kamiruku.sonata.utils.getAlbumArt
import com.kamiruku.sonata.utils.toTime

@OptIn(ExperimentalFoundationApi::class)
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
                    onClick = { onClick() },
                    onLongClick = { onLongClick() }
                )
                .padding(horizontal = 25.dp)
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val context = LocalContext.current
            val imageRequest = remember(firstSong.albumId, context) {
                ImageRequest.Builder(context)
                    .data(getAlbumArt(albumId = firstSong.albumId))
                    .size(128)
                    .crossfade(true)
                    .build()
            }

            AsyncImage(
                model = imageRequest,
                contentDescription = "Album art",
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

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

                val duration = remember(title, list) {
                    list.sumOf { it.duration }.toTime()
                }

                val subText = remember(title, list) {
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
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(35.dp)
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(4.dp))
            ) {
                Icon(
                    imageVector =
                        if (isSelected) { Icons.Outlined.CheckBox }
                        else { Icons.Outlined.CheckBoxOutlineBlank },
                    contentDescription =
                        if (isSelected) { "selected" }
                        else { "not_selected" },
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            if (isSelected) {
                Box(
                    Modifier
                        .padding(vertical = 16.dp)
                        .padding(horizontal = 25.dp)
                        .size(70.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(0.3f))
                )
            }
        }
    }
}