package io.github.sjos0.spritexp.domain.usecase

/**
 * Metadados da sprite sheet em JSON, no formato hash do TexturePacker, com campos de animação.
 * Puro (sem Android), para ser testado na JVM.
 */
object SpriteMetadata {
    const val SHEET_FILE_NAME = "spritesheet.png"

    fun toJson(
        imageName: String,
        frameWidth: Int,
        frameHeight: Int,
        columns: Int,
        rows: Int,
        frameCount: Int,
        fps: Int,
        sheetWidth: Int = frameWidth * columns.coerceAtLeast(1),
        sheetHeight: Int = frameHeight * rows.coerceAtLeast(1),
        sheetIncluded: Boolean = true
    ): String {
        val cols = columns.coerceAtLeast(1)
        val safeFps = fps.coerceAtLeast(1)
        val durationMs = 1000 / safeFps
        val frames = (0 until frameCount).joinToString(",\n    ") { i ->
            val x = (i % cols) * frameWidth
            val y = (i / cols) * frameHeight
            """{"filename": "frame_${"%02d".format(i + 1)}", "frame": {"x": $x, "y": $y, "w": $frameWidth, "h": $frameHeight}, "rotated": false, "trimmed": false, "spriteSourceSize": {"x": 0, "y": 0, "w": $frameWidth, "h": $frameHeight}, "sourceSize": {"w": $frameWidth, "h": $frameHeight}, "duration": $durationMs}"""
        }
        return """{
  "meta": {
    "app": "SpriteXP",
    "version": "1.1",
    "image": "$imageName",
    "format": "RGBA8888",
    "size": {"w": $sheetWidth, "h": $sheetHeight},
    "scale": 1,
    "sheetIncluded": $sheetIncluded,
    "frameWidth": $frameWidth,
    "frameHeight": $frameHeight,
    "columns": $cols,
    "rows": $rows,
    "frameCount": $frameCount,
    "fps": $safeFps
  },
  "frames": [
    $frames
  ]
}
"""
    }
}
