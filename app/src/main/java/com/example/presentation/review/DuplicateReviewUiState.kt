package com.example.presentation.review

import com.example.domain.models.AudioFile
import com.example.domain.models.DuplicateReviewItem

data class DuplicateReviewUiState(
    val isLoading: Boolean = true,
    val reviewItems: List<DuplicateReviewItem> = emptyList(),
    val filesSelectedForDeletion: Set<AudioFile> = emptySet(),
    val currentlyPlayingPath: String? = null
) {
    val duplicateGroupsCount: Int get() = reviewItems.size
    val duplicateFilesCount: Int get() = reviewItems.sumOf { it.group.files.size - 1 }
    val totalRecoverableStorage: Long get() = reviewItems.sumOf { it.group.sizeBytes * (it.group.files.size - 1) }
    
    val selectedFilesCount: Int get() = filesSelectedForDeletion.size
    val estimatedStorageAfterDeletion: Long get() = totalRecoverableStorage - filesSelectedForDeletion.sumOf { it.sizeBytes }
}
