package com.kamiruku.sonata

import SwipeBackContainer
import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.kamiruku.sonata.features.library.AllSongsScreen
import com.kamiruku.sonata.features.library.FileRootScreen
import com.kamiruku.sonata.features.library.FolderScreen
import com.kamiruku.sonata.features.library.LibraryScreen
import com.kamiruku.sonata.features.nowPlaying.PlayerScreen
import com.kamiruku.sonata.features.search.GroupScreen
import com.kamiruku.sonata.features.search.SearchScreen
import com.kamiruku.sonata.features.settings.SettingsScreen
import com.kamiruku.sonata.navigation.Navigator
import com.kamiruku.sonata.navigation.SonataRoute
import com.kamiruku.sonata.state.DirectionalLazyListState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SonataNavHost(
    navigator: Navigator,
    navigationState: NavigationState,
    viewModel: SharedViewModel,
    onScrollDirectionChanged: (Boolean) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val buttonEnabled = uiState == LibraryUIState.Ready

    val roots by viewModel.rootNodes.collectAsState()
    val songList by viewModel.songList.collectAsState()
    val allSongsPath = remember(songList) {
        songList.mapNotNull { it.song?.path }
    }

    val inSelectionMode by viewModel.inSelectionMode.collectAsState()
    val selectedItems by viewModel.selectedItems.collectAsState()

    LaunchedEffect(selectedItems) {
        if (selectedItems.isNotEmpty()) viewModel.setSelectionMode(true)
    }

    val animationDelay = 300

    val transitionMetadata = NavDisplay.transitionSpec {
        slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(animationDelay)
        ) + fadeIn(initialAlpha = 0.8f) + scaleIn(initialScale = 0.8f) togetherWith
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(animationDelay)
                ) + fadeOut() + scaleOut(targetScale = 0.8f)
    } + NavDisplay.popTransitionSpec {
        slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(animationDelay)
        ) + fadeIn(initialAlpha = 0.8f) + scaleIn(initialScale = 0.8f) togetherWith
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(animationDelay)
                ) + fadeOut() + scaleOut(targetScale = 0.8f)
    } + NavDisplay.predictivePopTransitionSpec {
        slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(animationDelay)
        ) + fadeIn(initialAlpha = 0.8f) + scaleIn(initialScale = 0.8f) togetherWith
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(animationDelay)
                ) + fadeOut() + scaleOut(targetScale = 0.8f)
    }

    val entryProvider = entryProvider {
        entry<SonataRoute.LibraryHome> {
            LaunchedEffect(Unit) {
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
            LaunchedEffect(Unit) {
                viewModel.clearSelected()
            }

            val listState = rememberLazyListState()

            SwipeBackContainer(
                onBack = { navigator.goBack() },
                listState = listState,
                size = songList.size,
                onScrollDirectionChanged = onScrollDirectionChanged
            ) {
                AllSongsScreen(
                    listState = listState,
                    songList = songList,
                    selectedItems = selectedItems,
                    inSelectionMode = inSelectionMode,
                    onToggleSelect = { path ->
                        viewModel.toggleSelect(path)
                    },
                    onPlay = { song ->
                        println("Clicked ${song.title}")
                    }
                )
            }
        }

        entry<SonataRoute.FolderRoot>(
            metadata = transitionMetadata
        ) {
            LaunchedEffect(Unit) {
                viewModel.clearSelected()
                viewModel.setSelectionMode(false)
            }

            val listState = rememberLazyListState()

            SwipeBackContainer(
                onBack = { navigator.goBack() },
                listState = listState,
                size = roots.size,
                onScrollDirectionChanged = onScrollDirectionChanged
            ) {
                FileRootScreen(
                    nodes = roots,
                    onOpen = { node ->
                        navigator.navigate(SonataRoute.Folder(node.absolutePath))
                    }
                )
            }
        }

        entry<SonataRoute.Folder>(
            metadata = transitionMetadata
        ) { key ->
            val node = viewModel.findNode(key.absolutePath) ?: run {
                Log.d("folder find node","couldn't find node for: ${key.absolutePath}")
                LaunchedEffect(true) {
                    navigator.goBack()
                }
                return@entry
            }

            LaunchedEffect(key.absolutePath) {
                viewModel.clearSelected()
            }

            val listState = rememberLazyListState()

            SwipeBackContainer(
                onBack = { navigator.goBack() },
                listState = listState,
                size = node.children.size,
                onScrollDirectionChanged = onScrollDirectionChanged
            ) {
                FolderScreen(
                    listState = listState,
                    node = node,
                    allPaths = allSongsPath,
                    selectedItems = selectedItems,
                    inSelectionMode = inSelectionMode,
                    onToggleSelect = { path ->
                        viewModel.toggleSelect(path)
                    },
                    onToggleSelectFolder = { paths ->
                        viewModel.toggleSelect(paths)
                    },
                    onOpen = { child ->
                        navigator.navigate(SonataRoute.Folder(child.absolutePath))
                    },
                    onPlay = { song ->
                        println("Clicked ${song.title}")
                    }
                )
            }
        }

        entry<SonataRoute.Search> {
            LaunchedEffect(Unit) {
                viewModel.clearSelected()
            }

            SearchScreen(
                viewModel = viewModel,
                selectedItems = selectedItems,
                inSelectionMode = inSelectionMode,
                onToggleSelect = { path ->
                    viewModel.toggleSelect(path)
                },
                onToggleSelectGroup = { paths ->
                    viewModel.toggleSelect(paths)
                },
                onOpen = { title ->
                    navigator.navigate(SonataRoute.SearchGroup(title))
                },
                onClick = { song ->
                    println(song.title)
                },
            )
        }

        val filteredSongs by viewModel.filteredSongs.collectAsState()

        entry<SonataRoute.SearchGroup>(
            metadata = transitionMetadata
        ) { key ->
            val list = filteredSongs[key.title] ?: run {
                Log.d("group find list","couldn't find list for: ${key.title}")
                LaunchedEffect(true) {
                    navigator.goBack()
                }
                return@entry
            }

            LaunchedEffect(Unit) {
                viewModel.clearSelected()
            }

            val listState = rememberLazyListState()

            SwipeBackContainer(
                onBack = { navigator.goBack() },
                listState = listState,
                size = list.size,
                onScrollDirectionChanged = onScrollDirectionChanged
            ) {
                GroupScreen(
                    listState = listState,
                    title = key.title,
                    list = list,
                    selectedItems = selectedItems,
                    inSelectionMode = inSelectionMode,
                    onToggleSelect = { path ->
                        viewModel.toggleSelect(path)
                    },
                    onPlay = { song ->
                        println("Clicked ${song.title}")
                    },
                )
            }
        }

        entry<SonataRoute.SettingsHome> {
            SettingsScreen(
                onGeneralClick = { navigator.navigate(SonataRoute.SettingsGeneral) },
                onLibraryClick = { navigator.navigate(SonataRoute.SettingsLibrary) },
                onAudioClick = { navigator.navigate(SonataRoute.SettingsAudio) },
                onAboutClick = { navigator.navigate(SonataRoute.SettingsAbout) }
            )
        }

        entry<SonataRoute.SettingsGeneral> {

        }

        entry<SonataRoute.SettingsLibrary> {
            com.kamiruku.sonata.features.settings.LibraryScreen(viewModel)
        }

        entry<SonataRoute.SettingsAudio> {

        }

        entry<SonataRoute.SettingsAbout> {

        }
    }

    val entries = navigationState.toEntries(entryProvider)

    LaunchedEffect(entries.lastOrNull()) {
        withFrameNanos {  }
        delay((animationDelay * 0.25).milliseconds)
        navigator.onTransitionFinished()
    }

    NavDisplay(
        entries = entries,
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