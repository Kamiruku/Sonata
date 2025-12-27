package com.kamiruku.sonata.navigation

sealed class SonataRoute(val route: String) {

    data object Library : SonataRoute("library")
    data object LibraryHome : SonataRoute("library/home")
    data object FolderRoot : SonataRoute("library/folder_root")
    data object Folder : SonataRoute("library/folder/{id}") {
        fun create(id: Int) = "library/folder/$id"
    }
    data object AllSongs : SonataRoute("library/all_songs")

    data object NowPlaying : SonataRoute("now_playing")

    data object Search: SonataRoute("search")
    
    data object Settings : SonataRoute("settings")
    data object SettingsHome: SonataRoute("settings/home")
    data object SettingsGeneral: SonataRoute("settings/general")
    data object SettingsLibrary: SonataRoute("settings/library")
    data object SettingsAudio: SonataRoute("settings/audio")
    data object SettingsAbout: SonataRoute("settings/about")
}