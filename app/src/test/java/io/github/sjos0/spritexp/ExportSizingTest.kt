package io.github.sjos0.spritexp

import io.github.sjos0.spritexp.domain.usecase.ExportSizing
import org.junit.Assert.assertEquals
import org.junit.Test

class ExportSizingTest {
    @Test
    fun multipliesBaseSideByDisplayScale() {
        assertEquals(1024, ExportSizing.finalSide(baseSide = 64, displayScale = 16))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsUnsafeOutputSide() {
        ExportSizing.finalSide(baseSide = 1024, displayScale = 16)
    }

    @Test
    fun requireSafeScaleAcceptsSquareUnderLimit() {
        assertEquals(16, ExportSizing.requireSafeScale(width = 64, height = 64, displayScale = 16))
    }

    @Test(expected = IllegalArgumentException::class)
    fun requireSafeScaleRejectsWhenHeightExceedsLimit() {
        // 100 x 16 = 1600 OK na largura; 600 x 16 = 9600 > 8192 na altura
        ExportSizing.requireSafeScale(width = 100, height = 600, displayScale = 16)
    }
}
