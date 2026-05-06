package com.pitchmeasure.app.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import be.tarsos.dsp.pitch.Yin
import com.pitchmeasure.app.model.PitchInfo
import com.pitchmeasure.app.model.PitchUtils

class PitchDetector(
    private val onPitchDetected: (PitchInfo?) -> Unit
) {
    private val sampleRate = 44100
    private val bufferSize = 2048
    private val yin = Yin(sampleRate.toFloat(), bufferSize)

    private var audioRecord: AudioRecord? = null
    private var thread: Thread? = null
    @Volatile
    private var isRunning = false

    fun start() {
        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            maxOf(minBufferSize, bufferSize * 2)
        )

        audioRecord?.startRecording()
        isRunning = true

        thread = Thread {
            val shortBuffer = ShortArray(bufferSize)
            val floatBuffer = FloatArray(bufferSize)

            while (isRunning) {
                val read = audioRecord?.read(shortBuffer, 0, bufferSize) ?: break
                if (read <= 0) continue

                for (i in 0 until read) {
                    floatBuffer[i] = shortBuffer[i] / 32768f
                }

                val result = yin.getPitch(floatBuffer)
                if (result.isPitched && result.probability > 0.8f && result.pitch > 0) {
                    val pitchInfo = PitchUtils.frequencyToPitchInfo(result.pitch)
                    onPitchDetected(pitchInfo)
                } else {
                    onPitchDetected(null)
                }
            }
        }.also { it.start() }
    }

    fun stop() {
        isRunning = false
        try {
            thread?.join(1000)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        thread = null
        try {
            audioRecord?.stop()
        } catch (_: IllegalStateException) {
            // already stopped or not initialized
        }
        audioRecord?.release()
        audioRecord = null
    }
}
