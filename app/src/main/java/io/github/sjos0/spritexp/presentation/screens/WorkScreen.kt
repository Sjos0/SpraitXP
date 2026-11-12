package io.github.sjos0.spritexp.presentation.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import io.github.sjos0.spritexp.ui.theme.PixelifyFontFamily
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sjos0.spritexp.domain.model.ChromaMode
import io.github.sjos0.spritexp.domain.model.DitherMode
import io.github.sjos0.spritexp.domain.model.PalettePreset
import io.github.sjos0.spritexp.domain.model.PixelArtSettings
import io.github.sjos0.spritexp.presentation.components.XpButton
import io.github.sjos0.spritexp.presentation.components.XpGroupBox
import io.github.sjos0.spritexp.presentation.components.XpRadioButton
import io.github.sjos0.spritexp.presentation.components.XpSlider
import io.github.sjos0.spritexp.presentation.components.XpSegmentedProgressBar

private val DarkBg = Color(0xFF1A1A1E)
private val PanelBg = Color(0xFF2A2A2E)
private val GreenFrame = Color(0xFF388E3C)
private val Gold = Color(0xFFFFE066)
private val Muted = Color(0xFFBBBBBB)
/** Texto de apoio sobre superfícies claras (GroupBox): cinza escuro para contraste. */
private val HintOnLight = Color(0xFF4A4A4A)

private enum class WorkTab(val label: String, val icon: ImageVector) {
    AJUSTES("Ajustes", Icons.Filled.Tune),
    ANIMACAO("Animar", Icons.Filled.Timer),
    PALETA("Paleta", Icons.Filled.Palette),
    EXPORTAR("Exportar", Icons.Filled.Download),
    ANALISADOR("Sheet", Icons.Filled.GridOn)
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
    sourceFrameCount: Int,
    isImporting: Boolean,
    importProgress: Float,
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
    onShowAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tab by remember { mutableStateOf(WorkTab.AJUSTES) }
    var menuOpen by remember { mutableStateOf(false) }
    var picking by remember { mutableStateOf(false) }
    var enlargeOpen by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().background(DarkBg)) {
        TopBar(
            menuOpen = menuOpen,
            onToggleMenu = { menuOpen = !menuOpen }
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

        // Área fixa: importação, prévias, play e linha do tempo ficam sempre visíveis.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            XpButton(
                text = if (isImporting) "Importando… ${(importProgress * 100).toInt()}%" else "Importar vídeo",
                onClick = onPickVideo,
                isPrimaryGreen = true,
                enabled = !isImporting,
                modifier = Modifier.fillMaxWidth(),
                testTag = "work_import_video"
            )
            if (isImporting) {
                XpSegmentedProgressBar(progress = importProgress, modifier = Modifier.fillMaxWidth())
            }

            Preview(
                originalFrame = originalFrame,
                convertedFrame = convertedFrame,
                isConverting = isConverting,
                picking = picking,
                onPickOriginal = { c ->
                    picking = false
                    onSettingsChange { it.copy(chromaKeyColor = c) }
                },
                onEnlarge = { enlargeOpen = true }
            )

            PlaybackBar(
                isPlaying = isPlaying,
                currentIndex = currentFrameIndex,
                frameCount = frameCount,
                onTogglePlay = onTogglePlay
            )

            Timeline(frames = frames, current = currentFrameIndex, onSelect = onSelectFrame)
        }

        ToolDock(selected = tab, onSelect = { tab = it })

        // Só o painel da ferramenta rola; prévias e play continuam no topo, sem voltar a tela.
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (tab) {
                WorkTab.AJUSTES -> AdjustmentsPanel(
                    settings = settings,
                    onSettingsChange = onSettingsChange,
                    picking = picking,
                    onTogglePicking = { picking = !picking }
                )
                WorkTab.ANIMACAO -> AnimationPanel(
                    sourceFrameCount = sourceFrameCount,
                    startFrame = startFrame,
                    endFrame = endFrame,
                    fps = fps,
                    onIntervalChange = onIntervalChange,
                    onFpsChange = onFpsChange
                )
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

    if (enlargeOpen) {
        EnlargeDialog(
            frames = if (frames.isNotEmpty()) frames else listOfNotNull(convertedFrame),
            fps = fps,
            onDismiss = { enlargeOpen = false }
        )
    }
}

