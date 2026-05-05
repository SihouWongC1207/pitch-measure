package com.pitchmeasure.app.model

import kotlin.math.log2
import kotlin.math.roundToInt

data class PitchInfo(
    val frequency: Float,
    val noteName: String,
    val octave: Int,
    val centsDeviation: Float
) {
    val displayNote: String get() = "$noteName$octave"
    val displayFrequency: String get() = String.format("%.1f Hz", frequency)
    val displayCents: String get() = String.format("%+.0f 音分", centsDeviation)
}

object PitchUtils {

    private val NOTE_NAMES = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    fun frequencyToMidi(freq: Float): Double {
        return 69.0 + 12.0 * log2(freq / 440.0)
    }

    fun frequencyToPitchInfo(freq: Float): PitchInfo {
        val midi = frequencyToMidi(freq)
        val roundedMidi = midi.roundToInt()
        val cents = ((midi - roundedMidi) * 100).toFloat()
        val noteIndex = ((roundedMidi % 12) + 12) % 12
        val noteName = NOTE_NAMES[noteIndex]
        val octave = (roundedMidi / 12) - 1
        return PitchInfo(freq, noteName, octave, cents)
    }
}
