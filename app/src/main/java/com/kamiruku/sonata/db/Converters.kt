package com.kamiruku.sonata.db

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromArtistsString(artist: String): Array<String> {
        return if (artist.isBlank()) emptyArray()
        else artist.split('/').toTypedArray()
    }

    @TypeConverter
    fun artistsToString(artists: Array<String>): String {
        return artists.joinToString("/")
    }
}