package com.example.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ExportFormat
import com.example.presentation.components.XpButton
import com.example.presentation.components.XpRadioButton
import com.example.presentation.components.XpSegmentedProgressBar
import com.example.presentation.components.XpWindow
import com.example.ui.theme.PixelifyFontFamily
import com.example.ui.theme.VT323FontFamily
import com.example.ui.theme.XpTextBlack

/**
 * Módulo E: Exportação (Sprite sheet PNG, ZIP de PNGs, JSON de metadados, barra de progresso)
 * matching mockup Retro_export_dialog_window_mockup
 */
@Composable
fun ExportScreen(
    currentFormat: ExportFormat,
    isExporting: Boolean,
    progress: Float,
    onFormatChange: (ExportFormat) -> Unit,
    onExportClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        XpWindow(
            title = "Exportar",
            icon = {
                Icon(
                    imageVector = Icons.Default.SaveAlt,
                    contentDescription = null,
                    tint = Color.White
                )
            },
            onCloseClick = onCloseClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Alerta e Descrição
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alerta",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(38.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Escolha o formato\npara exportação:",
                        fontFamily = PixelifyFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = XpTextBlack
                    )
                }

                // Opções de Formato de Exportação
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    ExportFormat.values().forEach { fmt ->
                        XpRadioButton(
                            selected = currentFormat == fmt,
                            onClick = { onFormatChange(fmt) },
                            label = fmt.displayName
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Barra de Progresso Segmentada XP
                XpSegmentedProgressBar(
                    progress = if (isExporting) progress else 0f
                )

                if (isExporting) {
                    Text(
                        text = "Exportando... ${(progress * 100).toInt()}%",
                        fontFamily = VT323FontFamily,
                        fontSize = 18.sp,
                        color = Color(0xFF0A246A),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botões Cancelar e Exportar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    XpButton(
                        text = "Cancelar",
                        onClick = onCloseClick,
                        modifier = Modifier.weight(1f),
                        testTag = "export_cancel_button"
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    XpButton(
                        text = if (isExporting) "Aguarde..." else "Exportar",
                        onClick = onExportClick,
                        enabled = !isExporting,
                        isPrimaryGreen = true,
                        modifier = Modifier.weight(1f),
                        testTag = "export_confirm_button"
                    )
                }
            }
        }
    }
}
