package com.kamiruku.sonata.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface SongDao {
    @Query("SELECT * FROM songs")
    suspend fun getAllSongs(): List<SongEntity>

    @Query("SELECT * FROM songs WHERE path = :path LIMIT 1")
    suspend fun getSong(path: String): SongEntity?

    @Query("SELECT * FROM songs WHERE title LIKE :query")
    suspend fun searchByTitle(query: String): List<SongEntity>

    @Query("SELECT * FROM songs WHERE album = :album")
    suspend fun getSongsByAlbum(album: String): List<SongEntity>

    @Query("SELECT * FROM songs WHERE artists LIKE :artist")
    suspend fun getSongsByArtist(artist: String): List<SongEntity>

    @Query("SELECT COUNT(*) FROM songs")
    suspend fun getSongCount(): Int

    @Query("SELECT path, date_modified AS dateModified FROM songs")
    suspend fun getPathAndDateModified(): List<PathDate>

    @Query("DELETE FROM songs WHERE path IN (:paths)")
    suspend fun deleteByPaths(paths: List<String>)

    @Upsert
    suspend fun upsertAll(songs: List<SongEntity>)

    @Delete
    suspend fun delete(song: SongEntity)
}