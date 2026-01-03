package com.kamiruku.sonata

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.kamiruku.sonata.features.library.AllSongsScreen
import com.kamiruku.sonata.features.library.FileRootScreen
import com.kamiruku.sonata.features.library.FolderScreen
import com.kamiruku.sonata.features.library.LibraryScreen
import com.kamiruku.sonata.features.settings.SettingsScreen
import com.kamiruku.sonata.navigation.SonataRoute
import com.kamiruku.sonata.state.DirectionalLazyListState
import com.kamiruku.sonata.ui.components.SongDetailsDialog

@Composable
fun SonataNavHost(
    navController: NavHostController,
    viewModel: SharedViewModel,
    onScrollDirectionChanged: (Boolean) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route

        when (uiState) {
            LibraryUIState.Loading -> { }

            LibraryUIState.Empty -> {
                if (currentRoute != SonataRoute.Settings.route)
                    navController.navigate(SonataRoute.Settings.route)
            }
            LibraryUIState.Ready -> {
                if (currentRoute != SonataRoute.Library.route)
                    navController.navigate(SonataRoute.Library.route)
            }
        }
    }

    val root = remember(uiState) {
        if (uiState == LibraryUIState.Ready) viewModel.getRootNode()
        else null
    }

    val songList = remember(uiState) {
        if (uiState == LibraryUIState.Ready) viewModel.getSongList()
        else emptyList()
    }

    var selectedFile by remember { mutableStateOf<FileNode?>(null) }

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
                        selectedFile = node
                    }
                )
            }

            composable(SonataRoute.FolderRoot.route) {
                root?.let {
                    FileRootScreen(
                        node = root,
                        onOpen = { node ->
                            navController.navigate(SonataRoute.Folder.create(node.sortId))
                        }
                    )
                }
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
                        selectedFile = node
                    },
                    onBack = { navController.popBackStack() },
                    onScrollDirectionChanged = onScrollDirectionChanged
                )
            }
        }

        composable(route = SonataRoute.Search.route) {

        }

        navigation(
            route = SonataRoute.Settings.route,
            startDestination = SonataRoute.SettingsHome.route
        ) {
            composable(route = SonataRoute.SettingsHome.route) {
                SettingsScreen(
                    onGeneralClick = { navController.navigate(SonataRoute.SettingsGeneral.route) },
                    onLibraryClick = { navController.navigate(SonataRoute.SettingsLibrary.route) },
                    onAudioClick = { navController.navigate(SonataRoute.SettingsAudio.route) },
                    onAboutClick = { navController.navigate(SonataRoute.SettingsAbout.route) }
                )
            }

            composable(SonataRoute.SettingsGeneral.route) {

            }

            composable(SonataRoute.SettingsLibrary.route) {

            }

            composable(SonataRoute.SettingsAudio.route) {

            }

            composable(SonataRoute.SettingsAbout.route) {

            }
        }
    }

    SongDetailsDialog(
        file = selectedFile,
        onDismiss = { selectedFile = null }
    )
}

@Composable
fun rememberDirectionalLazyListState(
    lazyListState: LazyListState,
): DirectionalLazyListState {
    return remember {
        DirectionalLazyListState(lazyListState)
    }
}