package com.example.domain.models

data class DuplicateReviewItem(
    val group: ExactDuplicateGroup,
    val fileMetadata: Map<AudioFile, FileMetadata>? = null,
    val recommendedToKeep: AudioFile,
    val recommendationReasons: Map<AudioFile, List<String>> = emptyMap(),
    val isMetadataLoaded: Boolean = false,
    val isMetadataLoading: Boolean = false
)
