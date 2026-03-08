package com.kamiruku.sonata.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kamiruku.sonata.NavigationState
import com.kamiruku.sonata.SharedViewModel

@Composable
fun AnimatedBottomBar(
    visible: Boolean,
    navigator: Navigator,
    navigationState: NavigationState,
    viewModel: SharedViewModel,
    showMiniPlayer: Boolean,
    onMiniPlayerClick: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { it },
        exit = slideOutVertically { fullHeight -> fullHeight }
    ) {
        Column {
            AnimatedVisibility(showMiniPlayer) {
                MiniPlayer(viewModel, onMiniPlayerClick)
            }
            BottomNavBar(navigator, navigationState)
        }
    }
}

@Composable
fun MiniPlayer(viewModel: SharedViewModel, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick)
    ) {
        //tiny horizontal pager
    }
}

@Composable
fun BottomNavBar(navigator: Navigator, navigationState: NavigationState) {
    Row(
        Modifier
            .background(MaterialTheme.colorScheme.inversePrimary)
            .height(90.dp)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(top = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavigationBarItem(
            selected = SonataRoute.LibraryHome == navigationState.topLevelRoute,
            onClick = { navigator.navigate(SonataRoute.LibraryHome, popUpTo = true) },
            icon = {
                Icon(
                    Icons.Outlined.LibraryMusic,
                    contentDescription = "library"
                )
            }
        )

        NavigationBarItem(
            selected = SonataRoute.Search == navigationState.topLevelRoute,
            onClick = { navigator.navigate(SonataRoute.Search) },
            icon = {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = "search"
                )
            }
        )

        NavigationBarItem(
            selected = SonataRoute.SettingsHome == navigationState.topLevelRoute,
            onClick = { navigator.navigate(SonataRoute.SettingsHome, popUpTo = true) },
            icon = {
                Icon(
                    Icons.Outlined.Settings,
                    contentDescription = "settings"
                )
            }
        )
    }
}