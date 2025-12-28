package com.kamiruku.sonata

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue


@Parcelize
data class FileNode(
    val name: String,
    var song: @RawValue Song? = null,
    val isFolder: Boolean,
    val children: MutableMap<String, FileNode> = mutableMapOf(),

    var musicTotal: Int = 0,
    var durationTotal: Long = 0,
    var albumId: Long = 0L, //or closest
    var sortId: Int = 0
) : Parcelable

data class Song (
    val iD: Long,
    val title: String,
    val artist: String,
    val path: String,
    val album: String,
    val duration: Long,
    val albumId: Long,
    val track: Int,
    val disc: Int,
    val year: String
)

object FileTreeBuilder {
    fun buildTree(audioList: List<Song>): FileNode {
        val commonPrefix = findCommonPrefix(audioList)
        val lastFolderName = commonPrefix.trimEnd('/').substringAfterLast('/')

        val root = FileNode(lastFolderName, isFolder = true)
        var sortId = 0

        for (song in audioList) {
            val parts = song.path
                .replace(commonPrefix, "")
                .split('/')
                .filter { it.isNotBlank() }

            var currentNode = root

            for ((index, part) in parts.withIndex()) {
                val isLast = (index == parts.lastIndex)

                currentNode = currentNode.children.getOrPut(part) {
                    sortId++
                    FileNode(
                        name = part,
                        isFolder = !isLast,
                        song = if (isLast) song else null,
                        sortId = sortId
                    )
                }
            }
        }
        computeTotalAndSort(root)
        return root
    }

    private fun computeTotalAndSort(node: FileNode) {
        if (!node.isFolder) {
            node.musicTotal = 1
            node.durationTotal = node.song?.duration ?: 0
            node.albumId = node.song?.albumId ?: 0L
            return
        }

        var count = 0
        var duration = 0L
        var albumId: Long? = null

        node.children.values.forEach { child ->
            computeTotalAndSort(child)
            count += child.musicTotal
            duration += child.durationTotal
            if (albumId == null && child.albumId != 0L) albumId = child.albumId
        }

        node.musicTotal = count
        node.durationTotal = duration
        node.albumId = albumId ?: 0L

        val sorted = node.children.toSortedMap(compareBy { it.lowercase() })
        node.children.clear()
        node.children.putAll(sorted)
    }

    private fun findCommonPrefix(audioList: List<Song>): String {
        val paths = audioList.map { it.path }

        if (paths.isEmpty()) return ""

        val first = paths[0]
        var end: Int

        for (i in first.indices) {
            val c = first[i]
            for (s in paths) {
                if (i >= s.length || s[i] != c) {
                    end = i
                    return first.take(end)
                }
            }
        }
        return first
    }
}

class SharedViewModel: ViewModel() {
    private var rootNode: FileNode? = null
    private val nodeIndex = mutableMapOf<Int, FileNode>()

    private var songList: List<FileNode>? = null

    fun setList(rootNode: FileNode) {
        this@SharedViewModel.rootNode = rootNode
        nodeIndex.clear()
        buildIndex(rootNode)
        songList = rootNode.flattenSongs()
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

    fun FileNode.flattenSongs(): List<FileNode> {
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