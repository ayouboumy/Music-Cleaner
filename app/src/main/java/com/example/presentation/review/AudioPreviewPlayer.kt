package com.example.presentation.review

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioPreviewPlayer(private val context: Context) : DefaultLifecycleObserver {
    private var mediaPlayer: MediaPlayer? = null
    
    private val _currentlyPlaying = MutableStateFlow<String?>(null)
    val currentlyPlaying: StateFlow<String?> = _currentlyPlaying.asStateFlow()

    fun play(uriString: String) {
        if (_currentlyPlaying.value == uriString) {
            pause()
            return
        }
        stop()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.parse(uriString))
                setOnPreparedListener {
                    it.start()
                }
                setOnCompletionListener {
                    _currentlyPlaying.value = null
                }
                prepareAsync()
            }
            _currentlyPlaying.value = uriString
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

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        pause()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        stop()
    }
}
