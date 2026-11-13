package io.github.sjos0.spritexp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.sjos0.spritexp.data.update.UpdateStage
import io.github.sjos0.spritexp.presentation.viewmodel.MainViewModel
import io.github.sjos0.spritexp.ui.theme.PixelifyFontFamily

private val XpText = Color(0xFF000000)
private val XpGreen = Color(0xFF3C9A3C)
private val XpTrack = Color(0xFFD4D0C8)

/** Diálogos de atualização e mensagens do app no shell quadrado do SpriteXP. */
@Composable
fun UpdateDialogHost(viewModel: MainViewModel) {
    val state by viewModel.updateState.collectAsState()
    val info = state.info
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    uiState.alertMessage?.let { message ->
        XpModal(title = "SpriteXP", onDismissRequest = { viewModel.dismissAlert() }) {
            Text(text = message, color = XpText, fontFamily = PixelifyFontFamily)
            XpButton(
                text = "OK",
                onClick = { viewModel.dismissAlert() },
                isPrimaryGreen = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    when (state.stage) {
        UpdateStage.AVAILABLE -> if (info != null) {
            XpModal(title = "Nova versão disponível", onDismissRequest = { viewModel.dismissUpdate() }) {
                Text("A versão ${info.tag} está disponível. Deseja atualizar agora?", color = XpText, fontFamily = PixelifyFontFamily)
                XpButton("Atualizar", { viewModel.startUpdate() }, isPrimaryGreen = true, modifier = Modifier.fillMaxWidth())
                XpButton("Depois", { viewModel.dismissUpdate() }, modifier = Modifier.fillMaxWidth())
            }
        }

        UpdateStage.NEED_PERMISSION -> XpModal(
            title = "Permitir instalação",
            onDismissRequest = { viewModel.dismissUpdate() }
        ) {
            Text(
                "Para atualizar o SpriteXP, permita a instalação de apps para ele. " +
                    "Quando você voltar, a atualização continuará sozinha.",
                color = XpText,
                fontFamily = PixelifyFontFamily
            )
            XpButton(
                "Permitir",
                { context.startActivity(viewModel.unknownSourcesIntent()) },
                isPrimaryGreen = true,
                modifier = Modifier.fillMaxWidth()
            )
            XpButton("Depois", { viewModel.dismissUpdate() }, modifier = Modifier.fillMaxWidth())
        }

        UpdateStage.DOWNLOADING -> XpModal(
            title = "Baixando atualização ${info?.tag ?: ""}".trim(),
            onDismissRequest = {}
        ) {
            Text("${(state.progress * 100).toInt()}%", color = XpText, fontFamily = PixelifyFontFamily)
            Box(Modifier.fillMaxWidth().height(14.dp).background(XpTrack)) {
                Box(
                    Modifier.fillMaxWidth(state.progress).fillMaxHeight().background(XpGreen)
                )
            }
        }

        UpdateStage.INSTALLING -> XpModal(title = "Instalar atualização", onDismissRequest = {}) {
            Text(
                "O instalador do Android foi aberto. Confirme a instalação na tela do sistema. " +
                    "O app pode fechar durante a troca; se isso acontecer, abra o SpriteXP novamente.",
                color = XpText,
                fontFamily = PixelifyFontFamily
            )
        }

        UpdateStage.ERROR -> XpModal(
            title = "Erro na atualização",
            onDismissRequest = { viewModel.dismissUpdate() }
        ) {
            Text(state.message ?: "Não foi possível concluir a atualização.", color = XpText, fontFamily = PixelifyFontFamily)
            XpButton("OK", { viewModel.dismissUpdate() }, isPrimaryGreen = true, modifier = Modifier.fillMaxWidth())
        }

        else -> Unit
    }
}
