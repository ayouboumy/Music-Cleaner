package com.example.domain.usecases.review

import com.example.data.metadata.MetadataExtractor
import com.example.domain.models.AudioFile
import com.example.domain.models.DuplicateReviewItem
import com.example.domain.models.ExactDuplicateGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecommendationEngine(private val metadataExtractor: MetadataExtractor) {

    fun buildInitialItems(groups: List<ExactDuplicateGroup>): List<DuplicateReviewItem> {
        return groups.map { group ->
            val bestFile = group.files.maxByOrNull { it.dateModifiedMs } ?: group.files.first()
            DuplicateReviewItem(
                group = group,
                recommendedToKeep = bestFile,
                recommendationReasons = mapOf(bestFile to listOf("Newest file (Metadata pending)"))
            )
        }
    }

    suspend fun loadMetadataAndRecommend(item: DuplicateReviewItem): DuplicateReviewItem = withContext(Dispatchers.Default) {
        if (item.isMetadataLoaded) return@withContext item
        
        val group = item.group
        val metadataMap = group.files.associateWith { metadataExtractor.extractMetadata(it) }
        
        val highestBitrate = metadataMap.values.maxOfOrNull { it.bitrate } ?: 0
        val newestDate = group.files.maxOfOrNull { it.dateModifiedMs } ?: 0

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
            if (file.relativePath.contains("Music", ignoreCase = true)) {
                score += 15
                reasons.add("Located in Music folder")
            }
            if (file.dateModifiedMs == newestDate && file.dateModifiedMs > 0) {
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
        
        item.copy(
            fileMetadata = metadataMap,
            recommendedToKeep = bestFile,
            recommendationReasons = reasonsMap,
            isMetadataLoaded = true,
            isMetadataLoading = false
        )
    }
}
