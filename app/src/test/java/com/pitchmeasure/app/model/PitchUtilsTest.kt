package com.pitchmeasure.app.model

import org.junit.Assert.*
import org.junit.Test

class PitchUtilsTest {

    @Test
    fun `A4 440Hz maps to note A4 with zero cents`() {
        val info = PitchUtils.frequencyToPitchInfo(440f)
        assertEquals("A", info.noteName)
        assertEquals(4, info.octave)
        assertEquals(0f, info.centsDeviation, 0.5f)
        assertEquals(440f, info.frequency, 0.1f)
    }

    @Test
    fun `middle C 261_63Hz maps to C4`() {
        val info = PitchUtils.frequencyToPitchInfo(261.63f)
        assertEquals("C", info.noteName)
        assertEquals(4, info.octave)
        assertTrue(Math.abs(info.centsDeviation) < 1f)
    }

    @Test
    fun `466_16Hz is A#4 with zero cents`() {
        val info = PitchUtils.frequencyToPitchInfo(466.16f)
        assertEquals("A#", info.noteName)
        assertEquals(4, info.octave)
        assertTrue(Math.abs(info.centsDeviation) < 2f)
    }

    @Test
    fun `442Hz is A4 with slight positive cents`() {
        val info = PitchUtils.frequencyToPitchInfo(442f)
        assertEquals("A", info.noteName)
        assertEquals(4, info.octave)
        assertTrue(info.centsDeviation > 0f)
        assertTrue(info.centsDeviation < 50f)
    }

    @Test
    fun `displayNote returns note name with octave`() {
        val info = PitchInfo(440f, "A", 4, 0f)
        assertEquals("A4", info.displayNote)
    }

    @Test
    fun `displayFrequency formats with one decimal`() {
        val info = PitchInfo(440f, "A", 4, 0f)
        assertTrue(info.displayFrequency.contains("440.0"))
    }

    @Test
    fun `frequencyToMidi returns 69 for 440Hz`() {
        assertEquals(69.0, PitchUtils.frequencyToMidi(440f), 0.01)
    }

    @Test
    fun `frequencyToMidi returns 60 for 261_63Hz`() {
        assertEquals(60.0, PitchUtils.frequencyToMidi(261.63f), 0.01)
    }
}
