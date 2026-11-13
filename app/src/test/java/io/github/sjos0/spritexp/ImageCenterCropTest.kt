package io.github.sjos0.spritexp

import io.github.sjos0.spritexp.domain.usecase.ImageRules
import io.github.sjos0.spritexp.domain.usecase.SquareCrop
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImageCenterCropTest {

    @Test
    fun squareImageKeepsFullFrame() {
        assertEquals(SquareCrop(x = 0, y = 0, side = 512), ImageRules.centerSquareCrop(512, 512))
    }

    @Test
    fun landscapeImageIsCroppedFromTheCenterColumns() {
        // 1920x1080 (16:9): lado = 1080, sobra 840 px na largura, 420 para cada lado.
        assertEquals(SquareCrop(x = 420, y = 0, side = 1080), ImageRules.centerSquareCrop(1920, 1080))
    }

    @Test
    fun portraitImageIsCroppedFromTheCenterRows() {
        // 1080x1920 (9:16): lado = 1080, sobra 840 px na altura, 420 para cada lado.
        assertEquals(SquareCrop(x = 0, y = 420, side = 1080), ImageRules.centerSquareCrop(1080, 1920))
    }

    @Test
    fun oddDifferenceRoundsDownOnTheLeadingEdge() {
        // 101x100: sobra 1 px na largura, o quadrado começa em x = 0 e termina em x = 99.
        assertEquals(SquareCrop(x = 0, y = 0, side = 100), ImageRules.centerSquareCrop(101, 100))
    }

    @Test
    fun invalidDimensionsReturnNull() {
        assertNull(ImageRules.centerSquareCrop(0, 480))
        assertNull(ImageRules.centerSquareCrop(480, -1))
    }
}
