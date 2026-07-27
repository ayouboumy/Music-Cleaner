package com.example.data.metadata

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.example.domain.models.AudioFile
import com.example.domain.models.FileMetadata

class MetadataExtractor(private val context: Context) {
    fun extractMetadata(audioFile: AudioFile): FileMetadata {
        var album = "Unknown Album"
        var bitrate = 0
        var hasAlbumArt = false
        var mimeType = "audio/*"
        
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, Uri.parse(audioFile.uri))
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
        
        return FileMetadata(album, bitrate, hasAlbumArt, audioFile.dateModifiedMs, mimeType, audioFile.relativePath)
    }
}
