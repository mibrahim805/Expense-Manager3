package com.expensemanager.android.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

enum class FinanceThemeMode {
    MIDNIGHT,
    ALMOND,
}

data class FinanceThemeColors(
    val appBackground: Color,
    val surface: Color,
    val elevatedSurface: Color,
    val border: Color,
    val primary: Color,
    val pressedPrimary: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val mutedText: Color,
    val income: Color,
    val expense: Color,
    val warning: Color,
    val disabled: Color,
    val balanceStart: Color,
    val balanceEnd: Color,
    val navigation: Color,
    val selectedNavigation: Color,
    val categoryTints: List<Color>,
)

private val MidnightColors = FinanceThemeColors(
    appBackground = Color(0xFF07111F),
    surface = Color(0xFF101E33),
    elevatedSurface = Color(0xFF152640),
    border = Color(0xFF1D3049),
    primary = Color(0xFF4785FF),
    pressedPrimary = Color(0xFF376DD8),
    primaryText = Color(0xFFF5F7FF),
    secondaryText = Color(0xFF9BAECC),
    mutedText = Color(0xFF7185A3),
    income = Color(0xFF20D6B0),
    expense = Color(0xFFFF5975),
    warning = Color(0xFFF5BF67),
    disabled = Color(0xFF344057),
    balanceStart = Color(0xFF172D52),
    balanceEnd = Color(0xFF101E33),
    navigation = Color(0xFF101E33),
    selectedNavigation = Color(0xFF152640),
    categoryTints = listOf(
        Color(0xFF9A7428), Color(0xFF315E9E), Color(0xFF66549A), Color(0xFF8D3D4C),
        Color(0xFF2C887B), Color(0xFF3C6F98), Color(0xFF69518B), Color(0xFF237A72),
        Color(0xFF386C9B), Color(0xFF49688F), Color(0xFF7C4D70),
    ),
)

private val AlmondColors = FinanceThemeColors(
    appBackground = Color(0xFFFAF7F0),
    surface = Color(0xFFFFFFFF),
    elevatedSurface = Color(0xFFF6F0E7),
    border = Color(0xFFE7DFD2),
    primary = Color(0xFFB58B58),
    pressedPrimary = Color(0xFF9A7147),
    primaryText = Color(0xFF302820),
    secondaryText = Color(0xFF726556),
    mutedText = Color(0xFF998B7B),
    income = Color(0xFF367D5B),
    expense = Color(0xFFB65C55),
    warning = Color(0xFF9A7147),
    disabled = Color(0xFFD8CDBE),
    balanceStart = Color(0xFFE8D8BE),
    balanceEnd = Color(0xFFE8D8BE),
    navigation = Color(0xFFFFFFFF),
    selectedNavigation = Color(0xFFF3E9DB),
    categoryTints = listOf(
        Color(0xFFE6C98C), Color(0xFFC8D9E8), Color(0xFFD9CBE8), Color(0xFFE6C4C8),
        Color(0xFFBFE0D3), Color(0xFFC7DDE8), Color(0xFFD9CBE8), Color(0xFFB9DACA),
        Color(0xFFC9DCEB), Color(0xFFD3DCE8), Color(0xFFE3C8D3),
    ),
)

private val LocalFinanceColors = staticCompositionLocalOf { MidnightColors }

@Composable
fun financeColors(): FinanceThemeColors = LocalFinanceColors.current

fun financeThemeColors(mode: FinanceThemeMode): FinanceThemeColors =
    if (mode == FinanceThemeMode.ALMOND) AlmondColors else MidnightColors

private val FinanceTypography = Typography(
    displaySmall = androidx.compose.ui.text.TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
    headlineSmall = androidx.compose.ui.text.TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = androidx.compose.ui.text.TextStyle(fontSize = 16.sp),
    bodyMedium = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
    labelMedium = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
)

@Composable
fun FinanceTheme(
    mode: FinanceThemeMode,
    content: @Composable () -> Unit,
) {
    val colors = financeThemeColors(mode)
    val scheme = if (mode == FinanceThemeMode.ALMOND) {
        lightColorScheme(
            primary = colors.primary,
            onPrimary = Color.White,
            primaryContainer = colors.balanceStart,
            onPrimaryContainer = colors.primaryText,
            secondary = colors.secondaryText,
            onSecondary = colors.primaryText,
            background = colors.appBackground,
            onBackground = colors.primaryText,
            surface = colors.surface,
            onSurface = colors.primaryText,
            surfaceVariant = colors.elevatedSurface,
            onSurfaceVariant = colors.secondaryText,
            outline = colors.border,
            error = colors.expense,
            tertiary = colors.income,
            onTertiary = Color.White,
        )
    } else {
        darkColorScheme(
            primary = colors.primary,
            onPrimary = colors.primaryText,
            primaryContainer = colors.pressedPrimary,
            onPrimaryContainer = colors.primaryText,
            secondary = colors.secondaryText,
            onSecondary = colors.appBackground,
            background = colors.appBackground,
            onBackground = colors.primaryText,
            surface = colors.surface,
            onSurface = colors.primaryText,
            surfaceVariant = colors.elevatedSurface,
            onSurfaceVariant = colors.secondaryText,
            outline = colors.border,
            error = colors.expense,
            tertiary = colors.income,
            onTertiary = colors.primaryText,
        )
    }

    CompositionLocalProvider(LocalFinanceColors provides colors) {
        MaterialTheme(colorScheme = scheme, typography = FinanceTypography, content = content)
    }
}
