package io.github.sjos0.spritexp.presentation.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sjos0.spritexp.presentation.components.XpButton
import io.github.sjos0.spritexp.presentation.components.XpGroupBox
import io.github.sjos0.spritexp.presentation.components.XpRadioButton
import io.github.sjos0.spritexp.presentation.components.XpWindow
import io.github.sjos0.spritexp.ui.theme.PixelifyFontFamily
import io.github.sjos0.spritexp.ui.theme.VT323FontFamily
import io.github.sjos0.spritexp.ui.theme.XpStartGreen
import io.github.sjos0.spritexp.ui.theme.XpTextBlack

/**
 * Tela de Configurações matching mockup Retro_settings_app_UI_mockup
 */
@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    soundEnabled: Boolean,
    language: String,
    onDarkThemeChange: (Boolean) -> Unit,
    onSoundChange: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit,
    onShowAbout: () -> Unit,
    onCheckUpdates: () -> Unit,
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
            title = "Configurações",
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
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
                // Tema
                XpGroupBox(title = "Tema") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        XpRadioButton(
                            selected = !isDarkTheme,
                            onClick = { onDarkThemeChange(false) },
                            label = "Claro"
                        )
                        XpRadioButton(
                            selected = isDarkTheme,
                            onClick = { onDarkThemeChange(true) },
                            label = "Escuro"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sons
                XpGroupBox(title = "Sons") {
                    // Animação leve (250 ms): knob desliza e o track troca de cor, sem teleportar.
                    val knobOffset by animateDpAsState(
                        targetValue = if (soundEnabled) 26.dp else 0.dp,
                        animationSpec = tween(durationMillis = 250),
                        label = "soundKnobOffset"
                    )
                    val trackColor by animateColorAsState(
                        targetValue = if (soundEnabled) XpStartGreen else Color(0xFFACA899),
                        animationSpec = tween(durationMillis = 250),
                        label = "soundTrackColor"
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onSoundChange(!soundEnabled) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp, 26.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(trackColor)
                                .border(1.dp, Color.Black, RoundedCornerShape(13.dp))
                                .padding(2.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .offset(x = knobOffset)
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFECE9D8))
                                    .border(1.dp, Color(0xFF707070), RoundedCornerShape(10.dp))
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = if (soundEnabled) "Ativado" else "Desativado",
                            fontFamily = PixelifyFontFamily,
                            color = XpTextBlack,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Idioma
                XpGroupBox(title = "Idioma") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .background(Color.White)
                            .border(1.dp, Color.Black)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = language,
                            fontFamily = PixelifyFontFamily,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Botão Sobre
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    XpButton(
                        text = "ℹ Sobre",
                        onClick = onShowAbout,
                        modifier = Modifier.width(140.dp),
                        testTag = "settings_about_button"
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    XpButton(
                        text = "⟳ Atualizar",
                        onClick = onCheckUpdates,
                        modifier = Modifier.width(140.dp),
                        testTag = "settings_update_button"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botões OK e Cancelar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    XpButton(
                        text = "OK",
                        onClick = onCloseClick,
                        modifier = Modifier.weight(1f),
                        testTag = "settings_ok_button"
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    XpButton(
                        text = "Cancelar",
                        onClick = onCloseClick,
                        modifier = Modifier.weight(1f),
                        testTag = "settings_cancel_button"
                    )
                }
            }
        }
    }
}
