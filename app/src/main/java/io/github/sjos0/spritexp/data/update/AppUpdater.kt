package io.github.sjos0.spritexp.data.update

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.provider.Settings
import io.github.sjos0.spritexp.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** Release mais recente publicada no GitHub. */
data class ReleaseInfo(
    val tag: String,
    val buildNumber: Int,
    val apkUrl: String,
    val apkName: String,
    val notes: String
)

enum class UpdateStage { IDLE, CHECKING, AVAILABLE, DOWNLOADING, NEED_PERMISSION, INSTALLING, ERROR }

data class UpdateUiState(
    val stage: UpdateStage = UpdateStage.IDLE,
    val info: ReleaseInfo? = null,
    val progress: Float = 0f,
    val message: String? = null
)

/** Resultado da instalação, vindo do PackageInstaller. */
sealed interface InstallEvent {
    data object Installed : InstallEvent
    data class Failed(val reason: String) : InstallEvent
}

object InstallEvents {
    /** Fila sem limite: o resultado fica guardado até a tela consumir. */
    val events = Channel<InstallEvent>(Channel.UNLIMITED)
}

object UpdateVersion {
    private val TAG_REGEX = Regex("""^v\d+\.\d+\.(\d+)$""")

    /** "v1.0.12" vira 12. Retorna null para tags fora do padrão. */
    fun buildNumberFromTag(tag: String): Int? =
        TAG_REGEX.find(tag.trim())?.groupValues?.get(1)?.toIntOrNull()
}

/**
 * Busca a release mais recente no GitHub, baixa o APK com progresso
 * e abre a instalação do próprio Android (PackageInstaller).
 */
class AppUpdater(private val context: Context) {

    suspend fun fetchLatest(): ReleaseInfo? = withContext(Dispatchers.IO) {
        val json = JSONObject(httpGet(LATEST_URL))
        val tag = json.getString("tag_name")
        val build = UpdateVersion.buildNumberFromTag(tag) ?: return@withContext null
        val assets = json.getJSONArray("assets")
        for (i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            val name = asset.getString("name")
            if (name.endsWith(".apk")) {
                return@withContext ReleaseInfo(
                    tag = tag,
                    buildNumber = build,
                    apkUrl = asset.getString("browser_download_url"),
                    apkName = name,
                    notes = json.optString("body", "")
                )
            }
        }
        null
    }

    fun isNewer(info: ReleaseInfo): Boolean = info.buildNumber > BuildConfig.VERSION_CODE

    /** Android 8+: o app precisa de permissão para instalar outros apps (concedida pelo usuário, uma vez). */
    fun canInstallPackages(): Boolean = context.packageManager.canRequestPackageInstalls()

    /** Tela do próprio sistema, já apontando para este app, para liberar a instalação. */
    fun unknownSourcesIntent(): Intent =
        Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    /** Baixa o APK para o cache interno e informa o progresso de 0 a 1. */
    suspend fun download(info: ReleaseInfo, onProgress: (Float) -> Unit): File =
        withContext(Dispatchers.IO) {
            val conn = (URL(info.apkUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 30_000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", USER_AGENT)
            }
            try {
                if (conn.responseCode !in 200..299) error("HTTP ${conn.responseCode}")
                val total = conn.contentLengthLong
                val file = File(context.cacheDir, "update.apk")
                file.delete()
                conn.inputStream.use { input ->
                    file.outputStream().use { out ->
                        val buffer = ByteArray(64 * 1024)
                        var done = 0L
                        while (true) {
                            val read = input.read(buffer)
                            if (read == -1) break
                            out.write(buffer, 0, read)
                            done += read
                            if (total > 0) onProgress((done.toFloat() / total).coerceIn(0f, 1f))
                        }
                    }
                }
                if (file.length() == 0L) error("APK vazio")
                file
            } finally {
                conn.disconnect()
            }
        }

    /**
     * Grava o APK em uma sessão do PackageInstaller e confirma.
     * O Android mostra a tela de instalação; o resultado chega em [InstallResultReceiver].
     */
    fun install(apk: File) {
        val installer = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
            .apply {
                setAppPackageName(context.packageName)
                // Pede ao Android que não exija toque do usuário. Em apps instalados por fora da loja,
                // o sistema ainda pode mostrar a confirmação; o app não consegue forçar isso.
                if (Build.VERSION.SDK_INT >= 34) {
                    setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
                }
            }
        val sessionId = installer.createSession(params)
        installer.openSession(sessionId).use { session ->
            apk.inputStream().use { input ->
                session.openWrite("SpriteXP-update", 0, apk.length()).use { out ->
                    input.copyTo(out)
                    session.fsync(out)
                }
            }
            val intent = Intent(context, InstallResultReceiver::class.java)
                .setPackage(context.packageName)
            val pending = PendingIntent.getBroadcast(
                context,
                sessionId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            session.commit(pending.intentSender)
        }
    }

    private fun httpGet(url: String): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", USER_AGENT)
        }
        try {
            if (conn.responseCode !in 200..299) error("HTTP ${conn.responseCode}")
            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    private companion object {
        const val REPO = "Sjos0/SpriteXP"
        const val LATEST_URL = "https://api.github.com/repos/$REPO/releases/latest"
        const val USER_AGENT = "SpriteXP-updater"
    }
}
