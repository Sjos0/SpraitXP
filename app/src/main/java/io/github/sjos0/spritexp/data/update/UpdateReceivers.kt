package io.github.sjos0.spritexp.data.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import io.github.sjos0.spritexp.MainActivity

/** Recebe o status da instalação. Quando o Android pede confirmação, abre a tela de instalação. */
class InstallResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                @Suppress("DEPRECATION")
                val confirm = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                if (confirm != null) {
                    confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(confirm)
                } else {
                    InstallEvents.events.trySend(
                        InstallEvent.Failed("o Android não enviou a tela de confirmação")
                    )
                }
            }
            PackageInstaller.STATUS_SUCCESS -> InstallEvents.events.trySend(InstallEvent.Installed)
            else -> InstallEvents.events.trySend(
                InstallEvent.Failed(
                    intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                        ?: "motivo desconhecido"
                )
            )
        }
    }
}

/** Depois que o app é substituído por uma versão nova, o Android o reinicia por aqui. */
class PackageReplacedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        val launch = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(launch)
    }
}
