package com.kamiruku.sonata.utils

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import com.kamiruku.sonata.FileNode
import java.util.Locale

fun getAlbumArt(albumId: Long): Uri =
    ContentUris.withAppendedId(
        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
        albumId)


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

fun FileNode.flattenNodes(): List<FileNode> {
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

fun List<String>.findFirstIndex(curPath: String): Int {
    var index = this.binarySearch(curPath)
    //will never find an exact match since we are finding a folder in a list of file paths
    index = - (index + 1)
    return index
}