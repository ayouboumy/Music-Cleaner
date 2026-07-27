package com.example.presentation.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.common.utils.DependencyProvider
import com.example.domain.models.ExactDuplicateGroup
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DuplicateReviewViewModel(
    private val exactDuplicateGroups: List<ExactDuplicateGroup>
) : ViewModel() {
    
    private val recommendationEngine = DependencyProvider.recommendationEngine
    
    val selectionManager = SelectionManager()
    val audioPlayer = AudioPreviewPlayer()

    private val _uiState = MutableStateFlow(DuplicateReviewUiState())
    
    val uiState: StateFlow<DuplicateReviewUiState> = combine(
        _uiState,
        selectionManager.selectedForDeletion,
        audioPlayer.currentlyPlaying
    ) { state, selected, playing ->
        state.copy(
            filesSelectedForDeletion = selected,
            currentlyPlayingPath = playing
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DuplicateReviewUiState())

    init {
        viewModelScope.launch {
            val items = recommendationEngine.buildReviewItems(exactDuplicateGroups)
            _uiState.update { it.copy(isLoading = false, reviewItems = items) }
            
            // By default, select all except recommended
            selectionManager.keepRecommended(items)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
    }
}

class DuplicateReviewViewModelFactory(private val groups: List<ExactDuplicateGroup>) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DuplicateReviewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DuplicateReviewViewModel(groups) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
