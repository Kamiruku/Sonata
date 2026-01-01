package com.kamiruku.sonata.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [SongEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class SonataDatabase: RoomDatabase() {
    abstract fun songDao(): SongDao

    companion object {
        @Volatile
        private var INSTANCE: SonataDatabase? = null

        fun getDatabase(context: Context): SonataDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SonataDatabase::class.java,
                    "songs.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}