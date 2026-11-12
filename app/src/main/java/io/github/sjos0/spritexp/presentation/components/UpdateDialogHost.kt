package io.github.sjos0.spritexp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.sjos0.spritexp.data.update.UpdateStage
import io.github.sjos0.spritexp.presentation.viewmodel.MainViewModel

private val XpBeige = Color(0xFFECE9D8)
private val XpGreen = Color(0xFF3C9A3C)
private val XpTrack = Color(0xFFD4D0C8)

/** Diálogos de atualização: aviso, progresso do download, instalação e erro. */
@Composable
fun UpdateDialogHost(viewModel: MainViewModel) {
    val state by viewModel.updateState.collectAsState()
    val info = state.info

    val context = LocalContext.current

    when (state.stage) {
        UpdateStage.NEED_PERMISSION -> AlertDialog(
            onDismissRequest = { viewModel.dismissUpdate() },
            containerColor = XpBeige,
            title = { Text("Permitir instalação") },
            text = {
                Text(
                    "Para atualizar o SpriteXP sem sair do app, permita a instalação de apps para ele. " +
                        "Quando você voltar, a atualização continua sozinha."
                )
            },
            confirmButton = {
                TextButton(onClick = { context.startActivity(viewModel.unknownSourcesIntent()) }) {
                    Text("Permitir")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissUpdate() }) { Text("Depois") }
            }
        )

        UpdateStage.DOWNLOADING -> AlertDialog(
            onDismissRequest = {},
            containerColor = XpBeige,
            title = { Text("Baixando atualização ${info?.tag ?: ""}".trim()) },
            text = {
                Column {
                    Text("${(state.progress * 100).toInt()}%")
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .background(XpTrack)
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(state.progress)
                                .fillMaxHeight()
                                .background(XpGreen)
                        )
                    }
                }
            },
            confirmButton = {}
        )

        UpdateStage.INSTALLING -> AlertDialog(
            onDismissRequest = {},
            containerColor = XpBeige,
            title = { Text("Instalar atualização") },
            text = { Text("Abrindo o instalador do Android. Confirme a instalação na tela do sistema.") },
            confirmButton = {}
        )

        UpdateStage.ERROR -> AlertDialog(
            onDismissRequest = { viewModel.dismissUpdate() },
            containerColor = XpBeige,
            title = { Text("Erro na atualização") },
            text = { Text(state.message ?: "Não foi possível concluir a atualização.") },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissUpdate() }) { Text("OK") }
            }
        )

        else -> Unit
    }
}
