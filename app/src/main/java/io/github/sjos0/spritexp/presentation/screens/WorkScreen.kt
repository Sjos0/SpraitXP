package io.github.sjos0.spritexp.presentation.screens

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.TextStyle
import java.util.Locale
import java.util.Date
import java.text.SimpleDateFormat
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import io.github.sjos0.spritexp.domain.model.SettingsPreset
import io.github.sjos0.spritexp.presentation.components.XpCollapsibleGroupBox
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
import io.github.sjos0.spritexp.presentation.components.XpRadioButton
import io.github.sjos0.spritexp.presentation.components.XpSlider
import io.github.sjos0.spritexp.presentation.components.XpSegmentedProgressBar

private val GreenFrame = Color(0xFF388E3C)
private val Gold = Color(0xFFFFE066)
/** Texto de apoio sobre superfícies claras (GroupBox): cinza escuro para contraste. */
private val HintOnLight = Color(0xFF4A4A4A)

/** Cores da tela de trabalho por tema. O escuro mantém o visual XP atual. */
private data class WorkColors(
    val background: Color,
    val panel: Color,
    val text: Color,
    val muted: Color,
    val toolIdle: Color
)

private val DarkWorkColors = WorkColors(
    background = Color(0xFF1A1A1E),
    panel = Color(0xFF2A2A2E),
    text = Color.White,
    muted = Color(0xFFD0D0D0),
    toolIdle = Color(0xFF3A3A40)
)

private val LightWorkColors = WorkColors(
    background = Color(0xFFF2F0E6),
    panel = Color(0xFFE2DED0),
    text = Color(0xFF1A1A1E),
    muted = Color(0xFF4A4A4A),
    toolIdle = Color(0xFFD6D2C4)
)

private val LocalWorkColors = staticCompositionLocalOf { DarkWorkColors }

