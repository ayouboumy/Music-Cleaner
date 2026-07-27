package com.example.domain.models

data class ExactDuplicateGroup(
    val hash: String,
    val files: List<AudioFile>
) {
    val sizeBytes: Long
        get() = files.firstOrNull()?.sizeBytes ?: 0L
}
