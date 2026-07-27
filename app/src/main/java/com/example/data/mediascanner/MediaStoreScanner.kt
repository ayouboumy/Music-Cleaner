package com.example.data.mediascanner

import android.content.Context
import android.provider.MediaStore
import com.example.domain.models.AudioFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MediaStoreScanner(private val context: Context) : MediaScanner {
    override suspend fun scanAudioFiles(onProgress: (Int) -> Unit): List<AudioFile> = withContext(Dispatchers.IO) {
        val audioFiles = mutableListOf<AudioFile>()
        
        // Use external content URI
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATA
        )

        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            val totalCount = cursor.count
            var currentIndex = 0

            while (cursor.moveToNext()) {
                try {
                    val path = cursor.getString(dataColumn)
                    
                    if (path != null && File(path).exists()) {
                        val id = cursor.getLong(idColumn)
                        val title = cursor.getString(titleColumn) ?: "Unknown Title"
                        val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                        val duration = cursor.getLong(durationColumn)
                        val size = cursor.getLong(sizeColumn)

                        audioFiles.add(
                            AudioFile(
                                id = id,
                                title = title,
                                artist = artist,
                                durationMs = duration,
                                sizeBytes = size,
                                path = path
                            )
                        )
                    }
                } catch (e: Exception) {
                    // Ignore unreadable files without crashing
                }
                
                currentIndex++
                if (currentIndex % 10 == 0 || currentIndex == totalCount) {
                    val progress = if (totalCount > 0) (currentIndex * 100) / totalCount else 100
                    onProgress(progress)
                }
            }
        }
        return@withContext audioFiles
    }
}
