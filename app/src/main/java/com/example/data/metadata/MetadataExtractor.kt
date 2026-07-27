package com.example.data.metadata

import android.media.MediaMetadataRetriever
import com.example.domain.models.AudioFile
import com.example.domain.models.FileMetadata
import java.io.File

class MetadataExtractor {
    fun extractMetadata(audioFile: AudioFile): FileMetadata {
        val file = File(audioFile.path)
        val dateModified = file.lastModified()
        val folder = file.parentFile?.name ?: ""
        
        var album = "Unknown Album"
        var bitrate = 0
        var hasAlbumArt = false
        var mimeType = "audio/*"
        
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(audioFile.path)
            album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "Unknown Album"
            bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull() ?: 0
            mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) ?: "audio/*"
            hasAlbumArt = retriever.embeddedPicture != null
        } catch (e: Exception) {
            // Ignore format errors
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {}
        }
        
        return FileMetadata(album, bitrate, hasAlbumArt, dateModified, mimeType, folder)
    }
}
