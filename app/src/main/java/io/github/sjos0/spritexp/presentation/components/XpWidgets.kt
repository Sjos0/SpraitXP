package io.github.sjos0.spritexp.presentation.components

import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sjos0.spritexp.ui.theme.LunaTitleMid
import io.github.sjos0.spritexp.ui.theme.LunaTitleStart
import io.github.sjos0.spritexp.ui.theme.PixelifyFontFamily
import io.github.sjos0.spritexp.ui.theme.VT323FontFamily
import io.github.sjos0.spritexp.ui.theme.XpInnerBorderDark
import io.github.sjos0.spritexp.ui.theme.XpInnerBorderLight
import io.github.sjos0.spritexp.ui.theme.XpSelectionYellow
import io.github.sjos0.spritexp.ui.theme.XpStartGreen
import io.github.sjos0.spritexp.ui.theme.XpTextBlack
import io.github.sjos0.spritexp.ui.theme.XpWindowBg

/**
 * Barra de progresso segmentada em blocos verdes estilo clássico XP / Windows 2000.
 */
@Composable
fun XpSegmentedProgressBar(
    progress: Float, // 0.0f .. 1.0f
    modifier: Modifier = Modifier,
    totalBlocks: Int = 18
) {
    val filledBlocks = (progress.coerceIn(0f, 1f) * totalBlocks).toInt()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(26.dp)
            .clip(RectangleShape)
            .background(Color(0xFFE4E0D6))
            .border(2.dp, Color(0xFF7F9DB9), RectangleShape)
            .padding(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until totalBlocks) {
                val isFilled = i < filledBlocks
                val boxModifier = Modifier
                    .weight(1f)
                    .height(20.dp)
                    .clip(RectangleShape)
                val coloredModifier = if (isFilled) {
                    boxModifier.background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF7CDE7C), XpStartGreen, Color(0xFF267A26))
                        )
                    )
                } else {
                    boxModifier.background(Color.Transparent)
                }
                Box(modifier = coloredModifier)
            }
        }
    }
}

/**
 * Slider estilo retrô com indicação de valor numérico em fonte VT323.
 */
@Composable
fun XpSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    displayFormatter: (Float) -> String = { "%.0f".format(it) }
) {
    var editing by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = XpTextBlack,
                fontFamily = PixelifyFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = displayFormatter(value),
                color = LunaTitleStart,
                fontFamily = VT323FontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.clickable { editing = true }.padding(start = 8.dp)
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = XpStartGreen,
                activeTrackColor = LunaTitleMid,
                inactiveTrackColor = Color(0xFFC0BCB0)
            )
        )
    }
    if (editing) {
        NumericEditDialog(
            label = label,
            current = value,
            valueRange = valueRange,
            onConfirm = { onValueChange(it); editing = false },
            onDismiss = { editing = false }
        )
    }
}

/**
 * Digitar o valor exato. Aceita só número; fora do intervalo é ajustado para o limite mais próximo.
 */
@Composable
private fun NumericEditDialog(
    label: String,
    current: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onConfirm: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val integerRange = valueRange.start % 1f == 0f && valueRange.endInclusive % 1f == 0f
    fun fmt(v: Float): String = if (integerRange) v.toInt().toString() else "%.2f".format(v)
    var text by remember { mutableStateOf(fmt(current)) }
    val parsed = text.replace(',', '.').toFloatOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = XpWindowBg,
        titleContentColor = Color.Black,
        textContentColor = Color.Black,
        title = { Text(text = "Editar $label", fontFamily = PixelifyFontFamily, color = LunaTitleStart) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { raw -> text = raw.filter { it.isDigit() || it == '.' || it == ',' || it == '-' }.take(8) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = parsed == null,
                supportingText = {
                    Text(
                        text = if (parsed == null) "Digite um número."
                        else "Entre ${fmt(valueRange.start)} e ${fmt(valueRange.endInclusive)}.",
                        fontSize = 11.sp
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                enabled = parsed != null,
                onClick = { parsed?.let { onConfirm(it.coerceIn(valueRange)) } }
            ) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

/**
 * Radio button retrô estilo Windows XP
 */
@Composable
fun XpRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, if (selected) LunaTitleMid else Color(0xFF7F9DB9), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = XpTextBlack,
            fontFamily = PixelifyFontFamily,
            fontSize = 14.sp
        )
    }
}

/**
 * Group Box que abre e fecha ao tocar no título. Começa fechado por padrão.
 */
@Composable
fun XpCollapsibleGroupBox(
    title: String,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFD4D0C8), RectangleShape)
            .padding(1.dp)
            .border(1.dp, Color(0xFFFFFFFF), RectangleShape)
            .background(XpWindowBg)
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(bottom = if (expanded) 6.dp else 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = LunaTitleStart,
                fontFamily = PixelifyFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (expanded) "[-]" else "[+]",
                color = LunaTitleStart,
                fontFamily = VT323FontFamily,
                fontSize = 18.sp
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(400)) + fadeIn(animationSpec = tween(400)),
            exit = shrinkVertically(animationSpec = tween(350)) + fadeOut(animationSpec = tween(350))
        ) {
            CompositionLocalProvider(LocalContentColor provides XpTextBlack) {
                Column { content() }
            }
        }
    }
}

/**
 * Painel com borda em relevo interno (Group Box estilo XP)
 */
@Composable
fun XpGroupBox(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFD4D0C8), RectangleShape)
            .padding(1.dp)
            .border(1.dp, Color(0xFFFFFFFF), RectangleShape)
            .background(XpWindowBg)
            .padding(10.dp)
    ) {
        Text(
            text = title,
            color = LunaTitleStart,
            fontFamily = PixelifyFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        CompositionLocalProvider(LocalContentColor provides XpTextBlack) { content() }
    }
}
