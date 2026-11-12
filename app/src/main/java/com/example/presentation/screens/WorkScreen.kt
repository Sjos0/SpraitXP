package com.example.presentation.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.window.Dialog
import com.example.domain.model.*
import com.example.presentation.components.XpButton
import com.example.presentation.components.XpRadioButton
import com.example.presentation.components.XpSlider
import com.example.ui.theme.*

private val PanelBg = Color(0xFF2A2A2E)
private val PanelBorder = Color(0xFF0055EA)
private val ChipBg = Color(0xFF3A3A40)
private val ChipSelected = Color(0xFF245EDC)

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
    var menuOpen by remember { mutableStateOf(false) }
    var showAnalyzer by remember { mutableStateOf(false) }
    var leftOpen by remember { mutableStateOf(false) }
    var rightOpen by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier.fillMaxSize().background(Color(0xFF1A1A1E))) {
        val isWide = maxWidth >= 720.dp
        Column(Modifier.fillMaxSize().padding(6.dp)) {
            Row(
                Modifier.fillMaxWidth().height(40.dp)
                    .background(Brush.horizontalGradient(listOf(LunaTitleStart, LunaTitleMid, LunaTitleEnd)))
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isWide) {
                    IconButton(onClick = { leftOpen = true }) {
                        Icon(Icons.Default.Tune, null, tint = XpTextWhite)
                    }
                }
                Text("SpriteXP", color = XpTextWhite, fontFamily = PixelifyFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                XpButton(text = if (isImporting) "Importando…" else "Importar vídeo", onClick = onPickVideo, enabled = !isImporting, isPrimaryGreen = true, modifier = Modifier.height(32.dp))
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Default.MoreVert, "Menu", tint = XpTextWhite)
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(text = { Text("Projetos", fontFamily = PixelifyFontFamily) }, onClick = { menuOpen = false; onNavigateProjects() })
                        DropdownMenuItem(text = { Text("Configurações", fontFamily = PixelifyFontFamily) }, onClick = { menuOpen = false; onNavigateSettings() })
                        DropdownMenuItem(text = { Text("Verificar atualização", fontFamily = PixelifyFontFamily) }, onClick = { menuOpen = false; onCheckUpdates() })
                        DropdownMenuItem(text = { Text("Sobre", fontFamily = PixelifyFontFamily) }, onClick = { menuOpen = false; onShowAbout() })
                    }
                }
                if (!isWide) {
                    IconButton(onClick = { rightOpen = true }) {
                        Icon(Icons.Default.ViewModule, null, tint = XpTextWhite)
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            if (isWide) {
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    LeftPanel(settings, onSettingsChange, Modifier.width(260.dp).fillMaxHeight())
                    CenterPanel(originalFrame, convertedFrame, isConverting, currentFrameIndex, frameCount, isPlaying, fps, startFrame, endFrame, onTogglePlay, onFpsChange, onIntervalChange, Modifier.weight(1f).fillMaxHeight())
                    RightPanel(frames, currentFrameIndex, onSelectFrame, settings, onExportSheet, onExportZip, isExporting, { showAnalyzer = true }, Modifier.width(220.dp).fillMaxHeight())
                }
            } else {
                CenterPanel(originalFrame, convertedFrame, isConverting, currentFrameIndex, frameCount, isPlaying, fps, startFrame, endFrame, onTogglePlay, onFpsChange, onIntervalChange, Modifier.weight(1f).fillMaxWidth())
            }
        }
        if (!isWide && leftOpen) {
            Box(Modifier.fillMaxSize().background(Color(0x99000000)).clickable { leftOpen = false })
            Box(Modifier.fillMaxHeight().fillMaxWidth(0.88f).widthIn(max = 300.dp).align(Alignment.CenterStart).background(PanelBg).border(2.dp, PanelBorder).padding(8.dp)) {
                LeftPanel(settings, onSettingsChange, Modifier.fillMaxSize())
            }
        }
        if (!isWide && rightOpen) {
            Box(Modifier.fillMaxSize().background(Color(0x99000000)).clickable { rightOpen = false })
            Box(Modifier.fillMaxHeight().fillMaxWidth(0.85f).widthIn(max = 280.dp).align(Alignment.CenterEnd).background(PanelBg).border(2.dp, PanelBorder).padding(8.dp)) {
                RightPanel(frames, currentFrameIndex, onSelectFrame, settings, onExportSheet, onExportZip, isExporting, { showAnalyzer = true }, Modifier.fillMaxSize())
            }
        }
        if (showAnalyzer) {
            Dialog(onDismissRequest = { showAnalyzer = false }) {
                Box(Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.9f).background(PanelBg).border(2.dp, PanelBorder).padding(8.dp)) {
                    AnalyzerScreen(analyzerSheet, analyzerColumns, analyzerRows, analyzerStart, analyzerEnd, analyzerFps, onAnalyzerRange, onPickSheet, { showAnalyzer = false; onExportSheet() }, { showAnalyzer = false }, Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
private fun LeftPanel(settings: PixelArtSettings, onSettingsChange: ((PixelArtSettings) -> PixelArtSettings) -> Unit, modifier: Modifier) {
    Column(modifier.background(PanelBg).border(1.dp, PanelBorder).verticalScroll(rememberScrollState()).padding(8.dp)) {
        Title("Fundo (chroma key)")
        Row {
            XpRadioButton(settings.chromaKeyEnabled, { onSettingsChange { it.copy(chromaKeyEnabled = true) } }, "Ativo")
            Spacer(Modifier.width(8.dp))
            XpRadioButton(!settings.chromaKeyEnabled, { onSettingsChange { it.copy(chromaKeyEnabled = false) } }, "Off")
        }
        Box(Modifier.size(28.dp).background(settings.chromaKeyColor).border(1.dp, Color.White))
        ChromaMode.entries.forEach { m -> XpRadioButton(settings.chromaMode == m, { onSettingsChange { it.copy(chromaMode = m) } }, m.displayName) }
        XpSlider("Tolerância", settings.chromaTolerance, { v -> onSettingsChange { it.copy(chromaTolerance = v) } }, 0f..100f)
        XpSlider("Matiz ±", settings.chromaHueTolerance, { v -> onSettingsChange { it.copy(chromaHueTolerance = v) } }, 0f..90f)
        XpSlider("Sat. mín.", settings.chromaSatMin, { v -> onSettingsChange { it.copy(chromaSatMin = v) } }, 0f..1f, { "%.2f".format(it) })
        XpSlider("Brilho mín.", settings.chromaValMin, { v -> onSettingsChange { it.copy(chromaValMin = v) } }, 0f..1f, { "%.2f".format(it) })
        XpSlider("Suavização", settings.chromaFeather, { v -> onSettingsChange { it.copy(chromaFeather = v) } }, 0f..30f)
        XpRadioButton(settings.chromaDespill, { onSettingsChange { it.copy(chromaDespill = !it.chromaDespill) } }, "Vazamento de verde")
        Title("Aprimoramento")
        XpSlider("Realce de bordas", settings.edgeEnhance, { v -> onSettingsChange { it.copy(edgeEnhance = v) } }, 0f..200f)
        XpSlider("Contraste", settings.contrast, { v -> onSettingsChange { it.copy(contrast = v) } }, -100f..100f)
        XpSlider("Brilho", settings.brightness, { v -> onSettingsChange { it.copy(brightness = v) } }, -100f..100f)
        Title("Resolução 1:1")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(16, 32, 48, 64, 96, 128, 256).forEach { r ->
                Chip("$r", settings.targetResolution == r) { onSettingsChange { it.copy(targetResolution = r) } }
            }
        }
        Title("Escala de pixel")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(1, 2, 4, 8, 16).forEach { s ->
                Chip("${s}x", settings.displayScale == s) { onSettingsChange { it.copy(displayScale = s) } }
            }
        }
        Title("Paleta")
        PalettePreset.entries.forEach { p -> XpRadioButton(settings.palettePreset == p, { onSettingsChange { it.copy(palettePreset = p) } }, p.displayName) }
        if (settings.palettePreset == PalettePreset.AUTO_EXTRACT) {
            XpSlider("Máx. cores", settings.maxAutoColors.toFloat(), { v -> onSettingsChange { it.copy(maxAutoColors = v.toInt().coerceIn(2, 64)) } }, 2f..64f)
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            settings.palettePreset.colors.take(16).forEach { c -> Box(Modifier.size(16.dp).background(c).border(0.5.dp, Color.Black)) }
        }
        Title("Dithering")
        DitherMode.entries.forEach { d -> XpRadioButton(settings.ditherMode == d, { onSettingsChange { it.copy(ditherMode = d) } }, d.displayName) }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun CenterPanel(
    original: Bitmap?, converted: Bitmap?, loading: Boolean, idx: Int, count: Int,
    playing: Boolean, fps: Int, start: Int, end: Int,
    onToggle: () -> Unit, onFps: (Int) -> Unit, onInterval: (Int, Int) -> Unit, modifier: Modifier
) {
    Column(modifier.background(PanelBg).border(1.dp, PanelBorder).padding(8.dp)) {
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FrameBox("ORIGINAL", original, false, Modifier.weight(1f).fillMaxHeight())
            FrameBox("PIXEL ART", converted, loading, Modifier.weight(1f).fillMaxHeight())
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            XpButton(if (playing) "Pause" else "Play", onToggle, isPrimaryGreen = !playing, modifier = Modifier.width(88.dp))
            Text("${(idx + 1).coerceAtLeast(1)}/${count.coerceAtLeast(1)}", color = XpDarkText, fontFamily = VT323FontFamily, fontSize = 20.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("FPS", color = XpDarkTextMuted, fontFamily = PixelifyFontFamily, fontSize = 12.sp)
                XpButton("−", { onFps((fps - 1).coerceAtLeast(1)) }, modifier = Modifier.width(36.dp))
                Text("$fps", color = XpDarkText, fontFamily = VT323FontFamily, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 6.dp))
                XpButton("+", { onFps((fps + 1).coerceAtMost(30)) }, modifier = Modifier.width(36.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Início", color = XpDarkTextMuted, fontFamily = PixelifyFontFamily, fontSize = 12.sp)
            XpButton("−", { onInterval((start - 1).coerceAtLeast(1), end) }, modifier = Modifier.width(36.dp))
            Text("$start", color = XpDarkText, fontFamily = VT323FontFamily, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 4.dp))
            XpButton("+", { onInterval(start + 1, end) }, modifier = Modifier.width(36.dp))
            Spacer(Modifier.width(12.dp))
            Text("Fim", color = XpDarkTextMuted, fontFamily = PixelifyFontFamily, fontSize = 12.sp)
            XpButton("−", { onInterval(start, (end - 1).coerceAtLeast(start)) }, modifier = Modifier.width(36.dp))
            Text("$end", color = XpDarkText, fontFamily = VT323FontFamily, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 4.dp))
            XpButton("+", { onInterval(start, end + 1) }, modifier = Modifier.width(36.dp))
        }
    }
}

@Composable
private fun FrameBox(title: String, bmp: Bitmap?, loading: Boolean, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = XpTextWhite, fontFamily = PixelifyFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(Modifier.height(4.dp))
        Box(Modifier.fillMaxSize().clip(RoundedCornerShape(4.dp)).background(XpDarkInnerDark).border(2.dp, Color(0xFF404046), RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
            when {
                loading -> Text("Convertendo…", color = XpDarkTextMuted, fontFamily = PixelifyFontFamily)
                bmp != null -> Image(bmp.asImageBitmap(), title, Modifier.fillMaxSize().padding(4.dp), contentScale = ContentScale.Fit)
                else -> Text("Sem quadro", color = XpDarkTextMuted, fontFamily = PixelifyFontFamily, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun RightPanel(
    frames: List<Bitmap>, idx: Int, onSelect: (Int) -> Unit, settings: PixelArtSettings,
    onSheet: () -> Unit, onZip: () -> Unit, exporting: Boolean, onAnalyzer: () -> Unit, modifier: Modifier
) {
    Column(modifier.background(PanelBg).border(1.dp, PanelBorder).padding(8.dp)) {
        Title("Quadros")
        if (frames.isEmpty()) Text("Importe um vídeo", color = XpDarkTextMuted, fontFamily = PixelifyFontFamily, fontSize = 12.sp)
        else LazyVerticalGrid(GridCells.Adaptive(56.dp), Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            itemsIndexed(frames) { i, bmp ->
                Box(Modifier.size(56.dp).border(2.dp, if (i == idx) Color(0xFFFFE066) else Color(0xFF555555)).clickable { onSelect(i) }.background(XpDarkInnerDark), contentAlignment = Alignment.Center) {
                    Image(bmp.asImageBitmap(), "Quadro ${i + 1}", Modifier.fillMaxSize().padding(2.dp), contentScale = ContentScale.Fit)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Title("Paleta ativa")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            val colors = settings.palettePreset.colors
            if (colors.isEmpty()) Text("Auto", color = XpDarkTextMuted, fontFamily = PixelifyFontFamily, fontSize = 11.sp)
            else colors.take(16).forEach { c -> Box(Modifier.size(14.dp).background(c).border(0.5.dp, Color.Black)) }
        }
        Spacer(Modifier.height(10.dp))
        Title("Exportar")
        XpButton(if (exporting) "Exportando…" else "Baixar Sprite Sheet", onSheet, enabled = !exporting && frames.isNotEmpty(), isPrimaryGreen = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(6.dp))
        XpButton("Baixar PNGs (ZIP)", onZip, enabled = !exporting && frames.isNotEmpty(), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(6.dp))
        XpButton("Analisador", onAnalyzer, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(6.dp))
        XpButton("Automator (em breve)", {}, enabled = false, modifier = Modifier.fillMaxWidth())
    }
}

@Composable private fun Title(t: String) {
    Text(t, color = LunaTitleEnd, fontFamily = PixelifyFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(bottom = 4.dp, top = 2.dp))
}

@Composable private fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(Modifier.clip(RoundedCornerShape(3.dp)).background(if (selected) ChipSelected else ChipBg).border(1.dp, if (selected) LunaTitleEnd else Color(0xFF555555), RoundedCornerShape(3.dp)).clickable(onClick = onClick).padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(text, color = XpTextWhite, fontFamily = PixelifyFontFamily, fontSize = 12.sp)
    }
}
