package com.example.service

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class AudioPlayerHelper(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var updateJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _speed = MutableStateFlow(1.0f)
    val speed: StateFlow<Float> = _speed.asStateFlow()

    fun loadAndPlay(filePathOrUri: String?, simulatedDurationMs: Long = 134000L) {
        if (filePathOrUri != null && File(filePathOrUri).exists()) {
            playRealFile(File(filePathOrUri))
        } else {
            playSimulated(simulatedDurationMs)
        }
    }

    private fun playRealFile(file: File) {
        stop()
        try {
            val player = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                start()
            }
            mediaPlayer = player
            _durationMs.value = player.duration.toLong()
            _isPlaying.value = true

            updateJob = CoroutineScope(Dispatchers.Main).launch {
                while (isActive && _isPlaying.value) {
                    val pos = try {
                        mediaPlayer?.currentPosition?.toLong() ?: 0L
                    } catch (_: Exception) {
                        0L
                    }
                    _currentPositionMs.value = pos
                    if (pos >= _durationMs.value) {
                        _isPlaying.value = false
                        break
                    }
                    delay(200)
                }
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerHelper", "Failed to play audio file", e)
            playSimulated(134000L)
        }
    }

    private fun playSimulated(totalDuration: Long) {
        stop()
        _durationMs.value = totalDuration
        _isPlaying.value = true

        updateJob = CoroutineScope(Dispatchers.Main).launch {
            var current = _currentPositionMs.value
            while (isActive && _isPlaying.value) {
                current += (200 * _speed.value).toLong()
                if (current >= totalDuration) {
                    current = 0
                    _isPlaying.value = false
                    _currentPositionMs.value = 0
                    break
                }
                _currentPositionMs.value = current
                delay(200)
            }
        }
    }

    fun pause() {
        _isPlaying.value = false
        try {
            mediaPlayer?.pause()
        } catch (_: Exception) {}
        updateJob?.cancel()
    }

    fun resume() {
        if (_currentPositionMs.value >= _durationMs.value) {
            _currentPositionMs.value = 0
        }
        if (mediaPlayer != null) {
            mediaPlayer?.start()
            _isPlaying.value = true
        } else {
            playSimulated(_durationMs.value)
        }
    }

    fun seekTo(positionMs: Long) {
        _currentPositionMs.value = positionMs
        try {
            mediaPlayer?.seekTo(positionMs.toInt())
        } catch (_: Exception) {}
    }

    fun toggleSpeed() {
        val next = when (_speed.value) {
            1.0f -> 1.5f
            1.5f -> 2.0f
            else -> 1.0f
        }
        _speed.value = next
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mediaPlayer?.let {
                    it.playbackParams = it.playbackParams.setSpeed(next)
                }
            } catch (_: Exception) {}
        }
    }

    fun stop() {
        _isPlaying.value = false
        updateJob?.cancel()
        updateJob = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        _currentPositionMs.value = 0L
    }
}
