package com.hustle.bankapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hustle.bankapp.theme.SurfaceDark

/**
 * Custom glassmorphism modifier that creates a brutalist/hacker frosted glass effect.
 * Instead of expensive blur algorithms that drop frames on low-end Androids,
 * this uses a calculated alpha overlay on the pitch-black background with a
 * stark, high-contrast border stroke.
 */
fun Modifier.glassmorphism(
    cornerRadius: Dp = 24.dp,
    alpha: Float = 0.5f,
    borderColor: Color = Color.White.copy(alpha = 0.08f)
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    return this
        .clip(shape)
        .background(SurfaceDark.copy(alpha = alpha))
        .border(width = 1.dp, color = borderColor, shape = shape)
}
