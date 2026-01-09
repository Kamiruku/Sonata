package com.kamiruku.sonata.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.kamiruku.sonata.NavigationState
import com.kamiruku.sonata.Navigator

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
                Text("Lib")
            }
        )

        NavigationBarItem(
            selected = SonataRoute.Search == navigationState.topLevelRoute,
            onClick = { navigator.navigate(SonataRoute.Search) },
            icon = { Text("Search") }
        )

        NavigationBarItem(
            selected = SonataRoute.SettingsHome == navigationState.topLevelRoute,
            onClick = { navigator.navigate(SonataRoute.SettingsHome, popUpTo = true) },
            icon = { Text("Set") }
        )
    }
}