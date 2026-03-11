package com.kamiruku.sonata.taglib

data class PictureObject(
    val data: ByteArray,
    val description: String,
    val pictureType: String,
    val mimeType: String
)
