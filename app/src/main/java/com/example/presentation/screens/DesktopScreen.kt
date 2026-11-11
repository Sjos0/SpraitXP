package com.example.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.components.XpButton
import com.example.presentation.components.XpWindow
import com.example.ui.theme.PixelifyFontFamily
import com.example.ui.theme.XpDeskHill
import com.example.ui.theme.XpDeskSky
import com.example.ui.theme.XpTextBlack

/**
 * Área de Trabalho clássica estilo Windows XP com fundo vetorial estilizado
 * (céu azul, colinas verdes) e ícones oficiais Material Symbols/Icons.
 */
@Composable
fun DesktopScreen(
    alertMessage: String?,
    onDismissAlert: () -> Unit,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Fundo Vetorial puro em Canvas (sem nenhum mock de imagem externa)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Céu Azul com gradiente
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF3B82F6), XpDeskSky, Color(0xFF93C5FD)),
                    startY = 0f,
                    endY = h * 0.65f
                ),
                size = size
            )

            // Nuvens estilizadas vetoriais
            drawCircle(color = Color(0x33FFFFFF), radius = w * 0.25f, center = Offset(w * 0.2f, h * 0.15f))
            drawCircle(color = Color(0x44FFFFFF), radius = w * 0.35f, center = Offset(w * 0.5f, h * 0.12f))
            drawCircle(color = Color(0x33FFFFFF), radius = w * 0.28f, center = Offset(w * 0.8f, h * 0.18f))

            // Colina de fundo (Verde escuro suave)
            val hillBackPath = Path().apply {
                moveTo(0f, h * 0.55f)
                cubicTo(w * 0.35f, h * 0.48f, w * 0.7f, h * 0.62f, w, h * 0.52f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = hillBackPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF48A82D), Color(0xFF2E7D1C)),
                    startY = h * 0.48f,
                    endY = h
                )
            )

            // Colina da frente (Verde vibrante clássico)
            val hillFrontPath = Path().apply {
                moveTo(0f, h * 0.64f)
                cubicTo(w * 0.4f, h * 0.54f, w * 0.75f, h * 0.72f, w, h * 0.60f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = hillFrontPath,
                brush = Brush.verticalGradient(
                    colors = listOf(XpDeskHill, Color(0xFF3B7F24), Color(0xFF1E5214)),
                    startY = h * 0.54f,
                    endY = h
                )
            )
        }

        // Grade de Ícones de Atalho na Área de Trabalho com Material Icons
        Column(
            modifier = Modifier
                .padding(top = 130.dp, start = 20.dp, end = 20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DesktopIcon(
                    icon = Icons.Default.Image,
                    label = "Imagem",
                    badgeColor = Color(0xFF2563EB),
                    onClick = { onNavigate("converter") },
                    testTag = "desktop_icon_image"
                )
                DesktopIcon(
                    icon = Icons.Default.Videocam,
                    label = "Vídeo",
                    badgeColor = Color(0xFF7C3AED),
                    onClick = { onNavigate("video") },
                    testTag = "desktop_icon_video"
                )
                DesktopIcon(
                    icon = Icons.Default.Search,
                    label = "Analisador",
                    badgeColor = Color(0xFF059669),
                    onClick = { onNavigate("analyzer") },
                    testTag = "desktop_icon_analyzer"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DesktopIcon(
                    icon = Icons.Default.ColorLens,
                    label = "Paleta",
                    badgeColor = Color(0xFFEA580C),
                    onClick = { onNavigate("palette") },
                    testTag = "desktop_icon_palette"
                )
                DesktopIcon(
                    icon = Icons.Default.SaveAlt,
                    label = "Exportar",
                    badgeColor = Color(0xFF0284C7),
                    onClick = { onNavigate("export") },
                    testTag = "desktop_icon_export"
                )
                DesktopIcon(
                    icon = Icons.Default.Folder,
                    label = "Projetos",
                    badgeColor = Color(0xFFCA8A04),
                    onClick = { onNavigate("projects") },
                    testTag = "desktop_icon_projects"
                )
            }
        }

        // Janela de Boas-vindas / Console Dialog (exibida sobre a área de trabalho se houver alerta)
        AnimatedVisibility(
            visible = alertMessage != null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp, start = 20.dp, end = 20.dp)
        ) {
            if (alertMessage != null) {
                XpWindow(
                    title = "Console",
                    onCloseClick = onDismissAlert,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = alertMessage,
                            color = XpTextBlack,
                            fontFamily = PixelifyFontFamily,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        XpButton(
                            text = "OK",
                            onClick = onDismissAlert,
                            modifier = Modifier.width(100.dp),
                            testTag = "console_ok_button"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DesktopIcon(
    icon: ImageVector,
    label: String,
    badgeColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        modifier = Modifier
            .size(88.dp)
            .clickable(onClick = onClick)
            .padding(4.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.radialGradient(colors = listOf(badgeColor.copy(alpha = 0.9f), badgeColor)))
                .border(1.5.dp, Color.White, RoundedCornerShape(8.dp))
                .shadow(4.dp, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White,
            fontFamily = PixelifyFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.shadow(2.dp)
        )
    }
}
