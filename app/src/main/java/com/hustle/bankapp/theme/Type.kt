package com.hustle.bankapp.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val RobotoMono = FontFamily.Monospace

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = RobotoMono,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = RobotoMono,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        color = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = TextPrimary
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = TextSecondary
    )
)
