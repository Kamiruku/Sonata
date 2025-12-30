package com.kamiruku.sonata.taglib

object TagLib {
    init { System.loadLibrary("taglib") }

    external fun getMetadata(fd: Int): HashMap<String, Array<String>>

    external fun getAudioProperties(fd: Int): IntArray
}