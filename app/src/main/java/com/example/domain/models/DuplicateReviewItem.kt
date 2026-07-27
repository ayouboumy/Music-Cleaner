package com.example.domain.models

data class DuplicateReviewItem(
    val group: ExactDuplicateGroup,
    val fileMetadata: Map<AudioFile, FileMetadata>,
    val recommendedToKeep: AudioFile,
    val recommendationReasons: Map<AudioFile, List<String>>
)
