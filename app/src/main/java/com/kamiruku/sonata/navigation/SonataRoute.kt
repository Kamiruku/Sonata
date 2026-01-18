package com.kamiruku.sonata.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface SonataRoute: NavKey {
    @Serializable data object LibraryHome : NavKey
    @Serializable data object FolderRoot : NavKey
    @Serializable data class Folder(val path: String) : NavKey
    @Serializable data object AllSongs : NavKey

    @Serializable data object NowPlaying : NavKey

    @Serializable data object Search: NavKey
    @Serializable data object SettingsHome: NavKey
    @Serializable data object SettingsGeneral: NavKey
    @Serializable data object SettingsLibrary: NavKey
    @Serializable data object SettingsAudio: NavKey
    @Serializable data object SettingsAbout: NavKey
}