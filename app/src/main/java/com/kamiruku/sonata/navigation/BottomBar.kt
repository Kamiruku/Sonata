package com.kamiruku.sonata.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun AnimatedBottomBar(
    visible: Boolean,
    navController: NavHostController
) {
    //will include mini-player later, that's why this is here
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { it },
        exit = slideOutVertically { fullHeight -> fullHeight }
    ) {
        Column {
            BottomNavBar(navController)
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute?.startsWith(SonataRoute.Library.route) == true,
            onClick = {
                navController.navigate(SonataRoute.Library.route) {
                    popUpTo(SonataRoute.Library.route) { inclusive = false }
                    launchSingleTop = true
                }
            },
            icon = {
                Text("Lib")
            }
        )

        NavigationBarItem(
            selected = currentRoute == SonataRoute.Search.route,
            onClick = {
                navController.navigate(SonataRoute.Search.route) {
                    //clicking back here will return to library home
                    //remove popUpTo and launchSingleTop if going back to nested screens is required
                    popUpTo(SonataRoute.Search.route) { inclusive = false }
                    launchSingleTop = true
                }
            },
            icon = { Text("Search") }
        )

        NavigationBarItem(
            selected = currentRoute?.startsWith(SonataRoute.Settings.route) == true,
            onClick = {
                navController.navigate(SonataRoute.Settings.route) {
                    popUpTo(SonataRoute.Settings.route) { inclusive = false }
                    launchSingleTop = true
                }
            },
            icon = { Text("Set") }
        )
    }
}