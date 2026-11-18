package com.example.trackrdata.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
private val DefaultTypography = Typography()

val Typography = Typography(
    headlineLarge = DefaultTypography.headlineLarge.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),

    titleLarge = DefaultTypography.titleLarge.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp
    ),

    titleMedium = DefaultTypography.titleMedium.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),

    bodyLarge = DefaultTypography.bodyLarge.copy(
        letterSpacing = 0.sp
    ),

    bodyMedium = DefaultTypography.bodyMedium.copy(
        letterSpacing = 0.sp
    )
)