/** Ampliação em tela cheia: só o sprite, em fundo quadriculado para julgar a transparência. */
@Composable
private fun EnlargeDialog(frames: List<Bitmap>, fps: Int, onDismiss: () -> Unit) {
    var index by remember { mutableStateOf(0) }
    var playing by remember { mutableStateOf(false) }

    // A animação existe só enquanto o diálogo está aberto: ao fechar, a LaunchedEffect é cancelada.
    LaunchedEffect(playing, frames.size, fps) {
        if (playing && frames.size > 1) {
            while (true) {
                delay(1000L / fps.coerceAtLeast(1))
                index = (index + 1) % frames.size
            }
        }
    }

    val current = frames.getOrNull(index.coerceIn(0, maxOf(0, frames.size - 1)))
    val counter = if (frames.isEmpty()) "0/0" else "${index.coerceIn(0, frames.size - 1) + 1}/${frames.size}"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize().checkerboard()) {
            if (current != null) {
                Image(
                    bitmap = current.asImageBitmap(),
                    contentDescription = "Sprite ampliado",
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentScale = ContentScale.Fit,
                    filterQuality = FilterQuality.None
                )
            }
            XpButton(
                text = "Fechar",
                onClick = onDismiss,
                isDangerOrange = true,
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                testTag = "enlarge_close"
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color(0xCC000000))
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                XpButton(
                    text = if (playing) "Pausar" else "Play",
                    onClick = { playing = !playing },
                    isPrimaryGreen = true,
                    modifier = Modifier.width(140.dp),
                    testTag = "enlarge_play"
                )
                Text(text = "Quadro $counter", color = Color.White, fontSize = 13.sp)
            }
        }
    }
}

/** Fundo xadrez cinza, para ver áreas transparentes do sprite. */
private fun Modifier.checkerboard(): Modifier = this.drawBehind {
    val cell = 16.dp.toPx()
    val cols = (size.width / cell).toInt() + 1
    val rows = (size.height / cell).toInt() + 1
    for (row in 0 until rows) {
        for (col in 0 until cols) {
            drawRect(
                color = if ((row + col) % 2 == 0) Color(0xFF3A3A3A) else Color(0xFF2A2A2A),
                topLeft = Offset(col * cell, row * cell),
                size = Size(cell, cell)
            )
        }
    }
}

@Composable
private fun TopBar(menuOpen: Boolean, onToggleMenu: () -> Unit) {
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
            text = if (menuOpen) "✕" else "☰",
            onClick = onToggleMenu,
            modifier = Modifier.width(48.dp),
            testTag = "work_menu_button"
        )
    }
}

@Composable
private fun Preview(
    originalFrame: Bitmap?,
    convertedFrame: Bitmap?,
    isConverting: Boolean,
    picking: Boolean,
    onPickOriginal: (Color) -> Unit,
    onEnlarge: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PreviewPanel(
            title = "ORIGINAL",
            bitmap = originalFrame,
            loading = false,
            picking = picking,
            onPick = onPickOriginal,
            modifier = Modifier.weight(1f)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PreviewPanel(
                title = "PIXEL ART",
                bitmap = convertedFrame,
                loading = isConverting,
                picking = false,
                onPick = null,
                modifier = Modifier.fillMaxWidth()
            )
            XpButton(
                text = "Ampliar",
                onClick = onEnlarge,
                enabled = convertedFrame != null,
                modifier = Modifier.fillMaxWidth(),
                testTag = "work_enlarge"
            )
        }
    }
}

