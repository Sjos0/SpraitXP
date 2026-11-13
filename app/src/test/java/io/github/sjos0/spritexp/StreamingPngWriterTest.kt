package io.github.sjos0.spritexp

import io.github.sjos0.spritexp.data.pipeline.StreamingPngWriter
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import javax.imageio.ImageIO

class StreamingPngWriterTest {

    private lateinit var dir: File

    @Before
    fun setUp() {
        dir = createTempDir(prefix = "png-writer")
    }

    @After
    fun tearDown() {
        dir.deleteRecursively()
    }

    private fun pixel(argb: Int): Triple<Int, Int, Int> =
        Triple((argb shr 16) and 0xFF, (argb shr 8) and 0xFF, argb and 0xFF)

    @Test
    fun smallImageRoundTripsThroughImageIO() {
        val file = File(dir, "small.png")
        StreamingPngWriter.write(file, width = 3, height = 2, fillRow = { y, dst ->
            for (x in 0 until 3) {
                val o = 1 + x * 4
                dst[o] = (x * 80).toByte()
                dst[o + 1] = (y * 100).toByte()
                dst[o + 2] = 7
                dst[o + 3] = 255.toByte()
            }
        })

        val img = ImageIO.read(file)
        assertEquals(3, img.width)
        assertEquals(2, img.height)
        val argb = img.getRGB(2, 1)
        assertEquals(255, (argb ushr 24) and 0xFF)
        assertEquals(Triple(160, 100, 7), pixel(argb))
    }

    @Test
    fun largeImageSplitsIntoSeveralIdatChunksAndStillDecodes() {
        val w = 300
        val h = 300
        val file = File(dir, "large.png")
        // Ruído determinístico: não comprime bem, então o fluxo passa de um chunk IDAT.
        fun v(x: Int, y: Int): Int = (x * 131 + y * 17 + (x * y) % 97) and 0xFF
        val rows = ArrayList<Int>()
        StreamingPngWriter.write(file, width = w, height = h, fillRow = { y, dst ->
            for (x in 0 until w) {
                val o = 1 + x * 4
                dst[o] = v(x, y).toByte()
                dst[o + 1] = v(y, x).toByte()
                dst[o + 2] = v(x + y, 3).toByte()
                dst[o + 3] = 255.toByte()
            }
        }, onRow = { rows.add(it) })

        assertEquals(h, rows.size)
        val img = ImageIO.read(file)
        assertEquals(w, img.width)
        assertEquals(h, img.height)
        for ((x, y) in listOf(0 to 0, 299 to 0, 0 to 299, 150 to 42, 299 to 299)) {
            val (r, g, b) = pixel(img.getRGB(x, y))
            assertEquals("R em ($x,$y)", v(x, y), r)
            assertEquals("G em ($x,$y)", v(y, x), g)
            assertEquals("B em ($x,$y)", v(x + y, 3), b)
        }
    }
}
