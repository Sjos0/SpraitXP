package com.example.presentation.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.components.XpButton
import com.example.presentation.components.XpGroupBox
import com.example.presentation.components.XpSlider
import com.example.presentation.components.XpWindow
import com.example.ui.theme.LunaTitleStart
import com.example.ui.theme.PixelifyFontFamily
import com.example.ui.theme.VT323FontFamily
import com.example.ui.theme.XpSelectionYellow
import com.example.ui.theme.XpStartGreen
import com.example.ui.theme.XpTextBlack
import com.example.ui.theme.XpTextWhite
import kotlinx.coroutines.delay

/**
 * Módulo C: Analisador de Sprite Sheet (detectar grade, selecionar faixa de frames,
 * visualização com numeração e prévia de animação em loop).
 */
@Composable
fun AnalyzerScreen(
    sheetBitmap: Bitmap?,
    columns: Int,
    rows: Int,
    startRange: Int,
    endRange: Int,
    fps: Int,
    onRangeChange: (Int, Int) -> Unit,
    onExportClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalFrames = columns * rows
    var activePreviewFrame by remember { mutableStateOf(startRange) }
    var isPlaying by remember { mutableStateOf(true) }

    // Loop de animação nos frames selecionados
    LaunchedEffect(isPlaying, startRange, endRange, fps) {
        if (!isPlaying || startRange > endRange) return@LaunchedEffect
        while (isPlaying) {
            val next = if (activePreviewFrame < endRange) activePreviewFrame + 1 else startRange
            activePreviewFrame = next
            delay((1000L / maxOf(1, fps)))
        }
    }

    // Fatia de frame selecionado
    val previewSubBitmap = remember(sheetBitmap, activePreviewFrame, columns, rows) {
        if (sheetBitmap != null && activePreviewFrame in 1..totalFrames) {
            val idx = activePreviewFrame - 1
            val fw = sheetBitmap.width / columns
            val fh = sheetBitmap.height / rows
            val col = idx % columns
            val row = idx / columns
            val x = (col * fw).coerceIn(0, sheetBitmap.width - fw)
            val y = (row * fh).coerceIn(0, sheetBitmap.height - fh)
            Bitmap.createBitmap(sheetBitmap, x, y, fw, fh)
        } else null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        XpWindow(
            title = "Analisador",
            icon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.White
                )
            },
            onCloseClick = onCloseClick,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Barra de Menus da Janela (Arquivo, Editar, Exibir, Ajuda)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFD4D0C8))
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Arquivo", fontFamily = PixelifyFontFamily, fontSize = 13.sp)
                    Text("Editar", fontFamily = PixelifyFontFamily, fontSize = 13.sp)
                    Text("Exibir", fontFamily = PixelifyFontFamily, fontSize = 13.sp)
                    Text("Ajuda", fontFamily = PixelifyFontFamily, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Grade com numeração dos frames sobrepostos
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFFE8E8E8))
                        .border(1.dp, Color(0xFF999999))
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(totalFrames) { i ->
                            val frameNum = i + 1
                            val isSelected = frameNum in startRange..endRange

                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(if (isSelected) Color(0x66FFE066) else Color.Transparent)
                                    .border(
                                        width = if (isSelected) 2.dp else 0.5.dp,
                                        color = if (isSelected) XpSelectionYellow else Color(0xFF388E3C)
                                    )
                                    .clickable {
                                        if (frameNum < startRange) {
                                            onRangeChange(frameNum, endRange)
                                        } else {
                                            onRangeChange(startRange, frameNum)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = String.format("%02d", frameNum),
                                    fontFamily = VT323FontFamily,
                                    fontSize = 12.sp,
                                    color = Color.Black,
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(2.dp)
                                )

                                if (sheetBitmap != null) {
                                    val fw = sheetBitmap.width / columns
                                    val fh = sheetBitmap.height / rows
                                    val col = i % columns
                                    val row = i / columns
                                    val x = (col * fw).coerceIn(0, sheetBitmap.width - fw)
                                    val y = (row * fh).coerceIn(0, sheetBitmap.height - fh)
                                    val sub = Bitmap.createBitmap(sheetBitmap, x, y, fw, fh)
                                    Image(
                                        bitmap = sub.asImageBitmap(),
                                        contentDescription = "Frame $frameNum",
                                        modifier = Modifier.size(38.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Painel de Prévia & Controles de Animação
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFECE9D8))
                        .border(1.dp, Color(0xFFACA899))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Preview Box
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Preview", fontFamily = PixelifyFontFamily, fontSize = 12.sp)
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.White)
                                .border(1.dp, Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            if (previewSubBitmap != null) {
                                Image(
                                    bitmap = previewSubBitmap.asImageBitmap(),
                                    contentDescription = "Preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                        Row(modifier = Modifier.padding(top = 4.dp)) {
                            XpButton(
                                text = if (isPlaying) "⏸" else "▶",
                                onClick = { isPlaying = !isPlaying },
                                modifier = Modifier.size(34.dp, 28.dp),
                                testTag = "analyzer_play_pause_button"
                            )
                        }
                    }

                    // Controles de Faixa e FPS
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Ini: ", fontFamily = PixelifyFontFamily, fontSize = 13.sp)
                                Box(
                                    modifier = Modifier
                                        .size(32.dp, 26.dp)
                                        .background(Color.White)
                                        .border(1.dp, Color.Black),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$startRange",
                                        fontFamily = VT323FontFamily,
                                        fontSize = 17.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Text("Fim: ", fontFamily = PixelifyFontFamily, fontSize = 13.sp)
                                Box(
                                    modifier = Modifier
                                        .size(32.dp, 26.dp)
                                        .background(Color.White)
                                        .border(1.dp, Color.Black),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$endRange",
                                        fontFamily = VT323FontFamily,
                                        fontSize = 17.sp
                                    )
                                }
                            }

                            XpButton(
                                text = "Exportar",
                                onClick = onExportClick,
                                isPrimaryGreen = true,
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.SaveAlt,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                testTag = "analyzer_export_button"
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "FPS: $fps",
                            fontFamily = VT323FontFamily,
                            fontSize = 16.sp,
                            color = LunaTitleStart
                        )
                    }
                }
            }
        }
    }
}
