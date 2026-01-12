package com.kamiruku.sonata.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.kamiruku.sonata.NavigationState

@Composable
fun AnimatedBottomBar(
    visible: Boolean,
    navigator: Navigator,
    navigationState: NavigationState
) {
    //will include mini-player later, that's why this is here
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { it },
        exit = slideOutVertically { fullHeight -> fullHeight }
    ) {
        Column {
            BottomNavBar(navigator, navigationState)
        }
    }
}

@Composable
fun BottomNavBar(navigator: Navigator, navigationState: NavigationState) {
    NavigationBar {
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