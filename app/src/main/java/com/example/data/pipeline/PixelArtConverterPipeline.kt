package com.example.data.pipeline

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import com.example.domain.usecase.PixelArtConverterUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

class PixelArtConverterPipeline : PixelArtConverterUseCase {

    private val bayerMatrix4x4 = arrayOf(
        intArrayOf(0, 8, 2, 10),
        intArrayOf(12, 4, 14, 6),
        intArrayOf(3, 11, 1, 9),
        intArrayOf(15, 7, 13, 5)
    )

    override suspend fun convert(
        source: Bitmap,
        targetWidth: Int,
        targetHeight: Int,
        chromaKeyEnabled: Boolean,
        chromaColorInt: Int,
        chromaTolerance: Float,
        contrast: Float,
        brightness: Float,
        edgeEnhance: Float,
        paletteColorsInt: List<Int>,
        ditherModeName: String,
        chromaModeDistance: Boolean,
        chromaHueTolerance: Float,
        chromaSatMin: Float,
        chromaValMin: Float,
        chromaFeather: Float,
        chromaDespill: Boolean,
        extraChromaColorsInt: IntArray
    ): Bitmap = withContext(Dispatchers.Default) {
        val scaled = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false)
        val w = scaled.width
        val h = scaled.height
        val pixels = IntArray(w * h)
        scaled.getPixels(pixels, 0, w, 0, 0, w, h)

        val contrastFactor = if (contrast != 0f) {
            (259f * (contrast + 255f)) / (255f * (259f - contrast))
        } else 1.0f

        val rBuf = FloatArray(w * h)
        val gBuf = FloatArray(w * h)
        val bBuf = FloatArray(w * h)
        val aBuf = IntArray(w * h)

        for (i in 0 until (w * h)) {
            val color = pixels[i]
            val a = AndroidColor.alpha(color)
            var r = AndroidColor.red(color).toFloat()
            var g = AndroidColor.green(color).toFloat()
            var b = AndroidColor.blue(color).toFloat()

            if (chromaKeyEnabled && a > 30) {
                val keyedA = ChromaKey.keyedAlpha(
                    argb = color,
                    keyArgb = chromaColorInt,
                    extraKeyArgb = extraChromaColorsInt,
                    modeDistance = chromaModeDistance,
                    distanceTolerance = chromaTolerance,
                    hueTolerance = chromaHueTolerance,
                    satMin = chromaSatMin,
                    valMin = chromaValMin,
                    feather = chromaFeather
                )
                if (keyedA == 0) {
                    aBuf[i] = 0
                    rBuf[i] = 0f
                    gBuf[i] = 0f
                    bBuf[i] = 0f
                    continue
                }
                aBuf[i] = if (keyedA < 255) min(a, keyedA) else a
            } else {
                aBuf[i] = a
            }

            if (chromaDespill && aBuf[i] > 0) {
                val ds = ChromaKey.despill(r, g, b)
                r = ds[0]; g = ds[1]; b = ds[2]
            }

            if (brightness != 0f) {
                r += brightness
                g += brightness
                b += brightness
            }

            if (contrast != 0f) {
                r = contrastFactor * (r - 128f) + 128f
                g = contrastFactor * (g - 128f) + 128f
                b = contrastFactor * (b - 128f) + 128f
            }

            rBuf[i] = min(255f, max(0f, r))
            gBuf[i] = min(255f, max(0f, g))
            bBuf[i] = min(255f, max(0f, b))
        }

        if (edgeEnhance > 0f) {
            val factor = (edgeEnhance / 100f) * 0.4f
            for (y in 1 until h - 1) {
                for (x in 1 until w - 1) {
                    val idx = y * w + x
                    if (aBuf[idx] == 0) continue
                    val up = (y - 1) * w + x
                    val down = (y + 1) * w + x
                    val left = y * w + (x - 1)
                    val right = y * w + (x + 1)

                    val edgeR = (4 * rBuf[idx] - rBuf[up] - rBuf[down] - rBuf[left] - rBuf[right]) * factor
                    val edgeG = (4 * gBuf[idx] - gBuf[up] - gBuf[down] - gBuf[left] - gBuf[right]) * factor
                    val edgeB = (4 * bBuf[idx] - bBuf[up] - bBuf[down] - bBuf[left] - bBuf[right]) * factor

                    rBuf[idx] = min(255f, max(0f, rBuf[idx] + edgeR))
                    gBuf[idx] = min(255f, max(0f, gBuf[idx] + edgeG))
                    bBuf[idx] = min(255f, max(0f, bBuf[idx] + edgeB))
                }
            }
        }

