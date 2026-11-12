package com.example.presentation.components

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.update.UpdateStage
import com.example.presentation.viewmodel.MainViewModel

private val XpBeige = Color(0xFFECE9D8)
private val XpGreen = Color(0xFF3C9A3C)
private val XpTrack = Color(0xFFD4D0C8)

/** Diálogos de atualização: aviso, progresso do download, instalação e erro. */
@Composable
fun UpdateDialogHost(viewModel: MainViewModel) {
    val state by viewModel.updateState.collectAsState()
    val info = state.info

    when (state.stage) {
        UpdateStage.AVAILABLE -> if (info != null) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissUpdate() },
                containerColor = XpBeige,
                title = { Text("Atualização disponível") },
                text = { Text("A versão ${info.tag} está pronta. Deseja baixar e instalar agora?") },
                confirmButton = {
                    TextButton(onClick = { viewModel.startUpdate() }) { Text("Atualizar") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissUpdate() }) { Text("Depois") }
                }
            )
        }

        UpdateStage.DOWNLOADING -> AlertDialog(
            onDismissRequest = {},
            containerColor = XpBeige,
            title = { Text("Baixando atualização") },
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
            title = { Text("Atualização") },
            text = { Text(state.message ?: "Algo deu errado na atualização.") },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissUpdate() }) { Text("OK") }
            }
        )

        else -> Unit
    }
}
