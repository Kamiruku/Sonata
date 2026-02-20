package com.kamiruku.sonata.db

import android.content.Context

class SongRepository(context: Context) {
    private val songDao = SonataDatabase.getDatabase(context).songDao()

    suspend fun getSongCount(): Int {
        return songDao.getSongCount()
    }

    suspend fun insertSongs(songs: List<SongEntity>) {
        songDao.upsertAll(songs)
    }

    suspend fun getAllSongs(): List<SongEntity> {
        return songDao.getAllSongs()
    }

    suspend fun getDatePathMap(): Map<String, Long> {
        return songDao.getPathAndDateModified()
            .associate { it.path to it.dateModified }
    }

    suspend fun deleteByPaths(paths: Collection<String>) {
        if (paths.isNotEmpty()) {
            songDao.deleteByPaths(paths.toList())
        }
    }

    suspend fun getSongAll(q: String): List<SongEntity> {
        return songDao.searchByAll("%$q%")
    }

    suspend fun getArtists(q: String): List<String> {
        return songDao.searchArtists("%$q%")
    }

    suspend fun getArtistSongs(artist: String): List<SongEntity> {
        return songDao.getSongsByArtist(artist)
    }

    suspend fun getAlbums(q: String): List<String> {
        return songDao.searchAlbums("%$q%")
    }

    suspend fun getAlbumSongs(album: String): List<SongEntity> {
        return songDao.getSongsByAlbum(album)
    }
}