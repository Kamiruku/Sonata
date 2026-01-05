package com.kamiruku.sonata

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedViewModel(): ViewModel() {
    private val _rootNode = MutableStateFlow<FileNode?>(null)
    val rootNode: StateFlow<FileNode?> = _rootNode

    private val _songList = MutableStateFlow<List<FileNode>>(emptyList())
    val songList: StateFlow<List<FileNode>> = _songList

    private val nodeIndex = mutableMapOf<Int, FileNode>()

    private val _uiState = MutableStateFlow<LibraryUIState>(LibraryUIState.Loading)
    val uiState: StateFlow<LibraryUIState> = _uiState.asStateFlow()

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

    fun findNode(sortId: Int): FileNode? = nodeIndex[sortId]
}

sealed interface LibraryUIState {
    object Empty: LibraryUIState
    object Ready: LibraryUIState
    object Loading: LibraryUIState
}