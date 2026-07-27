package com.example.presentation.review

import android.media.MediaPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioPreviewPlayer {
    private var mediaPlayer: MediaPlayer? = null
    
    private val _currentlyPlaying = MutableStateFlow<String?>(null)
    val currentlyPlaying: StateFlow<String?> = _currentlyPlaying.asStateFlow()

    fun play(path: String) {
        if (_currentlyPlaying.value == path) {
            pause()
            return
        }
        stop()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                start()
                setOnCompletionListener {
                    _currentlyPlaying.value = null
                }
            }
            _currentlyPlaying.value = path
        } catch (e: Exception) {
            _currentlyPlaying.value = null
        }
    }

    fun pause() {
        mediaPlayer?.pause()
        _currentlyPlaying.value = null
    }

    fun stop() {
        mediaPlayer?.release()
        mediaPlayer = null
        _currentlyPlaying.value = null
    }
}
