package com.example.presentation.review

import com.example.domain.models.AudioFile
import com.example.domain.models.DuplicateReviewItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SelectionManager {
    private val _selectedForDeletion = MutableStateFlow<Set<AudioFile>>(emptySet())
    val selectedForDeletion: StateFlow<Set<AudioFile>> = _selectedForDeletion.asStateFlow()

    fun toggleSelection(file: AudioFile, item: DuplicateReviewItem) {
        val current = _selectedForDeletion.value.toMutableSet()
        if (current.contains(file)) {
            current.remove(file)
        } else {
            val selectedInGroup = item.group.files.count { current.contains(it) }
            if (selectedInGroup < item.group.files.size - 1) {
                current.add(file)
            }
        }
        _selectedForDeletion.value = current
    }

    fun keepRecommended(items: List<DuplicateReviewItem>) {
        val newSelection = mutableSetOf<AudioFile>()
        for (item in items) {
            val toDelete = item.group.files.filter { it != item.recommendedToKeep }
            newSelection.addAll(toDelete)
        }
        _selectedForDeletion.value = newSelection
    }

    fun keepNewest(items: List<DuplicateReviewItem>) {
        val newSelection = mutableSetOf<AudioFile>()
        for (item in items) {
            val newest = item.group.files.maxByOrNull { it.dateModifiedMs }
            val toDelete = item.group.files.filter { it != newest }
            newSelection.addAll(toDelete)
        }
        _selectedForDeletion.value = newSelection
    }

    fun keepOldest(items: List<DuplicateReviewItem>) {
        val newSelection = mutableSetOf<AudioFile>()
        for (item in items) {
            val oldest = item.group.files.minByOrNull { it.dateModifiedMs }
            val toDelete = item.group.files.filter { it != oldest }
            newSelection.addAll(toDelete)
        }
        _selectedForDeletion.value = newSelection
    }

    fun keepLargest(items: List<DuplicateReviewItem>) {
        val newSelection = mutableSetOf<AudioFile>()
        for (item in items) {
            val largest = item.group.files.first() // Sizes are exact duplicates
            val toDelete = item.group.files.filter { it != largest }
            newSelection.addAll(toDelete)
        }
        _selectedForDeletion.value = newSelection
    }

    fun invertSelection(items: List<DuplicateReviewItem>) {
        val current = _selectedForDeletion.value
        val newSelection = mutableSetOf<AudioFile>()
        for (item in items) {
            val currentlySelected = item.group.files.filter { current.contains(it) }
            val currentlyUnselected = item.group.files.filter { !current.contains(it) }
            
            if (currentlyUnselected.size == 1) {
                val toLeave = currentlySelected.firstOrNull() ?: currentlyUnselected.first()
                newSelection.addAll(item.group.files.filter { it != toLeave })
            } else {
                if (currentlySelected.isEmpty()) {
                     val toLeave = currentlyUnselected.first()
                     newSelection.addAll(currentlyUnselected.filter { it != toLeave })
                } else {
                     newSelection.addAll(currentlyUnselected)
                }
            }
        }
        _selectedForDeletion.value = newSelection
    }

    fun selectAllDuplicates(items: List<DuplicateReviewItem>) {
         keepRecommended(items)
    }

    fun clearSelection() {
        _selectedForDeletion.value = emptySet()
    }
    
    fun updateSelectionRaw(newSelection: Set<AudioFile>) {
        _selectedForDeletion.value = newSelection
    }
}
