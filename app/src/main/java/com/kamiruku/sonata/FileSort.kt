package com.kamiruku.sonata

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.util.Locale


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
    var iD: Long = 0,
    var title: String? = null,
    var artist: String? = null,
    var path: String? = null,
    var album: String? = null,
    var duration: Long? = null,
    var albumId: Long = 0
)

object FileTreeBuilder {
    fun buildTree(audioList: List<Song>): FileNode {
        val root = FileNode("Music", isFolder = true)
        var sortId = 0

        for (song in audioList) {
            //placeholder
            val parts = song.path
                ?.replace("storage/E58E-9E76/Music", "")
                ?.split('/')
                ?.filter { it.isNotBlank() }
                ?: continue

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
}

fun Long.toTime(): String {
    //ms -> s
    val seconds = this / 1000
    val minutes = seconds/ 60
    val hours = minutes/ 60

    val mins = minutes - hours * 60
    val secs = seconds - (hours * 3600 + mins * 60)

    return if (hours == 0L) String.format(Locale.US, "%02d:%02d", mins, secs)
    else String.format(Locale.US, "%02d:%02d:%02d", hours, mins, secs)
}

class SharedViewModel: ViewModel() {
    private var songList: FileNode? = null
    private val nodeIndex = mutableMapOf<Int, FileNode>()

    fun setList(rootNode: FileNode) {
        songList = rootNode
        nodeIndex.clear()
        buildIndex(rootNode)
    }

    fun getList(): FileNode? {
        return songList
    }

    private fun buildIndex(node: FileNode) {
        nodeIndex[node.sortId] = node
        for (child in node.children.values) {
            buildIndex(child)
        }
    }

    fun findNode(sortId: Int): FileNode? = nodeIndex[sortId]
}