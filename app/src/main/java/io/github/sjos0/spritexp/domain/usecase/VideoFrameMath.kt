package io.github.sjos0.spritexp.domain.usecase

import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** Cálculos puros de quadros e grade de sprite sheet. Sem Android, testáveis na JVM. */
object VideoFrameMath {
    const val DEFAULT_FPS = 30
    const val MAX_FPS = 240

    /** Taxa informada pelo aparelho; quando ausente ou inválida, usa 30 fps. */
    fun normalizeFps(raw: Float?): Int =
        raw?.roundToInt()?.takeIf { it in 1..MAX_FPS } ?: DEFAULT_FPS

    /** Quantidade de quadros da origem: todos eles, sem amostragem. */
    fun frameCount(durationMs: Long, fps: Int): Int =
        ((durationMs * fps) / 1000L).toInt().coerceAtLeast(1)

    /** Instante exato, em microssegundos, do quadro [index] (0 based). */
    fun frameTimeUs(index: Int, fps: Int): Long =
        index * 1_000_000L / fps.coerceIn(1, MAX_FPS)

    /**
     * Índices (0 based) dentro do buffer que cobre [rawStart..rawEnd] (1 based, inclusivo),
     * ou null quando [start..end] não cabe nele e é preciso extrair de novo.
     */
    fun localIndices(rawStart: Int, rawEnd: Int, start: Int, end: Int): IntRange? =
        if (rawEnd >= rawStart && start >= rawStart && end <= rawEnd && start <= end) {
            (start - rawStart)..(end - rawStart)
        } else {
            null
        }

    fun sheetColumns(count: Int): Int = ceil(sqrt(count.coerceAtLeast(1).toDouble())).toInt()

    fun sheetRows(count: Int): Int {
        val columns = sheetColumns(count)
        return (count.coerceAtLeast(1) + columns - 1) / columns
    }
}

/** Duração (ms) e taxa de quadros da origem de um vídeo. */
data class VideoTiming(val durationMs: Long, val fps: Int) {
    val frameCount: Int get() = VideoFrameMath.frameCount(durationMs, fps)
}
