package io.github.sjos0.spritexp.presentation.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import io.github.sjos0.spritexp.domain.model.ChromaMode
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import io.github.sjos0.spritexp.presentation.components.XpSlider
import io.github.sjos0.spritexp.presentation.components.XpWindow
import io.github.sjos0.spritexp.ui.theme.PixelifyFontFamily
import io.github.sjos0.spritexp.ui.theme.VT323FontFamily
import io.github.sjos0.spritexp.ui.theme.XpDarkPanel
import io.github.sjos0.spritexp.ui.theme.XpStartGreen
import io.github.sjos0.spritexp.ui.theme.XpTextBlack
import io.github.sjos0.spritexp.ui.theme.XpTextWhite

@Composable
fun ConverterScreen(
    originalBitmap: Bitmap?,
    convertedBitmap: Bitmap?,
    isConverting: Boolean,
    settings: PixelArtSettings,
    onSettingsChange: ((PixelArtSettings) -> PixelArtSettings) -> Unit,
    onImagePicked: (Bitmap) -> Unit,
    onExportClick: () -> Unit,
    onCloseClick: () -> Unit,
    frameCount: Int = 1,
    fps: Int = 8,
    onFpsChange: (Int) -> Unit = {},
    onQuickPlay: () -> Unit = {},
    onRenderPlay: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var zoom by remember { mutableFloatStateOf(1f) }
    var localFps by remember(fps) { mutableIntStateOf(fps.coerceIn(1, 30)) }
    var pickingExtra by remember { mutableStateOf(false) }
    var originalViewSize by remember { mutableStateOf(IntSize.Zero) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val src = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(src) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                onImagePicked(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val resolutionChips = listOf(16, 32, 48, 64, 96, 128, 256)
    val scaleChips = listOf(1, 2, 4, 8, 16)
    val outputW = settings.targetResolution * settings.displayScale
    val outputH = settings.targetResolution * settings.displayScale
    val previewSize = (130 * zoom).dp.coerceIn(80.dp, 220.dp)

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        XpWindow(
            title = "Conversor",
            icon = {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = Color.White
                )
            },
            onCloseClick = onCloseClick,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Painel ORIGINAL vs PIXEL ART + zoom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(XpDarkPanel)
                        .border(2.dp, Color(0xFF1A1A1A), RoundedCornerShape(4.dp))
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            XpButton(
                                text = "−",
                                onClick = { zoom = (zoom - 0.25f).coerceAtLeast(0.5f) },
                                modifier = Modifier.width(48.dp),
                                testTag = "converter_zoom_out"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${(zoom * 100).toInt()}%",
                                color = XpTextWhite,
                                fontFamily = VT323FontFamily,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            XpButton(
                                text = "+",
                                onClick = { zoom = (zoom + 0.25f).coerceAtMost(2f) },
                                modifier = Modifier.width(48.dp),
                                testTag = "converter_zoom_in"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            XpButton(
                                text = "Reset",
                                onClick = { zoom = 1f },
                                modifier = Modifier.width(72.dp),
                                testTag = "converter_zoom_reset"
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "ORIGINAL",
                                    color = XpTextWhite,
                                    fontFamily = PixelifyFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(previewSize)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xFF388E3C))
                                        .border(2.dp, if (pickingExtra) Color.Yellow else Color.White, RoundedCornerShape(3.dp))
                                        .onSizeChanged { originalViewSize = it }
                                        .pointerInput(originalBitmap, originalViewSize, pickingExtra) {
                                            detectTapGestures { offset: Offset ->
                                                val bmp = originalBitmap ?: return@detectTapGestures
                                                val vw = originalViewSize.width.toFloat().coerceAtLeast(1f)
                                                val vh = originalViewSize.height.toFloat().coerceAtLeast(1f)
                                                val bw = bmp.width.toFloat()
                                                val bh = bmp.height.toFloat()
                                                val scale = minOf(vw / bw, vh / bh)
                                                val dispW = bw * scale
                                                val dispH = bh * scale
                                                val left = (vw - dispW) / 2f
                                                val top = (vh - dispH) / 2f
                                                val lx = offset.x - left
                                                val ly = offset.y - top
                                                if (lx < 0f || ly < 0f || lx >= dispW || ly >= dispH) return@detectTapGestures
                                                val px = (lx / scale).toInt().coerceIn(0, bmp.width - 1)
                                                val py = (ly / scale).toInt().coerceIn(0, bmp.height - 1)
                                                val argb = bmp.getPixel(px, py)
                                                val picked = Color(argb)
                                                if (pickingExtra) {
                                                    onSettingsChange { s ->
                                                        val list = s.extraChromaColors.toMutableList()
                                                        if (list.size < 3) list.add(picked)
                                                        s.copy(extraChromaColors = list)
                                                    }
                                                    pickingExtra = false
                                                } else {
                                                    onSettingsChange { it.copy(chromaKeyColor = picked) }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (originalBitmap != null) {
                                        Image(
                                            bitmap = originalBitmap.asImageBitmap(),
                                            contentDescription = "Original",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "PIXEL ART",
                                    color = XpTextWhite,
                                    fontFamily = PixelifyFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(previewSize)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xFF388E3C))
                                        .border(2.dp, Color.White, RoundedCornerShape(3.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isConverting) {
                                        CircularProgressIndicator(
                                            color = XpStartGreen,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    } else if (convertedBitmap != null) {
                                        Image(
                                            bitmap = convertedBitmap.asImageBitmap(),
                                            contentDescription = "Pixel Art",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Play / Render & Play / FPS
                XpGroupBox(title = "Prévia e render") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        XpButton(
                            text = "Quick Play ($frameCount)",
                            onClick = onQuickPlay,
                            modifier = Modifier.weight(1f),
                            testTag = "converter_quick_play"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "FPS",
                                fontFamily = PixelifyFontFamily,
                                fontSize = 12.sp,
                                color = XpTextBlack
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                XpButton(
                                    text = "−",
                                    onClick = {
                                        val next = (localFps - 1).coerceAtLeast(1)
                                        localFps = next
                                        onFpsChange(next)
                                    },
                                    modifier = Modifier.width(40.dp),
                                    testTag = "converter_fps_minus"
                                )
                                Text(
                                    text = "$localFps",
                                    fontFamily = VT323FontFamily,
                                    fontSize = 20.sp,
                                    color = XpTextBlack,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                                XpButton(
                                    text = "+",
                                    onClick = {
                                        val next = (localFps + 1).coerceAtMost(30)
                                        localFps = next
                                        onFpsChange(next)
                                    },
                                    modifier = Modifier.width(40.dp),
                                    testTag = "converter_fps_plus"
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        XpButton(
                            text = if (isConverting) "Render…" else "Render & Play",
                            onClick = onRenderPlay,
                            isPrimaryGreen = true,
                            modifier = Modifier.weight(1f),
                            testTag = "converter_render_play"
                        )
                    }
                    if (isConverting) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Progresso: convertendo…",
                            fontFamily = PixelifyFontFamily,
                            fontSize = 12.sp,
                            color = Color(0xFF555555)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Remoção de fundo (chroma key)
                XpGroupBox(title = "Remoção de fundo (Chroma Key)") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSettingsChange { it.copy(chromaKeyEnabled = !it.chromaKeyEnabled) }
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .border(2.dp, Color(0xFF7F9DB9))
                                .background(if (settings.chromaKeyEnabled) XpStartGreen else Color.White)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (settings.chromaKeyEnabled) "Ativado 🟩" else "Desativado ⬜",
                            fontFamily = PixelifyFontFamily,
                            fontSize = 14.sp,
                            color = XpTextBlack
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Cor chave:",
                            fontFamily = PixelifyFontFamily,
                            fontSize = 13.sp,
                            color = XpTextBlack
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(settings.chromaKeyColor)
                                .border(2.dp, Color.Black)
                                .testTag("converter_chroma_swatch")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (pickingExtra) "toque na ORIGINAL p/ cor extra" else "toque na ORIGINAL p/ escolher",
                            fontFamily = VT323FontFamily,
                            fontSize = 14.sp,
                            color = Color(0xFF555555)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Modo",
                        fontFamily = PixelifyFontFamily,
                        fontSize = 13.sp,
                        color = XpTextBlack
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        XpRadioButton(
                            selected = settings.chromaMode == ChromaMode.DISTANCE,
                            onClick = { onSettingsChange { it.copy(chromaMode = ChromaMode.DISTANCE) } },
                            label = "Distância"
                        )
                        XpRadioButton(
                            selected = settings.chromaMode == ChromaMode.HUE,
                            onClick = { onSettingsChange { it.copy(chromaMode = ChromaMode.HUE) } },
                            label = "Matiz"
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (settings.chromaMode == ChromaMode.DISTANCE) {
                        XpSlider(
                            label = "Tolerância",
                            value = settings.chromaTolerance,
                            onValueChange = { tol ->
                                onSettingsChange { it.copy(chromaTolerance = tol) }
                            },
                            valueRange = 0f..100f,
                            displayFormatter = { "${it.toInt()}%" }
                        )
                    } else {
                        XpSlider(
                            label = "Tolerância de matiz",
                            value = settings.chromaHueTolerance,
                            onValueChange = { v ->
                                onSettingsChange { it.copy(chromaHueTolerance = v) }
                            },
                            valueRange = 0f..90f,
                            displayFormatter = { "${it.toInt()}°" }
                        )
                        XpSlider(
                            label = "Saturação mínima",
                            value = settings.chromaSatMin,
                            onValueChange = { v ->
                                onSettingsChange { it.copy(chromaSatMin = v) }
                            },
                            valueRange = 0f..1f,
                            displayFormatter = { "%.2f".format(it) }
                        )
                        XpSlider(
                            label = "Brilho mínimo",
                            value = settings.chromaValMin,
                            onValueChange = { v ->
                                onSettingsChange { it.copy(chromaValMin = v) }
                            },
                            valueRange = 0f..1f,
                            displayFormatter = { "%.2f".format(it) }
                        )
                    }

                    XpSlider(
                        label = "Suavização da borda",
                        value = settings.chromaFeather,
                        onValueChange = { v ->
                            onSettingsChange { it.copy(chromaFeather = v) }
                        },
                        valueRange = 0f..30f,
                        displayFormatter = { "${it.toInt()}" }
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSettingsChange { it.copy(chromaDespill = !it.chromaDespill) }
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .border(2.dp, Color(0xFF7F9DB9))
                                .background(if (settings.chromaDespill) XpStartGreen else Color.White)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Remover vazamento de verde",
                            fontFamily = PixelifyFontFamily,
                            fontSize = 14.sp,
                            color = XpTextBlack
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Cores extras (${settings.extraChromaColors.size}/3)",
                        fontFamily = PixelifyFontFamily,
                        fontSize = 12.sp,
                        color = XpTextBlack
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        settings.extraChromaColors.forEachIndexed { idx, c ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(c)
                                    .border(1.dp, Color.Black)
                                    .clickable {
                                        onSettingsChange { s ->
                                            s.copy(extraChromaColors = s.extraChromaColors.filterIndexed { i, _ -> i != idx })
                                        }
                                    }
                            ) {
                                Text("X", color = Color.White, fontSize = 10.sp, modifier = Modifier.align(Alignment.Center))
                            }
                        }
                        if (settings.extraChromaColors.size < 3) {
                            XpButton(
                                text = "+ Adicionar cor",
                                onClick = { pickingExtra = true },
                                modifier = Modifier.height(32.dp),
                                testTag = "converter_add_chroma_color"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Aprimoramento
                XpGroupBox(title = "Aprimoramento") {
                    XpSlider(
                        label = "Realce de bordas",
                        value = settings.edgeEnhance,
                        onValueChange = { ee ->
                            onSettingsChange { it.copy(edgeEnhance = ee) }
                        },
                        valueRange = 0f..200f,
                        displayFormatter = { "${it.toInt()}%" }
                    )
                    XpSlider(
                        label = "Contraste",
                        value = settings.contrast,
                        onValueChange = { c ->
                            onSettingsChange { it.copy(contrast = c) }
                        },
                        valueRange = -100f..100f,
                        displayFormatter = { "${it.toInt()}" }
                    )
                    XpSlider(
                        label = "Brilho",
                        value = settings.brightness,
                        onValueChange = { b ->
                            onSettingsChange { it.copy(brightness = b) }
                        },
                        valueRange = -100f..100f,
                        displayFormatter = { "${it.toInt()}" }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    XpButton(
                        text = "Reset aprimoramentos",
                        onClick = {
                            onSettingsChange {
                                it.copy(edgeEnhance = 0f, contrast = 0f, brightness = 0f)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "converter_reset_enhancements"
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Resolução alvo (sempre 1:1)
                XpGroupBox(title = "Resolução alvo (1:1)") {
                    Text(
                        text = "Proporção travada em 1:1",
                        fontFamily = PixelifyFontFamily,
                        fontSize = 12.sp,
                        color = Color(0xFF555555)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    XpSlider(
                        label = "Lado (px)",
                        value = settings.targetResolution.toFloat(),
                        onValueChange = { res ->
                            onSettingsChange { it.copy(targetResolution = res.toInt().coerceIn(16, 256)) }
                        },
                        valueRange = 16f..256f,
                        displayFormatter = { "${it.toInt()} x ${it.toInt()}" }
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        resolutionChips.forEach { size ->
                            val selected = settings.targetResolution == size
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (selected) XpStartGreen else Color(0xFFECE9D8))
                                    .border(1.dp, Color.Black, RoundedCornerShape(3.dp))
                                    .clickable {
                                        onSettingsChange { it.copy(targetResolution = size) }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$size",
                                    fontFamily = PixelifyFontFamily,
                                    fontSize = 13.sp,
                                    color = XpTextBlack
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Escala de pixel
                XpGroupBox(title = "Escala de pixel") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        scaleChips.forEach { scale ->
                            val selected = settings.displayScale == scale
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (selected) XpStartGreen else Color(0xFFECE9D8))
                                    .border(1.dp, Color.Black, RoundedCornerShape(3.dp))
                                    .clickable {
                                        onSettingsChange { it.copy(displayScale = scale) }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${scale}x",
                                    fontFamily = PixelifyFontFamily,
                                    fontSize = 13.sp,
                                    color = XpTextBlack
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Saída: $outputW x ${outputH}px",
                        fontFamily = VT323FontFamily,
                        fontSize = 18.sp,
                        color = XpTextBlack
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Paleta
                XpGroupBox(title = "Paleta de cores") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        XpRadioButton(
                            selected = settings.palettePreset == PalettePreset.AUTO_EXTRACT,
                            onClick = {
                                onSettingsChange { it.copy(palettePreset = PalettePreset.AUTO_EXTRACT) }
                            },
                            label = "Auto Extract"
                        )
                        XpRadioButton(
                            selected = settings.palettePreset != PalettePreset.AUTO_EXTRACT,
                            onClick = {
                                if (settings.palettePreset == PalettePreset.AUTO_EXTRACT) {
                                    onSettingsChange { it.copy(palettePreset = PalettePreset.PICO_8) }
                                }
                            },
                            label = "Preset"
                        )
                    }

                    if (settings.palettePreset == PalettePreset.AUTO_EXTRACT) {
                        Spacer(modifier = Modifier.height(6.dp))
                        XpSlider(
                            label = "Máx. cores",
                            value = settings.maxAutoColors.toFloat(),
                            onValueChange = { n ->
                                onSettingsChange { it.copy(maxAutoColors = n.toInt().coerceIn(2, 64)) }
                            },
                            valueRange = 2f..64f,
                            displayFormatter = { "${it.toInt()}" }
                        )
                    } else {
                        Spacer(modifier = Modifier.height(6.dp))
                        Column {
                            listOf(
                                PalettePreset.PICO_8,
                                PalettePreset.SWEETIE_16,
                                PalettePreset.ENDESGA_32,
                                PalettePreset.GAME_BOY
                            ).forEach { preset ->
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

                    val activeColors = settings.palettePreset.colors
                    if (activeColors.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Paleta ativa (${activeColors.size})",
                            fontFamily = PixelifyFontFamily,
                            fontSize = 12.sp,
                            color = XpTextBlack
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            activeColors.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(color)
                                        .border(1.dp, Color.Black)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dithering
                XpGroupBox(title = "Dithering") {
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

                Spacer(modifier = Modifier.height(12.dp))

                // Ações
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    XpButton(
                        text = "Nova Imagem",
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        testTag = "converter_pick_image_button"
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    XpButton(
                        text = "Exportar",
                        onClick = onExportClick,
                        isPrimaryGreen = true,
                        modifier = Modifier.weight(1f),
                        testTag = "converter_export_button"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
