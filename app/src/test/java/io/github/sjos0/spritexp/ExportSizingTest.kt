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
}
