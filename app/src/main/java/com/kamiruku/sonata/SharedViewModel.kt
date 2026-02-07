package com.kamiruku.sonata

import android.app.Application
import android.os.Environment
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kamiruku.sonata.datastore.DataStoreInstance
import com.kamiruku.sonata.db.SongEntity
import com.kamiruku.sonata.db.SongRepository
import com.kamiruku.sonata.utils.findFirstIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SharedViewModel(
    application: Application,
    private val songRepository: SongRepository
): AndroidViewModel(application) {
    private val _rootNodes = MutableStateFlow<List<FileNode>?>(null)
    val rootNodes: StateFlow<List<FileNode>?> = _rootNodes

    private val _songList = MutableStateFlow<List<FileNode>>(emptyList())
    val songList: StateFlow<List<FileNode>> = _songList

    private val nodeIndex = mutableMapOf<String, FileNode>()

    private val _uiState = MutableStateFlow<LibraryUIState>(LibraryUIState.Loading)
    val uiState: StateFlow<LibraryUIState> = _uiState.asStateFlow()

    val query = MutableStateFlow("")
    var filteredSongs by mutableStateOf<List<Song>>(emptyList())

    var selectedItems by mutableStateOf<Set<String>>(emptySet())

    private val _inSelectionMode = MutableStateFlow(false)
    val inSelectionMode: StateFlow<Boolean> = _inSelectionMode.asStateFlow()

    private var dbSongList: List<Song>? = null

    fun setList(rootNodes: List<FileNode>) {
        _rootNodes.value = rootNodes
        nodeIndex.clear()
        buildIndex(rootNodes)
        _songList.value = nodeIndex.values.filter { !it.isFolder }
        //_songList.value = rootNodes.flattenSongs().sortedBy { it.path }

        _uiState.value =
            if (songList.value.isEmpty()) LibraryUIState.Empty
            else LibraryUIState.Ready
    }

    private fun buildIndex(nodes: List<FileNode>) {
        fun addToIndex(node: FileNode) {
            nodeIndex[node.absolutePath] = node
            for (child in node.children.values) {
                addToIndex(child)
            }
        }

        for (node in nodes) {
            addToIndex(node)
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

    val rootPaths: List<File> = run {
        val dirs = application.getExternalFilesDirs(null)

        val storages = mutableSetOf<File>()
        storages.add(Environment.getExternalStorageDirectory())

        dirs.filterNotNull().forEach { dir ->
            val root = dir.absolutePath.split("/Android/")[0]
            val file = File(root)
            if (file.exists()) {
                storages.add(file)
            }
        }
        storages.toList()
    }

    fun loadCachedSongs() {
        viewModelScope.launch(Dispatchers.IO) {
            val paths = pathSrcs.filterNotNull().first().sorted()
            if (paths.isEmpty()) return@launch

            val songList = songRepository.getAllSongs().map {
                it.toUiModel()
            }
            val songString = songList.map { it.path }

            val indexList = mutableListOf<Int>()
            val pathList = mutableListOf<FileNode>()

            /**
             * Assumes that contents of songList will be contained by all source paths.
             * Needs a way to ensure this.
             */

            for (src in paths) {
                //root should never be null
                val root = rootPaths.find { src.startsWith("${it.path}/") }!!.path
                val relSrc = src.removePrefix("$root/") + '/'
                val index = songString.findFirstIndex(relSrc)
                if (index >= songString.size) continue
                if (songString[index].startsWith(relSrc)) {
                    indexList.add(index)
                }
            }

            for (i in indexList.indices) {
                val start = indexList[i]
                val end = if (i != indexList.lastIndex) indexList[i + 1] else songList.size
                val list = songList.subList(start, end)
                val rootNode = FileTreeBuilder.buildTree(list)
                pathList.add(rootNode)
            }

            setList(pathList)
            dbSongList = songList
        }
    }

    fun syncMusic() {
        viewModelScope.launch(Dispatchers.IO) {
            val paths = pathSrcs.filterNotNull().first().sorted()
            if (paths.isEmpty()) return@launch

            val mediaStoreSource = MediaStoreSource(getApplication<Application>().contentResolver)

            mediaStoreSource.syncLibrary(songRepository, paths)
            val songList = songRepository.getAllSongs().map {
                it.toUiModel()
            }
            if (songList == dbSongList) {
                Log.d("Sync Music", "songList was the same.")
                return@launch
            }

            val songString = songList.map { it.path }

            val indexList = mutableListOf<Int>()
            val pathList = mutableListOf<FileNode>()

            for (src in paths) {
                //root should never be null
                val root = rootPaths.find { src.startsWith("${it.path}/") }!!.path
                val relSrc = src.removePrefix("$root/") + '/'
                val index = songString.findFirstIndex(relSrc)
                if (index >= songString.size) continue
                if (songString[index].startsWith(relSrc)) {
                    indexList.add(index)
                }
            }

            Log.i("IndexList", indexList.toString())

            for (i in indexList.indices) {
                val start = indexList[i]
                val end = if (i != indexList.lastIndex) indexList[i + 1] else songList.size
                val list = songList.subList(start, end)
                val rootNode = FileTreeBuilder.buildTree(list)
                pathList.add(rootNode)
            }

            setList(pathList)
        }
    }

    fun toggleSelect(path: String) {
        selectedItems =
            if (path in selectedItems) selectedItems - path
            else selectedItems + path
    }

    fun toggleSelect(paths: List<String>) {
        selectedItems =
            if (selectedItems.containsAll(paths)) selectedItems - paths
            else selectedItems + paths
    }

    fun setSelected(paths: List<String>) {
        selectedItems = paths.toSet()
    }

    fun clearSelected(mode: Boolean = false) {
        //if mode = true, keep select screen open even after clearing
        selectedItems = emptySet()
        setSelectionMode(mode)
    }

    fun setSelectionMode(mode: Boolean) {
        _inSelectionMode.value = mode
    }

    private val _pathSrcs = MutableStateFlow<Set<String>?>(null)
    val pathSrcs: StateFlow<Set<String>?> = _pathSrcs

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

    init {
        getPathSrcs()
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