package com.example.domain.usecases

import com.example.domain.models.AudioFile
import com.example.domain.models.CandidateGroup
import com.example.data.detector.CandidateDetector

class FindCandidateGroupsUseCase(
    private val candidateDetector: CandidateDetector
) {
    suspend operator fun invoke(files: List<AudioFile>): List<CandidateGroup> {
        return candidateDetector.findCandidates(files)
    }
}
