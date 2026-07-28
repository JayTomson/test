package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppSettings
import com.example.data.BookStatus

val PlusJakartaSansFamily = FontFamily.SansSerif

@Immutable
data class ReadTrackerThemeColors(
    val screenBg: Color,
    val cardBg: Color,
    val textFg: Color,
    val dividerColor: Color,
    val accent: Color,
    val accentDim: Color,
    val cPlanned: Color,
    val cReading: Color,
    val cPaused: Color,
    val cCompleted: Color,
    val cDropped: Color,
    val tagSeries: Color,
    val tagWeb: Color,
    val tagSingle: Color,
    val tagHybrid: Color,
    val tagOngoing: Color
) {
    val accentOnColor: Color
        get() = if (accent.luminance() > 0.5f) Color.Black else Color.White

    val textSecondary: Color
        get() = textFg.copy(alpha = 0.6f)

    fun getColorForStatus(status: BookStatus): Color {
        return when (status) {
            BookStatus.PLANNED -> cPlanned
            BookStatus.READING -> cReading
            BookStatus.PAUSED -> cPaused
            BookStatus.COMPLETED -> cCompleted
            BookStatus.DROPPED -> cDropped
        }
    }
}

val LocalReadTrackerColors = staticCompositionLocalOf<ReadTrackerThemeColors> {
    ReadTrackerThemeColors(
        screenBg = Color(0xFF0F0F0F),
        cardBg = Color(0xFF1C1C1E),
        textFg = Color(0xFFFFFFFF),
        dividerColor = Color(0x1AFFFFFF),
        accent = Color(0xFFFF9F0A),
        accentDim = Color(0x1FFF9F0A),
        cPlanned = Color(0xFF60A5FA),
        cReading = Color(0xFF34D399),
        cPaused = Color(0xFFFBBF24),
        cCompleted = Color(0xFFA78BFA),
        cDropped = Color(0xFFF87171),
        tagSeries = Color(0xFFA78BFA),
        tagWeb = Color(0xFFFBBF24),
        tagSingle = Color(0xFFFF9F0A),
        tagHybrid = Color(0xFFFF9F0A),
        tagOngoing = Color(0xFF34D399)
    )
}

// Dimensions
val RadiusSmall = 8.dp
val RadiusMedium = 12.dp
val RadiusLarge = 20.dp

val PaddingSmall = 8.dp
val PaddingMedium = 16.dp
val PaddingLarge = 24.dp

@Composable
fun ReadTrackerTheme(
    settings: AppSettings,
    content: @Composable () -> Unit
) {
    val accentColor = Color(settings.accent)
    val accentDimColor = accentColor.copy(alpha = 0.12f)

    val (screenBg, cardBg, textFg, dividerColor) = when (settings.themeMode) {
        0 -> Quadruple(Color(0xFF000000), Color(0xFF141414), Color(0xFFFFFFFF), Color(0x1AFFFFFF)) // AMOLED
        2 -> Quadruple(Color(0xFFF2F2F7), Color(0xFFFFFFFF), Color(0xFF000000), Color(0x1F000000)) // Light
        else -> Quadruple(Color(0xFF0F0F0F), Color(0xFF1C1C1E), Color(0xFFFFFFFF), Color(0x1AFFFFFF)) // Dark (1)
    }

    val trackerColors = ReadTrackerThemeColors(
        screenBg = screenBg,
        cardBg = cardBg,
        textFg = textFg,
        dividerColor = dividerColor,
        accent = accentColor,
        accentDim = accentDimColor,
        cPlanned = Color(settings.cPlanned),
        cReading = Color(settings.cReading),
        cPaused = Color(settings.cPaused),
        cCompleted = Color(settings.cCompleted),
        cDropped = Color(settings.cDropped),
        tagSeries = Color(settings.tagSeries),
        tagWeb = Color(settings.tagWeb),
        tagSingle = Color(settings.tagSingle),
        tagHybrid = Color(settings.tagHybrid),
        tagOngoing = Color(settings.tagOngoing)
    )

    val typography = Typography(
        titleLarge = TextStyle(
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            letterSpacing = (-0.3).sp
        ),
        titleMedium = TextStyle(
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 17.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        ),
        labelSmall = TextStyle(
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    )

    val colorScheme = if (settings.themeMode == 2) {
        lightColorScheme(
            primary = accentColor,
            background = screenBg,
            surface = cardBg,
            onBackground = textFg,
            onSurface = textFg
        )
    } else {
        darkColorScheme(
            primary = accentColor,
            background = screenBg,
            surface = cardBg,
            onBackground = textFg,
            onSurface = textFg
        )
    }

    CompositionLocalProvider(
        LocalReadTrackerColors provides trackerColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
