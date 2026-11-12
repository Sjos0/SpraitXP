package com.example

import com.example.data.update.UpdateVersion
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
}
