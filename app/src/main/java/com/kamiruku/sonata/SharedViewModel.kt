package com.kamiruku.sonata

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kamiruku.sonata.db.SongEntity
import com.kamiruku.sonata.db.SongRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SharedViewModel(
    application: Application,
    private val songRepository: SongRepository
): AndroidViewModel(application) {
    private val _rootNode = MutableStateFlow<FileNode?>(null)
    val rootNode: StateFlow<FileNode?> = _rootNode

    private val _songList = MutableStateFlow<List<FileNode>>(emptyList())
    val songList: StateFlow<List<FileNode>> = _songList

    private val nodeIndex = mutableMapOf<String, FileNode>()

    private val _uiState = MutableStateFlow<LibraryUIState>(LibraryUIState.Loading)
    val uiState: StateFlow<LibraryUIState> = _uiState.asStateFlow()

    val query = MutableStateFlow("")
    var filteredSongs by mutableStateOf<List<Song>>(emptyList())

    @Volatile private var dbSongList: List<Song>? = null

    fun setList(rootNode: FileNode) {
        _rootNode.value = rootNode
        nodeIndex.clear()
        buildIndex(rootNode)
        _songList.value = rootNode.flattenSongs()

        _uiState.value =
            if (songList.value.isEmpty()) LibraryUIState.Empty
            else LibraryUIState.Ready
    }

    private fun buildIndex(node: FileNode) {
        nodeIndex[node.sortId] = node
        for (child in node.children.values) {
            buildIndex(child)
        }
    }

    private fun FileNode.flattenSongs(): List<FileNode> {
        val result = mutableListOf<FileNode>()

        fun dfs(node: FileNode) {
            if (!node.isFolder) {
                result += node
                return
            }

            node.children.values.forEach { child ->
                dfs(child)
            }
        }

        dfs(this)
        return result
    }

    fun findNode(sortId: String): FileNode? = nodeIndex[sortId]

    fun loadCachedSongs() {
        viewModelScope.launch(Dispatchers.IO) {
            val songList = songRepository.getAllSongs().map {
                it.toUiModel()
            }
            val rootNode = FileTreeBuilder.buildTree(songList)
            setList(rootNode)
            dbSongList = songList
        }
    }

    fun syncMusic() {
        viewModelScope.launch(Dispatchers.IO) {
            val mediaStoreSource = MediaStoreSource(getApplication<Application>().contentResolver)

            mediaStoreSource.syncLibrary(songRepository)
            val songList = songRepository.getAllSongs().map {
                it.toUiModel()
            }
            if (songList == dbSongList) {
                Log.d("Sync Music", "songList was the same.")
                return@launch
            }
            val rootNode = FileTreeBuilder.buildTree(songList)
            setList(rootNode)
        }
    }

    init {
        viewModelScope.launch {
            query
                .debounce(400)
                .mapLatest { q ->
                    if (q.isBlank()) emptyList<Song>()
                    else withContext(Dispatchers.IO) {
                        songRepository.getSongByTitle(q).map { it.toUiModel() }
                    }
                }
                .collectLatest { filteredSongs = it }
        }
    }

    fun SongEntity.toUiModel(): Song {
        return Song(
            iD = this.mediaStoreId,
            albumId = this.mediaStoreAlbumId,
            artists = this.artists,
            title = this.title,
            album = this.album,
            date = this.date,
            albumArtist = this.albumArtist,
            track = this.track,
            disc = this.disc,
            bitrate = this.bitrate,
            sampleRate = this.sampleRate,
            channels = this.channels,
            bitsPerSample = this.bitsPerSample,
            duration = this.duration,
            dateModified = this.dateModified,
            size = this.size,
            path = this.path
        )
    }
}

sealed interface LibraryUIState {
    object Empty: LibraryUIState
    object Ready: LibraryUIState
    object Loading: LibraryUIState
}