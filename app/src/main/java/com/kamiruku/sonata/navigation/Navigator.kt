package com.kamiruku.sonata.navigation

import androidx.navigation3.runtime.NavKey
import com.kamiruku.sonata.NavigationState

/**
 * Handles navigation events (forward and back) by updating the navigation state.
 */
class Navigator(val state: NavigationState){
    fun navigate(route: NavKey, popUpTo: Boolean = false) {
        // Single-top
        val currentStack = state.backStacks[state.topLevelRoute] ?:
        error("Stack for ${state.topLevelRoute} not found")
        if (currentStack.last() == route) return

        if (route in state.backStacks.keys) {
            // This is a top level route, just switch to it.
            state.topLevelRoute = route

            // Remove popUpTo if switching to top level (and not navigating to it) is sufficient
            if (popUpTo) {
                state.backStacks[state.topLevelRoute]?.clear()
                state.backStacks[state.topLevelRoute]?.add(route)
            }

        } else {
            state.backStacks[state.topLevelRoute]?.add(route)
        }
    }

    fun goBack() {
        val currentStack = state.backStacks[state.topLevelRoute] ?:
        error("Stack for ${state.topLevelRoute} not found")
        val currentRoute = currentStack.last()

        // If we're at the base of the current route, go back to the start route stack.
        if (currentRoute == state.topLevelRoute) {
            state.topLevelRoute = state.startRoute
        } else {
            currentStack.removeLastOrNull()
        }
    }
}