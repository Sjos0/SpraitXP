package com.example.presentation.screens

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ExportFormat
import com.example.domain.model.PixelArtSettings
import com.example.presentation.components.XpWindow
import com.example.ui.theme.LunaTitleMid
import com.example.ui.theme.PixelifyFontFamily
import com.example.ui.theme.XpTextBlack
import com.example.ui.theme.XpWindowBg

enum class WorkspaceTab(val label: String) {
    IMAGEM("Imagem"),
    PALETA("Paleta"),
    ANIMACAO("Animação"),
    ANALISADOR("Analisador"),
    EXPORTAR("Exportar")
}

@Composable
fun WorkspaceScreen(
    originalBitmap: Bitmap?,
    convertedBitmap: Bitmap?,
    isConverting: Boolean,
    settings: PixelArtSettings,
    onSettingsChange: ((PixelArtSettings) -> PixelArtSettings) -> Unit,
    onImagePicked: (Bitmap) -> Unit,
    onImportMultipleImages: () -> Unit,
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
    isImporting: Boolean,
    sheetBitmap: Bitmap?,
    columns: Int,
    rows: Int,
    startRange: Int,
    endRange: Int,
    analyzerFps: Int,
    onRangeChange: (Int, Int) -> Unit,
    onPickSheet: () -> Unit,
    currentFormat: ExportFormat,
    isExporting: Boolean,
    exportProgress: Float,
    onFormatChange: (ExportFormat) -> Unit,
    onExportClick: () -> Unit,
    onNavigateProjects: () -> Unit,
    onNavigateSettings: () -> Unit,
    onCheckUpdates: () -> Unit,
    onShowAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(WorkspaceTab.IMAGEM) }
    var menuExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().padding(6.dp)) {
        XpWindow(
            title = "SpriteXP — Área de trabalho",
            icon = { Text("🎨", fontSize = 14.sp) },
            onCloseClick = null,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = LunaTitleMid
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Projetos", fontFamily = PixelifyFontFamily) },
                                onClick = { menuExpanded = false; onNavigateProjects() }
                            )
                            DropdownMenuItem(
                                text = { Text("Configurações", fontFamily = PixelifyFontFamily) },
                                onClick = { menuExpanded = false; onNavigateSettings() }
                            )
                            DropdownMenuItem(
                                text = { Text("Verificar atualização", fontFamily = PixelifyFontFamily) },
                                onClick = { menuExpanded = false; onCheckUpdates() }
                            )
                            DropdownMenuItem(
                                text = { Text("Sobre", fontFamily = PixelifyFontFamily) },
                                onClick = { menuExpanded = false; onShowAbout() }
                            )
                        }
                    }
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    WorkspaceTab.entries.forEach { tab ->
                        val selected = tab == selectedTab
                        Box(
                            Modifier
                                .border(
                                    width = 1.dp,
                                    color = if (selected) Color(0xFF003C74) else Color(0xFF7F9DB9),
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                                .background(
                                    if (selected) XpWindowBg else Color(0xFFD4D0C8),
                                    RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                                .clickable { selectedTab = tab }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = tab.label,
                                color = if (selected) XpTextBlack else Color(0xFF555555),
                                fontFamily = PixelifyFontFamily,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Box(
                    Modifier
                        .fillMaxSize()
                        .border(1.dp, Color(0xFF7F9DB9))
                        .background(XpWindowBg)
                        .padding(4.dp)
                ) {
                    when (selectedTab) {
                        WorkspaceTab.IMAGEM -> ConverterScreen(
                            originalBitmap = originalBitmap,
                            convertedBitmap = convertedBitmap,
                            isConverting = isConverting,
                            settings = settings,
                            onSettingsChange = onSettingsChange,
                            onImagePicked = onImagePicked,
                            onExportClick = { selectedTab = WorkspaceTab.EXPORTAR },
                            onCloseClick = {},
                            frameCount = frames.size.coerceAtLeast(1),
                            fps = fps,
                            onFpsChange = onFpsChange,
                            onQuickPlay = onTogglePlay,
                            onRenderPlay = onTogglePlay,
                            modifier = Modifier.fillMaxSize()
                        )
                        WorkspaceTab.PALETA -> PaletteScreen(
                            settings = settings,
                            previewBitmap = convertedBitmap ?: originalBitmap,
                            onSettingsChange = onSettingsChange,
                            onApply = { },
                            onCloseClick = {},
                            modifier = Modifier.fillMaxSize()
                        )
                        WorkspaceTab.ANIMACAO -> VideoScreen(
                            frames = frames,
                            startFrame = startFrame,
                            endFrame = endFrame,
                            currentFrameIndex = currentFrameIndex,
                            isPlaying = isPlaying,
                            fps = fps,
                            onIntervalChange = onIntervalChange,
                            onFpsChange = onFpsChange,
                            onSelectFrame = onSelectFrame,
                            onTogglePlay = onTogglePlay,
                            onPickVideo = onImportMultipleImages,
                            isImporting = isImporting,
                            onExportClick = { selectedTab = WorkspaceTab.EXPORTAR },
                            onCloseClick = {},
                            modifier = Modifier.fillMaxSize()
                        )
                        WorkspaceTab.ANALISADOR -> AnalyzerScreen(
                            sheetBitmap = sheetBitmap,
                            columns = columns,
                            rows = rows,
                            startRange = startRange,
                            endRange = endRange,
                            fps = analyzerFps,
                            onRangeChange = onRangeChange,
                            onPickSheet = onPickSheet,
                            onExportClick = { selectedTab = WorkspaceTab.EXPORTAR },
                            onCloseClick = {},
                            modifier = Modifier.fillMaxSize()
                        )
                        WorkspaceTab.EXPORTAR -> ExportScreen(
                            currentFormat = currentFormat,
                            isExporting = isExporting,
                            progress = exportProgress,
                            onFormatChange = onFormatChange,
                            onExportClick = onExportClick,
                            onCloseClick = {},
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
