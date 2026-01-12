package com.kamiruku.sonata.features.library.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
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
import com.kamiruku.sonata.FileNode
import com.kamiruku.sonata.utils.getAlbumArt
import com.kamiruku.sonata.utils.toTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileListItem(
    node: FileNode,
    onClick: (FileNode) -> Unit,
    onLongClick: (FileNode) -> Unit
) {
    /*
    Row for individual file/folder
        Async on far left
        Column splits right into top and bottom
            top - name
            bottom - extension, length, etc
    */
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick(node) },
                onLongClick = { onLongClick(node) }
            )
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val context = LocalContext.current
        val imageRequest = remember(node.albumId, context) {
            ImageRequest.Builder(context)
                .data(getAlbumArt(albumId = node.albumId))
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
                text = if (!node.isFolder) {
                    node.song?.title ?: "Unknown"
                } else {
                    node.name
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    //TODO implement marquee check
                    .basicMarquee(),
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Clip

            )

            val subText = remember(node.sortId) {
                if (!node.isFolder) {
                    val ext = node.song?.path?.substring(
                        node.song?.path?.lastIndexOf('.')?.plus(1) ?: 0
                    )
                    val hr =
                        (node.song?.bitsPerSample ?: 0) >= 24 ||
                                (node.song?.sampleRate ?: 0) >= 96000
                    if (hr) "${ext?.uppercase()} | HR | ${node.song?.duration?.toTime()}"
                    else "${ext?.uppercase()} | ${node.song?.duration?.toTime()}"
                } else {
                    "${node.musicTotal} | ${node.durationTotal.toTime()}"
                }
            }

            Row {
                Text(
                    text = subText,
                    modifier = Modifier.weight(1f),
                    fontSize = 16.sp,
                    maxLines = 1
                )

                if (node.isFolder) {
                    Icon(
                        imageVector = Icons.Outlined.Folder,
                        contentDescription = "folder",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}