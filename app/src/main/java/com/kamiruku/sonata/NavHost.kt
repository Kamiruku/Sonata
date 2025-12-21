package com.kamiruku.sonata

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun SonataNavHost(navController: NavHostController, viewModel: SharedViewModel) {
    val root = viewModel.getList() ?: return

    NavHost(
        navController = navController,
        startDestination = "root"
    ) {
        composable("root") {
            RootScreen(
                node = root,
                onOpen = { node ->
                    navController.navigate("folder/${node.sortId}")
                }
            )
        }

        composable(
            route = "folder/{id}",
            arguments = listOf(
                navArgument("id") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: return@composable
            val node = viewModel.findNode(id) ?: return@composable

            FolderScreen(
                node = node,
                onOpen = { child ->
                    navController.navigate("folder/${child.sortId}")
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun RootScreen(
    node: FileNode,
    onOpen: (FileNode) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 50.dp,
            bottom = 50.dp,
            start = 25.dp,
            end = 25.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            FileListItem(node) {
                if (node.isFolder) onOpen(node)
            }
        }
    }
}

@Composable
fun FolderScreen(
    node: FileNode,
    onOpen: (FileNode) -> Unit,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 50.dp,
            bottom = 50.dp,
            start = 25.dp,
            end = 25.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = node.sortId) {
            FolderHeader(node, onBack)
        }

        items(
            node.children.values.toList(),
            key = { it.sortId }
        ) { child ->
            FileListItem(child) {
                if (child.isFolder) onOpen(child)
            }
        }
    }
}

@Composable
fun FolderHeader(
    node: FileNode,
    onBack: () -> Unit
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
            .background(Color.Black, RoundedCornerShape(8.dp))
    ) {
        if (node.albumId != 0L) {
            AsyncImage(
                model = imageRequest,
                contentDescription = "Top folder album art",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Box(
            modifier = Modifier
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                .clickable { onBack() }
        ) {
            Text(
                text = "← Back",
                color = Color.White,
                modifier = Modifier
                    .padding(8.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        RoundedCornerShape(4.dp)
                    )
            ) {
                Text(
                    text = node.name,
                    color = Color.White,
                    fontSize = 22.sp,
                    modifier = Modifier
                        .padding(6.dp)
                )
            }

            Box(
                modifier = Modifier
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        RoundedCornerShape(4.dp)
                    )
            ) {
                Text(
                    text = "\uD834\uDD1E ${node.musicTotal} | ${node.durationTotal.toTime()}",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(6.dp)
                )
            }
        }
    }
}

@Composable
fun FileListItem(
    node: FileNode,
    onClick: () -> Unit
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
                onClick = onClick
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
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = if (!node.isFolder) {
                    node.song?.title ?: "Unknown"
                } else {
                    node.name
                },
                modifier = Modifier
                    .fillMaxWidth()
                    //TODO implement marquee check
                    .basicMarquee(),
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                //TODO remove hardcoded color
                color = Color.White
            )
            val subText = remember(node.sortId) {
                if (!node.isFolder) {
                    val ext = node.song?.path?.substring(
                        node.song?.path?.lastIndexOf('.')?.plus(1) ?: 0
                    )
                    "${ext?.uppercase()} | ${node.song?.duration?.toTime()}"
                } else {
                    "\uD834\uDD1E ${node.musicTotal} | ${node.durationTotal.toTime()}"
                }
            }
            Text(
                text = subText,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 16.sp,
                maxLines = 1,
                //TODO remove hardcoded color
                color = Color.White
            )
        }
    }
}

private fun getAlbumArt(albumId: Long): Uri =
    ContentUris.withAppendedId(
        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
        albumId)