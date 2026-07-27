package com.example.domain.models

data class ScanReport(
    val exactDuplicateGroups: List<ExactDuplicateGroup>,
    val skippedFiles: List<AudioFile>
) {
    val totalDuplicateReclaimableSize: Long
        get() = exactDuplicateGroups.sumOf { it.sizeBytes * (it.files.size - 1) }
        
    val exactDuplicatesCount: Int
        get() = exactDuplicateGroups.sumOf { it.files.size - 1 }
}
