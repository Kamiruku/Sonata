package com.kamiruku.sonata

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kamiruku.sonata.navigation.AnimatedBottomBar

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SonataApp(navController: NavHostController, viewModel: SharedViewModel) {
    var bottomBarVisible by remember { mutableStateOf(true) }
    val backStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(backStackEntry?.destination?.route) {
        bottomBarVisible = true
    }

    Scaffold(
        bottomBar = {
            AnimatedBottomBar(
                visible = bottomBarVisible,
                navController = navController
            )
        },
        content = { paddingValues ->
            SonataNavHost(
                navController = navController,
                viewModel = viewModel,
                onScrollDirectionChanged = { scrollingUp ->
                    bottomBarVisible = scrollingUp
                }
            )
        }
    )
}