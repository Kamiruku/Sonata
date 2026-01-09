package com.kamiruku.sonata

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
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
    navigator: Navigator,
    navigationState: NavigationState,
    viewModel: SharedViewModel,
    onScrollDirectionChanged: (Boolean) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val buttonEnabled = uiState == LibraryUIState.Ready

    val root by viewModel.rootNode.collectAsState()
    val songList by viewModel.songList.collectAsState()

    var selectedFile by remember { mutableStateOf<FileNode?>(null) }

    val entryProvider: (NavKey) -> NavEntry<NavKey> = entryProvider {
        entry<SonataRoute.LibraryHome> {
            LibraryScreen(
                buttonEnabled = buttonEnabled,
                onAllSongsClick = { navigator.navigate(SonataRoute.AllSongs) },
                onFolderClick = { navigator.navigate(SonataRoute.FolderRoot) }
            )
        }

        entry<SonataRoute.AllSongs> {
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

        entry<SonataRoute.FolderRoot> {
            root?.let {
                FileRootScreen(
                    node = it,
                    onOpen = { node ->
                        navigator.navigate(SonataRoute.Folder(node.sortId))
                    }
                )
            }
        }

        entry<SonataRoute.Folder> { key ->
            val node = viewModel.findNode(key.id) ?: return@entry
            FolderScreen(
                node = node,
                onOpen = { child ->
                    navigator.navigate(SonataRoute.Folder(child.sortId))
                },
                onPlay = { node ->
                    println("Clicked ${node.song?.title}")
                },
                openDetails = { node ->
                    selectedFile = node
                },
                onBack = { navigator.goBack() },
                onScrollDirectionChanged = onScrollDirectionChanged
            )
        }

        entry<SonataRoute.Search> {

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