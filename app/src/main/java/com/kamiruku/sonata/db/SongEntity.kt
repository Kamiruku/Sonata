package com.kamiruku.sonata.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    indices = [Index("path"), Index("title"), Index("album")]
)
data class SongEntity(
    //mediastore id
    @PrimaryKey val mediaStoreId: Long,
    @ColumnInfo(name = "mediastore_album_id") val mediaStoreAlbumId: Long,

    //metadata
    @ColumnInfo(name = "artists") val artists: Array<String>,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "album") val album: String,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "album_artist") val albumArtist: String,
    @ColumnInfo(name = "track") val track: String,
    @ColumnInfo(name = "disc")val disc: String,

    //audio properties
    @ColumnInfo(name = "bitrate") val bitrate : Int,
    @ColumnInfo(name = "sample_rate") val sampleRate: Int,
    @ColumnInfo(name = "channels") val channels: Int,
    @ColumnInfo(name = "bits_per_sample") val bitsPerSample: Int,
    @ColumnInfo(name = "duration") val duration: Long,

    //file properties
    @ColumnInfo(name = "date_modified") val dateModified: Long,
    @ColumnInfo(name = "size") val size: Long,
    @ColumnInfo(name = "path") val path: String,
    @ColumnInfo(name = "folder") val folder: String,
    @ColumnInfo(name = "file_name") val fileName: String,

    val albumArtHash: String? = null,
    val albumArtPath: String? = null
)