package com.example.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LunaTitleEnd
import com.example.ui.theme.LunaTitleMid
import com.example.ui.theme.LunaTitleStart
import com.example.ui.theme.PixelifyFontFamily
import com.example.ui.theme.VT323FontFamily
import com.example.ui.theme.XpCloseOrange
import com.example.ui.theme.XpInnerBorderDark
import com.example.ui.theme.XpStartGreen
import com.example.ui.theme.XpTaskbarBg
import com.example.ui.theme.XpTaskbarDark
import com.example.ui.theme.XpTextBlack
import com.example.ui.theme.XpTextMuted
import com.example.ui.theme.XpTextWhite
import com.example.ui.theme.XpWindowBg
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun XpTaskbar(
    isStartMenuOpen: Boolean,
    onStartClick: () -> Unit,
    activeScreenName: String,
    onActiveTaskClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf("12:00") }

    LaunchedEffect(Unit) {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        while (true) {
            currentTime = format.format(Date())
            delay(10000)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(XpTaskbarBg, XpTaskbarDark)
                )
            )
            .border(width = 1.dp, color = Color(0xFF3880FF))
            .navigationBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botão Iniciar (Start Verde com relevo curvo)
        Box(
            modifier = Modifier
                .height(44.dp)
                .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(XpStartGreen, Color(0xFF4AC44A))
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color(0xFF287028),
                    shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                )
                .clickable(onClick = onStartClick)
                .padding(horizontal = 14.dp)
                .testTag("xp_start_button"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Menu Iniciar",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Iniciar",
                    color = XpTextWhite,
                    fontFamily = PixelifyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Botão da Janela / Tarefa ativa na barra
        if (activeScreenName.isNotBlank() && activeScreenName != "Área de Trabalho") {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF1E52B7))
                    .border(1.dp, Color(0xFF5A94FF), RoundedCornerShape(3.dp))
                    .clickable(onClick = onActiveTaskClick)
                    .padding(horizontal = 8.dp)
                    .testTag("taskbar_active_task"),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = activeScreenName,
                    color = XpTextWhite,
                    fontFamily = PixelifyFontFamily,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        // Bandeja do sistema (Relógio retrô)
        Box(
            modifier = Modifier
                .padding(end = 6.dp)
                .height(34.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF0F367D))
                .border(1.dp, Color(0xFF0A246A), RoundedCornerShape(3.dp))
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🕒",
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = currentTime,
                    color = XpTextWhite,
                    fontFamily = VT323FontFamily,
                    fontSize = 18.sp
                )
            }
        }
    }
}

/**
 * Menu Iniciar clássico Windows XP em gaveta usando ícones Material
 */
@Composable
fun XpStartMenu(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .width(260.dp)
                .shadow(16.dp, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .border(2.dp, LunaTitleStart, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(XpWindowBg)
        ) {
            // Cabeçalho do usuário Artista XP com Material Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(LunaTitleStart, LunaTitleMid, LunaTitleEnd)
                        )
                    )
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                        .background(Color(0xFF1E52B7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Avatar Artista",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Artista XP",
                    color = XpTextWhite,
                    fontFamily = PixelifyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            // Lista de ferramentas principais com Material Icons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 6.dp)
            ) {
                StartMenuItem(
                    icon = Icons.Default.Image,
                    title = "Conversor",
                    subtitle = "Imagem para Pixel Art",
                    onClick = { onNavigate("converter"); onDismiss() }
                )
                StartMenuItem(
                    icon = Icons.Default.Videocam,
                    title = "Vídeo",
                    subtitle = "Extrair Sprite Sheet",
                    onClick = { onNavigate("video"); onDismiss() }
                )
                StartMenuItem(
                    icon = Icons.Default.Search,
                    title = "Analisador",
                    subtitle = "Detectar grade e frames",
                    onClick = { onNavigate("analyzer"); onDismiss() }
                )
                StartMenuItem(
                    icon = Icons.Default.ColorLens,
                    title = "Paletas",
                    subtitle = "PICO-8, Sweetie, Dither",
                    onClick = { onNavigate("palette"); onDismiss() }
                )
                StartMenuItem(
                    icon = Icons.Default.SaveAlt,
                    title = "Exportar",
                    subtitle = "PNG, ZIP e JSON",
                    onClick = { onNavigate("export"); onDismiss() }
                )
                StartMenuItem(
                    icon = Icons.Default.Folder,
                    title = "Meus Projetos",
                    subtitle = "Biblioteca e conjuntos",
                    onClick = { onNavigate("projects"); onDismiss() }
                )
            }

            HorizontalDivider(color = XpInnerBorderDark, thickness = 1.dp)

            // Rodapé do menu: Configurações e Sair
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFD4D0C8))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clickable { onNavigate("settings"); onDismiss() }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configurações",
                        tint = LunaTitleStart,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Configurações",
                        color = XpTextBlack,
                        fontFamily = PixelifyFontFamily,
                        fontSize = 14.sp
                    )
                }

                Row(
                    modifier = Modifier
                        .clickable { onNavigate("desktop"); onDismiss() }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Fechar menu",
                        tint = XpCloseOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StartMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = LunaTitleMid,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                color = XpTextBlack,
                fontFamily = PixelifyFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                text = subtitle,
                color = XpTextMuted,
                fontSize = 11.sp
            )
        }
    }
}
