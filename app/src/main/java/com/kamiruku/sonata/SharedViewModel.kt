package com.kamiruku.sonata

import android.app.Application
import android.os.Environment
import android.util.Log
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
    private val _rootNodes = MutableStateFlow<List<FileNode>>(emptyList())
    val rootNodes: StateFlow<List<FileNode>> = _rootNodes.asStateFlow()

    private val _songList = MutableStateFlow<List<FileNode>>(emptyList())
    val songList: StateFlow<List<FileNode>> = _songList.asStateFlow()

    private var nodeIndex: Map<String, FileNode> = emptyMap()

    private val _uiState = MutableStateFlow<LibraryUIState>(LibraryUIState.Loading)
    val uiState: StateFlow<LibraryUIState> = _uiState.asStateFlow()

    val query = MutableStateFlow("")
    private val _filteredSongs = MutableStateFlow<List<Song>>(emptyList())
    val filteredSongs: StateFlow<List<Song>> = _filteredSongs.asStateFlow()

    private val _selectedItems = MutableStateFlow<Set<String>>(emptySet())
    val selectedItems: StateFlow<Set<String>> = _selectedItems.asStateFlow()

    private val _inSelectionMode = MutableStateFlow(false)
    val inSelectionMode: StateFlow<Boolean> = _inSelectionMode.asStateFlow()

    private var dbSongList: List<Song>? = null

    /**
     * songList should be sorted without explicitly sorting because songRepository
     * songs are returned sorted by path, which means the tree should be sorted
     * and computeTotal should preserve this order.
     */
    private fun setList(rootNodes: List<FileNode>) {
        _rootNodes.value = rootNodes
        _songList.value = nodeIndex.values.filter { !it.isFolder }//.sortedBy { it.absolutePath }

        check(_songList.value.size == dbSongList?.size) {
            "_songList size: ${_songList.value.size} is not equal to dbSongList size: ${dbSongList?.size}"
        }

        _uiState.value =
            if (_songList.value.isEmpty()) LibraryUIState.Empty
            else LibraryUIState.Ready
    }

    fun findNode(sortId: String): FileNode? = nodeIndex[sortId]

    val rootPaths: List<File> = buildSet {
        addAll(
            application.getExternalFilesDirs(null)
            .filterNotNull()
            .map { File(it.absolutePath.substringBefore("/Android/")) }
            .filter { it.exists() }
        )
        add(Environment.getExternalStorageDirectory())
    }.toList()

    fun loadCachedSongs() {
        viewModelScope.launch(Dispatchers.IO) {
            val snapshotPaths = getSnapShotPathSrcs()
            if (snapshotPaths.isEmpty()) return@launch

            Log.d("snapshot_paths", snapshotPaths.toString())

            val songList = songRepository.getAllSongs().map {
                it.toUiModel()
            }
            dbSongList = songList

            buildTree(songList, snapshotPaths)
        }
    }

    fun syncMusic() {
        viewModelScope.launch(Dispatchers.IO) {
            val paths = pathSrcs.filterNotNull().first()
            val mediaStoreSource = MediaStoreSource(getApplication<Application>().contentResolver)

            //savedPaths only contains folder which actually have contents
            val savedPaths = mediaStoreSource.syncLibrary(songRepository, paths.toList())
            //get updated songList (to paths)
            val songList = songRepository.getAllSongs().map {
                it.toUiModel()
            }
            if (songList == dbSongList) {
                Log.d("Sync Music", "songList was the same.")
                return@launch
            }
            dbSongList = songList

            /*
            only snapshot folders with content
            if there is a change in the retrieved songs, this also guarantees there
            won't be empty folders on loadCached Songs
             */
            Log.d("SyncMusic savedPaths", savedPaths.toString())
            saveSnapShotPathSrcs(savedPaths.toSet())

            buildTree(songList, savedPaths)
        }
    }

    /**
     * paths is guaranteed to have no empty folders, hence songList will only be empty if paths is empty
     */
    private fun buildTree(songList: List<Song>, paths: Set<String>) {
        if (paths.isEmpty()) {
            nodeIndex = emptyMap()
            setList(emptyList())
            return
        }

        val indexList = mutableListOf<Int>()
        val pathList = mutableListOf<FileNode>()

        val relPaths = paths.map { src ->
            val root = rootPaths.find { src.startsWith("${it.path}/") }!!.path
            src.removePrefix("$root/") + '/'
        }.sorted()

        for (relSrc in relPaths.withIndex()) {
            val folderIndex = songList.findFirstIndex(relSrc.value) { it.path }

            check(folderIndex < songList.size) {
                "folder index: $folderIndex is bigger than amount of songs: ${songList.size}"
            }
            check((songList[folderIndex].path).startsWith(relSrc.value)) {
                "${songList[folderIndex].path} " +
                        "\ndid not start with ${relSrc.value} " +
                        "\nfor its index: $folderIndex"
            }
            if (relSrc.index != 0) {
                check((songList[folderIndex - 1].path).startsWith(relPaths[relSrc.index - 1])) {
                    "end boundary: " +
                            "\n${songList[folderIndex - 1].path} " +
                            "\ndid not start with the previous src: ${relPaths[relSrc.index - 1]} " +
                            "\nfor end boundary index: ${folderIndex - 1}"
                }
            }

            indexList.add(folderIndex)
        }

        check(indexList[0] == 0) {
            "indexList had: ${indexList[0]} at index 0 instead of 0\n" +
                    indexList.toString()
        }

        val localNodeIndex = mutableMapOf<String, FileNode>()
        var listLength = 0

        for (i in indexList.indices) {
            val start = indexList[i]
            val end = if (i != indexList.lastIndex) indexList[i + 1] else songList.size
            val list = songList.subList(start, end)
            listLength += list.size
            val rootNode = FileTreeBuilder.buildTree(list, relPaths[i], localNodeIndex)
            pathList.add(rootNode)
        }

        check(listLength == songList.size) {
            "listLength: $listLength was not equal to songListSize: ${songList.size}"
        }

        nodeIndex = localNodeIndex
        setList(pathList)
    }

    fun toggleSelect(path: String) {
        _selectedItems.value =
            if (path in _selectedItems.value) _selectedItems.value - path
            else _selectedItems.value + path
    }

    fun toggleSelect(paths: List<String>) {
        _selectedItems.value =
            if (_selectedItems.value.containsAll(paths)) _selectedItems.value - paths
            else _selectedItems.value + paths
    }

    fun setSelected(paths: List<String>) {
        _selectedItems.value = paths.toSet()
    }

    fun clearSelected(mode: Boolean = false) {
        //if mode = true, keep select screen open even after clearing
        _selectedItems.value = emptySet()
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

    /**
     * getSnapShotPathSrcs is only called once, on app startup.
     */
    private suspend fun getSnapShotPathSrcs(): Set<String> =
        DataStoreInstance.getPathSrcs(
            getApplication(),
            DataStoreInstance.SnapShot_PathSrcs_KEY
        ).first()

    /**
     * saveSnapShotPathSrcs will only be called by syncMusic whenever changes to the db occur.
     * It does not contain empty folders.
     */
    private fun saveSnapShotPathSrcs(value: Set<String>) {
        viewModelScope.launch {
            DataStoreInstance.savePathSrcs(
                getApplication(),
                DataStoreInstance.SnapShot_PathSrcs_KEY,
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
                .collectLatest { _filteredSongs.value = it }
        }
    }

    private fun SongEntity.toUiModel(): Song {
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