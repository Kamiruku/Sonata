package com.kamiruku.sonata

import DirectionalLazyListState
import ScrollDirection
import android.annotation.SuppressLint
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.util.Locale

sealed class SonataRoute(val route: String) {

    data object Library : SonataRoute("library")

    data object LibraryHome : SonataRoute("library/home")

    data object FolderRoot : SonataRoute("library/folder_root")
    data object Folder : SonataRoute("library/folder/{id}") {
        fun create(id: Int) = "library/folder/$id"
    }
    data object AllSongs : SonataRoute("library/all_songs")

    data object NowPlaying : SonataRoute("now_playing")
    data object Settings : SonataRoute("settings")
    data object Search: SonataRoute("search")
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SonataApp(navController: NavHostController, viewModel: SharedViewModel) {
    var bottomBarVisible by remember { mutableStateOf(true) }
    val backStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(backStackEntry?.destination?.route) {
        bottomBarVisible = true
    }

    Scaffold(
        bottomBar = {
            AnimatedBottomBar(
                visible = bottomBarVisible,
                navController = navController
            )
        },
        content = { paddingValues ->
            SonataNavHost(
                navController = navController,
                viewModel = viewModel,
                onScrollDirectionChanged = { scrollingUp ->
                    bottomBarVisible = scrollingUp
                }
            )
        }
    )
}

@Composable
fun SonataNavHost(
    navController: NavHostController,
    viewModel: SharedViewModel,
    onScrollDirectionChanged: (Boolean) -> Unit
) {
    val root = viewModel.getRootNode() ?: return
    val songList = viewModel.getSongList()
    if (songList.isEmpty()) return

    NavHost(
        navController = navController,
        startDestination = SonataRoute.Library.route
    ) {
        navigation(
            route = SonataRoute.Library.route,
            startDestination = SonataRoute.LibraryHome.route
        ) {
            composable(SonataRoute.LibraryHome.route) {
                LibraryScreen(
                    onAllSongsClick = { navController.navigate(SonataRoute.AllSongs.route) },
                    onFolderClick = { navController.navigate(SonataRoute.FolderRoot.route) }
                )
            }

            composable(SonataRoute.AllSongs.route) {
                AllSongsScreen(
                    songList = songList,
                    onScrollDirectionChanged = onScrollDirectionChanged,
                    onPlay = { node ->
                        println("Clicked ${node.song?.title}")
                    },
                    openDetails = { node ->
                        println("Long clicked ${node.song?.title}")
                    }
                )
            }

            composable(SonataRoute.FolderRoot.route) {
                FileRootScreen(
                    node = root,
                    onOpen = { node ->
                        navController.navigate(SonataRoute.Folder.create(node.sortId))
                    }
                )
            }

            composable(
                route = SonataRoute.Folder.route,
                arguments = listOf(navArgument("id") {
                    type = NavType.IntType }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: return@composable
                val node = viewModel.findNode(id) ?: return@composable

                FolderScreen(
                    node = node,
                    onOpen = { child ->
                        navController.navigate(SonataRoute.Folder.create(child.sortId))
                    },
                    onPlay = { node ->
                        println("Clicked ${node.song?.title}")
                    },
                    openDetails = { node ->
                        println("Long clicked ${node.song?.title}")
                    },
                    onBack = { navController.popBackStack() },
                    onScrollDirectionChanged = onScrollDirectionChanged
                )
            }
        }

        composable(
            route = SonataRoute.Search.route
        ) {

        }

        composable(
            route = SonataRoute.Settings.route
        ) {

        }
    }
}

@Composable
fun AnimatedBottomBar(
    visible: Boolean,
    navController: NavHostController
) {
    //will include mini-player later, that's why this is here
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { it },
        exit = slideOutVertically { fullHeight -> fullHeight }
    ) {
        Column {
            BottomNavBar(navController)
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute?.startsWith(SonataRoute.Library.route) == true,
            onClick = {
                navController.navigate(SonataRoute.Library.route) {
                    //destroy everything above library but not itself
                    popUpTo(SonataRoute.Library.route) { inclusive = false }
                    launchSingleTop = true
                } },
            icon = { Text("Lib")
            }
        )

        NavigationBarItem(
            selected = currentRoute == SonataRoute.Search.route,
            onClick = { navController.navigate(SonataRoute.Search.route) },
            icon = { Text("Search") }
        )

        NavigationBarItem(
            selected = currentRoute == SonataRoute.Settings.route,
            onClick = { navController.navigate(SonataRoute.Settings.route) },
            icon = { Text("Set") }
        )
    }
}
@Composable
fun rememberDirectionalLazyListState(
    lazyListState: LazyListState,
): DirectionalLazyListState {
    return remember {
        DirectionalLazyListState(lazyListState)
    }
}

@Composable
fun LibraryScreen(
    onAllSongsClick: () -> Unit,
    onFolderClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(25.dp)
    ) {
        item {
            Text(
                text = "Library",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 30.dp),
                fontSize = 22.sp)
        }

