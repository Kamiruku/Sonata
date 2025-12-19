package com.kamiruku.sonata

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.util.Locale


@Parcelize
data class FileNode(
    val name: String,
    val song: @RawValue Song? = null,
    val isFolder: Boolean = true,
    val children: MutableList<FileNode> = mutableListOf(),
    var musicTotal: Int = 0,
    var durationTotal: Long = 0
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
        val root = FileNode("Music",null)

        for (song in audioList) {
            //placeholder
            val parts = song.path?.
                replace("storage/E58E-9E76/Music", "")?.
                split('/')?.filter { it.isNotEmpty() }
            var currentNode = root

            for ((index, part) in parts!!.withIndex()) {
                val isLast = (index == parts.lastIndex)
                val existingChild = currentNode.children.find { it.name == part }

                if (existingChild != null) {
                    currentNode = existingChild
                } else {
                    val newNode = FileNode(
                        name = part,
                        song = song,
                        isFolder = !isLast
                    )
                    currentNode.children.add(newNode)
                    currentNode = newNode
                }
            }
        }
        sortChildren(root)
        childrenTotalCountDuration(root)
        return root
    }

    private fun sortChildren(nodes: FileNode) {
        nodes.children.sortBy { it.name.lowercase() }
        for (child in nodes.children) {
            sortChildren(child)
        }
    }

    private fun childrenTotalCountDuration(nodes: FileNode) {
        var count = 0
        var duration = 0L

        nodes.children.forEach { child ->
            if (!child.isFolder) {
                count++
                duration += child.song?.duration ?: 0L
            } else {
                childrenTotalCountDuration(child)
                count += child.musicTotal
                duration += child.durationTotal
            }
        }
        nodes.musicTotal = count
        nodes.durationTotal = duration
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