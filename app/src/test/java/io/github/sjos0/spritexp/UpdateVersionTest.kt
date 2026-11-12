package io.github.sjos0.spritexp

import io.github.sjos0.spritexp.data.update.UpdateVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UpdateVersionTest {

    @Test
    fun parsesBuildNumberFromTag() {
        assertEquals(12, UpdateVersion.buildNumberFromTag("v1.0.12"))
        assertEquals(3, UpdateVersion.buildNumberFromTag(" v1.0.3 "))
    }

    @Test
    fun rejectsTagsOutsideThePattern() {
        assertNull(UpdateVersion.buildNumberFromTag("beta"))
        assertNull(UpdateVersion.buildNumberFromTag("v1.0"))
    }

    @Test
    fun parsesZeroBuildNumber() {
        assertEquals(0, UpdateVersion.buildNumberFromTag("v1.0.0"))
    }

    @Test
    fun parsesTwoDigitBuildNumber() {
        assertEquals(10, UpdateVersion.buildNumberFromTag("v2.1.10"))
    }

    @Test
    fun parsesLeadingZerosAsInteger() {
        assertEquals(7, UpdateVersion.buildNumberFromTag("v1.0.007"))
    }

    @Test
    fun rejectsInvalidTags() {
        assertNull(UpdateVersion.buildNumberFromTag("v1.0.3-beta"))
        assertNull(UpdateVersion.buildNumberFromTag(""))
        assertNull(UpdateVersion.buildNumberFromTag("1.0.3"))
        assertNull(UpdateVersion.buildNumberFromTag("v1.0.3.1"))
    }

    @Test
    fun acceptsSurroundingWhitespace() {
        assertEquals(12, UpdateVersion.buildNumberFromTag("  v1.0.12  "))
        assertEquals(0, UpdateVersion.buildNumberFromTag("\tv1.0.0\n"))
    }
}
