package com.kamiruku.sonata.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object DataStoreInstance {
    private val Context.dataStore: DataStore<Preferences>
            by preferencesDataStore(name = "settings")

    val PathSrcs_KEY = stringSetPreferencesKey("path_sources")

    suspend fun savePathSrcs(context: Context, key: Preferences.Key<Set<String>>, value: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    fun getPathSrcs(context: Context, key: Preferences.Key<Set<String>>): Flow<Set<String>?> {
        return context.dataStore.data.map { preferences ->
            preferences[key]
        }
    }
}