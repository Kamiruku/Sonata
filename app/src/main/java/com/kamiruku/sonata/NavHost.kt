package com.kamiruku.sonata

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

        composable(route = SonataRoute.Search.route) {

        }

        navigation(
            route = SonataRoute.Settings.route,
            startDestination = SonataRoute.SettingsHome.route
        ) {
            composable(route = SonataRoute.SettingsHome.route) {
                SettingsScreen(
                    onGeneralClick = { navController.navigate(SonataRoute.SettingsGeneral.route) },
                    onLibraryClick = { navController.navigate(SonataRoute.LibraryHome.route) },
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
}

@Composable
fun rememberDirectionalLazyListState(
    lazyListState: LazyListState,
): DirectionalLazyListState {
    return remember {
        DirectionalLazyListState(lazyListState)
    }
}