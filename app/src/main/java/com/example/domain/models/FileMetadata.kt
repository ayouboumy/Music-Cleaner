package com.example.domain.models

data class FileMetadata(
    val album: String,
    val bitrate: Int,
    val hasAlbumArt: Boolean,
    val dateModifiedMs: Long,
    val mimeType: String,
    val relativeFolder: String
)