@Composable
private fun PreviewPanel(
    title: String,
    bitmap: Bitmap?,
    loading: Boolean,
    picking: Boolean,
    onPick: ((Color) -> Unit)?,
    modifier: Modifier
) {
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (picking) "$title (toque para escolher a cor)" else title,
            color = if (picking) Gold else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        val tapModifier = if (picking && onPick != null && bitmap != null) {
            Modifier.pointerInput(bitmap) {
                detectTapGestures { offset ->
                    val pickedColor = pixelAt(bitmap, offset, boxSize)
                    if (pickedColor != null) onPick(pickedColor)
                }
            }
        } else {
            Modifier
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .onSizeChanged { boxSize = it }
                .then(tapModifier)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black)
                .border(
                    width = if (picking) 3.dp else 2.dp,
                    color = if (picking) Gold else Color.White,
                    shape = RoundedCornerShape(4.dp)
                ),
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
                else -> Text(text = "Importe um vídeo", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

/**
 * Converte o toque na caixa da prévia para a coordenada do bitmap (ContentScale.Fit, centralizado)
 * e devolve a cor do pixel. Retorna null se o toque cair fora da imagem.
 */
private fun pixelAt(bitmap: Bitmap, offset: Offset, box: IntSize): Color? {
    if (box.width == 0 || box.height == 0) return null
    val bw = bitmap.width.toFloat()
    val bh = bitmap.height.toFloat()
    val scale = minOf(box.width / bw, box.height / bh)
    val offX = (box.width - bw * scale) / 2f
    val offY = (box.height - bh * scale) / 2f
    val px = ((offset.x - offX) / scale).toInt()
    val py = ((offset.y - offY) / scale).toInt()
    if (px !in 0 until bitmap.width || py !in 0 until bitmap.height) return null
    return Color(bitmap.getPixel(px, py))
}

@Composable
private fun PlaybackBar(
    isPlaying: Boolean,
    currentIndex: Int,
    frameCount: Int,
    onTogglePlay: () -> Unit
) {
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
}

/** Intervalo e velocidade da animação. Mudanças aparecem nas prévias do topo sem reimportar. */
@Composable
private fun AnimationPanel(
    sourceFrameCount: Int,
    startFrame: Int,
    endFrame: Int,
    fps: Int,
    onIntervalChange: (Int, Int) -> Unit,
    onFpsChange: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        XpGroupBox(title = "Animação") {
            if (sourceFrameCount > 1) {
                XpSlider(
                    label = "Início",
                    value = startFrame.toFloat(),
                    onValueChange = { v -> onIntervalChange(v.toInt(), maxOf(v.toInt(), endFrame)) },
                    valueRange = 1f..sourceFrameCount.toFloat(),
                    displayFormatter = { "${it.toInt()}" }
                )
                XpSlider(
                    label = "Fim",
                    value = endFrame.toFloat(),
                    onValueChange = { v -> onIntervalChange(startFrame, maxOf(v.toInt(), startFrame)) },
                    valueRange = 1f..sourceFrameCount.toFloat(),
                    displayFormatter = { "${it.toInt()}" }
                )
            } else {
                Text(text = "Importe um vídeo para escolher o intervalo.", color = HintOnLight, fontSize = 12.sp)
            }
            XpSlider(
                label = "FPS",
                value = fps.toFloat(),
                onValueChange = { onFpsChange(it.toInt()) },
                valueRange = 1f..60f,
                displayFormatter = { "${it.toInt()} fps" }
            )
        }
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
                    .size(48.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.Black)
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

/** Dock de ícones: cada ferramenta vira uma caixinha, sem texto cortado. */
@Composable
private fun ToolDock(selected: WorkTab, onSelect: (WorkTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PanelBg)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        WorkTab.values().forEach { tool ->
            val isSelected = tool == selected
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isSelected) GreenFrame else Color(0xFF3A3A40))
                    .border(1.dp, if (isSelected) Gold else Color.Black, RoundedCornerShape(4.dp))
                    .clickable { onSelect(tool) }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.label,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = tool.label,
                    color = Color.White,
                    fontFamily = PixelifyFontFamily,
                    fontSize = 10.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip
                )
            }
        }
    }
}

@Composable
private fun AdjustmentsPanel(
    settings: PixelArtSettings,
    onSettingsChange: ((PixelArtSettings) -> PixelArtSettings) -> Unit,
    picking: Boolean,
    onTogglePicking: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        XpGroupBox(title = "Remoção de fundo") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(settings.chromaKeyColor)
                        .border(2.dp, Color.Black)
                )
                XpButton(
                    text = if (picking) "Cancelar conta-gotas" else "Conta-gotas",
                    onClick = onTogglePicking,
                    isPrimaryGreen = picking,
                    modifier = Modifier.weight(1f),
                    testTag = "work_eyedropper"
                )
            }
            Text(
                text = if (picking) "Toque na imagem ORIGINAL no pixel do fundo." else "Escolha a cor do fundo tocando na imagem.",
                color = HintOnLight,
                fontSize = 11.sp
            )
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
                color = HintOnLight,
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
            // Auto só acrescenta o slider de máximo de cores; as outras paletas continuam listadas.
            if (isAuto) {
                XpSlider(
                    label = "Máx. de cores",
                    value = settings.maxAutoColors.toFloat(),
                    onValueChange = { v -> onSettingsChange { it.copy(maxAutoColors = v.toInt().coerceIn(2, 64)) } },
                    valueRange = 2f..64f,
                    displayFormatter = { "${it.toInt()}" }
                )
            }
            PalettePreset.values().filter { it != PalettePreset.AUTO_EXTRACT }.forEach { p ->
                XpRadioButton(
                    selected = settings.palettePreset == p,
                    onClick = { onSettingsChange { it.copy(palettePreset = p) } },
                    label = p.displayName
                )
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
                text = "Baixar ZIP completo",
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
                Text(text = "Importe um vídeo antes de exportar.", color = HintOnLight, fontSize = 12.sp)
            }
        }
        XpGroupBox(title = "Automator") {
            Text(text = "Em breve", color = HintOnLight, fontSize = 12.sp)
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
                        .background(Color.Black)
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
                Text(text = "Abra uma sprite sheet 1:1 para analisar.", color = HintOnLight, fontSize = 12.sp)
            }
        }
    }
}
