package com.example.presentation.components

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
import com.example.ui.theme.LunaTitleMid
import com.example.ui.theme.LunaTitleStart
import com.example.ui.theme.PixelifyFontFamily
import com.example.ui.theme.VT323FontFamily
import com.example.ui.theme.XpInnerBorderDark
import com.example.ui.theme.XpInnerBorderLight
import com.example.ui.theme.XpSelectionYellow
import com.example.ui.theme.XpStartGreen
import com.example.ui.theme.XpTextBlack
import com.example.ui.theme.XpWindowBg

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
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFFE4E0D6))
            .border(2.dp, Color(0xFF7F9DB9), RoundedCornerShape(3.dp))
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
                    .clip(RoundedCornerShape(1.dp))
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
                fontSize = 20.sp
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
            .border(1.dp, Color(0xFFD4D0C8), RoundedCornerShape(4.dp))
            .padding(1.dp)
            .border(1.dp, Color(0xFFFFFFFF), RoundedCornerShape(3.dp))
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
        content()
    }
}
