package io.github.sjos0.spritexp.presentation.screens

import android.graphics.Bitmap
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sjos0.spritexp.domain.model.DitherMode
import io.github.sjos0.spritexp.domain.model.PalettePreset
import io.github.sjos0.spritexp.domain.model.PixelArtSettings
import io.github.sjos0.spritexp.presentation.components.XpButton
import io.github.sjos0.spritexp.presentation.components.XpGroupBox
import io.github.sjos0.spritexp.presentation.components.XpRadioButton
import io.github.sjos0.spritexp.presentation.components.XpWindow
import io.github.sjos0.spritexp.ui.theme.PixelifyFontFamily
import io.github.sjos0.spritexp.ui.theme.VT323FontFamily

/**
 * Módulo D: Paletas e Dithering (PICO-8, Sweetie-16, Endesga 32, Game Boy,
 * Dithering Ordenado e Floyd-Steinberg).
 */
@Composable
fun PaletteScreen(
    settings: PixelArtSettings,
    previewBitmap: Bitmap?,
    onSettingsChange: ((PixelArtSettings) -> PixelArtSettings) -> Unit,
    onApply: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activePalette = settings.palettePreset.colors

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        XpWindow(
            title = "Paleta",
            icon = {
                Icon(
                    imageVector = Icons.Default.ColorLens,
                    contentDescription = null,
                    tint = Color.White
                )
            },
            onCloseClick = onCloseClick,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Caixa de Cores da Paleta Ativa
                XpGroupBox(title = "Cores (${activePalette.size})") {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(8),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        items(activePalette) { color ->
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(color)
                                    .border(1.dp, Color.Black)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Presets de Paleta
                XpGroupBox(title = "Preset:") {
                    Column {
                        PalettePreset.values().forEach { preset ->
                            XpRadioButton(
                                selected = settings.palettePreset == preset,
                                onClick = {
                                    onSettingsChange { it.copy(palettePreset = preset) }
                                },
                                label = preset.displayName
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Modos de Dithering
                XpGroupBox(title = "Dithering:") {
                    Column {
                        DitherMode.values().forEach { mode ->
                            XpRadioButton(
                                selected = settings.ditherMode == mode,
                                onClick = {
                                    onSettingsChange { it.copy(ditherMode = mode) }
                                },
                                label = mode.displayName
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mini Prévia com a Paleta
                XpGroupBox(title = "Visualização", modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF6FA8F5))
                            .border(1.dp, Color(0xFF404040)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (previewBitmap != null) {
                            Image(
                                bitmap = previewBitmap.asImageBitmap(),
                                contentDescription = "Visualização com Paleta",
                                modifier = Modifier.size(90.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botões de Ação
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    XpButton(
                        text = "OK",
                        onClick = onApply,
                        modifier = Modifier.weight(1f),
                        testTag = "palette_ok_button"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    XpButton(
                        text = "Cancelar",
                        onClick = onCloseClick,
                        modifier = Modifier.weight(1f),
                        testTag = "palette_cancel_button"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    XpButton(
                        text = "Aplicar",
                        onClick = onApply,
                        isPrimaryGreen = true,
                        modifier = Modifier.weight(1f),
                        testTag = "palette_apply_button"
                    )
                }
            }
        }
    }
}
