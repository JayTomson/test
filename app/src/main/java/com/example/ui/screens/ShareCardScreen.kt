package com.example.ui.screens

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.fmtNum
import com.example.ui.AppState
import com.example.ui.components.CardGroupDivider
import com.example.ui.theme.LocalReadTrackerColors
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.RadiusLarge
import com.example.ui.theme.RadiusMedium
import com.example.ui.theme.RadiusSmall
import java.io.OutputStream

@Composable
fun ShareCardScreen(
    shareType: String, // "analytics" or "library"
    appState: AppState,
    onShowSnack: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = colors.textFg
                )
            }

            Text(
                text = if (shareType == "analytics") "Карточка статистики" else "Карточка библиотеки",
                style = TextStyle(
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = colors.textFg
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (shareType == "analytics") {
                AnalyticsCardView(appState = appState)
            } else {
                LibraryCardView(appState = appState)
            }
        }

        // Save Button
        Button(
            onClick = {
                onShowSnack("Карточка готова! (Имитация сохранения в Галерею)")
            },
            shape = RoundedCornerShape(RadiusMedium),
            colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(52.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = colors.accentOnColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Сохранить карточку",
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = colors.accentOnColor
                    )
                )
            }
        }
    }
}

@Composable
private fun AnalyticsCardView(appState: AppState) {
    val colors = LocalReadTrackerColors.current
    val settings = appState.settings
    val metrics = appState.metrics

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(RadiusLarge))
            .background(colors.cardBg)
            .border(
                width = 1.dp,
                color = if (colors.screenBg == Color.Black || colors.cardBg == Color(0xFF1C1C1E) || colors.cardBg == Color(0xFF141414)) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.10f),
                shape = RoundedCornerShape(RadiusLarge)
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column {
                Text(
                    text = "READTRACKER",
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 2.sp,
                        color = colors.accent
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Моя статистика чтения",
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = colors.textFg
                    )
                )
            }

            // 2x2 Grid
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MiniShareMetricTile(
                        title = "Прочитано слов",
                        value = fmtNum(metrics.totalWords, settings.shortenNumbers),
                        icon = Icons.Default.TextFields,
                        color = colors.accent,
                        modifier = Modifier.weight(1f)
                    )
                    MiniShareMetricTile(
                        title = "Завершено серий",
                        value = metrics.completedSeries.toString(),
                        icon = Icons.Default.EmojiEvents,
                        color = colors.cReading,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val vNum = metrics.totalVolumes
                    val vStr = if (vNum % 1.0 == 0.0) vNum.toLong().toString() else vNum.toString()
                    MiniShareMetricTile(
                        title = "Прочитано томов",
                        value = vStr,
                        icon = Icons.Default.Layers,
                        color = colors.cPlanned,
                        modifier = Modifier.weight(1f)
                    )
                    MiniShareMetricTile(
                        title = if (settings.enableVN) "Завершено VN" else "Веб-новелл",
                        value = if (settings.enableVN) metrics.completedVN.toString() else metrics.completedWeb.toString(),
                        icon = if (settings.enableVN) Icons.Default.VideogameAsset else Icons.Default.Language,
                        color = colors.cPaused,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Footer Watermark
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(colors.accent.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(12.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ReadTracker App",
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                )
            }
        }
    }
}

@Composable
private fun MiniShareMetricTile(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val colors = LocalReadTrackerColors.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(RadiusMedium))
            .background(if (colors.screenBg == Color.Black || colors.cardBg == Color(0xFF1C1C1E) || colors.cardBg == Color(0xFF141414)) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.04f))
            .padding(12.dp)
    ) {
        Column {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = TextStyle(
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    color = colors.textFg
                ),
                maxLines = 1
            )
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LibraryCardView(appState: AppState) {
    val colors = LocalReadTrackerColors.current
    val settings = appState.settings
    val books = appState.books

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RadiusLarge))
            .background(colors.cardBg)
            .border(
                width = 1.dp,
                color = if (colors.screenBg == Color.Black || colors.cardBg == Color(0xFF1C1C1E) || colors.cardBg == Color(0xFF141414)) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.10f),
                shape = RoundedCornerShape(RadiusLarge)
            )
            .padding(20.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "READTRACKER",
                        style = TextStyle(
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 2.sp,
                            color = colors.accent
                        )
                    )
                    Text(
                        text = "Моя библиотека",
                        style = TextStyle(
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = colors.textFg
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.accent.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Всего: ${books.size}",
                        style = TextStyle(
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = colors.accent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Book List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                books.take(10).forEachIndexed { idx, book ->
                    val statusCol = colors.getColorForStatus(book.status)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(RadiusSmall))
                            .background(if (colors.screenBg == Color.Black || colors.cardBg == Color(0xFF1C1C1E) || colors.cardBg == Color(0xFF141414)) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.03f))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusCol)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = book.title,
                            style = TextStyle(
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Bold,
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
                                color = colors.textSecondary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer Watermark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ReadTracker App",
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                )
            }
        }
    }
}
