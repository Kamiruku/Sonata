package com.kamiruku.sonata

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedViewModel(): ViewModel() {
    private var rootNode: FileNode? = null
    private val nodeIndex = mutableMapOf<Int, FileNode>()
    private var songList: List<FileNode>? = null

    private val _uiState = MutableStateFlow<LibraryUIState>(LibraryUIState.Loading)
    val uiState: StateFlow<LibraryUIState> = _uiState.asStateFlow()

    fun setList(rootNode: FileNode) {
        this@SharedViewModel.rootNode = rootNode
        nodeIndex.clear()
        buildIndex(rootNode)
        songList = rootNode.flattenSongs()

        _uiState.value =
            if (songList?.isEmpty() == true) LibraryUIState.Empty
            else LibraryUIState.Ready
    }

    fun getRootNode(): FileNode? {
        return rootNode
    }

    fun getSongList(): List<FileNode> {
        return songList ?: listOf()
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

    fun findNode(sortId: Int): FileNode? = nodeIndex[sortId]
}

sealed interface LibraryUIState {
    object Empty: LibraryUIState
    object Ready: LibraryUIState
    object Loading: LibraryUIState
}