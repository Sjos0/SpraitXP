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
    suspend fun extractFrames(
        videoUriString: String,
        startFrame: Int,
        endFrame: Int,
        fpsStep: Int,
        maxFrames: Int = 24
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
