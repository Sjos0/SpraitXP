package io.github.sjos0.spritexp.domain.usecase

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
        ditherModeName: String,
        chromaModeDistance: Boolean = true,
        chromaHueTolerance: Float = 25f,
        chromaSatMin: Float = 0.35f,
        chromaValMin: Float = 0.12f,
        chromaFeather: Float = 0f,
        chromaDespill: Boolean = false,
        extraChromaColorsInt: IntArray = intArrayOf()
    ): Bitmap
}

interface VideoFrameExtractorUseCase {
    /**
     * Extrai os quadros de [firstFrame] até [lastFrame] (índices 0 based, na taxa [sourceFps] da origem).
     * Cada quadro é ajustado para um quadrado de [targetSide] pixels.
     */
    suspend fun extractFrames(
        videoUriString: String,
        firstFrame: Int,
        lastFrame: Int,
        sourceFps: Int,
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
