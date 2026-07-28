package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.OutlinedFlag
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BookStatus
import com.example.data.fmtNum
import com.example.ui.AppState
import com.example.ui.components.CardGroup
import com.example.ui.components.CardGroupDivider
import com.example.ui.components.SectionLabel
import com.example.ui.theme.LocalReadTrackerColors
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.RadiusMedium
import com.example.ui.theme.RadiusSmall

@Composable
fun AnalyticsScreen(
    appState: AppState,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalReadTrackerColors.current
    val settings = appState.settings
    val metrics = appState.metrics

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.screenBg)
    ) {
        // AppBar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Аналитика",
                style = TextStyle(
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    letterSpacing = (-0.5).sp,
                    color = colors.textFg
                ),
                modifier = Modifier.weight(1f)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))

                // Metric Cards Section
                val smallCards = mutableListOf<MetricCardData>()

                smallCards.add(
                    MetricCardData(
                        title = "Завершено серий",
                        value = metrics.completedSeries.toString(),
                        icon = Icons.Default.EmojiEvents,
                        color = colors.cReading
                    )
                )

                if (settings.showWebStats) {
                    smallCards.add(
                        MetricCardData(
                            title = if (settings.stackedStats) "Завершено веб-новелл" else "Завершено веб",
                            value = metrics.completedWeb.toString(),
                            icon = Icons.Default.Language,
                            color = colors.cCompleted
                        )
                    )
                }

                if (settings.showVolumeStats && metrics.anyBooksCountVolumes) {
                    val vNum = metrics.totalVolumes
                    val vStr = if (vNum % 1.0 == 0.0) vNum.toLong().toString() else vNum.toString()
                    smallCards.add(
                        MetricCardData(
                            title = "Прочитано томов",
                            value = vStr,
                            icon = Icons.Default.Layers,
                            color = colors.cPlanned
                        )
                    )
                }

                if (settings.showVNStats && metrics.anyVN) {
                    smallCards.add(
                        MetricCardData(
                            title = "Завершено VN",
                            value = metrics.completedVN.toString(),
                            icon = Icons.Default.VideogameAsset,
                            color = colors.cPaused
                        )
                    )
                    smallCards.add(
                        MetricCardData(
                            title = "Концовки",
                            value = metrics.totalEndings.toString(),
                            icon = Icons.Default.OutlinedFlag,
                            color = colors.cCompleted
                        )
                    )
                }

                if (settings.stackedStats) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        smallCards.forEach { data ->
                            MetricCardTile(data = data, modifier = Modifier.fillMaxWidth())
                        }
                    }
                } else {
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        smallCards.forEach { data ->
                            MetricCardTile(
                                data = data,
                                modifier = Modifier
                                    .fillMaxWidth(0.48f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Wide card: Total Words
                MetricCardTile(
                    data = MetricCardData(
                        title = "Прочитано слов",
                        value = fmtNum(metrics.totalWords, settings.shortenNumbers),
                        icon = Icons.Default.TextFields,
                        color = colors.accent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Dynamic Sections from statsSections
                settings.statsSections.forEach { sectionKey ->
                    when (sectionKey) {
                        "topWords" -> {
                            if (metrics.topByWords.isNotEmpty()) {
                                SectionLabel("ТОП-5 ПО СЛОВАМ")
                                CardGroup {
                                    metrics.topByWords.forEachIndexed { idx, book ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${idx + 1}",
                                                style = TextStyle(
                                                    fontFamily = PlusJakartaSansFamily,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 15.sp,
                                                    color = if (idx == 0) colors.accent else Color.Gray
                                                ),
                                                modifier = Modifier.width(24.dp)
                                            )
                                            Text(
                                                text = book.title,
                                                style = TextStyle(
                                                    fontFamily = PlusJakartaSansFamily,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp,
                                                    color = colors.textFg
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "${fmtNum(book.effectiveWords, settings.shortenNumbers)} сл.",
                                                style = TextStyle(
                                                    fontFamily = PlusJakartaSansFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = Color.Gray
                                                )
                                            )
                                        }
                                        if (idx < metrics.topByWords.lastIndex) {
                                            CardGroupDivider()
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                        "wordsByStatus" -> {
                            SectionLabel("СЛОВА ПО СТАТУСАМ")
                            CardGroup {
                                BookStatus.entries.forEachIndexed { idx, st ->
                                    val stColor = colors.getColorForStatus(st)
                                    val stWords = metrics.wordsByStatus[st.ordinal]
                                    val fraction = if (metrics.totalWords > 0) stWords.toFloat() / metrics.totalWords.toFloat() else 0f

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(9.dp)
                                                        .clip(CircleShape)
                                                        .background(stColor)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = st.label,
                                                    style = TextStyle(
                                                        fontFamily = PlusJakartaSansFamily,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 13.sp,
                                                        color = colors.textFg
                                                    )
                                                )
                                            }

                                            Text(
                                                text = "${fmtNum(stWords, settings.shortenNumbers)} сл.",
                                                style = TextStyle(
                                                    fontFamily = PlusJakartaSansFamily,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.sp,
                                                    color = stColor
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(stColor.copy(alpha = 0.10f))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                                                    .height(4.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(stColor)
                                            )
                                        }
                                    }

                                    if (idx < BookStatus.entries.lastIndex) {
                                        CardGroupDivider()
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }

                // "ПО СТАТУСАМ" (always present)
                SectionLabel("ПО СТАТУСАМ")
                val totalBooksCount = appState.books.size
                CardGroup {
                    BookStatus.entries.forEachIndexed { idx, st ->
                        val stColor = colors.getColorForStatus(st)
                        val stCount = metrics.countByStatus[st.ordinal]
                        val fraction = if (totalBooksCount > 0) stCount.toFloat() / totalBooksCount.toFloat() else 0f

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(9.dp)
                                            .clip(CircleShape)
                                            .background(stColor)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = st.label,
                                        style = TextStyle(
                                            fontFamily = PlusJakartaSansFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = colors.textFg
                                        )
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(stColor.copy(alpha = 0.12f))
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = stCount.toString(),
                                        style = TextStyle(
                                            fontFamily = PlusJakartaSansFamily,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            color = stColor
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(stColor.copy(alpha = 0.10f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction.coerceIn(0f, 1f))
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(stColor)
                                )
                            }
                        }

                        if (idx < BookStatus.entries.lastIndex) {
                            CardGroupDivider()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Settings tile
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenSettings),
                    shape = RoundedCornerShape(RadiusMedium),
                    color = colors.cardBg
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(RadiusSmall))
                                .background(colors.accentDim),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Настройки",
                                style = TextStyle(
                                    fontFamily = PlusJakartaSansFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = colors.textFg
                                )
                            )
                            Text(
                                text = "Управление функциями, тема, экспорт",
                                style = TextStyle(
                                    fontFamily = PlusJakartaSansFamily,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

private data class MetricCardData(
    val title: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

@Composable
private fun MetricCardTile(
    data: MetricCardData,
    modifier: Modifier = Modifier
) {
    val colors = LocalReadTrackerColors.current

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(RadiusMedium),
        color = colors.cardBg
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(RadiusSmall))
                    .background(data.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = data.icon,
                    contentDescription = null,
                    tint = data.color,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = data.value,
                style = TextStyle(
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    color = colors.textFg
                ),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = data.title,
                style = TextStyle(
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color.Gray
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
