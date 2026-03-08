package com.kamiruku.sonata.features.nowPlaying

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kamiruku.sonata.SharedViewModel
import com.kamiruku.sonata.utils.getAlbumArt
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.pow

@Composable
fun PlayerScreen(viewModel: SharedViewModel, closePlayer: () -> Unit) {
    val context = LocalContext.current

    val pagerState = rememberPagerState(
        //random page
        initialPage = 850,
        pageCount = { viewModel.songList.value.size }
    )

    LaunchedEffect(pagerState.currentPage) {
        viewModel
        //check current pos then change?...
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(
                onClick = {},
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
    ) {
        Spacer(Modifier.height(30.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) { page ->
            val song = viewModel.songList.value[page].song ?: return@HorizontalPager

            val imageRequest = remember(song.albumId) {
                ImageRequest.Builder(context)
                    .data(getAlbumArt(albumId = song.albumId))
                    .crossfade(true)
                    .build()
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        val pageOffset = (pagerState.currentPage - page + pagerState.currentPageOffsetFraction).coerceIn(-1f, 1f)
                        val pageAlpha = ((1f - pageOffset.absoluteValue).pow(0.5f)).coerceIn(0f, 1f)

                        translationX = pageOffset * 150.dp.toPx()
                        alpha = pageAlpha.absoluteValue
                    }
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(0.9f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .clickable(onClick = closePlayer)
                    ) {
                        AsyncImage(
                            model = imageRequest,
                            contentDescription = "Album art",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = song.album,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )
                }
            }
        }

        Spacer(Modifier.height(30.dp))

        Row(
            Modifier
                .fillMaxWidth(0.85f)
                .align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { }) {
                Icon(
                    Icons.Outlined.AccessTime,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "timer"
                )
            }

            IconButton(onClick = { }) {
                Icon(
                    Icons.Outlined.Shuffle,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "shuffle"
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        val coroutineScope = rememberCoroutineScope()

        Row(
            Modifier
                .fillMaxWidth(0.85f)
                .align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {
                coroutineScope.launch {
                    pagerState.scrollToPage(pagerState.currentPage - 1)
                }
            }) {
                Icon(
                    Icons.Outlined.SkipPrevious,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "previous",
                    modifier = Modifier.scale(1.2f)
                )
            }

            IconButton(
                onClick = { },
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
            ) {
                Icon(
                    Icons.Outlined.PlayArrow,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    contentDescription = "play",
                    modifier = Modifier.scale(1.2f)
                )
            }

            IconButton(onClick = {
                coroutineScope.launch {
                    pagerState.scrollToPage(pagerState.currentPage + 1)
                }
            }) {
                Icon(
                    Icons.Outlined.SkipNext,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "next",
                    modifier = Modifier.scale(1.2f)
                )
            }
        }

        Spacer(Modifier.height(100.dp))

        Row(
            Modifier
                .fillMaxWidth(0.85f)
                .align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            //current pos
            Text(
                text = 0.00.toString()
            )

            //metadata
            Text(
                text = "${0.00} kHz ${0} kbps flac",
                color = MaterialTheme.colorScheme.primary
            )

            //song length
            Text(
                text = 0.00.toString()
            )
        }
    }

    BackHandler { closePlayer() }
}