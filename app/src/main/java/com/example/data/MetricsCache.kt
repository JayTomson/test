package com.example.data

data class MetricsCache(
    val totalWords: Long = 0L,
    val totalVolumes: Double = 0.0,
    val completedSeries: Int = 0,
    val completedWeb: Int = 0,
    val completedVN: Int = 0,
    val totalEndings: Int = 0,
    val anyVN: Boolean = false,
    val anyBooksCountVolumes: Boolean = false,
    val countByStatus: IntArray = IntArray(5),
    val wordsByStatus: LongArray = LongArray(5),
    val topByWords: List<Book> = emptyList()
) {
    companion object {
        fun calculate(books: List<Book>): MetricsCache {
            var totalWords = 0L
            var totalVolumes = 0.0
            var completedSeries = 0
            var completedWeb = 0
            var completedVN = 0
            var totalEndings = 0
            var anyVN = false
            var anyBooksCountVolumes = false
            val countByStatus = IntArray(5)
            val wordsByStatus = LongArray(5)

            for (book in books) {
                val words = book.effectiveWords
                val volumes = book.effectiveVolumes
                val statusIdx = book.status.ordinal

                totalWords += words
                if (book.countVolumes || book.isHybridFormat) {
                    totalVolumes += volumes
                    anyBooksCountVolumes = true
                }
                if (book.isVN) {
                    anyVN = true
                    totalEndings += (book.endingsRead ?: 0)
                }

                if (statusIdx in 0..4) {
                    countByStatus[statusIdx] += 1
                    wordsByStatus[statusIdx] += words
                }

                if (book.status == BookStatus.COMPLETED) {
                    if (book.isSeries) completedSeries++
                    if (book.isWeb) completedWeb++
                    if (book.isVN) completedVN++
                }
            }

            val topByWords = books
                .filter { it.effectiveWords > 0 }
                .sortedByDescending { it.effectiveWords }
                .take(5)

            return MetricsCache(
                totalWords = totalWords,
                totalVolumes = totalVolumes,
                completedSeries = completedSeries,
                completedWeb = completedWeb,
                completedVN = completedVN,
                totalEndings = totalEndings,
                anyVN = anyVN,
                anyBooksCountVolumes = anyBooksCountVolumes,
                countByStatus = countByStatus,
                wordsByStatus = wordsByStatus,
                topByWords = topByWords
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MetricsCache

        if (totalWords != other.totalWords) return false
        if (totalVolumes != other.totalVolumes) return false
        if (completedSeries != other.completedSeries) return false
        if (completedWeb != other.completedWeb) return false
        if (completedVN != other.completedVN) return false
        if (totalEndings != other.totalEndings) return false
        if (anyVN != other.anyVN) return false
        if (anyBooksCountVolumes != other.anyBooksCountVolumes) return false
        if (!countByStatus.contentEquals(other.countByStatus)) return false
        if (!wordsByStatus.contentEquals(other.wordsByStatus)) return false
        if (topByWords != other.topByWords) return false

        return true
    }

    override fun hashCode(): Int {
        var result = totalWords.hashCode()
        result = 31 * result + totalVolumes.hashCode()
        result = 31 * result + completedSeries
        result = 31 * result + completedWeb
        result = 31 * result + completedVN
        result = 31 * result + totalEndings
        result = 31 * result + anyVN.hashCode()
        result = 31 * result + anyBooksCountVolumes.hashCode()
        result = 31 * result + countByStatus.contentHashCode()
        result = 31 * result + wordsByStatus.contentHashCode()
        result = 31 * result + topByWords.hashCode()
        return result
    }
}
