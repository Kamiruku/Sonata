package com.kamiruku.sonata.taglib

object TagLib {
    init { System.loadLibrary("sonata") }

    external fun getMetadata(path: String): HashMap<String, Array<String>>

    external fun getAudioProperties(fd: Int): IntArray
}