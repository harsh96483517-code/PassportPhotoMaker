package com.passportphotomaker.print.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** A frosted, translucent "liquid glass" card with a soft gradient sheen and hairline border. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    strong: Boolean = false,
    contentPadding: androidx.compose.foundation.layout.PaddingValues =
        androidx.compose.foundation.layout.PaddingValues(18.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val base = if (strong) Color(0xE6FFFFFF) else Color(0xB3FFFFFF)
    val glassBrush = Brush.verticalGradient(
        colors = listOf(
            base.copy(alpha = (if (strong) 0.92f else 0.72f)),
            base.copy(alpha = (if (strong) 0.72f else 0.46f))
        )
    )
    val borderColor = Color(0x59FFFFFF)

    val clickModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else Modifier

    Column(
        modifier = modifier
            .shadow(
                elevation = 14.dp,
                shape = shape,
                ambientColor = Color(0x331677FF),
                spotColor = Color(0x331677FF)
            )
            .clip(shape)
            .background(glassBrush)
            .border(BorderStroke(1.dp, borderColor), shape)
            .then(clickModifier)
            .padding(contentPadding),
        content = content
    )
}

/** Glossy blue gradient button with a pressed spring animation. */
@Composable
fun GlassPrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 600f),
        label = "glassButtonScale"
    )
    val shape = RoundedCornerShape(20.dp)
    val gradient = if (enabled) {
        Brush.horizontalGradient(listOf(Color(0xFF2E8BFF), Color(0xFF1677FF), Color(0xFF0B5CD6)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFF9DB8DD), Color(0xFF8AA6CC)))
    }

    Row(
        modifier = modifier
            .scale(scale)
            .shadow(12.dp, shape, ambientColor = Color(0x551677FF), spotColor = Color(0x551677FF))
            .clip(shape)
            .background(gradient)
            .border(1.dp, Color(0x66FFFFFF), shape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled
            ) { onClick() }
            .padding(horizontal = 22.dp, vertical = 15.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.size(10.dp))
        }
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/** Secondary translucent glass button. */
@Composable
fun GlassSecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(Color(0x33FFFFFF))
            .border(1.dp, Color(0x55FFFFFF), shape)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 15.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.size(8.dp))
        }
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onAction() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

/** Decorative gradient backdrop used behind screens for the liquid-glass effect. */
@Composable
fun GlassBackdrop(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        // Soft blue glows for depth
        Box(
            Modifier
                .size(260.dp)
                .align(Alignment.TopEnd)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x331677FF), Color.Transparent)
                    )
                )
        )
        Box(
            Modifier
                .size(220.dp)
                .align(Alignment.BottomStart)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x2238C6FF), Color.Transparent)
                    )
                )
        )
        content()
    }
}

@Composable
fun VSpace(h: Int) = Spacer(Modifier.height(h.dp))
