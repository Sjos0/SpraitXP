package io.github.sjos0.spritexp.data.storage

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File

/**
 * Pasta pública "SpriteXP" dentro de Downloads, no armazenamento interno do aparelho.
 * Android 10 ou superior grava pelo MediaStore (sem permissão extra);
 * Android 8 e 9 gravam direto no arquivo, com a permissão de armazenamento do manifest.
 */
object SpriteXpStorage {
    const val FOLDER = "SpriteXP"

    /** Caminho mostrado ao usuário: Arquivos > Downloads > SpriteXP. */
    const val DISPLAY_DIR = "Download/SpriteXP"

    fun mimeFor(name: String): String = when (name.substringAfterLast('.', "").lowercase()) {
        "zip" -> "application/zip"
        "png" -> "image/png"
        "json" -> "application/json"
        else -> "application/octet-stream"
    }

    /** Copia um arquivo gerado para a pasta SpriteXP. Devolve o nome salvo, ou null se falhar. */
    fun publish(context: Context, source: File): String? = runCatching {
        writeBytes(context, source.name, source.readBytes())
        source.name
    }.getOrNull()

    /** Lê um texto salvo na pasta SpriteXP (ex.: presets.json). Null se não existir. */
    fun readText(context: Context, name: String): String? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val uri = findUri(context, name) ?: return null
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        } else {
            val file = File(legacyDir(), name)
            if (file.exists()) file.readText() else null
        }
    }.getOrNull()

    /** Grava, ou substitui, um texto na pasta SpriteXP. */
    fun writeText(context: Context, name: String, text: String): Boolean =
        runCatching {
            writeBytes(context, name, text.toByteArray(Charsets.UTF_8))
            true
        }.getOrDefault(false)

    private fun writeBytes(context: Context, name: String, bytes: ByteArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val uri = findUri(context, name) ?: run {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeFor(name))
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/$FOLDER")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: error("MediaStore não criou o arquivo $name")
            }
            // "wt" trunca o arquivo antes de gravar. Sem isso, um conteúdo menor que o anterior
            // (ex.: presets.json encurtado) deixaria bytes antigos no fim e quebraria o arquivo.
            resolver.openOutputStream(uri, "wt")?.use { it.write(bytes) }
                ?: error("Sem saída para $name")
            resolver.update(
                uri,
                ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) },
                null,
                null
            )
        } else {
            legacyDir().mkdirs()
            File(legacyDir(), name).writeBytes(bytes)
        }
    }

    /** Acha o arquivo com esse nome dentro de Download/SpriteXP (o nome sozinho não é único). */
    private fun findUri(context: Context, name: String): Uri? {
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.RELATIVE_PATH
        )
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
        context.contentResolver.query(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            arrayOf(name),
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH)
            while (cursor.moveToNext()) {
                val path = cursor.getString(pathColumn) ?: continue
                if (path.trimEnd('/').endsWith(FOLDER)) {
                    return ContentUris.withAppendedId(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        cursor.getLong(idColumn)
                    )
                }
            }
        }
        return null
    }

    @Suppress("DEPRECATION")
    private fun legacyDir(): File =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FOLDER)
}
