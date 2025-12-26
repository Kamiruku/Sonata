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
    data object Settings : SonataRoute("settings")
    data object Search: SonataRoute("search")
}