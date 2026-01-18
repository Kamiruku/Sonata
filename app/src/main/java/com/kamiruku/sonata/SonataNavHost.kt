package com.kamiruku.sonata

import SwipeBackContainer
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.kamiruku.sonata.features.library.AllSongsScreen
import com.kamiruku.sonata.features.library.FileRootScreen
import com.kamiruku.sonata.features.library.FolderScreen
import com.kamiruku.sonata.features.library.LibraryScreen
import com.kamiruku.sonata.features.search.SearchScreen
import com.kamiruku.sonata.features.settings.SettingsScreen
import com.kamiruku.sonata.navigation.Navigator
import com.kamiruku.sonata.navigation.SonataRoute
import com.kamiruku.sonata.state.DirectionalLazyListState

@Composable
fun SonataNavHost(
    navigator: Navigator,
    navigationState: NavigationState,
    viewModel: SharedViewModel,
    onScrollDirectionChanged: (Boolean) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val buttonEnabled = uiState == LibraryUIState.Ready

    val root by viewModel.rootNode.collectAsState()
    val songList by viewModel.songList.collectAsState()

    val inSelectionMode by viewModel.inSelectionMode.collectAsState()
    LaunchedEffect(viewModel.selectedItems) {
        if (viewModel.selectedItems.isNotEmpty()) viewModel.setSelectionMode(true)
    }

    val transitionMetadata = NavDisplay.transitionSpec {
        slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(300)
        ) + fadeIn(initialAlpha = 0.8f) + scaleIn(initialScale = 0.8f) togetherWith
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                ) + fadeOut() + scaleOut(targetScale = 0.8f)
    } + NavDisplay.popTransitionSpec {
        slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(300)
        ) + fadeIn(initialAlpha = 0.8f) + scaleIn(initialScale = 0.8f) togetherWith
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut() + scaleOut(targetScale = 0.8f)
    } + NavDisplay.predictivePopTransitionSpec {
        slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(300)
        ) + fadeIn(initialAlpha = 0.8f) + scaleIn(initialScale = 0.8f) togetherWith
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut() + scaleOut(targetScale = 0.8f)
    }

    val entryProvider = entryProvider {
        entry<SonataRoute.LibraryHome> {
            LaunchedEffect(it) {
                viewModel.clearSelected()
            }

            LibraryScreen(
                buttonEnabled = buttonEnabled,
                onAllSongsClick = { navigator.navigate(SonataRoute.AllSongs) },
                onFolderClick = { navigator.navigate(SonataRoute.FolderRoot) }
            )
        }

        entry<SonataRoute.AllSongs>(
            metadata = transitionMetadata
        ) {
            LaunchedEffect(it) {
                viewModel.clearSelected()
            }

            SwipeBackContainer(
                onBack = { navigator.goBack() }
            ) {
                AllSongsScreen(
                    selectedItems = viewModel.selectedItems,
                    inSelectionMode = inSelectionMode,
                    onToggleSelect = { path ->
                        viewModel.toggleSelect(path)
                    },
                    songList = songList,
                    onScrollDirectionChanged = onScrollDirectionChanged,
                    onPlay = { song ->
                        println("Clicked ${song.title}")
                    }
                )
            }

        }

        entry<SonataRoute.FolderRoot>(
            metadata = transitionMetadata
        ) {
            LaunchedEffect(it) {
                viewModel.clearSelected()
                viewModel.setSelectionMode(false)
            }

            root?.let {
                SwipeBackContainer(
                    onBack = { navigator.goBack() }
                ) {
                    FileRootScreen(
                        node = it,
                        onOpen = { node ->
                            navigator.navigate(SonataRoute.Folder(node.sortId))
                        }
                    )
                }
            }
        }

        entry<SonataRoute.Folder>(
            metadata = transitionMetadata
        ) { key ->
            val node = viewModel.findNode(key.id) ?: return@entry

            LaunchedEffect(key.id) {
                viewModel.clearSelected()
            }

            SwipeBackContainer(
                onBack = { navigator.goBack() }
            ) {
                FolderScreen(
                    selectedItems = viewModel.selectedItems,
                    inSelectionMode = inSelectionMode,
                    onToggleSelect = { path ->
                        viewModel.toggleSelect(path)
                    },
                    onToggleSelectFolder = { paths ->
                        viewModel.toggleSelect(paths)

                    },
                    node = node,
                    onOpen = { child ->
                        navigator.navigate(SonataRoute.Folder(child.sortId))
                    },
                    onPlay = { song ->
                        println("Clicked ${song.title}")
                    },
                    onScrollDirectionChanged = onScrollDirectionChanged
                )
            }
        }

        entry<SonataRoute.Search> {
            val textFieldState = rememberTextFieldState()
            val filteredSongs = viewModel.filteredSongs

            LaunchedEffect(it) {
                viewModel.clearSelected()
            }

            SearchScreen(
                textFieldState,
                onQueryChange = {
                    viewModel.query.value = textFieldState.text.toString()
                },
                searchResults = filteredSongs,
                onClick = { song ->
                    println(song.title)
                },
                selectedItems = viewModel.selectedItems,
                inSelectionMode = inSelectionMode,
                onToggleSelect = { path ->
                    viewModel.toggleSelect(path)
                },
            )
        }

        entry<SonataRoute.NowPlaying> {

        }

        entry<SonataRoute.SettingsHome> {
            SettingsScreen(
                songList.isNotEmpty(),
                onGeneralClick = { navigator.navigate(SonataRoute.SettingsGeneral) },
                onLibraryClick = { navigator.navigate(SonataRoute.SettingsLibrary) },
                onAudioClick = { navigator.navigate(SonataRoute.SettingsAudio) },
                onAboutClick = { navigator.navigate(SonataRoute.SettingsAbout) }
            )
        }

        entry<SonataRoute.SettingsGeneral> {

        }

        entry<SonataRoute.SettingsLibrary> {

        }

        entry<SonataRoute.SettingsAudio> {

        }

        entry<SonataRoute.SettingsAbout> {

        }
    }

    NavDisplay(
        entries = navigationState.toEntries(entryProvider),
        onBack = { navigator.goBack() }
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