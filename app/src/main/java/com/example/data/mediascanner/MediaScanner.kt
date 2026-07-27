package com.example.data.mediascanner

import com.example.domain.models.AudioFile

interface MediaScanner {
    suspend fun scanAudioFiles(onProgress: (Int) -> Unit): List<AudioFile>
}
