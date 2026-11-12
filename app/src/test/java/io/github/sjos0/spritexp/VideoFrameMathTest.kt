package io.github.sjos0.spritexp

import io.github.sjos0.spritexp.domain.usecase.VideoFrameMath
import io.github.sjos0.spritexp.domain.usecase.VideoTiming
import org.junit.Assert.assertEquals
import org.junit.Test

class VideoFrameMathTest {

    @Test
    fun threeSecondsAt30fpsKeepsEveryFrame() {
        assertEquals(90, VideoTiming(durationMs = 3000, fps = 30).frameCount)
    }

    @Test
    fun frameTimeUsesExactMicroseconds() {
        assertEquals(40_000L, VideoFrameMath.frameTimeUs(1, 25))
    }

    @Test
    fun unknownFpsFallsBackTo30() {
        assertEquals(30, VideoFrameMath.normalizeFps(null))
        assertEquals(30, VideoFrameMath.normalizeFps(0f))
    }

    @Test
    fun sheetGridFitsAllFrames() {
        assertEquals(10, VideoFrameMath.sheetColumns(90))
        assertEquals(9, VideoFrameMath.sheetRows(90))
    }

    @Test
    fun shortClipHasAtLeastOneFrame() {
        assertEquals(1, VideoTiming(durationMs = 0, fps = 30).frameCount)
    }
}
