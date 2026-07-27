package com.example.presentation.uiState

import com.example.domain.models.AudioFile
import com.example.domain.models.CandidateGroup
import com.example.domain.models.ExactDuplicateGroup

data class HomeUiState(
    val isScanning: Boolean = false,
    val scanProgress: Int = 0,
    
    val isHashing: Boolean = false,
    val hashProgress: Int = 0,
    
    val totalSongs: Int = 0,
    val hasPermissionError: Boolean = false,
    
    val candidateGroupsFound: Int = 0,
    val candidateFilesCount: Int = 0,
    val candidateGroups: List<CandidateGroup> = emptyList(),
    
    val exactGroupsFound: Int = 0,
    val exactDuplicatesCount: Int = 0,
    val reclaimableSize: Long = 0L,
    val exactDuplicateGroups: List<ExactDuplicateGroup> = emptyList(),
    
    val showReviewScreen: Boolean = false
)
