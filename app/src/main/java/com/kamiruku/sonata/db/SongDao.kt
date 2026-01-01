package com.kamiruku.sonata.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface SongDao {
    @Query("SELECT * FROM songs")
    fun getAllSongs(): List<SongEntity>

    @Query("SELECT * FROM songs WHERE path = :path LIMIT 1")
    fun getSong(path: String): SongEntity?

    @Query("SELECT * FROM songs WHERE title LIKE :query")
    fun searchByTitle(query: String): List<SongEntity>

    @Query("SELECT * FROM songs WHERE album = :album")
    fun getSongsByAlbum(album: String): List<SongEntity>

    @Query("SELECT * FROM songs WHERE artists LIKE :artist")
    fun getSongsByArtist(artist: String): List<SongEntity>

    @Query("SELECT * FROM songs WHERE path = :path AND date_modified = :dateModified LIMIT 1")
    fun getSongByPathAndDate(path: String, dateModified: Long): SongEntity?

    @Query("SELECT COUNT(*) FROM songs")
    fun getSongCount(): Int

    @Upsert
    fun upsertAll(songs: List<SongEntity>)

    @Delete
    fun delete(song: SongEntity)
}