        val resultPixels = IntArray(w * h)
        val palette = if (paletteColorsInt.isNotEmpty()) paletteColorsInt else listOf(
            AndroidColor.BLACK, AndroidColor.WHITE, AndroidColor.GRAY, AndroidColor.RED,
            AndroidColor.GREEN, AndroidColor.BLUE, AndroidColor.YELLOW, AndroidColor.CYAN
        )

        when (ditherModeName) {
            "ORDERED" -> {
                for (y in 0 until h) {
                    for (x in 0 until w) {
                        val i = y * w + x
                        if (aBuf[i] == 0) {
                            resultPixels[i] = AndroidColor.TRANSPARENT
                            continue
                        }
                        val threshold = (bayerMatrix4x4[y % 4][x % 4] - 8) * 4f
                        val dr = min(255f, max(0f, rBuf[i] + threshold))
                        val dg = min(255f, max(0f, gBuf[i] + threshold))
                        val db = min(255f, max(0f, bBuf[i] + threshold))
                        resultPixels[i] = findNearestColor(dr, dg, db, palette)
                    }
                }
            }
            "FLOYD_STEINBERG" -> {
                for (y in 0 until h) {
                    for (x in 0 until w) {
                        val i = y * w + x
                        if (aBuf[i] == 0) {
                            resultPixels[i] = AndroidColor.TRANSPARENT
                            continue
                        }
                        val oldR = rBuf[i]
                        val oldG = gBuf[i]
                        val oldB = bBuf[i]
                        val newColor = findNearestColor(oldR, oldG, oldB, palette)
                        resultPixels[i] = newColor

                        val errR = oldR - AndroidColor.red(newColor)
                        val errG = oldG - AndroidColor.green(newColor)
                        val errB = oldB - AndroidColor.blue(newColor)

                        if (x + 1 < w) {
                            val rightIdx = y * w + (x + 1)
                            rBuf[rightIdx] = min(255f, max(0f, rBuf[rightIdx] + errR * 7f / 16f))
                            gBuf[rightIdx] = min(255f, max(0f, gBuf[rightIdx] + errG * 7f / 16f))
                            bBuf[rightIdx] = min(255f, max(0f, bBuf[rightIdx] + errB * 7f / 16f))
                        }
                        if (y + 1 < h) {
                            if (x - 1 >= 0) {
                                val dlIdx = (y + 1) * w + (x - 1)
                                rBuf[dlIdx] = min(255f, max(0f, rBuf[dlIdx] + errR * 3f / 16f))
                                gBuf[dlIdx] = min(255f, max(0f, gBuf[dlIdx] + errG * 3f / 16f))
                                bBuf[dlIdx] = min(255f, max(0f, bBuf[dlIdx] + errB * 3f / 16f))
                            }
                            val downIdx = (y + 1) * w + x
                            rBuf[downIdx] = min(255f, max(0f, rBuf[downIdx] + errR * 5f / 16f))
                            gBuf[downIdx] = min(255f, max(0f, gBuf[downIdx] + errG * 5f / 16f))
                            bBuf[downIdx] = min(255f, max(0f, bBuf[downIdx] + errB * 5f / 16f))

                            if (x + 1 < w) {
                                val drIdx = (y + 1) * w + (x + 1)
                                rBuf[drIdx] = min(255f, max(0f, rBuf[drIdx] + errR * 1f / 16f))
                                gBuf[drIdx] = min(255f, max(0f, gBuf[drIdx] + errG * 1f / 16f))
                                bBuf[drIdx] = min(255f, max(0f, bBuf[drIdx] + errB * 1f / 16f))
                            }
                        }
                    }
                }
            }
            else -> {
                for (i in 0 until (w * h)) {
                    if (aBuf[i] == 0) {
                        resultPixels[i] = AndroidColor.TRANSPARENT
                    } else {
                        resultPixels[i] = findNearestColor(rBuf[i], gBuf[i], bBuf[i], palette)
                    }
                }
            }
        }

        val outBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        outBitmap.setPixels(resultPixels, 0, w, 0, 0, w, h)
        outBitmap
    }

    private fun findNearestColor(r: Float, g: Float, b: Float, palette: List<Int>): Int {
        var closest = palette[0]
        var minDist = Float.MAX_VALUE
        for (color in palette) {
            val pr = AndroidColor.red(color).toFloat()
            val pg = AndroidColor.green(color).toFloat()
            val pb = AndroidColor.blue(color).toFloat()
            val dist = (r - pr) * (r - pr) + (g - pg) * (g - pg) + (b - pb) * (b - pb)
            if (dist < minDist) {
                minDist = dist
                closest = color
            }
        }
        return closest
    }
}
