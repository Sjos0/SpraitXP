package io.github.sjos0.spritexp.data.pipeline

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Lógica pura de chroma key (testável em JVM sem Bitmap).
 * alphaKeyed: 0 = totalmente chave (remover), 255 = manter, intermediário = feather.
 */
object ChromaKey {

    /** Distância circular de matiz em graus [0, 180]. */
    fun hueDistance(a: Float, b: Float): Float {
        val d = abs(a - b) % 360f
        return min(d, 360f - d)
    }

    /** RGB 0..255 → HSV (h 0..360, s 0..1, v 0..1). */
    fun rgbToHsv(r: Int, g: Int, b: Int): FloatArray {
        val rf = r / 255f
        val gf = g / 255f
        val bf = b / 255f
        val maxc = max(rf, max(gf, bf))
        val minc = min(rf, min(gf, bf))
        val delta = maxc - minc
        val h = when {
            delta < 1e-6f -> 0f
            maxc == rf -> ((60f * ((gf - bf) / delta) + 360f) % 360f)
            maxc == gf -> (60f * ((bf - rf) / delta) + 120f)
            else -> (60f * ((rf - gf) / delta) + 240f)
        }
        val s = if (maxc < 1e-6f) 0f else delta / maxc
        return floatArrayOf(h, s, maxc)
    }

    /**
     * Retorna alpha resultante (0 = chave, 255 = manter).
     * modeDistance=true usa distância RGB; false usa HUE.
     */
    fun keyedAlpha(
        argb: Int,
        keyArgb: Int,
        extraKeyArgb: IntArray,
        modeDistance: Boolean,
        distanceTolerance: Float, // 0..100 → escala para ~441.67
        hueTolerance: Float,
        satMin: Float,
        valMin: Float,
        feather: Float
    ): Int {
        val a = (argb ushr 24) and 0xFF
        if (a <= 30) return 0
        val r = (argb ushr 16) and 0xFF
        val g = (argb ushr 8) and 0xFF
        val b = argb and 0xFF

        val keys = IntArray(1 + extraKeyArgb.size)
        keys[0] = keyArgb
        System.arraycopy(extraKeyArgb, 0, keys, 1, extraKeyArgb.size)

        if (modeDistance) {
            val maxDist = (distanceTolerance / 100f) * 441.67f
            var best = Float.MAX_VALUE
            for (k in keys) {
                val kr = (k ushr 16) and 0xFF
                val kg = (k ushr 8) and 0xFF
                val kb = k and 0xFF
                val d = sqrt(
                    ((r - kr) * (r - kr) + (g - kg) * (g - kg) + (b - kb) * (b - kb)).toDouble()
                ).toFloat()
                if (d < best) best = d
            }
            return when {
                best <= maxDist -> 0
                feather > 0f && best <= maxDist + feather -> {
                    val t = ((best - maxDist) / feather).coerceIn(0f, 1f)
                    (t * 255f).toInt()
                }
                else -> 255
            }
        } else {
            val hsv = rgbToHsv(r, g, b)
            var matched = false
            var bestHueDist = Float.MAX_VALUE
            for (k in keys) {
                val kr = (k ushr 16) and 0xFF
                val kg = (k ushr 8) and 0xFF
                val kb = k and 0xFF
                val khsv = rgbToHsv(kr, kg, kb)
                val hd = hueDistance(hsv[0], khsv[0])
                if (hd < bestHueDist) bestHueDist = hd
                if (hd <= hueTolerance && hsv[1] >= satMin && hsv[2] >= valMin) {
                    matched = true
                }
            }
            if (matched) return 0
            if (feather > 0f && bestHueDist <= hueTolerance + feather && hsv[1] >= satMin * 0.5f) {
                val t = ((bestHueDist - hueTolerance) / feather).coerceIn(0f, 1f)
                return (t * 255f).toInt()
            }
            return 255
        }
    }

    /** true se o pixel é considerado chave (corte seco, sem feather). */
    fun isKeyed(
        argb: Int,
        keyArgb: Int,
        extraKeyArgb: IntArray = intArrayOf(),
        modeDistance: Boolean = false,
        distanceTolerance: Float = 35f,
        hueTolerance: Float = 25f,
        satMin: Float = 0.35f,
        valMin: Float = 0.12f
    ): Boolean =
        keyedAlpha(
            argb, keyArgb, extraKeyArgb, modeDistance,
            distanceTolerance, hueTolerance, satMin, valMin, feather = 0f
        ) == 0

    /** Despill: limita G a max(R,B) quando G é o canal dominante. */
    fun despill(r: Float, g: Float, b: Float): FloatArray {
        val m = max(r, b)
        return if (g > m) floatArrayOf(r, m, b) else floatArrayOf(r, g, b)
    }
}
