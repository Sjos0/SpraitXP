package io.github.sjos0.spritexp.data.pipeline

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import io.github.sjos0.spritexp.domain.usecase.ImageRules
import io.github.sjos0.spritexp.domain.usecase.SpriteMetadata
import io.github.sjos0.spritexp.domain.usecase.SpriteSheetAnalyzerUseCase
import io.github.sjos0.spritexp.domain.usecase.VideoFrameExtractorUseCase
import io.github.sjos0.spritexp.domain.usecase.VideoFrameMath
import io.github.sjos0.spritexp.domain.usecase.VideoTiming
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class MediaProcessingPipeline(private val context: Context) :
    VideoFrameExtractorUseCase, SpriteSheetAnalyzerUseCase {

    private val tag = "SpriteXP"

    /** Amplia sem interpolação: cada pixel convertido vira um bloco uniforme. */
    fun scaleNearestNeighbor(bitmap: Bitmap, factor: Int): Bitmap {
        require(factor > 0) { "A escala precisa ser positiva" }
        if (factor == 1) return bitmap
        return Bitmap.createScaledBitmap(bitmap, bitmap.width * factor, bitmap.height * factor, false)
    }

    /** Confere a largura e a altura de um vídeo, considerando a rotação gravada no arquivo. */
    fun probeVideoSize(videoUriString: String): Pair<Int, Int>? {
        val retriever = MediaMetadataRetriever()
        return try {
            setSource(retriever, videoUriString)
            val w = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull()
            val h = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull()
            val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
            when {
                w == null || h == null -> null
                rotation == 90 || rotation == 270 -> h to w
                else -> w to h
            }
        } catch (e: Exception) {
            Log.e(tag, "Falha ao ler dimensões do vídeo", e)
            null
        } finally {
            retriever.release()
        }
    }

    /** Lê largura e altura de uma imagem sem decodificar os pixels. */
    /** Duração e taxa de quadros da origem. */
    fun probeVideoTiming(videoUriString: String): VideoTiming? {
        val retriever = MediaMetadataRetriever()
        return try {
            setSource(retriever, videoUriString)
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: return null
            val rawFps = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
                ?.toFloatOrNull()
            VideoTiming(durationMs = durationMs, fps = VideoFrameMath.normalizeFps(rawFps))
        } catch (e: Exception) {
            Log.e(tag, "Falha ao ler o vídeo", e)
            null
        } finally {
            retriever.release()
        }
    }

    fun probeImageSize(imageUriString: String): Pair<Int, Int>? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        return try {
            context.contentResolver.openInputStream(Uri.parse(imageUriString))?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
            if (options.outWidth > 0 && options.outHeight > 0) options.outWidth to options.outHeight else null
        } catch (e: Exception) {
            Log.e(tag, "Falha ao ler dimensões da imagem", e)
            null
        }
    }

    /**
     * Decodifica uma imagem. Se o lado maior passar de [maxSide], reduz com vizinho mais próximo,
     * para preservar os pixels de pixel art e economizar memória.
     */
    fun decodeImage(imageUriString: String, maxSide: Int): Bitmap? {
        val source = decodeSource(imageUriString) ?: return null

        val side = maxOf(source.width, source.height)
        if (side <= maxSide) return source
        val scale = maxSide.toFloat() / side
        return Bitmap.createScaledBitmap(
            source,
            (source.width * scale).toInt().coerceAtLeast(1),
            (source.height * scale).toInt().coerceAtLeast(1),
            false
        )
    }

    /**
     * Decodifica uma imagem em qualquer proporção e já a entrega em 1:1: recorta o maior quadrado
     * centralizado a partir da resolução original e só então reduz, se passar de [maxSide].
     * Assim a imagem 16:9 ou 9:16 perde as bordas laterais, e não a resolução do quadrado.
     */
    fun decodeImageCenterSquare(imageUriString: String, maxSide: Int): Bitmap? {
        val source = decodeSource(imageUriString) ?: return null
        val square = centerCropSquare(source)
        if (square.width <= maxSide) return square
        return Bitmap.createScaledBitmap(square, maxSide, maxSide, false)
    }

    private fun decodeSource(imageUriString: String): Bitmap? =
        try {
            context.contentResolver.openInputStream(Uri.parse(imageUriString))?.use {
                BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            Log.e(tag, "Falha ao decodificar imagem", e)
            null
        }

    override suspend fun extractFrames(
        videoUriString: String,
        firstFrame: Int,
        lastFrame: Int,
        sourceFps: Int,
        maxFrames: Int,
        targetSide: Int,
        onProgress: (Float) -> Unit
    ): List<Bitmap> = withContext(Dispatchers.IO) {
        val frames = mutableListOf<Bitmap>()
        val retriever = MediaMetadataRetriever()
        try {
            setSource(retriever, videoUriString)
            val last = minOf(lastFrame, firstFrame + maxFrames - 1)
            val total = (last - firstFrame.coerceAtLeast(0) + 1).coerceAtLeast(1)
            for (index in firstFrame.coerceAtLeast(0)..last) {
                // Pede cada quadro pelo instante exato (OPTION_CLOSEST), sem parar no keyframe.
                val frame = retriever.getFrameAtTime(
                    VideoFrameMath.frameTimeUs(index, sourceFps),
                    MediaMetadataRetriever.OPTION_CLOSEST
                ) ?: break
                frames.add(squareCropCenter(frame, targetSide))
                onProgress(frames.size.toFloat() / total)
            }
        } catch (e: Exception) {
            Log.e(tag, "Falha ao extrair quadros", e)
        } finally {
            retriever.release()
        }
        frames
    }

    override fun calculateFrames(
        sheetWidth: Int,
        sheetHeight: Int,
        columns: Int,
        rows: Int
    ): List<Pair<Int, Int>> {
        val list = mutableListOf<Pair<Int, Int>>()
        val fw = sheetWidth / maxOf(1, columns)
        val fh = sheetHeight / maxOf(1, rows)
        for (r in 0 until rows) {
            for (c in 0 until columns) {
                list.add(Pair(c * fw, r * fh))
            }
        }
        return list
    }

    suspend fun createMergedSpriteSheet(frames: List<Bitmap>, columns: Int): Bitmap =
        withContext(Dispatchers.Default) {
            if (frames.isEmpty()) return@withContext Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            val frameW = frames[0].width
            val frameH = frames[0].height
            val cols = maxOf(1, columns)
            val rows = (frames.size + cols - 1) / cols

            val sheet = Bitmap.createBitmap(frameW * cols, frameH * rows, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(sheet)
            val paint = Paint()

            frames.forEachIndexed { i, frame ->
                val col = i % cols
                val row = i / cols
                canvas.drawBitmap(frame, (col * frameW).toFloat(), (row * frameH).toFloat(), paint)
            }
            sheet
        }

    /**
     * ZIP completo sem manter todos os bitmaps ampliados na RAM.
     * Os frames são ampliados, gravados e liberados um por vez. A sheet final é
     * opcional: acima de [MAX_SHEET_PIXELS], os frames individuais continuam
     * obrigatoriamente disponíveis e o metadata informa que a sheet foi omitida.
     */
    suspend fun exportSpriteBundleStreaming(
        frames: List<Bitmap>,
        columns: Int,
        fps: Int,
        displayScale: Int,
        baseName: String,
        onProgress: (Float) -> Unit = {}
    ): File {
        require(frames.isNotEmpty()) { "Nenhum quadro para exportar" }
        val cols = maxOf(1, columns)
        val rows = (frames.size + cols - 1) / cols
        val baseFrameWidth = frames[0].width
        val baseFrameHeight = frames[0].height
        val scale = io.github.sjos0.spritexp.domain.usecase.ExportSizing.requireSafeScale(
            baseFrameWidth,
            baseFrameHeight,
            displayScale
        )
        val frameWidth = baseFrameWidth * scale
        val frameHeight = baseFrameHeight * scale
        val sheetWidth = frameWidth.toLong() * cols
        val sheetHeight = frameHeight.toLong() * rows
        val includeSheet = sheetWidth * sheetHeight <= MAX_SHEET_PIXELS
        val json = SpriteMetadata.toJson(
            imageName = if (includeSheet) SpriteMetadata.SHEET_FILE_NAME else "",
            frameWidth = frameWidth,
            frameHeight = frameHeight,
            columns = cols,
            rows = rows,
            frameCount = frames.size,
            fps = fps,
            sheetWidth = if (includeSheet) sheetWidth.toInt() else 0,
            sheetHeight = if (includeSheet) sheetHeight.toInt() else 0,
            sheetIncluded = includeSheet
        )
        return withContext(Dispatchers.IO) {
            val zipFile = File(context.cacheDir, "$baseName.zip")
            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                if (includeSheet) {
                    val baseSheet = createMergedSpriteSheet(frames, cols)
                    val scaledSheet = scaleNearestNeighbor(baseSheet, scale)
                    baseSheet.recycle()
                    zos.addBitmapEntry(SpriteMetadata.SHEET_FILE_NAME, scaledSheet)
                    scaledSheet.recycle()
                }
                zos.addEntry("metadata.json", json.toByteArray(Charsets.UTF_8))
                frames.forEachIndexed { i, frame ->
                    val scaled = scaleNearestNeighbor(frame, scale)
                    try {
                        zos.addBitmapEntry("frames/frame_${"%02d".format(i + 1)}.png", scaled)
                    } finally {
                        scaled.recycle()
                    }
                    onProgress((i + 1).toFloat() / frames.size)
                }
            }
            zipFile
        }
    }

    private fun ZipOutputStream.addEntry(name: String, bytes: ByteArray) {
        putNextEntry(ZipEntry(name))
        write(bytes)
        closeEntry()
    }

    private fun ZipOutputStream.addBitmapEntry(name: String, bitmap: Bitmap) {
        putNextEntry(ZipEntry(name))
        check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, this)) {
            "Não foi possível comprimir $name"
        }
        closeEntry()
    }

    private companion object {
        /** Aproximadamente 64 MiB em pixels ARGB antes de buffers de compressão. */
        const val MAX_SHEET_PIXELS = 16_000_000L
    }

    suspend fun exportJsonMetadata(
        projectName: String,
        frameWidth: Int,
        frameHeight: Int,
        columns: Int,
        rows: Int,
        frameCount: Int,
        fps: Int
    ): File = withContext(Dispatchers.IO) {
        val json = SpriteMetadata.toJson(
            imageName = "$projectName.png",
            frameWidth = frameWidth,
            frameHeight = frameHeight,
            columns = columns,
            rows = rows,
            frameCount = frameCount,
            fps = fps
        )
        val jsonFile = File(context.cacheDir, "$projectName.json")
        jsonFile.writeText(json)
        jsonFile
    }

    private fun setSource(retriever: MediaMetadataRetriever, source: String) {
        if (source.startsWith("content://") || source.startsWith("file://")) {
            retriever.setDataSource(context, Uri.parse(source))
        } else {
            retriever.setDataSource(source)
        }
    }

    /**
     * Recorta a imagem no centro para 1:1 (o maior quadrado possível), sem escalar.
     * Imagens já quadradas voltam como estão. Usado pelo import de imagem, que aceita qualquer proporção.
     */
    fun centerCropSquare(src: Bitmap): Bitmap {
        val crop = ImageRules.centerSquareCrop(src.width, src.height) ?: return src
        if (crop.x == 0 && crop.y == 0 && crop.side == src.width && crop.side == src.height) return src
        return Bitmap.createBitmap(src, crop.x, crop.y, crop.side, crop.side)
    }

    /**
     * Recorta o maior quadrado centralizado do quadro (proporção 1:1) e ajusta para [side] pixels.
     * Vídeos em outra proporção (retrato, paisagem) perdem as bordas laterais ou superior/inferior.
     */
    private fun squareCropCenter(src: Bitmap, side: Int): Bitmap {
        val s = minOf(src.width, src.height)
        val x = (src.width - s) / 2
        val y = (src.height - s) / 2
        val cropped = Bitmap.createBitmap(src, x, y, s, s)
        return if (s == side) cropped else Bitmap.createScaledBitmap(cropped, side, side, true)
    }
}
