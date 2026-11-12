package io.github.sjos0.spritexp.presentation.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
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
import io.github.sjos0.spritexp.presentation.components.XpButton
import io.github.sjos0.spritexp.presentation.components.XpGroupBox
import io.github.sjos0.spritexp.presentation.components.XpSlider
import io.github.sjos0.spritexp.presentation.components.XpWindow
import io.github.sjos0.spritexp.ui.theme.LunaTitleStart
import io.github.sjos0.spritexp.ui.theme.PixelifyFontFamily
import io.github.sjos0.spritexp.ui.theme.VT323FontFamily
import io.github.sjos0.spritexp.ui.theme.XpDarkPanel
import io.github.sjos0.spritexp.ui.theme.XpSelectionBorder
import io.github.sjos0.spritexp.ui.theme.XpSelectionYellow
import io.github.sjos0.spritexp.ui.theme.XpStartGreen
import io.github.sjos0.spritexp.ui.theme.XpTextBlack
import io.github.sjos0.spritexp.ui.theme.XpTextWhite

/**
 * Módulo B: Vídeo para sprite sheet com extração de frames, recorte de intervalo,
 * renderização, reprodução da animação e linha do tempo de frames.
 */
@Composable
fun VideoScreen(
    frames: List<Bitmap>,
    startFrame: Int,
    endFrame: Int,
    currentFrameIndex: Int,
    isPlaying: Boolean,
    fps: Int,
    onIntervalChange: (Int, Int) -> Unit,
    onFpsChange: (Int) -> Unit,
    onSelectFrame: (Int) -> Unit,
    onTogglePlay: () -> Unit,
    onExportClick: () -> Unit,
    onPickVideo: () -> Unit,
    isImporting: Boolean,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        XpWindow(
            title = "Vídeo",
            icon = {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = Color.White
                )
            },
            onCloseClick = onCloseClick,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Área de Visualização com Fundo Quadriculado Transparente Retro
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF888888))
                        .border(2.dp, Color(0xFF555555), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (frames.isNotEmpty() && currentFrameIndex < frames.size) {
                        Image(
                            bitmap = frames[currentFrameIndex].asImageBitmap(),
                            contentDescription = "Frame atual",
                            modifier = Modifier.size(140.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(
                            text = "Nenhum frame carregado",
                            color = Color.White,
                            fontFamily = PixelifyFontFamily
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Controles de Intervalo e Play/Pause
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Início: ",
                                fontFamily = PixelifyFontFamily,
                                fontSize = 14.sp
                            )
                            Box(
                                modifier = Modifier
                                    .size(34.dp, 28.dp)
                                    .background(Color.White)
                                    .border(1.dp, Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = String.format("%02d", startFrame),
                                    fontFamily = VT323FontFamily,
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Fim: ",
                                fontFamily = PixelifyFontFamily,
                                fontSize = 14.sp
                            )
                            Box(
                                modifier = Modifier
                                    .size(34.dp, 28.dp)
                                    .background(Color.White)
                                    .border(1.dp, Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = String.format("%02d", endFrame),
                                    fontFamily = VT323FontFamily,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }

                    XpButton(
                        text = if (isPlaying) "Pausar" else "Renderizar",
                        onClick = onTogglePlay,
                        isPrimaryGreen = true,
                        icon = {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        testTag = "video_render_play_button"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Barra de importação (Arquivo > Importar vídeo, 1:1 obrigatório)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    XpButton(
                        text = "Importar vídeo",
                        onClick = onPickVideo,
                        testTag = "video_import_button"
                    )
                    Text(
                        text = if (isImporting) "Extraindo quadros..." else "Recorte automático 1:1 (centro)",
                        fontFamily = VT323FontFamily,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Grade / Linha de Quadros (Timeline)
                XpGroupBox(
                    title = "Quadros (${frames.size})",
                    modifier = Modifier.weight(1f)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(frames) { index, frame ->
                            val isSelected = index == currentFrameIndex
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF808080))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) XpSelectionYellow else Color(0xFF404040),
                                        shape = RoundedCornerShape(3.dp)
                                    )
                                    .clickable { onSelectFrame(index) }
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = frame.asImageBitmap(),
                                    contentDescription = "Quadro $index",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                                Text(
                                    text = String.format("%02d", index + 1),
                                    color = Color.White,
                                    fontFamily = VT323FontFamily,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .background(Color(0x99000000))
                                        .padding(horizontal = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botão de Exportação de Sprite Sheet
                XpButton(
                    text = "Exportar Sprite Sheet",
                    onClick = onExportClick,
                    isPrimaryGreen = true,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "video_export_button"
                )
            }
        }
    }
}
