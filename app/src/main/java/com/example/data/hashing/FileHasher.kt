package com.example.data.hashing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

class FileHasher {
    suspend fun hashFile(path: String): String? = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (!file.exists() || !file.canRead()) return@withContext null
            
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().buffered().use { input ->
                val buffer = ByteArray(8192) // 8KB buffer
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            // Convert to Hex string
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            null
        }
    }
}
