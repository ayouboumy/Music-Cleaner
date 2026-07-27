package com.example.common.utils

import android.content.Context
import com.example.data.mediascanner.MediaScanner
import com.example.data.mediascanner.MediaStoreScanner
import com.example.data.detector.CandidateDetector
import com.example.domain.usecases.FindCandidateGroupsUseCase

import com.example.data.hashing.FileHasher
import com.example.data.detector.ExactDuplicateDetector
import com.example.domain.usecases.FindExactDuplicatesUseCase

/**
 * Manual Dependency Injection container.
 * Repositories and UseCases will be lazily instantiated here.
 */
object DependencyProvider {
    private var _appContext: Context? = null
    val appContext: Context
        get() = _appContext ?: throw IllegalStateException("DependencyProvider not initialized")

    fun initialize(context: Context) {
        _appContext = context.applicationContext
    }

    val mediaScanner: MediaScanner by lazy {
        MediaStoreScanner(appContext)
    }
    
    val candidateDetector: CandidateDetector by lazy {
        CandidateDetector()
    }
    
    val findCandidateGroupsUseCase: FindCandidateGroupsUseCase by lazy {
        FindCandidateGroupsUseCase(candidateDetector)
    }

    val fileHasher: FileHasher by lazy {
        FileHasher(appContext)
    }

    val exactDuplicateDetector: ExactDuplicateDetector by lazy {
        ExactDuplicateDetector(fileHasher)
    }

    val findExactDuplicatesUseCase: FindExactDuplicatesUseCase by lazy {
        FindExactDuplicatesUseCase(exactDuplicateDetector)
    }

    val metadataExtractor: com.example.data.metadata.MetadataExtractor by lazy {
        com.example.data.metadata.MetadataExtractor(appContext)
    }

    val recommendationEngine: com.example.domain.usecases.review.RecommendationEngine by lazy {
        com.example.domain.usecases.review.RecommendationEngine(metadataExtractor)
    }
}
