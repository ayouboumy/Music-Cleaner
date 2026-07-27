package com.example.data.detector

import com.example.domain.models.AudioFile
import com.example.domain.models.CandidateGroup
import com.example.domain.models.ExactDuplicateGroup
import com.example.domain.models.ScanReport
import com.example.data.hashing.FileHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

class ExactDuplicateDetector(
    private val fileHasher: FileHasher
) {
    suspend fun findExactDuplicates(
        candidateGroups: List<CandidateGroup>,
        onProgress: (Int, Int) -> Unit
    ): ScanReport = withContext(Dispatchers.Default) {
        val allCandidateFiles = candidateGroups.flatMap { it.files }
        val totalFiles = allCandidateFiles.size
        
        if (totalFiles == 0) {
            return@withContext ScanReport(emptyList(), emptyList())
        }

        val concurrencyLimit = Semaphore(4) // Max 4 concurrent disk reads
        var processedCount = 0
        val skippedFiles = mutableListOf<AudioFile>()

        // Hash files concurrently, but bounded by semaphore
        val hashedFiles = allCandidateFiles.map { file ->
            async {
                concurrencyLimit.withPermit {
                    ensureActive() // Stop if coroutine is cancelled
                    val hash = fileHasher.hashFile(file.path)
                    
                    val result = if (hash != null) {
                        file to hash
                    } else {
                        synchronized(skippedFiles) { skippedFiles.add(file) }
                        null
                    }

                    // Report progress safely
                    synchronized(this@ExactDuplicateDetector) {
                        processedCount++
                        onProgress(processedCount, totalFiles)
                    }
                    
                    result
                }
            }
        }.awaitAll().filterNotNull()

        // Group by exact SHA-256 matches
        val exactGroups = hashedFiles.groupBy { it.second }
            .filterValues { it.size > 1 }
            .map { (hash, pairs) ->
                ExactDuplicateGroup(
                    hash = hash,
                    files = pairs.map { it.first }
                )
            }

        ScanReport(
            exactDuplicateGroups = exactGroups,
            skippedFiles = skippedFiles
        )
    }
}
