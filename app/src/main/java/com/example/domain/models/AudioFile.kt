package com.example.domain.models

data class AudioFile(
    val id: Long,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val path: String
)
