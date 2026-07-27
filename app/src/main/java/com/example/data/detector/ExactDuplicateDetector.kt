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
import kotlinx.coroutines.channels.Channel
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

        var processedCount = 0
        val skippedFiles = mutableListOf<AudioFile>()
        val hashedFiles = mutableListOf<Pair<AudioFile, String>>()

        val channel = Channel<AudioFile>(Channel.UNLIMITED)
        allCandidateFiles.forEach { channel.trySend(it) }
        channel.close()

        val workerCount = 4
        
        val workers = List(workerCount) {
            async {
                for (file in channel) {
                    ensureActive()
                    val hash = fileHasher.hashFile(file.uri)
                    
                    if (hash != null) {
                        synchronized(hashedFiles) { hashedFiles.add(file to hash) }
                    } else {
                        synchronized(skippedFiles) { skippedFiles.add(file) }
                    }

                    synchronized(this@ExactDuplicateDetector) {
                        processedCount++
                        onProgress(processedCount, totalFiles)
                    }
                }
            }
        }
        
        workers.awaitAll()

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
