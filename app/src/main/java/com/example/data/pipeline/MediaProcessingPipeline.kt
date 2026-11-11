package com.example.data.pipeline

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.example.domain.usecase.SpriteSheetAnalyzerUseCase
import com.example.domain.usecase.VideoFrameExtractorUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class MediaProcessingPipeline(private val context: Context) : VideoFrameExtractorUseCase, SpriteSheetAnalyzerUseCase {

    override suspend fun extractFrames(
        videoUriString: String,
        startFrame: Int,
        endFrame: Int,
        fpsStep: Int,
        maxFrames: Int
    ): List<Bitmap> = withContext(Dispatchers.IO) {
        val frames = mutableListOf<Bitmap>()
        val retriever = MediaMetadataRetriever()
        try {
            if (videoUriString.startsWith("content://") || videoUriString.startsWith("file://")) {
                retriever.setDataSource(context, Uri.parse(videoUriString))
            } else {
                retriever.setDataSource(videoUriString)
            }

            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationStr?.toLongOrNull() ?: 3000L
            val totalUs = durationMs * 1000

            val frameCount = maxOf(1, endFrame - startFrame + 1)
            val stepUs = if (frameCount > 1) totalUs / frameCount else totalUs

            var currentUs = startFrame * stepUs
            var collected = 0

            while (collected < maxFrames && currentUs <= totalUs) {
                val frame = retriever.getFrameAtTime(currentUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                if (frame != null) {
                    frames.add(frame)
                    collected++
                }
                currentUs += (stepUs * maxOf(1, fpsStep))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                retriever.release()
            } catch (ignored: Exception) {}
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

    suspend fun createMergedSpriteSheet(frames: List<Bitmap>, columns: Int): Bitmap = withContext(Dispatchers.Default) {
        if (frames.isEmpty()) return@withContext Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val frameW = frames[0].width
        val frameH = frames[0].height
        val cols = maxOf(1, columns)
        val rows = (frames.size + cols - 1) / cols

        val sheet = Bitmap.createBitmap(frameW * cols, frameH * rows, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(sheet)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        frames.forEachIndexed { i, frame ->
            val col = i % cols
            val row = i / cols
            canvas.drawBitmap(frame, (col * frameW).toFloat(), (row * frameH).toFloat(), paint)
        }
        sheet
    }

    suspend fun exportZip(frames: List<Bitmap>, baseName: String): File = withContext(Dispatchers.IO) {
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
        val jsonFile = File(context.cacheDir, "$projectName.json")
        val jsonContent = """
        {
          "meta": {
            "app": "SpriteXP",
            "version": "1.0",
            "image": "$projectName.png",
            "format": "RGBA8888",
            "size": {"w": ${frameWidth * columns}, "h": ${frameHeight * rows}},
            "scale": 1
          },
          "frames": [
            ${(0 until frameCount).joinToString(",\n            ") { i ->
            val col = i % columns
            val row = i / columns
            """{
              "filename": "frame_$i",
              "frame": {"x": ${col * frameWidth}, "y": ${row * frameHeight}, "w": $frameWidth, "h": $frameHeight},
              "rotated": false,
              "trimmed": false,
              "spriteSourceSize": {"x": 0, "y": 0, "w": $frameWidth, "h": $frameHeight},
              "sourceSize": {"w": $frameWidth, "h": $frameHeight},
              "duration": ${1000 / maxOf(1, fps)}
            }"""
        }}
          ]
        }
        """.trimIndent()

        jsonFile.writeText(jsonContent)
        jsonFile
    }
}
