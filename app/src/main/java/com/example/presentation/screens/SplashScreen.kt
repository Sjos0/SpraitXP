package com.example.presentation.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.components.XpSegmentedProgressBar
import com.example.ui.theme.LunaTitleStart
import com.example.ui.theme.PixelifyFontFamily
import com.example.ui.theme.VT323FontFamily

/**
 * Splash screen retrô com gradiente procedural Canvas, ícones Material e sem arquivos de imagem mock.
 */
@Composable
fun SplashScreen(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("splash_screen")
    ) {
        // Fundo vetorial em Canvas (Céu retro azul com nuvens estilizadas)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF2563EB), Color(0xFF60A5FA), Color(0xFFBFDBFE))
                ),
                size = size
            )

            // Nuvens procedural
            drawCircle(color = Color(0x25FFFFFF), radius = w * 0.35f, center = Offset(w * 0.2f, h * 0.25f))
            drawCircle(color = Color(0x30FFFFFF), radius = w * 0.45f, center = Offset(w * 0.7f, h * 0.35f))
            drawCircle(color = Color(0x20FFFFFF), radius = w * 0.4f, center = Offset(w * 0.4f, h * 0.75f))
        }

        // Conteúdo central
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Emblema com Material Icon Palette / AutoFixHigh
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF38BDF8), Color(0xFF0369A1))
                        )
                    )
                    .border(3.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Logo SpriteXP",
                    tint = Color.White,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SpriteXP",
                fontFamily = PixelifyFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 38.sp,
                color = Color.White
            )

            Text(
                text = "Oficina de Pixel Art Retrô",
                fontFamily = PixelifyFontFamily,
                fontSize = 15.sp,
                color = Color(0xFFE0F2FE)
            )

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "Carregando... ${(progress * 100).toInt()}%",
                fontFamily = VT323FontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(8.dp))

            XpSegmentedProgressBar(
                progress = progress,
                modifier = Modifier.width(260.dp)
            )
        }

        Text(
            text = "SpriteXP • Android 8.0+",
            fontFamily = PixelifyFontFamily,
            fontSize = 13.sp,
            color = Color(0xFF1E3A8A),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}