        item {
            Text(
                text = "All Songs",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAllSongsClick() }
                    .padding(vertical = 22.dp),
                fontSize = 18.sp)
        }

        item {
            Text(
                text = "Folders Hierarchy",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onFolderClick() }
                    .padding(vertical = 22.dp),
                fontSize = 18.sp)
        }
    }
}

@Composable
fun AllSongsScreen(
    songList: List<FileNode>,
    onScrollDirectionChanged: (Boolean) -> Unit,
    onPlay: (FileNode) -> Unit,
    openDetails: (FileNode) -> Unit,
) {
    val listState = rememberLazyListState()
    val directionState = rememberDirectionalLazyListState(listState)

    val atTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
    }

    LaunchedEffect(directionState.scrollDirection, atTop) {
        val shouldShow = when {
            atTop -> true
            directionState.scrollDirection == ScrollDirection.Up -> true
            directionState.scrollDirection == ScrollDirection.Down -> false
            else -> return@LaunchedEffect
        }
        onScrollDirectionChanged(shouldShow)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues (25.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "All Songs",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 30.dp),
                fontSize = 22.sp
            )
        }

        items(
            items = songList,
            key = { it.sortId }
        ) { node ->
            FileListItem(
                node = node,
                onClick = { onPlay(node) },
                onLongClick = { openDetails(node) }
            )
        }
    }
}

@Composable
fun FileRootScreen(
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
            Text(
                text = "Folder Hierarchy",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 30.dp),
                fontSize = 22.sp,

                )
        }

        item {
            FileListItem(
                node = node,
                onClick = { onOpen(node) },
                onLongClick = {}
            )
        }
    }
}

@Composable
fun FolderScreen(
    node: FileNode,
    onOpen: (FileNode) -> Unit,
    onBack: () -> Unit,
    onPlay: (FileNode) -> Unit,
    openDetails: (FileNode) -> Unit,
    onScrollDirectionChanged: (Boolean) -> Unit
) {
    val listState = rememberLazyListState()
    val directionState = rememberDirectionalLazyListState(listState)

    val atTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
    }

    LaunchedEffect(directionState.scrollDirection, atTop) {
        val shouldShow = when {
            atTop -> true
            directionState.scrollDirection == ScrollDirection.Up -> true
            directionState.scrollDirection == ScrollDirection.Down -> false
            else -> return@LaunchedEffect
        }
        onScrollDirectionChanged(shouldShow)
    }

    LazyColumn(
        state = listState,
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
            if (child.isFolder) {
                FileListItem(
                    node = child,
                    onClick = { onOpen(child) },
                    onLongClick = {}
                )
            } else {
                FileListItem(
                    node = child,
                    onClick = { onPlay(child) },
                    onLongClick = { openDetails(child) }
                )
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
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
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
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                .clickable { onBack() }
        ) {
            Text(
                text = "← Back",
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
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(4.dp)
                    )
            ) {
                Text(
                    text = node.name,
                    fontSize = 22.sp,
                    modifier = Modifier
                        .padding(6.dp)
                )
            }

            Box(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(4.dp)
                    )
            ) {
                Text(
                    text = "\uD834\uDD1E ${node.musicTotal} | ${node.durationTotal.toTime()}",
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
                overflow = TextOverflow.Clip
                
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
                maxLines = 1
            )
        }
    }
}

private fun getAlbumArt(albumId: Long): Uri =
    ContentUris.withAppendedId(
        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
        albumId)


private fun Long.toTime(): String {
    //ms -> s
    val seconds = this / 1000
    val minutes = seconds/ 60
    val hours = minutes/ 60

    val mins = minutes - hours * 60
    val secs = seconds - (hours * 3600 + mins * 60)

    return if (hours == 0L) String.format(Locale.US, "%02d:%02d", mins, secs)
    else String.format(Locale.US, "%02d:%02d:%02d", hours, mins, secs)
}