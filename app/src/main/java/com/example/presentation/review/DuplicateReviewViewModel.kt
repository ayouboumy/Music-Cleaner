package com.example.presentation.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.common.utils.DependencyProvider
import com.example.domain.models.DuplicateReviewItem
import com.example.domain.models.ExactDuplicateGroup
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DuplicateReviewViewModel(
    private val exactDuplicateGroups: List<ExactDuplicateGroup>
) : ViewModel() {
    
    private val recommendationEngine = DependencyProvider.recommendationEngine
    
    val selectionManager = SelectionManager()
    val audioPlayer = AudioPreviewPlayer(DependencyProvider.appContext)

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
        val items = recommendationEngine.buildInitialItems(exactDuplicateGroups)
        _uiState.update { it.copy(isLoading = false, reviewItems = items) }
        selectionManager.keepRecommended(items)
    }

    fun loadMetadataForGroup(item: DuplicateReviewItem) {
        if (item.isMetadataLoaded || item.isMetadataLoading) return
        
        viewModelScope.launch {
            val items = _uiState.value.reviewItems.toMutableList()
            val index = items.indexOfFirst { it.group == item.group }
            if (index == -1) return@launch
            
            items[index] = item.copy(isMetadataLoading = true)
            _uiState.update { it.copy(reviewItems = items.toList()) }
            
            val updatedItem = recommendationEngine.loadMetadataAndRecommend(items[index])
            
            val currentItems = _uiState.value.reviewItems.toMutableList()
            val currentIndex = currentItems.indexOfFirst { it.group == item.group }
            if (currentIndex != -1) {
                currentItems[currentIndex] = updatedItem
                _uiState.update { it.copy(reviewItems = currentItems.toList()) }
                
                // Keep recommended if the user hasn't explicitly changed selection?
                // For safety, just let it update the UI. If we want we can update selection.
                if (!selectionManager.selectedForDeletion.value.contains(updatedItem.recommendedToKeep)) {
                    val newSelection = selectionManager.selectedForDeletion.value.toMutableSet()
                    newSelection.removeAll(updatedItem.group.files.toSet())
                    newSelection.addAll(updatedItem.group.files.filter { it != updatedItem.recommendedToKeep })
                    selectionManager.updateSelectionRaw(newSelection)
                }
            }
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
