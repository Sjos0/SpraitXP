package com.example

import com.example.domain.model.DitherMode
import com.example.domain.model.PalettePreset
import com.example.domain.model.SpriteProject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SpriteXpUnitTest {

    @Test
    fun testPalettePresets() {
        val pico8 = PalettePreset.PICO_8
        assertEquals(16, pico8.colors.size)

        val gb = PalettePreset.GAME_BOY
        assertEquals(4, gb.colors.size)

        val sweetie = PalettePreset.SWEETIE_16
        assertEquals(16, sweetie.colors.size)

        val endesga = PalettePreset.ENDESGA_32
        assertEquals(32, endesga.colors.size)
    }

    @Test
    fun testDitherModes() {
        val none = DitherMode.fromName("NONE")
        assertEquals(DitherMode.NONE, none)

        val ordered = DitherMode.fromName("ORDERED")
        assertEquals(DitherMode.ORDERED, ordered)

        val floyd = DitherMode.fromName("FLOYD_STEINBERG")
        assertEquals(DitherMode.FLOYD_STEINBERG, floyd)
    }

    @Test
    fun testSpriteProjectCreation() {
        val project = SpriteProject(
            id = "proj-1",
            title = "Mago de Fogo",
            dateString = "14/11/2026",
            frameCount = 36,
            resolution = 64,
            paletteName = "PICO-8"
        )
        assertEquals("Mago de Fogo", project.title)
        assertEquals(36, project.frameCount)
        assertEquals(64, project.resolution)
    }
}
