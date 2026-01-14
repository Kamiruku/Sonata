package com.kamiruku.sonata.features.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.kamiruku.sonata.FileNode
import com.kamiruku.sonata.utils.getAlbumArt
import com.kamiruku.sonata.utils.toTime

@Composable
fun FolderHeader(
    node: FileNode
) {
    val context = LocalContext.current
    val imageRequest = remember(node.albumId, context) {
        ImageRequest.Builder(context)
            .data(getAlbumArt(albumId = node.albumId))
            .size(1200)
            .crossfade(true)
            .build()
    }

    Box(
        modifier = Modifier
            .height(200.dp)
            .fillMaxWidth()
            /*
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
             */
    ) {
        if (node.albumId != 0L) {
            AsyncImage(
                model = imageRequest,
                contentDescription = "Top folder album art",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }

        /*
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    MaterialTheme.colorScheme.surface,
                    androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                )
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                .clickable { onBack() }
        ) {
            Text(
                text = "← Back",
                modifier = Modifier
                    .padding(8.dp)
            )
        }
         */

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surface,
                        androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                    )
            ) {
                Text(
                    text = node.name,
                    fontSize = 22.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(6.dp)
                        .basicMarquee()
                )
            }

            Box(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surface,
                        androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                    )
            ) {
                Text(
                    text = "${node.musicTotal} | ${node.durationTotal.toTime()}",
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(6.dp)
                )
            }
        }
    }
}