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
}