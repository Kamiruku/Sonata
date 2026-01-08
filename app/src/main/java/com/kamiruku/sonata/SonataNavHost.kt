package com.kamiruku.sonata

import SwipeBackContainer
import android.net.Uri
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
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
    val buttonEnabled = uiState == LibraryUIState.Ready

    val root by viewModel.rootNode.collectAsState()

    val songList by viewModel.songList.collectAsState()

    var selectedFile by remember { mutableStateOf<FileNode?>(null) }

    /*
    loading screen is library screen...
    for now, because there's minimal time it's actually active if
    there is no need to update db
    still want it to be accessible even when updating songs
     */
    val startDestination = when (uiState) {
        LibraryUIState.Empty -> SonataRoute.Settings.route
        LibraryUIState.Ready -> SonataRoute.Library.route
        LibraryUIState.Loading -> SonataRoute.Library.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        navigation(
            route = SonataRoute.Library.route,
            startDestination = SonataRoute.LibraryHome.route
        ) {
            composable(SonataRoute.LibraryHome.route) {
                LibraryScreen(
                    buttonEnabled = buttonEnabled,
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
                        node = it,
                        onOpen = { node ->
                            navController.navigate(SonataRoute.Folder.create(node.sortId))
                        }
                    )
                }
            }

            composable(
                route = SonataRoute.Folder.route,
                arguments = listOf(navArgument("id") {
                    type = NavType.StringType }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.let(Uri::decode) ?: return@composable
                val node = viewModel.findNode(id) ?: return@composable

                FolderScreen(
                    node = node,
                    onOpen = { child ->
                        navController.navigate(SonataRoute.Folder.create(Uri.encode(child.sortId)))
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
                    songList.isNotEmpty(),
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