package com.example

import com.example.data.pipeline.ChromaKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChromaKeyTest {

    @Test
    fun hueDistance_wrapsAround() {
        assertEquals(20f, ChromaKey.hueDistance(350f, 10f), 0.01f)
        assertEquals(20f, ChromaKey.hueDistance(10f, 350f), 0.01f)
    }

    @Test
    fun greenTone_matchesHueKey() {
        // RGB(10,161,46) ≈ matiz ~134°, sat alta, val moderado
        val argb = (0xFF shl 24) or (10 shl 16) or (161 shl 8) or 46
        val key = (0xFF shl 24) or (0 shl 16) or (200 shl 8) or 0 // verde saturado genérico; usamos HUE do próprio pixel
        // chave com matiz ~134: converter 10,161,46 como chave
        val keySelf = argb
        assertTrue(
            ChromaKey.isKeyed(
                argb = argb,
                keyArgb = keySelf,
                modeDistance = false,
                hueTolerance = 25f,
                satMin = 0.35f,
                valMin = 0.12f
            )
        )
        // chave verde clássico (0,255,0) hue 120 — distância 14 < 25
        val pureGreen = (0xFF shl 24) or (0 shl 16) or (255 shl 8) or 0
        assertTrue(
            ChromaKey.isKeyed(
                argb = argb,
                keyArgb = pureGreen,
                modeDistance = false,
                hueTolerance = 25f,
                satMin = 0.35f,
                valMin = 0.12f
            )
        )
    }

    @Test
    fun darkOutline_doesNotMatch() {
        val dark = (0xFF shl 24) or (20 shl 16) or (20 shl 8) or 35
        val pureGreen = (0xFF shl 24) or (0 shl 16) or (255 shl 8) or 0
        assertFalse(
            ChromaKey.isKeyed(
                argb = dark,
                keyArgb = pureGreen,
                modeDistance = false,
                hueTolerance = 25f,
                satMin = 0.35f,
                valMin = 0.12f
            )
        )
    }

    @Test
    fun red_doesNotMatchGreenHue() {
        val red = (0xFF shl 24) or (200 shl 16) or (30 shl 8) or 30
        val pureGreen = (0xFF shl 24) or (0 shl 16) or (255 shl 8) or 0
        assertFalse(
            ChromaKey.isKeyed(
                argb = red,
                keyArgb = pureGreen,
                modeDistance = false,
                hueTolerance = 25f,
                satMin = 0.35f,
                valMin = 0.12f
            )
        )
    }
}
