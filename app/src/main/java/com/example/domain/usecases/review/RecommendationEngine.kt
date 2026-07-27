package com.example.domain.usecases.review

import com.example.data.metadata.MetadataExtractor
import com.example.domain.models.AudioFile
import com.example.domain.models.DuplicateReviewItem
import com.example.domain.models.ExactDuplicateGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecommendationEngine(private val metadataExtractor: MetadataExtractor) {

    suspend fun buildReviewItems(groups: List<ExactDuplicateGroup>): List<DuplicateReviewItem> = withContext(Dispatchers.Default) {
        groups.map { group ->
            val metadataMap = group.files.associateWith { metadataExtractor.extractMetadata(it) }
            
            val highestBitrate = metadataMap.values.maxOfOrNull { it.bitrate } ?: 0
            val newestDate = metadataMap.values.maxOfOrNull { it.dateModifiedMs } ?: 0

            var maxScore = -1
            var bestFile = group.files.first()
            val reasonsMap = mutableMapOf<AudioFile, List<String>>()

            for (file in group.files) {
                val meta = metadataMap[file]!!
                var score = 0
                val reasons = mutableListOf<String>()

                if (meta.bitrate == highestBitrate && meta.bitrate > 0) {
                    score += 40
                    reasons.add("Highest bitrate")
                }
                if (meta.hasAlbumArt) {
                    score += 20
                    reasons.add("Album artwork exists")
                }
                if (meta.album != "Unknown Album" && file.artist != "Unknown Artist" && file.title != "Unknown Title") {
                    score += 15
                    reasons.add("Complete metadata")
                }
                if (meta.relativeFolder.equals("Music", ignoreCase = true)) {
                    score += 15
                    reasons.add("Located in Music folder")
                }
                if (meta.dateModifiedMs == newestDate && meta.dateModifiedMs > 0) {
                    score += 10
                    reasons.add("Newest modification date")
                }
                
                if (reasons.isEmpty()) {
                    reasons.add("Default choice")
                }
                
                reasonsMap[file] = reasons
                if (score > maxScore) {
                    maxScore = score
                    bestFile = file
                }
            }
            
            DuplicateReviewItem(
                group = group,
                fileMetadata = metadataMap,
                recommendedToKeep = bestFile,
                recommendationReasons = reasonsMap
            )
        }
    }
}
