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
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.RELATIVE_PATH
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
            val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
            val relativePathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH)

            val totalCount = cursor.count
            var currentIndex = 0

            while (cursor.moveToNext()) {
                try {
                    val id = cursor.getLong(idColumn)
                    val uri = android.content.ContentUris.withAppendedId(collection, id).toString()
                    val displayName = cursor.getString(displayNameColumn) ?: "Unknown File"
                    val dateModifiedMs = cursor.getLong(dateModifiedColumn) * 1000 // DATE_MODIFIED is in seconds
                    val relativePath = cursor.getString(relativePathColumn)?.removeSuffix("/") ?: "Unknown Folder"
                    
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
                            uri = uri,
                            displayName = displayName,
                            dateModifiedMs = dateModifiedMs,
                            relativePath = relativePath
                        )
                    )
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
