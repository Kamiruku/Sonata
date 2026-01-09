package com.kamiruku.sonata

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kamiruku.sonata.navigation.AnimatedBottomBar
import com.kamiruku.sonata.navigation.Navigator
import com.kamiruku.sonata.navigation.SonataRoute

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SonataApp(viewModel: SharedViewModel) {
    var bottomBarVisible by remember { mutableStateOf(true) }

    val uiState by viewModel.uiState.collectAsState()

    val initialRoute = remember {
        when (uiState) {
            LibraryUIState.Empty -> SonataRoute.SettingsHome
            else -> SonataRoute.LibraryHome
        }
    }

    val navigationState = rememberNavigationState(
        startRoute = initialRoute,
        topLevelRoutes = setOf(
            SonataRoute.LibraryHome,
            SonataRoute.Search,
            SonataRoute.SettingsHome,
            SonataRoute.NowPlaying
        )
    )

    val navigator = remember { Navigator(navigationState) }

    Scaffold(
        bottomBar = {
            AnimatedBottomBar(
                visible = bottomBarVisible,
                navigator = navigator,
                navigationState = navigationState
            )
        },
        content = { paddingValues ->
            SonataNavHost(
                navigator = navigator,
                navigationState = navigationState,
                viewModel = viewModel,
                onScrollDirectionChanged = { scrollingUp ->
                    bottomBarVisible = scrollingUp
                }
            )
        }
    )
}