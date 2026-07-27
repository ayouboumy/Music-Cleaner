package com.example.data.detector

import com.example.domain.models.AudioFile
import com.example.domain.models.CandidateGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

class CandidateDetector {
    suspend fun findCandidates(files: List<AudioFile>): List<CandidateGroup> = withContext(Dispatchers.Default) {
        val candidatesBySize = files.groupBy { it.sizeBytes }.filterValues { it.size > 1 }
        
        val finalGroups = mutableListOf<CandidateGroup>()
        
        for ((_, sizeGroup) in candidatesBySize) {
            val sortedByDuration = sizeGroup.sortedBy { it.durationMs }
            var currentGroup = mutableListOf<AudioFile>()
            
            for (file in sortedByDuration) {
                if (currentGroup.isEmpty()) {
                    currentGroup.add(file)
                } else {
                    val first = currentGroup.first()
                    // Group by duration using a tolerance of ±1 second (1000 ms)
                    if (abs(file.durationMs - first.durationMs) <= 1000) {
                        currentGroup.add(file)
                    } else {
                        if (currentGroup.size > 1) {
                            finalGroups.add(CandidateGroup(currentGroup.toList()))
                        }
                        currentGroup = mutableListOf(file)
                    }
                }
            }
            if (currentGroup.size > 1) {
                finalGroups.add(CandidateGroup(currentGroup))
            }
        }
        
        finalGroups
    }
}
