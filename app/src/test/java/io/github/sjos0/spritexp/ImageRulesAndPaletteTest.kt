package io.github.sjos0.spritexp

import io.github.sjos0.spritexp.data.pipeline.PaletteExtractor
import io.github.sjos0.spritexp.domain.usecase.ImageRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageRulesAndPaletteTest {

    private fun argb(r: Int, g: Int, b: Int, a: Int = 255): Int =
        (a shl 24) or (r shl 16) or (g shl 8) or b

    @Test
    fun squareImagesAreAccepted() {
        assertTrue(ImageRules.isSquare(64, 64))
        assertTrue(ImageRules.isSquare(1, 1))
    }

    @Test
    fun nonSquareOrEmptyImagesAreRejected() {
        assertFalse(ImageRules.isSquare(64, 32))
        assertFalse(ImageRules.isSquare(0, 0))
    }

    @Test
    fun extractionRespectsMaxColors() {
        val pixels = IntArray(256) { argb(it, 255 - it, (it * 3) % 256) }
        val palette = PaletteExtractor.extract(pixels, maxColors = 8)
        assertTrue(palette.size in 1..8)
    }

    @Test
    fun transparentPixelsAreIgnored() {
        val pixels = intArrayOf(argb(255, 0, 0, a = 0), argb(0, 0, 255))
        assertEquals(listOf(argb(0, 0, 255)), PaletteExtractor.extract(pixels, 4))
    }

    @Test
    fun chromaKeyPixelsAreIgnored() {
        val key = argb(0, 255, 0)
        val pixels = intArrayOf(argb(0, 255, 0), argb(0, 255, 0), argb(200, 30, 30))
        assertEquals(listOf(argb(200, 30, 30)), PaletteExtractor.extract(pixels, 4, key, 35f))
    }

    @Test
    fun allTransparentReturnsEmpty() {
        assertTrue(PaletteExtractor.extract(intArrayOf(0, 0), 8).isEmpty())
    }

    @Test
    fun twoDistinctColorsAreBothReturned() {
        val pixels = intArrayOf(argb(0, 0, 0), argb(255, 255, 255))
        assertEquals(2, PaletteExtractor.extract(pixels, 2).size)
    }
}
