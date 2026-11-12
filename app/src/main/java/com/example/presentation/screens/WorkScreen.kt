package com.example.presentation.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ChromaMode
import com.example.domain.model.DitherMode
import com.example.domain.model.PalettePreset
import com.example.domain.model.PixelArtSettings
import com.example.presentation.components.XpButton
import com.example.presentation.components.XpGroupBox
import com.example.presentation.components.XpRadioButton
import com.example.presentation.components.XpSlider

private val DarkBg = Color(0xFF1A1A1E)
private val PanelBg = Color(0xFF2A2A2E)
private val GreenFrame = Color(0xFF388E3C)
private val Gold = Color(0xFFFFE066)
private val Muted = Color(0xFFBBBBBB)

private enum class WorkTab(val label: String) {
    AJUSTES("Ajustes"),
    PALETA("Paleta"),
    EXPORTAR("Exportar"),
    ANALISADOR("Analisador")
}

/**
 * Tela de trabalho do SpriteXP, feita para celular na vertical (mobile first).
 * Ordem: barra superior, prévias, play, linha do tempo e abas de ferramentas.
 */
@Composable
fun WorkScreen(
    settings: PixelArtSettings,
    onSettingsChange: ((PixelArtSettings) -> PixelArtSettings) -> Unit,
    originalFrame: Bitmap?,
    convertedFrame: Bitmap?,
    isConverting: Boolean,
    frames: List<Bitmap>,
    currentFrameIndex: Int,
    frameCount: Int,
    isPlaying: Boolean,
    fps: Int,
    startFrame: Int,
    endFrame: Int,
    isImporting: Boolean,
    onPickVideo: () -> Unit,
    onTogglePlay: () -> Unit,
    onFpsChange: (Int) -> Unit,
    onIntervalChange: (Int, Int) -> Unit,
    onSelectFrame: (Int) -> Unit,
    onExportSheet: () -> Unit,
    onExportZip: () -> Unit,
    isExporting: Boolean,
    exportProgress: Float,
    analyzerSheet: Bitmap?,
    analyzerColumns: Int,
    analyzerRows: Int,
    analyzerStart: Int,
    analyzerEnd: Int,
    analyzerFps: Int,
    onAnalyzerRange: (Int, Int) -> Unit,
    onPickSheet: () -> Unit,
    onNavigateProjects: () -> Unit,
    onNavigateSettings: () -> Unit,
    onCheckUpdates: () -> Unit,
    onShowAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tab by remember { mutableStateOf(WorkTab.AJUSTES) }
    var menuOpen by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().background(DarkBg)) {
        TopBar(
            menuOpen = menuOpen,
            onToggleMenu = { menuOpen = !menuOpen },
            onCheckUpdates = onCheckUpdates
        )

        if (menuOpen) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PanelBg)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                XpButton(text = "Projetos", onClick = { menuOpen = false; onNavigateProjects() }, modifier = Modifier.fillMaxWidth())
                XpButton(text = "Configurações", onClick = { menuOpen = false; onNavigateSettings() }, modifier = Modifier.fillMaxWidth())
                XpButton(text = "Sobre", onClick = { menuOpen = false; onShowAbout() }, modifier = Modifier.fillMaxWidth())
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            XpButton(
                text = if (isImporting) "Importando vídeo..." else "Importar vídeo",
                onClick = onPickVideo,
                isPrimaryGreen = true,
                enabled = !isImporting,
                modifier = Modifier.fillMaxWidth(),
                testTag = "work_import_video"
            )

            Preview(originalFrame = originalFrame, convertedFrame = convertedFrame, isConverting = isConverting)

            PlaybackBar(
                isPlaying = isPlaying,
                currentIndex = currentFrameIndex,
                frameCount = frameCount,
                fps = fps,
                onTogglePlay = onTogglePlay,
                onFpsChange = onFpsChange
            )

            Timeline(frames = frames, current = currentFrameIndex, onSelect = onSelectFrame)

            TabRow(selected = tab, onSelect = { tab = it })

            when (tab) {
                WorkTab.AJUSTES -> AdjustmentsPanel(settings = settings, onSettingsChange = onSettingsChange)
                WorkTab.PALETA -> PalettePanel(settings = settings, onSettingsChange = onSettingsChange)
                WorkTab.EXPORTAR -> ExportPanel(
                    hasFrames = frames.isNotEmpty(),
                    isExporting = isExporting,
                    exportProgress = exportProgress,
                    onExportSheet = onExportSheet,
                    onExportZip = onExportZip
                )
                WorkTab.ANALISADOR -> AnalyzerPanel(
                    sheet = analyzerSheet,
                    columns = analyzerColumns,
                    rows = analyzerRows,
                    start = analyzerStart,
                    end = analyzerEnd,
                    onRange = onAnalyzerRange,
                    onPickSheet = onPickSheet
                )
            }
        }
    }
}

