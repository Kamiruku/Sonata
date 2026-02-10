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

fun <T> List<T>.findFirstIndex(curPath: String, selector: (T) -> String): Int {
    return binarySearch(this, 0, this.size, curPath, selector)
}

@Suppress("SameParameterValue")
private inline fun <T> binarySearch(
    list: List<T>, fromIndex: Int, toIndex: Int, key: String, selector: (T) -> String
): Int {
    var low = fromIndex
    var high = toIndex - 1

    while (low <= high) {
        val mid = (low + high) ushr 1
        val midVal = list[mid]

        val midKey = selector(midVal)

        if (midKey < key) low = mid + 1
        else if (midKey > key) high = mid - 1
        else error("index found for $key which should not have been found")
    }
    return low
}