/** Paleta da tela de trabalho para o tema atual. */
private val workColors: WorkColors
    @Composable get() = LocalWorkColors.current

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
    onPickMedia: () -> Unit,
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
    isDarkTheme: Boolean,
    presets: List<SettingsPreset>,
    onSavePreset: (String) -> Unit,
    onOverwritePreset: (String) -> Unit,
    onDeletePreset: (String) -> Unit,
    onApplyPreset: (String) -> Unit,
    onShowAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(LocalWorkColors provides if (isDarkTheme) DarkWorkColors else LightWorkColors) {
    var tab by remember { mutableStateOf(WorkTab.AJUSTES) }
    var menuOpen by remember { mutableStateOf(false) }
    var picking by remember { mutableStateOf(false) }
    var enlargeOpen by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().background(workColors.background)) {
        TopBar(
            menuOpen = menuOpen,
            onToggleMenu = { menuOpen = !menuOpen }
        )

        if (menuOpen) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(workColors.panel)
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
            if (isImporting) {
                Text(
                    text = "Importando… ${(importProgress * 100).toInt()}%",
                    color = workColors.text,
                    fontFamily = PixelifyFontFamily,
                    fontSize = 12.sp
                )
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
                onEnlarge = { enlargeOpen = true },
                onImportMedia = onPickMedia
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
                WorkTab.AJUSTES -> {
                    AdjustmentsPanel(
                        settings = settings,
                        onSettingsChange = onSettingsChange,
                        picking = picking,
                        onTogglePicking = { picking = !picking }
                    )
                    PresetsSection(
                        presets = presets,
                        onSave = onSavePreset,
                        onOverwrite = onOverwritePreset,
                        onDelete = onDeletePreset,
                        onApply = onApplyPreset
                    )
                }
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
                Text(fontFamily = PixelifyFontFamily, text = "Quadro $counter", color = Color.White, fontSize = 13.sp)
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

/** Botão Presets: abre a janela de salvar, carregar, sobrescrever e excluir. */
@Composable
private fun PresetsSection(
    presets: List<SettingsPreset>,
    onSave: (String) -> Unit,
    onOverwrite: (String) -> Unit,
    onDelete: (String) -> Unit,
    onApply: (String) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    XpButton(
        text = "Presets…",
        onClick = { open = true },
        modifier = Modifier.fillMaxWidth(),
        testTag = "work_presets"
    )
    if (open) {
        PresetsDialog(
            presets = presets,
            onSave = onSave,
            onOverwrite = onOverwrite,
            onDelete = onDelete,
            onApply = onApply,
            onClose = { open = false }
        )
    }
}

private enum class PresetAction { OVERWRITE, DELETE }

@Composable
private fun PresetsDialog(
    presets: List<SettingsPreset>,
    onSave: (String) -> Unit,
    onOverwrite: (String) -> Unit,
    onDelete: (String) -> Unit,
    onApply: (String) -> Unit,
    onClose: () -> Unit
) {
    var newName by remember { mutableStateOf("") }
    var pending by remember { mutableStateOf<Pair<PresetAction, SettingsPreset>?>(null) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onClose,
        containerColor = Color(0xFFECE9D8),
        titleContentColor = Color.Black,
        textContentColor = Color.Black,
        title = { Text(text = "Presets", fontFamily = PixelifyFontFamily, color = Color(0xFF0A246A)) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 440.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it.take(40) },
                    singleLine = true,
                    label = { Text(text = "Nome do novo preset", fontFamily = PixelifyFontFamily) },
                    // Cores explícitas: o dialog é bege e o tema da Activity é escuro (texto claro = invisível).
                    textStyle = TextStyle(color = Color.Black, fontFamily = PixelifyFontFamily, fontSize = 14.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black,
                        focusedBorderColor = Color(0xFF0A246A),
                        unfocusedBorderColor = Color(0xFF808080),
                        focusedLabelColor = Color(0xFF0A246A),
                        unfocusedLabelColor = Color(0xFF4A4A4A),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                XpButton(
                    text = "Salvar novo",
                    onClick = { onSave(newName); newName = "" },
                    enabled = newName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (presets.isEmpty()) {
                    Text(
                        text = "Nenhum preset salvo ainda.",
                        color = HintOnLight,
                        fontFamily = PixelifyFontFamily,
                        fontSize = 12.sp
                    )
                }
                presets.forEach { preset ->
                    Column(
                        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF808080)).padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = preset.name, color = Color.Black, fontFamily = PixelifyFontFamily, fontSize = 14.sp)
                        Text(
                            text = "Salvo em ${dateFormat.format(Date(preset.createdAt))} · ${preset.videoFps} fps",
                            color = HintOnLight,
                            fontFamily = PixelifyFontFamily,
                            fontSize = 11.sp
                        )
                        // Um botão por linha: o nome completo cabe, sem "Car..." nem "Sobr...".
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            XpButton(
                                text = "Carregar",
                                onClick = { onApply(preset.id); onClose() },
                                modifier = Modifier.fillMaxWidth()
                            )
                            XpButton(
                                text = "Sobrescrever",
                                onClick = { pending = PresetAction.OVERWRITE to preset },
                                modifier = Modifier.fillMaxWidth()
                            )
                            XpButton(
                                text = "Excluir",
                                onClick = { pending = PresetAction.DELETE to preset },
                                isDangerOrange = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text("Fechar") } }
    )

    pending?.let { (action, preset) ->
        val isDelete = action == PresetAction.DELETE
        AlertDialog(
            onDismissRequest = { pending = null },
            containerColor = Color(0xFFECE9D8),
            titleContentColor = Color.Black,
            textContentColor = Color.Black,
            title = { Text("Tem certeza?") },
            text = {
                Text(
                    if (isDelete) "O preset \"${preset.name}\" será excluído."
                    else "O preset \"${preset.name}\" será substituído pelos ajustes atuais."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (isDelete) onDelete(preset.id) else onOverwrite(preset.id)
                    pending = null
                }) { Text(if (isDelete) "Excluir" else "Sobrescrever") }
            },
            dismissButton = { TextButton(onClick = { pending = null }) { Text("Cancelar") } }
        )
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
        Text(fontFamily = PixelifyFontFamily, 
            text = "SpriteXP",
            color = workColors.text,
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
    onEnlarge: () -> Unit,
    onImportMedia: () -> Unit
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
            onImportClick = onImportMedia,
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
    onImportClick: (() -> Unit)? = null,
    modifier: Modifier
) {
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(fontFamily = PixelifyFontFamily, 
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
        val importModifier = if (!picking && onImportClick != null) {
            Modifier.clickable(enabled = !loading) { onImportClick() }
        } else {
            Modifier
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .onSizeChanged { boxSize = it }
                .then(tapModifier)
                .then(importModifier)
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
                loading -> Text(fontFamily = PixelifyFontFamily, text = "Convertendo...", color = Color.White, fontSize = 12.sp)
                bitmap != null -> Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                else -> Text(
                    fontFamily = PixelifyFontFamily,
                    text = if (onImportClick != null) "Toque para importar\nimagem ou vídeo" else "Aguardando a original",
                    color = Color.White,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
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
        Text(fontFamily = PixelifyFontFamily, 
            text = "Quadro ${currentIndex + 1}/$frameCount",
            color = workColors.text,
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
        XpCollapsibleGroupBox(title = "Animação") {
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
                Text(fontFamily = PixelifyFontFamily, text = "Importe um vídeo para escolher o intervalo.", color = HintOnLight, fontSize = 12.sp)
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
        Text(fontFamily = PixelifyFontFamily, 
            text = "Importe uma imagem ou vídeo para ver os quadros.",
            color = workColors.muted,
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
            .background(workColors.panel)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        WorkTab.values().forEach { tool ->
            val isSelected = tool == selected
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isSelected) GreenFrame else workColors.toolIdle)
                    .border(1.dp, if (isSelected) Gold else Color.Black, RoundedCornerShape(4.dp))
                    .clickable { onSelect(tool) }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.label,
                    tint = if (isSelected) Color.White else workColors.text,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = tool.label,
                    color = if (isSelected) Color.White else workColors.text,
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
        XpCollapsibleGroupBox(title = "Remoção de fundo") {
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
            Text(fontFamily = PixelifyFontFamily, 
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

        XpCollapsibleGroupBox(title = "Aprimoramento") {
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

        XpCollapsibleGroupBox(title = "Resolução e escala") {
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
            Text(fontFamily = PixelifyFontFamily, 
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
        XpCollapsibleGroupBox(title = "Paleta de cores") {
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
                Text(fontFamily = PixelifyFontFamily, text = "Paleta ativa (${swatches.size})", color = Color.Black, fontSize = 12.sp)
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

        XpCollapsibleGroupBox(title = "Dithering") {
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
        XpCollapsibleGroupBox(title = "Exportar quadros") {
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
                        .background(workColors.panel)
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
                Text(fontFamily = PixelifyFontFamily, text = "Importe uma imagem ou vídeo antes de exportar.", color = HintOnLight, fontSize = 12.sp)
            }
        }
        XpCollapsibleGroupBox(title = "Automator") {
            Text(fontFamily = PixelifyFontFamily, text = "Em breve", color = HintOnLight, fontSize = 12.sp)
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
        XpCollapsibleGroupBox(title = "Analisador de sprite sheet") {
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
                Text(fontFamily = PixelifyFontFamily, text = "Grade: ${columns}x${rows} ($total quadros)", color = Color.Black, fontSize = 12.sp)
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
                Text(fontFamily = PixelifyFontFamily, text = "Abra uma sprite sheet 1:1 para analisar.", color = HintOnLight, fontSize = 12.sp)
            }
        }
    }
}
