package com.example.emergency

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

private const val TAG = "EmergencySirenService"

class EmergencySirenService(private val scope: CoroutineScope) {

    private var sirenJob: Job? = null
    private var isPlaying = false

    fun startSiren() {
        if (isPlaying) return
        isPlaying = true

        sirenJob = scope.launch(Dispatchers.Default) {
            val sampleRate = 22050
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4096)

            var audioTrack: AudioTrack? = null
            try {
                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack.play()

                val buffer = ShortArray(1024)
                var phase = 0.0
                var currentFreq = 700.0
                var goingUp = true

                while (isActive && isPlaying) {
                    for (i in buffer.indices) {
                        // Modulate siren frequency between 700Hz and 1300Hz
                        if (goingUp) {
                            currentFreq += 1.2
                            if (currentFreq >= 1300.0) goingUp = false
                        } else {
                            currentFreq -= 1.2
                            if (currentFreq <= 700.0) goingUp = true
                        }

                        val sample = (sin(phase) * 26000).toInt().toShort()
                        buffer[i] = sample
                        phase += 2.0 * Math.PI * currentFreq / sampleRate
                        if (phase > 2.0 * Math.PI) {
                            phase -= 2.0 * Math.PI
                        }
                    }
                    audioTrack.write(buffer, 0, buffer.size)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing siren tone", e)
            } finally {
                try {
                    audioTrack?.stop()
                    audioTrack?.release()
                } catch (e: Exception) {
                    Log.e(TAG, "Error releasing audio track", e)
                }
            }
        }
    }

    fun stopSiren() {
        isPlaying = false
        sirenJob?.cancel()
        sirenJob = null
    }
}
