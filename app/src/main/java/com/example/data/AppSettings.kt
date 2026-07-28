package com.example.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Locale

@Serializable
data class AppSettings(
    val themeMode: Int = 1, // 0 AMOLED, 1 Dark, 2 Light
    val shortenNumbers: Boolean = false,
    val showShareButton: Boolean = false,
    val stackedStats: Boolean = false,
    val cardStyle: Int = 0, // 0 compact, 1 cover, 2 minimal, 3 expanded
    val hideBottomBar: Boolean = true,
    val showWebChapters: Boolean = true,
    val disableAnimations: Boolean = false,
    val sortByStatus: Boolean = true,
    val showBookmarks: Boolean = true,
    val bookmarkPosition: Int = 1, // 0 bottom, 1 in-row
    val enableAdaptationStart: Boolean = false,
    val enableHybrid: Boolean = true,
    val enableRating: Boolean = true,
    val ratingScale: Int = 10, // 5 or 10
    val statsMode: Int = 0, // 0 series+volumes, 1 series+web, 2 all, 3 series+web+VN
    val fixLibraryTitle: Boolean = false,
    val showStatusLabel: Boolean = true,
    val enableSearch: Boolean = false,
    val alignFilters: Boolean = false,
    val fabGlow: Boolean = true,
    val enableVN: Boolean = false,
    val enableTotalWords: Boolean = false,
    val savedTabIndex: Int = 0,
    val statsSections: List<String> = emptyList(), // e.g. ["topWords", "wordsByStatus"]
    val customColors: List<Int> = emptyList(),
    // Custom color tokens
    val accent: Int = 0xFFFF9F0A.toInt(),
    val cPlanned: Int = 0xFF60A5FA.toInt(),
    val cReading: Int = 0xFF34D399.toInt(),
    val cPaused: Int = 0xFFFBBF24.toInt(),
    val cCompleted: Int = 0xFFA78BFA.toInt(),
    val cDropped: Int = 0xFFF87171.toInt(),
    val tagSeries: Int = 0xFFA78BFA.toInt(),
    val tagWeb: Int = 0xFFFBBF24.toInt(),
    val tagSingle: Int = 0xFFFF9F0A.toInt(),
    val tagHybrid: Int = 0xFFFF9F0A.toInt(),
    val tagOngoing: Int = 0xFF34D399.toInt()
) {
    val showWebStats: Boolean
        get() = statsMode in listOf(1, 2, 3)

    val showVolumeStats: Boolean
        get() = statsMode in listOf(0, 2)

    val showVNStats: Boolean
        get() = statsMode in listOf(2, 3)

    fun getColorForStatus(status: BookStatus): Int {
        return when (status) {
            BookStatus.PLANNED -> cPlanned
            BookStatus.READING -> cReading
            BookStatus.PAUSED -> cPaused
            BookStatus.COMPLETED -> cCompleted
            BookStatus.DROPPED -> cDropped
        }
    }

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
        }

        fun parseSettings(jsonStr: String): AppSettings {
            if (jsonStr.isBlank()) return AppSettings()
            return try {
                json.decodeFromString(jsonStr)
            } catch (_: Exception) {
                AppSettings()
            }
        }

        fun encodeSettings(settings: AppSettings): String {
            return json.encodeToString(serializer(), settings)
        }

        val defaultStandardColors = listOf(
            0xFFFF9F0A.toInt(), 0xFFFF7A00.toInt(), 0xFFEF4444.toInt(), 0xFFF87171.toInt(),
            0xFFEC4899.toInt(), 0xFFD946EF.toInt(), 0xFFA78BFA.toInt(), 0xFF8B5CF6.toInt(),
            0xFF6366F1.toInt(), 0xFF60A5FA.toInt(), 0xFF38BDF8.toInt(), 0xFF22D3EE.toInt(),
            0xFF2DD4BF.toInt(), 0xFF34D399.toInt(), 0xFF4ADE80.toInt(), 0xFFA3E635.toInt(),
            0xFFFACC15.toInt(), 0xFFA1887F.toInt(), 0xFF9CA3AF.toInt(), 0xFFE5E7EB.toInt()
        )
    }
}

fun fmtNum(n: Long, shorten: Boolean): String {
    if (shorten) {
        if (n >= 1_000_000) {
            val valM = n / 1_000_000.0
            val formatted = String.format(Locale.US, "%.1f", valM).replace(".0", "")
            return "${formatted}M"
        }
        if (n >= 1_000) {
            val valK = kotlin.math.round(n / 1000.0).toLong()
            return "${valK}K"
        }
        return n.toString()
    } else {
        return String.format(Locale.US, "%,d", n).replace(',', ' ')
    }
}
