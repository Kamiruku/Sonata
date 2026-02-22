package com.kamiruku.sonata.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kamiruku.sonata.utils.getAlbumArt

@Composable
fun ListAlbumArt(albumId: Long) {
    val context = LocalContext.current

    val imageRequest = remember(albumId) {
        ImageRequest.Builder(context)
            .data(getAlbumArt(albumId = albumId))
            .size(128)
            .crossfade(false)
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
}