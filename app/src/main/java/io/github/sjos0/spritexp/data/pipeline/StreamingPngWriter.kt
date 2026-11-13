package io.github.sjos0.spritexp.data.pipeline

import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.CRC32
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream

/**
 * Grava um PNG RGBA de 8 bits linha a linha, sem montar a imagem inteira na memória.
 *
 * Cada linha é entregue por [fillRow] (bytes RGBA, sem o byte de filtro) e comprimida na hora.
 * O fluxo zlib vai para um arquivo temporário, que depois é repartido em chunks IDAT.
 * Memória usada: uma linha e um buffer de cópia, independente do tamanho final.
 */
object StreamingPngWriter {

    private val SIGNATURE = byteArrayOf(
        0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    )
    private const val BUFFER = 64 * 1024

    /**
     * @param fillRow preenche `dst` com a linha [y]: `dst[0]` é o filtro (sempre 0) e
     *   `dst[1 + x * 4 until 1 + x * 4 + 4]` são R, G, B, A do pixel x. O tamanho de `dst` é `1 + width * 4`.
     * @param onRow chamado depois de cada linha escrita.
     */
    fun write(
        file: File,
        width: Int,
        height: Int,
        fillRow: (y: Int, dst: ByteArray) -> Unit,
        onRow: (y: Int) -> Unit = {}
    ) {
        require(width > 0 && height > 0) { "O PNG precisa de tamanho positivo" }
        val compressed = File(file.parentFile, file.name + ".z")
        try {
            val deflater = Deflater(Deflater.BEST_SPEED)
            try {
                DeflaterOutputStream(
                    BufferedOutputStream(FileOutputStream(compressed), BUFFER),
                    deflater,
                    BUFFER
                ).use { zos ->
                    val row = ByteArray(1 + width * 4)
                    for (y in 0 until height) {
                        row[0] = 0 // filtro "None"
                        fillRow(y, row)
                        zos.write(row)
                        onRow(y)
                    }
                }
            } finally {
                deflater.end()
            }

            DataOutputStream(BufferedOutputStream(FileOutputStream(file), BUFFER)).use { out ->
                out.write(SIGNATURE)
                val ihdr = java.nio.ByteBuffer.allocate(13)
                    .putInt(width)
                    .putInt(height)
                    .put(8)  // profundidade de bits
                    .put(6)  // RGBA
                    .put(0)  // compressão
                    .put(0)  // filtro
                    .put(0)  // sem entrelaçamento
                    .array()
                writeChunk(out, "IHDR", ihdr, 0, ihdr.size)

                val buf = ByteArray(BUFFER)
                FileInputStream(compressed).use { input ->
                    while (true) {
                        val n = input.read(buf)
                        if (n <= 0) break
                        writeChunk(out, "IDAT", buf, 0, n)
                    }
                }
                writeChunk(out, "IEND", ByteArray(0), 0, 0)
            }
        } finally {
            compressed.delete()
        }
    }

    private fun writeChunk(out: DataOutputStream, type: String, data: ByteArray, off: Int, len: Int) {
        val typeBytes = type.toByteArray(Charsets.US_ASCII)
        out.writeInt(len)
        out.write(typeBytes)
        out.write(data, off, len)
        val crc = CRC32()
        crc.update(typeBytes)
        crc.update(data, off, len)
        out.writeInt(crc.value.toInt())
    }
}
