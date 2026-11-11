package com.example.data.pipeline

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import com.example.domain.usecase.SpriteSheetAnalyzerUseCase
import com.example.domain.usecase.VideoFrameExtractorUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class MediaProcessingPipeline(private val context: Context) :
    VideoFrameExtractorUseCase, SpriteSheetAnalyzerUseCase {

    private val tag = "SpriteXP"

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
        val source = try {
            context.contentResolver.openInputStream(Uri.parse(imageUriString))?.use {
                BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            Log.e(tag, "Falha ao decodificar imagem", e)
            null
        } ?: return null

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

    override suspend fun extractFrames(
        videoUriString: String,
        startMs: Long,
        endMs: Long,
        fps: Int,
        maxFrames: Int,
        targetSide: Int
    ): List<Bitmap> = withContext(Dispatchers.IO) {
        val frames = mutableListOf<Bitmap>()
        val retriever = MediaMetadataRetriever()
        try {
            setSource(retriever, videoUriString)
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
            val last = minOf(endMs, durationMs)
            // Intervalo entre amostras em milissegundos, derivado da taxa escolhida pelo usuário.
            val stepMs = 1000L / fps.coerceIn(1, 30)

            var t = startMs.coerceAtLeast(0L)
            while (t <= last && frames.size < maxFrames) {
                val frame = retriever.getFrameAtTime(t * 1000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                if (frame != null) frames.add(squareFit(frame, targetSide))
                t += stepMs
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

    suspend fun exportZip(frames: List<Bitmap>, baseName: String): File =
        withContext(Dispatchers.IO) {
            val zipFile = File(context.cacheDir, "$baseName.zip")
            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                frames.forEachIndexed { i, frame ->
                    val entryName = "${baseName}_frame_${String.format("%02d", i + 1)}.png"
                    zos.putNextEntry(ZipEntry(entryName))
                    val stream = ByteArrayOutputStream()
                    frame.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    zos.write(stream.toByteArray())
                    zos.closeEntry()
                }
            }
            zipFile
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
        val cols = maxOf(1, columns)
        val frameEntries = (0 until frameCount).joinToString(",\n    ") { i ->
            val col = i % cols
            val row = i / cols
            """{"filename": "frame_$i", "frame": {"x": ${col * frameWidth}, "y": ${row * frameHeight}, "w": $frameWidth, "h": $frameHeight}, "rotated": false, "trimmed": false, "spriteSourceSize": {"x": 0, "y": 0, "w": $frameWidth, "h": $frameHeight}, "sourceSize": {"w": $frameWidth, "h": $frameHeight}, "duration": ${1000 / maxOf(1, fps)}}"""
        }
        val json = """
{
  "meta": {
    "app": "SpriteXP",
    "version": "1.0",
    "image": "$projectName.png",
    "format": "RGBA8888",
    "size": {"w": ${frameWidth * cols}, "h": ${frameHeight * rows}},
    "scale": 1
  },
  "frames": [
    $frameEntries
  ]
}
""".trimIndent()

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

    /** Ajusta o quadro para um quadrado de [side] pixels. Entradas são 1:1, então é só redimensionar. */
    private fun squareFit(src: Bitmap, side: Int): Bitmap {
        if (src.width == side && src.height == side) return src
        return Bitmap.createScaledBitmap(src, side, side, true)
    }
}
