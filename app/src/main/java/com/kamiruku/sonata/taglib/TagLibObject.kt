package com.kamiruku.sonata.taglib

data class TagLibObject(
    val lengthInMilliseconds: Int,
    val bitrate: Int,
    val sampleRate: Int,
    val channels: Int,
    val bitsPerSample: Int,

    val propertyMap: HashMap<String, Array<String>>
)
