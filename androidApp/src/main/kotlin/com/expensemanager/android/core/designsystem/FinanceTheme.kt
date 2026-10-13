package com.expensemanager.android.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object FinanceColors {
    val AppBackground = Color(0xFF0B1220)
    val PrimarySurface = Color(0xFF151F32)
    val ElevatedSurface = Color(0xFF1C2A42)
    val CardBorder = Color(0xFF2B3B55)
    val PrimaryBlue = Color(0xFF6488FF)
    val PressedBlue = Color(0xFF4E72E8)
    val PrimaryText = Color(0xFFF6F8FC)
    val SecondaryText = Color(0xFFAAB7CD)
    val MutedText = Color(0xFF8798B3)
    val Positive = Color(0xFF34D5AB)
    val Negative = Color(0xFFFF8394)
    val Warning = Color(0xFFF5BF67)
    val Disabled = Color(0xFF344057)
}

private val FinanceDarkScheme = darkColorScheme(
    primary = FinanceColors.PrimaryBlue,
    onPrimary = FinanceColors.PrimaryText,
    primaryContainer = FinanceColors.PressedBlue,
    onPrimaryContainer = FinanceColors.PrimaryText,
    secondary = FinanceColors.SecondaryText,
    onSecondary = FinanceColors.AppBackground,
    background = FinanceColors.AppBackground,
    onBackground = FinanceColors.PrimaryText,
    surface = FinanceColors.PrimarySurface,
    onSurface = FinanceColors.PrimaryText,
    surfaceVariant = FinanceColors.ElevatedSurface,
    onSurfaceVariant = FinanceColors.SecondaryText,
    outline = FinanceColors.CardBorder,
    error = FinanceColors.Negative,
)

private val FinanceTypography = Typography(
    displaySmall = androidx.compose.ui.text.TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
    headlineSmall = androidx.compose.ui.text.TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = androidx.compose.ui.text.TextStyle(fontSize = 16.sp),
    bodyMedium = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
    labelMedium = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
)

@Composable
fun FinanceTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FinanceDarkScheme,
        typography = FinanceTypography,
        content = content,
    )
}
