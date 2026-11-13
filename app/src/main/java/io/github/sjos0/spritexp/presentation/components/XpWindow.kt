package io.github.sjos0.spritexp.presentation.components

import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sjos0.spritexp.ui.theme.LunaTitleEnd
import io.github.sjos0.spritexp.ui.theme.LunaTitleMid
import io.github.sjos0.spritexp.ui.theme.LunaTitleStart
import io.github.sjos0.spritexp.ui.theme.PixelifyFontFamily
import androidx.compose.ui.text.style.TextOverflow
import io.github.sjos0.spritexp.ui.theme.XpButtonFace
import io.github.sjos0.spritexp.ui.theme.XpCloseOrange
import io.github.sjos0.spritexp.ui.theme.XpCloseOrangePressed
import io.github.sjos0.spritexp.ui.theme.XpInnerBorderDark
import io.github.sjos0.spritexp.ui.theme.XpInnerBorderLight
import io.github.sjos0.spritexp.ui.theme.XpStartGreen
import io.github.sjos0.spritexp.ui.theme.XpStartGreenPressed
import io.github.sjos0.spritexp.ui.theme.XpTextBlack
import io.github.sjos0.spritexp.ui.theme.XpTextDisabled
import io.github.sjos0.spritexp.ui.theme.XpTextWhite
import io.github.sjos0.spritexp.ui.theme.XpWindowBg
import io.github.sjos0.spritexp.ui.theme.XpWindowBorder

/**
 * Janela clássica no estilo Windows XP Luna com barra de título em gradiente,
 * botões de controle e borda em relevo.
 */
@Composable
fun XpWindow(
    title: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    onCloseClick: (() -> Unit)? = null,
    onMinimizeClick: (() -> Unit)? = null,
    onMaximizeClick: (() -> Unit)? = null,
    isDarkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val windowBg = if (isDarkTheme) Color(0xFF2E2E32) else XpWindowBg
    val cornerRadius = 6.dp

    Column(
        modifier = modifier
            .shadow(12.dp, shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius, bottomStart = 4.dp, bottomEnd = 4.dp))
            .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius, bottomStart = 4.dp, bottomEnd = 4.dp))
            .border(2.dp, XpWindowBorder, RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius, bottomStart = 4.dp, bottomEnd = 4.dp))
            .background(windowBg)
    ) {
        // Title Bar Luna XP
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(LunaTitleStart, LunaTitleMid, LunaTitleEnd)
                    )
                )
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (icon != null) {
                    Box(modifier = Modifier.size(18.dp)) {
                        icon()
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = title,
                    color = XpTextWhite,
                    fontFamily = PixelifyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1
                )
            }

            // Window Buttons: Minimizar, Maximizar, Fechar
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onMinimizeClick != null) {
                    XpWindowButton(label = "_", onClick = onMinimizeClick)
                    Spacer(modifier = Modifier.width(3.dp))
                }
                if (onMaximizeClick != null) {
                    XpWindowButton(label = "□", onClick = onMaximizeClick)
                    Spacer(modifier = Modifier.width(3.dp))
                }
                if (onCloseClick != null) {
                    XpCloseButton(onClick = onCloseClick)
                }
            }
        }

        // Window Inner Content with bevel border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(windowBg)
                .padding(8.dp)
        ) {
            content()
        }
    }
}

@Composable
fun XpWindowButton(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(RectangleShape)
            .background(Color(0xFF3870E0))
            .border(1.dp, Color(0xFF6B9BFF), RectangleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontFamily = PixelifyFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
fun XpCloseButton(
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val bgColor = if (isPressed) XpCloseOrangePressed else XpCloseOrange

    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(RectangleShape)
            .background(bgColor)
            .border(1.dp, Color(0xFFFF9980), RectangleShape)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .testTag("window_close_button"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Fechar janela",
            tint = Color.White,
            modifier = Modifier.size(14.dp)
        )
    }
}

/**
 * Botão clássico estilo Windows XP com relevo 3D (Bevel).
 */
@Composable
fun XpButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimaryGreen: Boolean = false,
    isDangerOrange: Boolean = false,
    icon: (@Composable () -> Unit)? = null,
    testTag: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val baseBg = when {
        !enabled -> Color(0xFFD6D3CE)
        isDangerOrange -> if (isPressed) XpCloseOrangePressed else XpCloseOrange
        isPrimaryGreen -> if (isPressed) XpStartGreenPressed else XpStartGreen
        else -> if (isPressed) Color(0xFFDCD8C8) else XpButtonFace
    }

    val textColor = when {
        !enabled -> XpTextDisabled
        isPrimaryGreen || isDangerOrange -> XpTextWhite
        else -> XpTextBlack
    }

    val lightBorder = if (isPressed) XpInnerBorderDark else XpInnerBorderLight
    val darkBorder = if (isPressed) XpInnerBorderLight else XpInnerBorderDark

    val finalModifier = modifier
        .heightIn(min = 44.dp)
        .clip(RectangleShape)
        .background(baseBg)
        .border(1.dp, Color(0xFF003C74), RectangleShape)
        .padding(1.dp)
        .border(
            width = 1.dp,
            color = if (isPrimaryGreen || isDangerOrange) Color.Transparent else lightBorder,
            shape = RectangleShape
        )
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
        .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)

    Box(
        modifier = finalModifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .offset(x = if (isPressed) 1.dp else 0.dp, y = if (isPressed) 1.dp else 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontFamily = PixelifyFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
