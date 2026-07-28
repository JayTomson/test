package com.example.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray

@Serializable
enum class BookStatus(val label: String, val sortPriority: Int) {
    PLANNED("В планах", 0),
    READING("Читаю", 1),
    PAUSED("На паузе", 2),
    DROPPED("Брошено", 2),
    COMPLETED("Завершено", 3);

    companion object {
        fun safeValueOf(index: Int): BookStatus {
            val entries = entries
            return if (index in entries.indices) entries[index] else READING
        }
    }
}

@Serializable
data class VolumeEntry(
    val v: Double = 1.0,
    val w: Long = 0L
)

@Serializable
data class Book(
    val id: String = System.currentTimeMillis().toString(),
    val title: String = "Без названия",
    val status: BookStatus = BookStatus.READING,
    val isSeries: Boolean = false,
    val isWeb: Boolean = false,
    val isSingle: Boolean = false,
    val countVolumes: Boolean = true,
    val isOngoing: Boolean = false,
    val useDetailedVolumes: Boolean = false,
    val isVN: Boolean = false,
    val isHybridFormat: Boolean = false,
    val words: Long? = null,
    val volumes: Double? = null,
    val totalVolumesInSeries: Int? = null,
    val webChapters: Int? = null,
    val totalWebChapters: Int? = null,
    val endingsRead: Int? = null,
    val endingsTotal: Int? = null,
    val totalWordsInBook: Long? = null,
    val hybridWebChapters: Int? = null,
    val hybridTotalWebChapters: Int? = null,
    val rating: Int? = null,
    val startVolume: Int? = null,
    val startChapter: Int? = null,
    val coverColor: Int = 0xFF607D8B.toInt(),
    val coverUrl: String? = null,
    val localImagePath: String? = null,
    val volumeEntries: List<VolumeEntry> = emptyList(),
    val currentBookmark: String? = null
) {
    val effectiveWords: Long
        get() = if (useDetailedVolumes && !isWeb) {
            volumeEntries.sumOf { it.w }
        } else {
            words ?: 0L
        }

    val effectiveVolumes: Double
        get() = if (useDetailedVolumes) {
            volumeEntries.size.toDouble()
        } else {
            volumes ?: 0.0
        }

    fun volumeLabel(): String {
        if (!countVolumes && !isHybridFormat) return ""
        val vNum = effectiveVolumes
        val vStr = if (vNum % 1.0 == 0.0) vNum.toLong().toString() else vNum.toString()
        if (isOngoing) return "$vStr/? т."
        if (totalVolumesInSeries != null && totalVolumesInSeries > 0) {
            return "$vStr/$totalVolumesInSeries т."
        }
        return "$vStr т."
    }

    fun chapterLabel(): String {
        val c = if (isHybridFormat) (hybridWebChapters ?: 0) else (webChapters ?: 0)
        val t = if (isHybridFormat) hybridTotalWebChapters else totalWebChapters
        if (t != null && t > 0) return "$c/$t гл."
        return "$c гл."
    }

    fun endingsLabel(): String {
        val r = endingsRead ?: 0
        val t = endingsTotal
        if (t != null && t > 0) return "$r/$t кон."
        return "$r кон."
    }

    fun getRatingDisplay(scale: Int): String {
        val r = rating ?: return ""
        return if (scale == 5) {
            val starVal = kotlin.math.round(r / 2.0).toInt()
            "$starVal/5 ★"
        } else {
            "$r/10 ★"
        }
    }

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
        }

        fun parseLibrary(jsonStr: String): Pair<List<Book>, Boolean> {
            if (jsonStr.isBlank()) return Pair(emptyList(), false)
            return try {
                val element = json.parseToJsonElement(jsonStr)
                if (element is kotlinx.serialization.json.JsonArray) {
                    val list = mutableListOf<Book>()
                    for (item in element) {
                        try {
                            val book = json.decodeFromJsonElement(serializer(), item)
                            list.add(book)
                        } catch (_: Exception) {
                            // skip corrupted entry
                        }
                    }
                    Pair(list, false)
                } else {
                    val list = json.decodeFromString<List<Book>>(jsonStr)
                    Pair(list, false)
                }
            } catch (_: Exception) {
                // corrupted total parsing
                Pair(emptyList(), true)
            }
        }

        fun encodeLibrary(books: List<Book>): String {
            return json.encodeToString(kotlinx.serialization.builtins.ListSerializer(serializer()), books)
        }
    }
}
