package com.kamiruku.sonata

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kamiruku.sonata.features.nowPlaying.PlayerScreen
import com.kamiruku.sonata.navigation.AnimatedBottomBar
import com.kamiruku.sonata.navigation.Navigator
import com.kamiruku.sonata.navigation.SonataRoute

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SonataApp(viewModel: SharedViewModel) {
    var bottomBarVisible by remember { mutableStateOf(true) }
    var showFullPlayer by remember { mutableStateOf(false) }

    val navigationState = rememberNavigationState(
        startRoute = SonataRoute.LibraryHome,
        topLevelRoutes = setOf(
            SonataRoute.LibraryHome,
            SonataRoute.Search,
            SonataRoute.SettingsHome
        )
    )

    val navigator = remember { Navigator(navigationState) }

    val inSelectionMode by viewModel.inSelectionMode.collectAsState()

    Scaffold(
        bottomBar = {
            if (!inSelectionMode) {
                AnimatedBottomBar(
                    visible = bottomBarVisible,
                    navigator = navigator,
                    navigationState = navigationState,
                    viewModel = viewModel,
                    showMiniPlayer = !showFullPlayer,
                    onMiniPlayerClick = { showFullPlayer = true }
                )
            } else {
                SelectionBar(viewModel, navigationState, navigator)
            }
        }
    ) { scaffoldPadding ->
        SonataNavHost(
            navigator = navigator,
            navigationState = navigationState,
            viewModel = viewModel,
            onScrollDirectionChanged = { scrollingUp ->
                bottomBarVisible = scrollingUp
            }
        )

        AnimatedVisibility(
            visible = showFullPlayer,
            enter = slideInVertically(
                tween()
            ) { it - 160 },
            exit = slideOutVertically(
                tween()
            ) { it - 160 }
        ) {
            PlayerScreen(viewModel, closePlayer = { showFullPlayer = false })
        }
    }
}