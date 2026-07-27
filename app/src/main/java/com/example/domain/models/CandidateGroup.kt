package com.example.domain.models

data class CandidateGroup(
    val files: List<AudioFile>
) {
    val sizeBytes: Long
        get() = files.firstOrNull()?.sizeBytes ?: 0L
}
