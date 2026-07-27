package com.example.domain.usecases

import com.example.domain.models.CandidateGroup
import com.example.domain.models.ScanReport
import com.example.data.detector.ExactDuplicateDetector

class FindExactDuplicatesUseCase(
    private val exactDuplicateDetector: ExactDuplicateDetector
) {
    suspend operator fun invoke(
        candidateGroups: List<CandidateGroup>,
        onProgress: (Int, Int) -> Unit
    ): ScanReport {
        return exactDuplicateDetector.findExactDuplicates(candidateGroups, onProgress)
    }
}
