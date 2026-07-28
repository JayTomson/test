package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.AppSettings
import com.example.data.Book
import com.example.data.fmtNum
import com.example.ui.theme.LocalReadTrackerColors
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.RadiusMedium
import com.example.ui.theme.RadiusSmall
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookCard(
    book: Book,
    settings: AppSettings,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (settings.cardStyle) {
        1 -> BookCardWithCover(book, settings, onClick, onLongClick, modifier)
        2 -> BookCardMinimal(book, settings, onClick, onLongClick, modifier)
        3 -> BookCardExpanded(book, settings, onClick, onLongClick, modifier)
        else -> BookCardCompact(book, settings, onClick, onLongClick, modifier)
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun BookCardCompact(
    book: Book,
    settings: AppSettings,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalReadTrackerColors.current
    val statusColor = Color(settings.getColorForStatus(book.status))

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(RadiusMedium),
        color = colors.cardBg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status bar strip
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(statusColor)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Line 1: Title + Rating + Badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = book.title,
                        style = TextStyle(
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.textFg
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (settings.enableRating && book.rating != null) {
                        RatingBadge(text = book.getRatingDisplay(settings.ratingScale))
                    }

                    CardBadges(book, settings)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Line 2: Status label + Metrics
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (settings.showStatusLabel) {
                        Text(
                            text = book.status.label,
                            style = TextStyle(
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = statusColor
                            )
                        )
                    }

                    MetricsRow(book, settings, isCompact = true)
                }

                // Line 3: Bookmark if position == 0 (bottom)
                if (settings.showBookmarks && settings.bookmarkPosition == 0 && !book.currentBookmark.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    BookmarkRow(book.currentBookmark!!)
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BookCardWithCover(
    book: Book,
    settings: AppSettings,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalReadTrackerColors.current
    val statusColor = Color(settings.getColorForStatus(book.status))

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(RadiusMedium),
        color = colors.cardBg
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cover 52x74dp
            BookCoverImage(
                book = book,
                modifier = Modifier.size(width = 52.dp, height = 74.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = book.title,
                        style = TextStyle(
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.textFg
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (settings.enableRating && book.rating != null) {
                        RatingBadge(text = book.getRatingDisplay(settings.ratingScale))
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                CardBadges(book, settings)

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (settings.showStatusLabel) {
                        Text(
                            text = book.status.label,
                            style = TextStyle(
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = statusColor
                            )
                        )
                    }
                    MetricsRow(book, settings, isCompact = true)
                }

                if (settings.showBookmarks && settings.bookmarkPosition == 0 && !book.currentBookmark.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    BookmarkRow(book.currentBookmark!!)
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BookCardMinimal(
    book: Book,
    settings: AppSettings,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalReadTrackerColors.current
    val statusColor = Color(settings.getColorForStatus(book.status))
    val firstLetter = book.title.trim().take(1).uppercase().ifEmpty { "?" }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(statusColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = firstLetter,
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = statusColor
                    )
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = colors.textFg
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (settings.showStatusLabel) {
                        Text(
                            text = book.status.label,
                            style = TextStyle(
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = statusColor
                            )
                        )
                        Text(text = "·", color = Color.Gray.copy(alpha = 0.6f), fontSize = 11.sp)
                    }

                    val metaList = mutableListOf<String>()
                    if (book.isVN) {
                        metaList.add(book.endingsLabel())
                    } else {
                        val wordsFormatted = fmtNum(book.effectiveWords, settings.shortenNumbers)
                        metaList.add("$wordsFormatted сл.")
                        if (book.countVolumes || book.isHybridFormat) {
                            val vLbl = book.volumeLabel()
                            if (vLbl.isNotEmpty()) metaList.add(vLbl)
                        }
                        if ((book.isWeb || book.isHybridFormat) && settings.showWebChapters) {
                            metaList.add(book.chapterLabel())
                        }
                    }

                    Text(
                        text = metaList.joinToString(" · "),
                        style = TextStyle(
                            fontFamily = PlusJakartaSansFamily,
                            fontSize = 10.sp,
                            color = Color.Gray
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (settings.enableRating && book.rating != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = book.getRatingDisplay(settings.ratingScale),
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp,
                        color = colors.accent
                    )
                )
            }

            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 1.dp,
            color = colors.dividerColor.copy(alpha = 0.5f)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun BookCardExpanded(
    book: Book,
    settings: AppSettings,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalReadTrackerColors.current
    val statusColor = Color(settings.getColorForStatus(book.status))

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .drawWithContent {
                drawContent()
                // Left border 3dp status color 55%
                drawRect(
                    color = statusColor.copy(alpha = 0.55f),
                    size = androidx.compose.ui.geometry.Size(3.dp.toPx(), size.height)
                )
            }
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(RadiusMedium),
        color = colors.cardBg
    ) {
        Column(
            modifier = Modifier.padding(start = 14.dp, top = 12.dp, end = 14.dp, bottom = 13.dp)
        ) {
            // Line 1: Status Chip + Badges + Rating
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Status Chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.14f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = book.status.label,
                        style = TextStyle(
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = statusColor
                        )
                    )
                }

                CardBadges(book, settings)

                Spacer(modifier = Modifier.weight(1f))

                if (settings.enableRating && book.rating != null) {
                    RatingBadge(text = book.getRatingDisplay(settings.ratingScale))
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(17.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = book.title,
                style = TextStyle(
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    letterSpacing = (-0.2).sp,
                    color = colors.textFg
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Progress Bar Priority:
            // 1) VN endings (if endingsTotal > 0)
            // 2) Words from total (if enableTotalWords && totalWordsInBook > 0)
            // 3) Series volumes (if totalVolumesInSeries > 0)
            // 4) Chapters (if totalWebChapters > 0)
            var progressLabel: String? = null
            var progressFraction = 0f
            var barColor = statusColor

            if (book.isVN && (book.endingsTotal ?: 0) > 0) {
                val r = book.endingsRead ?: 0
                val t = book.endingsTotal!!
                progressLabel = "Концовки: $r/$t"
                progressFraction = (r.toFloat() / t.toFloat()).coerceIn(0f, 1f)
                barColor = colors.cCompleted
            } else if (settings.enableTotalWords && (book.totalWordsInBook ?: 0L) > 0L) {
                val r = book.effectiveWords
                val t = book.totalWordsInBook!!
                progressLabel = "Слова: ${fmtNum(r, settings.shortenNumbers)} / ${fmtNum(t, settings.shortenNumbers)}"
                progressFraction = (r.toFloat() / t.toFloat()).coerceIn(0f, 1f)
                barColor = colors.accent
            } else if ((book.totalVolumesInSeries ?: 0) > 0) {
                val v = book.effectiveVolumes
                val t = book.totalVolumesInSeries!!
                val vStr = if (v % 1.0 == 0.0) v.toLong().toString() else v.toString()
                progressLabel = "Тома: $vStr/$t т."
                progressFraction = (v.toFloat() / t.toFloat()).coerceIn(0f, 1f)
                barColor = statusColor
            } else {
                val t = if (book.isHybridFormat) book.hybridTotalWebChapters else book.totalWebChapters
                val c = if (book.isHybridFormat) (book.hybridWebChapters ?: 0) else (book.webChapters ?: 0)
                if (t != null && t > 0) {
                    progressLabel = "Главы: $c/$t гл."
                    progressFraction = (c.toFloat() / t.toFloat()).coerceIn(0f, 1f)
                    barColor = colors.tagWeb
                }
            }

            if (progressLabel != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = progressLabel,
                        style = TextStyle(
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = barColor
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(barColor.copy(alpha = 0.12f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFraction)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(barColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metrics row (skips volumes/chapters if already in progress bar)
            MetricsRow(
                book = book,
                settings = settings,
                isCompact = false,
                skipVolumes = ((book.totalVolumesInSeries ?: 0) > 0 && progressLabel?.startsWith("Тома") == true),
                skipChapters = (progressLabel?.startsWith("Главы") == true)
            )

            if (settings.showBookmarks && !book.currentBookmark.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                BookmarkRow(book.currentBookmark!!)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CardBadges(book: Book, settings: AppSettings) {
    val colors = LocalReadTrackerColors.current

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (book.isVN) {
            TagBadge(text = "VN", color = colors.accent)
        } else if (book.isHybridFormat) {
            TagBadge(text = "LN+WN", color = colors.tagHybrid)
        } else {
            if (book.isSeries) TagBadge(text = "Серия", color = colors.tagSeries)
            if (book.isWeb) TagBadge(text = "Веб", color = colors.tagWeb)
            if (book.isSingle) TagBadge(text = "Сингл", color = colors.tagSingle)
        }

        if (book.isOngoing) {
            TagBadge(text = "Онг.", color = colors.tagOngoing)
        }

        if (settings.enableAdaptationStart) {
            if ((book.isSeries || book.isSingle) && book.startVolume != null) {
                TagBadge(text = "Старт: т. ${book.startVolume}", color = colors.cReading)
            }
            if ((book.isWeb || book.isHybridFormat) && book.startChapter != null) {
                TagBadge(text = "Старт: гл. ${book.startChapter}", color = colors.cReading)
            }
        }
    }
}

@Composable
private fun MetricsRow(
    book: Book,
    settings: AppSettings,
    isCompact: Boolean,
    skipVolumes: Boolean = false,
    skipChapters: Boolean = false
) {
    val colors = LocalReadTrackerColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (book.isVN) {
            // Endings
            MetricItem(
                icon = Icons.Default.VideogameAsset,
                text = book.endingsLabel()
            )
        } else {
            // Words
            val wordsRead = fmtNum(book.effectiveWords, settings.shortenNumbers)
            val wordText = if (settings.enableTotalWords && (book.totalWordsInBook ?: 0L) > 0L) {
                "$wordsRead/${fmtNum(book.totalWordsInBook!!, settings.shortenNumbers)} сл."
            } else {
                "$wordsRead сл."
            }
            MetricItem(
                icon = Icons.Default.TextFields,
                text = wordText
            )

            // Volumes
            if (!skipVolumes && (book.countVolumes || book.isHybridFormat)) {
                val vLbl = book.volumeLabel()
                if (vLbl.isNotEmpty()) {
                    MetricItem(
                        icon = Icons.Default.Layers,
                        text = vLbl
                    )
                }
            }

            // Chapters
            if (!skipChapters && (book.isWeb || book.isHybridFormat) && settings.showWebChapters) {
                MetricItem(
                    icon = Icons.Default.FormatListNumbered,
                    text = book.chapterLabel()
                )
            }
        }

        // Bookmark in-row if position == 1
        if (settings.showBookmarks && settings.bookmarkPosition == 1 && !book.currentBookmark.isNull_orBlank()) {
            BookmarkRow(book.currentBookmark!!)
        }
    }
}

@Composable
private fun MetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(11.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = text,
            style = TextStyle(
                fontFamily = PlusJakartaSansFamily,
                fontSize = 11.sp,
                color = Color.Gray
            )
        )
    }
}

@Composable
private fun BookmarkRow(bookmark: String) {
    val colors = LocalReadTrackerColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Bookmark,
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.size(11.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = bookmark,
            style = TextStyle(
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = colors.accent
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BookCoverImage(
    book: Book,
    modifier: Modifier = Modifier
) {
    val colors = LocalReadTrackerColors.current
    val coverBgColor = Color(book.coverColor)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(RadiusSmall))
            .background(coverBgColor.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        val imagePath = book.localImagePath ?: book.coverUrl
        if (!imagePath.isNullOrBlank()) {
            if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                AsyncImage(
                    model = imagePath,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            } else {
                val file = File(imagePath)
                if (file.exists()) {
                    AsyncImage(
                        model = file,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.BrokenImage,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

private fun String?.isNull_orBlank(): Boolean = this == null || this.trim().isEmpty()
