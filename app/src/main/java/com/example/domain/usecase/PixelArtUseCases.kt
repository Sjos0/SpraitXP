package com.example.domain.usecase

import android.graphics.Bitmap

interface PixelArtConverterUseCase {
    suspend fun convert(
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
        ditherModeName: String
    ): Bitmap
}

interface VideoFrameExtractorUseCase {
    /**
     * Extrai quadros entre [startMs] e [endMs], amostrando a [fps] informada.
     * Cada quadro é ajustado para um quadrado de [targetSide] pixels.
     */
    suspend fun extractFrames(
        videoUriString: String,
        startMs: Long,
        endMs: Long,
        fps: Int,
        maxFrames: Int,
        targetSide: Int
    ): List<Bitmap>
}

interface SpriteSheetAnalyzerUseCase {
    fun calculateFrames(
        sheetWidth: Int,
        sheetHeight: Int,
        columns: Int,
        rows: Int
    ): List<Pair<Int, Int>>
}
