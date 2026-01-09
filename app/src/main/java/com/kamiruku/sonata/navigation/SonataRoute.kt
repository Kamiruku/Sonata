package com.kamiruku.sonata.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface SonataRoute: NavKey {
    @Serializable data object LibraryHome : SonataRoute
    @Serializable data object FolderRoot : SonataRoute
    @Serializable data class Folder(val id: String) : SonataRoute
    @Serializable data object AllSongs : SonataRoute

    @Serializable data object NowPlaying : SonataRoute

    @Serializable data object Search: SonataRoute
    @Serializable data object SettingsHome: SonataRoute
    @Serializable data object SettingsGeneral: SonataRoute
    @Serializable data object SettingsLibrary: SonataRoute
    @Serializable data object SettingsAudio: SonataRoute
    @Serializable data object SettingsAbout: SonataRoute
}