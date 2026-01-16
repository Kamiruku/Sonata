package com.kamiruku.sonata

import android.os.Parcelable
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
    var sortId: String = ""
) : Parcelable

data class Song (
    //mediastore ids
    val iD: Long,
    val albumId: Long,

    //metadata
    val artists: Array<String>,
    val title: String,
    val album: String,
    val date: String,
    val albumArtist: String,
    val track: String,
    val disc: String,

    //audio properties
    val bitrate : Int,
    val sampleRate: Int,
    val channels: Int,
    val bitsPerSample: Int,
    val duration: Long,

    //file properties
    val dateModified: Long,
    val size: Long,
    val path: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Song

        if (iD != other.iD) return false
        if (albumId != other.albumId) return false
        if (bitrate != other.bitrate) return false
        if (sampleRate != other.sampleRate) return false
        if (channels != other.channels) return false
        if (bitsPerSample != other.bitsPerSample) return false
        if (duration != other.duration) return false
        if (dateModified != other.dateModified) return false
        if (size != other.size) return false
        if (!artists.contentEquals(other.artists)) return false
        if (title != other.title) return false
        if (album != other.album) return false
        if (date != other.date) return false
        if (albumArtist != other.albumArtist) return false
        if (track != other.track) return false
        if (disc != other.disc) return false
        if (path != other.path) return false

        return true
    }

    override fun hashCode(): Int {
        var result = iD.hashCode()
        result = 31 * result + albumId.hashCode()
        result = 31 * result + bitrate
        result = 31 * result + sampleRate
        result = 31 * result + channels
        result = 31 * result + bitsPerSample
        result = 31 * result + duration.hashCode()
        result = 31 * result + dateModified.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + artists.contentHashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + album.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + albumArtist.hashCode()
        result = 31 * result + track.hashCode()
        result = 31 * result + disc.hashCode()
        result = 31 * result + path.hashCode()
        return result
    }
}

object FileTreeBuilder {
    fun buildTree(audioList: List<Song>): FileNode {
        val parentFolder = findParentFolder(audioList)
        val lastFolderName = parentFolder
            .trimEnd('/')
            .substringAfterLast('/')
            .ifBlank { "root" }

        val root = FileNode(lastFolderName, isFolder = true, sortId = lastFolderName)

        for (song in audioList) {
            val parts = song.path
                .removePrefix("$parentFolder/")
                .split('/')
                .filter { it.isNotBlank() }

            var currentNode = root

            for ((index, part) in parts.withIndex()) {
                val isLast = (index == parts.lastIndex)

                currentNode = currentNode.children.getOrPut(part) {
                    val newSortId =
                        if (currentNode.sortId.isEmpty()) part
                        else "${currentNode.sortId}/$part"
                    FileNode(
                        name = part,
                        isFolder = !isLast,
                        song = if (isLast) song else null,
                        sortId = newSortId
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

    private fun findParentFolder(audioList: List<Song>): String {
        val paths = audioList.map { it.path }

        if (paths.isEmpty()) return ""

        val splitPaths = paths.map { it.split('/') }

        val first = splitPaths[0]
        val common = mutableListOf<String>()

        for (index in first.indices) {
            val segment = first[index]

            if (splitPaths.any { it.size <= index || it[index] != segment }) {
                break
            }

            common += segment
        }

        return common.joinToString("/")
    }
}