package com.example.presentation.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.domain.model.PixelArtSettings
import com.example.presentation.components.XpButton
import com.example.presentation.components.XpGroupBox
import com.example.presentation.components.XpSlider
import com.example.presentation.components.XpWindow
import com.example.ui.theme.LunaTitleStart
import com.example.ui.theme.PixelifyFontFamily
import com.example.ui.theme.VT323FontFamily
import com.example.ui.theme.XpDarkPanel
import com.example.ui.theme.XpInnerBorderDark
import com.example.ui.theme.XpStartGreen
import com.example.ui.theme.XpTextBlack
import com.example.ui.theme.XpTextWhite
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore

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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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
                // Painel Escuro de Comparação: Original vs Pixel Art
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(XpDarkPanel)
                        .border(2.dp, Color(0xFF1A1A1A), RoundedCornerShape(4.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Original
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
                                    .size(130.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF388E3C))
                                    .border(2.dp, Color.White, RoundedCornerShape(3.dp)),
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

                        // Pixel Art
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
                                    .size(130.dp)
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

                Spacer(modifier = Modifier.height(10.dp))

                // Seletor de Resolução Alvo (16, 32, 48, 64, 96, 128)
                XpGroupBox(title = "Resolução & Prévia") {
                    XpSlider(
                        label = "Resolução Alvo (px)",
                        value = settings.targetResolution.toFloat(),
                        onValueChange = { res ->
                            onSettingsChange { it.copy(targetResolution = res.toInt()) }
                        },
                        valueRange = 16f..128f,
                        displayFormatter = { "${it.toInt()} x ${it.toInt()}" }
                    )

                    XpSlider(
                        label = "Tolerância Chroma Key",
                        value = settings.chromaTolerance,
                        onValueChange = { tol ->
                            onSettingsChange { it.copy(chromaTolerance = tol) }
                        },
                        valueRange = 0f..100f,
                        displayFormatter = { "${it.toInt()}%" }
                    )

                    XpSlider(
                        label = "Realce de Bordas",
                        value = settings.edgeEnhance,
                        onValueChange = { ee ->
                            onSettingsChange { it.copy(edgeEnhance = ee) }
                        },
                        valueRange = 0f..200f,
                        displayFormatter = { "${it.toInt()}%" }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botões de Ação
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
            }
        }
    }
}
