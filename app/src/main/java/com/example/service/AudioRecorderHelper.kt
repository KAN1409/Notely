package com.example.service

import android.content.Context
import android.media.MediaRecorder
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

class AudioRecorderHelper(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var amplitudeJob: Job? = null

    private val _amplitudes = MutableStateFlow<List<Float>>(emptyList())
    val amplitudes: StateFlow<List<Float>> = _amplitudes.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    fun startRecording(): File? {
        try {
            stopRecording() // ensure any previous is stopped

            val outputDir = File(context.filesDir, "recordings").apply { mkdirs() }
            val outputFile = File(outputDir, "rec_${System.currentTimeMillis()}.m4a")
            currentOutputFile = outputFile

            val newRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            newRecorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(96000)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            recorder = newRecorder
            _isRecording.value = true
            _elapsedSeconds.value = 0
            _amplitudes.value = emptyList()

            // Poll amplitudes and timer
            amplitudeJob = CoroutineScope(Dispatchers.Default).launch {
                var secondsCounter = 0
                var subSecondTicks = 0
                val ampList = mutableListOf<Float>()

                while (isActive && _isRecording.value) {
                    val maxAmp = try {
                        recorder?.maxAmplitude ?: 0
                    } catch (_: Exception) {
                        0
                    }
                    val normalized = (maxAmp / 32767f).coerceIn(0.05f, 1f)
                    ampList.add(normalized)
                    if (ampList.size > 36) {
                        ampList.removeAt(0)
                    }
                    _amplitudes.value = ampList.toList()

                    subSecondTicks++
                    if (subSecondTicks >= 10) {
                        secondsCounter++
                        subSecondTicks = 0
                        _elapsedSeconds.value = secondsCounter
                    }
                    delay(100)
                }
            }

            return outputFile
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Failed to start recording", e)
            _isRecording.value = false
            return null
        }
    }

    fun stopRecording(): File? {
        amplitudeJob?.cancel()
        amplitudeJob = null
        _isRecording.value = false

        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Error stopping recorder", e)
        } finally {
            recorder = null
        }

        return currentOutputFile
    }
}