@Composable
private fun TopBar(menuOpen: Boolean, onToggleMenu: () -> Unit, onCheckUpdates: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFF3A93FF), Color(0xFF245EDC), Color(0xFF0A246A))))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "SpriteXP",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f)
        )
        XpButton(
            text = "⟳ Atualizar",
            onClick = onCheckUpdates,
            modifier = Modifier.width(120.dp),
            testTag = "work_update_button"
        )
        Spacer(modifier = Modifier.width(8.dp))
        XpButton(
            text = if (menuOpen) "✕" else "☰",
            onClick = onToggleMenu,
            modifier = Modifier.width(48.dp),
            testTag = "work_menu_button"
        )
    }
}

@Composable
private fun Preview(originalFrame: Bitmap?, convertedFrame: Bitmap?, isConverting: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PreviewPanel(title = "ORIGINAL", bitmap = originalFrame, loading = false, modifier = Modifier.weight(1f))
        PreviewPanel(title = "PIXEL ART", bitmap = convertedFrame, loading = isConverting, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PreviewPanel(title: String, bitmap: Bitmap?, loading: Boolean, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(4.dp))
                .background(GreenFrame)
                .border(2.dp, Color.White, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            when {
                loading -> Text(text = "Convertendo...", color = Color.White, fontSize = 12.sp)
                bitmap != null -> Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                else -> Text(text = "Sem quadro", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun PlaybackBar(
    isPlaying: Boolean,
    currentIndex: Int,
    frameCount: Int,
    fps: Int,
    onTogglePlay: () -> Unit,
    onFpsChange: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            XpButton(
                text = if (isPlaying) "Pausar" else "Play",
                onClick = onTogglePlay,
                isPrimaryGreen = true,
                modifier = Modifier.width(120.dp),
                testTag = "work_play"
            )
            Text(
                text = "Quadro ${currentIndex + 1}/$frameCount",
                color = Color.White,
                fontSize = 13.sp
            )
        }
        XpSlider(
            label = "FPS",
            value = fps.toFloat(),
            onValueChange = { onFpsChange(it.toInt()) },
            valueRange = 1f..30f,
            displayFormatter = { "${it.toInt()} fps" }
        )
    }
}

@Composable
private fun Timeline(frames: List<Bitmap>, current: Int, onSelect: (Int) -> Unit) {
    if (frames.isEmpty()) {
        Text(
            text = "Importe um vídeo para ver os quadros.",
            color = Muted,
            fontSize = 12.sp
        )
        return
    }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(frames.size) { i ->
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(GreenFrame)
                    .border(
                        width = if (i == current) 3.dp else 1.dp,
                        color = if (i == current) Gold else Color.Black,
                        shape = RoundedCornerShape(3.dp)
                    )
                    .clickable { onSelect(i) }
            ) {
                Image(
                    bitmap = frames[i].asImageBitmap(),
                    contentDescription = "Quadro ${i + 1}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
private fun TabRow(selected: WorkTab, onSelect: (WorkTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        WorkTab.values().forEach { t ->
            XpButton(
                text = t.label,
                onClick = { onSelect(t) },
                isPrimaryGreen = t == selected,
                modifier = Modifier.width(110.dp),
                testTag = "work_tab_${t.name.lowercase()}"
            )
        }
    }
}

@Composable
private fun AdjustmentsPanel(settings: PixelArtSettings, onSettingsChange: ((PixelArtSettings) -> PixelArtSettings) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        XpGroupBox(title = "Remoção de fundo") {
            XpRadioButton(
                selected = settings.chromaKeyEnabled,
                onClick = { onSettingsChange { it.copy(chromaKeyEnabled = !it.chromaKeyEnabled) } },
                label = if (settings.chromaKeyEnabled) "Ligada" else "Desligada"
            )
            ChromaMode.values().forEach { m ->
                XpRadioButton(
                    selected = settings.chromaMode == m,
                    onClick = { onSettingsChange { it.copy(chromaMode = m) } },
                    label = "Modo ${m.displayName}"
                )
            }
            if (settings.chromaMode == ChromaMode.DISTANCE) {
                XpSlider(
                    label = "Tolerância",
                    value = settings.chromaTolerance,
                    onValueChange = { v -> onSettingsChange { it.copy(chromaTolerance = v) } },
                    valueRange = 0f..100f,
                    displayFormatter = { "${it.toInt()}%" }
                )
            } else {
                XpSlider(
                    label = "Tolerância de matiz",
                    value = settings.chromaHueTolerance,
                    onValueChange = { v -> onSettingsChange { it.copy(chromaHueTolerance = v) } },
                    valueRange = 0f..90f,
                    displayFormatter = { "${it.toInt()}°" }
                )
                XpSlider(
                    label = "Saturação mínima",
                    value = settings.chromaSatMin,
                    onValueChange = { v -> onSettingsChange { it.copy(chromaSatMin = v) } },
                    valueRange = 0f..1f,
                    displayFormatter = { "%.2f".format(it) }
                )
                XpSlider(
                    label = "Brilho mínimo",
                    value = settings.chromaValMin,
                    onValueChange = { v -> onSettingsChange { it.copy(chromaValMin = v) } },
                    valueRange = 0f..1f,
                    displayFormatter = { "%.2f".format(it) }
                )
            }
            XpSlider(
                label = "Suavização da borda",
                value = settings.chromaFeather,
                onValueChange = { v -> onSettingsChange { it.copy(chromaFeather = v) } },
                valueRange = 0f..30f,
                displayFormatter = { "${it.toInt()}" }
            )
            XpRadioButton(
                selected = settings.chromaDespill,
                onClick = { onSettingsChange { it.copy(chromaDespill = !it.chromaDespill) } },
                label = "Remover vazamento de verde"
            )
        }

        XpGroupBox(title = "Aprimoramento") {
            XpSlider(
                label = "Realce de bordas",
                value = settings.edgeEnhance,
                onValueChange = { v -> onSettingsChange { it.copy(edgeEnhance = v) } },
                valueRange = 0f..200f,
                displayFormatter = { "${it.toInt()}%" }
            )
            XpSlider(
                label = "Contraste",
                value = settings.contrast,
                onValueChange = { v -> onSettingsChange { it.copy(contrast = v) } },
                valueRange = -100f..100f,
                displayFormatter = { "${it.toInt()}" }
            )
            XpSlider(
                label = "Brilho",
                value = settings.brightness,
                onValueChange = { v -> onSettingsChange { it.copy(brightness = v) } },
                valueRange = -100f..100f,
                displayFormatter = { "${it.toInt()}" }
            )
        }

        XpGroupBox(title = "Resolução e escala") {
            XpSlider(
                label = "Lado (px, 1:1)",
                value = settings.targetResolution.toFloat(),
                onValueChange = { v -> onSettingsChange { it.copy(targetResolution = v.toInt().coerceIn(16, 256)) } },
                valueRange = 16f..256f,
                displayFormatter = { "${it.toInt()} px" }
            )
            ChipRow(
                options = listOf(16, 32, 48, 64, 96, 128, 256),
                selected = settings.targetResolution,
                suffix = "",
                onPick = { r -> onSettingsChange { it.copy(targetResolution = r) } }
            )
            ChipRow(
                options = listOf(1, 2, 4, 8, 16),
                selected = settings.displayScale,
                suffix = "x",
                onPick = { s -> onSettingsChange { it.copy(displayScale = s) } }
            )
            Text(
                text = "Saída: ${settings.targetResolution * settings.displayScale} px",
                color = Muted,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ChipRow(options: List<Int>, selected: Int, suffix: String, onPick: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { v ->
            XpButton(
                text = "$v$suffix",
                onClick = { onPick(v) },
                isPrimaryGreen = v == selected,
                modifier = Modifier.width(64.dp)
            )
        }
    }
}

@Composable
private fun PalettePanel(settings: PixelArtSettings, onSettingsChange: ((PixelArtSettings) -> PixelArtSettings) -> Unit) {
    val isAuto = settings.palettePreset == PalettePreset.AUTO_EXTRACT
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        XpGroupBox(title = "Paleta de cores") {
            XpRadioButton(
                selected = isAuto,
                onClick = { onSettingsChange { it.copy(palettePreset = PalettePreset.AUTO_EXTRACT) } },
                label = "Extração automática"
            )
            if (isAuto) {
                XpSlider(
                    label = "Máx. de cores",
                    value = settings.maxAutoColors.toFloat(),
                    onValueChange = { v -> onSettingsChange { it.copy(maxAutoColors = v.toInt().coerceIn(2, 64)) } },
                    valueRange = 2f..64f,
                    displayFormatter = { "${it.toInt()}" }
                )
            } else {
                PalettePreset.values().filter { it != PalettePreset.AUTO_EXTRACT }.forEach { p ->
                    XpRadioButton(
                        selected = settings.palettePreset == p,
                        onClick = { onSettingsChange { it.copy(palettePreset = p) } },
                        label = p.displayName
                    )
                }
            }
            val swatches = settings.palettePreset.colors
            if (swatches.isNotEmpty()) {
                Text(text = "Paleta ativa (${swatches.size})", color = Color.White, fontSize = 12.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    swatches.forEach { c ->
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(c)
                                .border(1.dp, Color.Black)
                        )
                    }
                }
            }
        }

        XpGroupBox(title = "Dithering") {
            DitherMode.values().forEach { m ->
                XpRadioButton(
                    selected = settings.ditherMode == m,
                    onClick = { onSettingsChange { it.copy(ditherMode = m) } },
                    label = m.displayName
                )
            }
        }
    }
}

@Composable
private fun ExportPanel(
    hasFrames: Boolean,
    isExporting: Boolean,
    exportProgress: Float,
    onExportSheet: () -> Unit,
    onExportZip: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        XpGroupBox(title = "Exportar quadros") {
            XpButton(
                text = "Baixar Sprite Sheet (PNG)",
                onClick = onExportSheet,
                isPrimaryGreen = true,
                enabled = hasFrames && !isExporting,
                modifier = Modifier.fillMaxWidth(),
                testTag = "work_export_sheet"
            )
            XpButton(
                text = "Baixar PNGs (ZIP)",
                onClick = onExportZip,
                enabled = hasFrames && !isExporting,
                modifier = Modifier.fillMaxWidth(),
                testTag = "work_export_zip"
            )
            if (isExporting) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(PanelBg)
                        .border(1.dp, Color.Black)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(exportProgress.coerceIn(0f, 1f))
                            .fillMaxSize()
                            .background(GreenFrame)
                    )
                }
            }
            if (!hasFrames) {
                Text(text = "Importe um vídeo antes de exportar.", color = Muted, fontSize = 12.sp)
            }
        }
        XpGroupBox(title = "Automator") {
            Text(text = "Em breve", color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun AnalyzerPanel(
    sheet: Bitmap?,
    columns: Int,
    rows: Int,
    start: Int,
    end: Int,
    onRange: (Int, Int) -> Unit,
    onPickSheet: () -> Unit
) {
    val total = (columns * rows).coerceAtLeast(1)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        XpGroupBox(title = "Analisador de sprite sheet") {
            XpButton(
                text = "Abrir sprite sheet",
                onClick = onPickSheet,
                isPrimaryGreen = true,
                modifier = Modifier.fillMaxWidth(),
                testTag = "work_open_sheet"
            )
            if (sheet != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(GreenFrame)
                        .border(1.dp, Color.Black)
                ) {
                    Image(
                        bitmap = sheet.asImageBitmap(),
                        contentDescription = "Sprite sheet",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                Text(text = "Grade: ${columns}x${rows} ($total quadros)", color = Color.White, fontSize = 12.sp)
                XpSlider(
                    label = "Início",
                    value = start.toFloat(),
                    onValueChange = { v -> onRange(v.toInt(), maxOf(v.toInt(), end)) },
                    valueRange = 1f..total.toFloat(),
                    displayFormatter = { "${it.toInt()}" }
                )
                XpSlider(
                    label = "Fim",
                    value = end.toFloat(),
                    onValueChange = { v -> onRange(minOf(start, v.toInt()), v.toInt()) },
                    valueRange = 1f..total.toFloat(),
                    displayFormatter = { "${it.toInt()}" }
                )
            } else {
                Text(text = "Abra uma sprite sheet 1:1 para analisar.", color = Muted, fontSize = 12.sp)
            }
        }
    }
}
