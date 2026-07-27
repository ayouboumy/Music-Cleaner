package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.utils.DependencyProvider
import com.example.presentation.uiState.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val mediaScanner = DependencyProvider.mediaScanner
    private val findCandidateGroupsUseCase = DependencyProvider.findCandidateGroupsUseCase
    private val findExactDuplicatesUseCase = DependencyProvider.findExactDuplicatesUseCase

    fun scanFiles() {
        viewModelScope.launch {
            // 1. Scan Phase
            _uiState.update { it.copy(isScanning = true, scanProgress = 0, hasPermissionError = false, showReviewScreen = false) }
            
            val files = mediaScanner.scanAudioFiles { progress ->
                _uiState.update { it.copy(scanProgress = progress) }
            }
            
            // 2. Candidate Detection Phase
            val candidates = findCandidateGroupsUseCase(files)
            val candidateFilesCount = candidates.sumOf { it.files.size }
            
            _uiState.update { 
                it.copy(
                    isScanning = false,
                    scanProgress = 100,
                    totalSongs = files.size,
                    candidateGroupsFound = candidates.size,
                    candidateFilesCount = candidateFilesCount,
                    candidateGroups = candidates
                ) 
            }

            // 3. Exact Duplicate Hashing Phase
            if (candidates.isNotEmpty()) {
                _uiState.update { it.copy(isHashing = true, hashProgress = 0) }

                val report = findExactDuplicatesUseCase(candidates) { processed, total ->
                    val progress = if (total > 0) (processed * 100) / total else 100
                    _uiState.update { it.copy(hashProgress = progress) }
                }

                _uiState.update {
                    it.copy(
                        isHashing = false,
                        hashProgress = 100,
                        exactGroupsFound = report.exactDuplicateGroups.size,
                        exactDuplicatesCount = report.exactDuplicatesCount,
                        reclaimableSize = report.totalDuplicateReclaimableSize,
                        exactDuplicateGroups = report.exactDuplicateGroups
                    )
                }
            }
        }
    }

    fun onPermissionDenied() {
        _uiState.update { it.copy(isScanning = false, hasPermissionError = true) }
    }

    fun openReview() {
        _uiState.update { it.copy(showReviewScreen = true) }
    }

    fun closeReview() {
        _uiState.update { it.copy(showReviewScreen = false) }
    }
}
