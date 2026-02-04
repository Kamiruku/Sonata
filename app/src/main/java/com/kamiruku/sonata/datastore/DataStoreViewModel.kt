package com.kamiruku.sonata.datastore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DataStoreViewModel(application: Application): AndroidViewModel(application) {
    private val _pathSrcs = MutableStateFlow<Set<String>?>(null)
    val pathSrcs: StateFlow<Set<String>?> = _pathSrcs

    init {
        getPathSrcs()
    }

    private fun getPathSrcs() {
        viewModelScope.launch {
            DataStoreInstance.getPathSrcs(
                getApplication(),
                DataStoreInstance.PathSrcs_KEY
            ).collect { value ->
                _pathSrcs.value = value
            }
        }
    }

    fun savePathSrcs(value: Set<String>) {
        viewModelScope.launch {
            DataStoreInstance.savePathSrcs(
                getApplication(),
                DataStoreInstance.PathSrcs_KEY,
                value
            )
        }
    }
}