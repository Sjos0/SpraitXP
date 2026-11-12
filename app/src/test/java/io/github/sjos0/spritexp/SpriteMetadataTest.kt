package io.github.sjos0.spritexp

import io.github.sjos0.spritexp.domain.usecase.SpriteMetadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpriteMetadataTest {

    private val json = SpriteMetadata.toJson(
        imageName = "spritesheet.png",
        frameWidth = 64,
        frameHeight = 64,
        columns = 10,
        rows = 9,
        frameCount = 90,
        fps = 30
    )

    @Test
    fun listsEveryFrameOnce() {
        assertEquals(90, Regex("\"filename\"").findAll(json).count())
    }

    @Test
    fun declaresSheetSizeAndGrid() {
        assertTrue(json.contains("\"size\": {\"w\": 640, \"h\": 576}"))
        assertTrue(json.contains("\"columns\": 10"))
        assertTrue(json.contains("\"frameCount\": 90"))
        assertTrue(json.contains("\"fps\": 30"))
    }

    @Test
    fun secondFrameSitsAtSecondColumn() {
        assertTrue(json.contains("\"frame\": {\"x\": 64, \"y\": 0, \"w\": 64, \"h\": 64}"))
    }

    @Test
    fun durationComesFromFps() {
        assertTrue(json.contains("\"duration\": 33"))
    }
}
