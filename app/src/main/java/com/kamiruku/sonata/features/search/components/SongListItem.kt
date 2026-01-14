package com.kamiruku.sonata.features.search.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun SongListItem(
    song: Song,
    onClick: (Song) -> Unit,
    onLongClick: (Song) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick(song) },
                onLongClick = { onLongClick(song) }
            )
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val context = LocalContext.current
        val imageRequest = remember(song.albumId, context) {
            ImageRequest.Builder(context)
                .data(getAlbumArt(albumId = song.albumId))
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
                text = song.title,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .basicMarquee(),
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )

            val subText = remember(song.iD) {
                val ext = song.path.substring(song.path.lastIndexOf('.').plus(1))
                val hr = song.bitsPerSample >= 24 || song.sampleRate >= 96000
                if (hr) "${ext.uppercase()} | HR | ${song.duration.toTime()}"
                else "${ext.uppercase()} | ${song.duration.toTime()}"
            }

            Text(
                text = subText,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                maxLines = 1
            )
        }
    }
}