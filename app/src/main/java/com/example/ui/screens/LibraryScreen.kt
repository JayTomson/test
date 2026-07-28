package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Book
import com.example.data.BookStatus
import com.example.ui.AppState
import com.example.ui.components.BookCard
import com.example.ui.theme.LocalReadTrackerColors
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.RadiusLarge
import com.example.ui.theme.RadiusMedium
import com.example.ui.theme.RadiusSmall
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    appState: AppState,
    onTabSelected: (Int) -> Unit,
    onSearchChanged: (String) -> Unit,
    onFilterChanged: (Set<String>) -> Unit,
    onEditBook: (Book) -> Unit,
    onDeleteBook: (String) -> Unit,
    onOpenAnalytics: () -> Unit,
    onOpenShareCard: (shareType: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalReadTrackerColors.current
    val settings = appState.settings
    var showFormatFilterSheet by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    var bookToDelete by remember { mutableStateOf<Book?>(null) }

    // Search Debounce state
    var localSearchText by remember(appState.searchQuery) { mutableStateOf(appState.searchQuery) }
    LaunchedEffect(localSearchText) {
        if (localSearchText != appState.searchQuery) {
            delay(250)
            onSearchChanged(localSearchText)
        }
    }

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
            if (settings.fixLibraryTitle) {
                Text(
                    text = "Библиотека",
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        letterSpacing = (-0.6).sp,
                        color = colors.textFg
                    ),
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Text(
                    text = "Библиотека",
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        letterSpacing = (-0.5).sp,
                        color = colors.textFg
                    ),
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (settings.hideBottomBar) {
                    IconButtonContainer(
                        onClick = onOpenAnalytics,
                        icon = Icons.Default.BarChart,
                        tint = colors.accent
                    )
                }

                if (settings.showShareButton) {
                    IconButtonContainer(
                        onClick = { showShareSheet = true },
                        icon = Icons.Default.IosShare,
                        tint = colors.accent
                    )
                }
            }
        }

        // Tabs
        val tabs = listOf("Все", "Читаю", "В планах", "Завершено", "На паузе", "Брошено")
        val currentTabIndex = appState.selectedTab.coerceIn(0, 5)

        ScrollableTabRow(
            selectedTabIndex = currentTabIndex,
            edgePadding = if (settings.alignFilters) 6.dp else 16.dp,
            containerColor = colors.screenBg,
            indicator = { tabPositions ->
                if (currentTabIndex in tabPositions.indices) {
                    SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[currentTabIndex]),
                        height = 2.2.dp,
                        color = colors.accent
                    )
                }
            },
            divider = {},
            modifier = Modifier.height(34.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = (currentTabIndex == index)
                Tab(
                    selected = isSelected,
                    onClick = { onTabSelected(index) },
                    text = {
                        Text(
                            text = title,
                            style = TextStyle(
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isSelected) colors.accent else Color.Gray
                            )
                        )
                    },
                    modifier = Modifier.height(28.dp)
                )
            }
        }

        // Search panel
        if (settings.enableSearch) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = localSearchText,
                    onValueChange = { localSearchText = it },
                    placeholder = {
                        Text(
                            text = "Поиск по названию...",
                            style = TextStyle(
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (localSearchText.isNotEmpty()) {
                            IconButton(onClick = {
                                localSearchText = ""
                                onSearchChanged("")
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(RadiusMedium),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.cardBg,
                        unfocusedContainerColor = colors.cardBg,
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = colors.textFg,
                        unfocusedTextColor = colors.textFg
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                )

                val isFilterActive = appState.selectedFormatFilters.isNotEmpty()
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(RadiusMedium))
                        .background(if (isFilterActive) colors.accent.copy(alpha = 0.15f) else colors.cardBg)
                        .clickable { showFormatFilterSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterAlt,
                        contentDescription = null,
                        tint = if (isFilterActive) colors.accent else Color.Gray,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }

        // Filter books
        val filteredBooks = remember(
            appState.books,
            currentTabIndex,
            appState.searchQuery,
            appState.selectedFormatFilters,
            settings.sortByStatus
        ) {
            appState.books.filter { book ->
                // Tab status filter
                val statusMatch = when (currentTabIndex) {
                    1 -> book.status == BookStatus.READING
                    2 -> book.status == BookStatus.PLANNED
                    3 -> book.status == BookStatus.COMPLETED
                    4 -> book.status == BookStatus.PAUSED
                    5 -> book.status == BookStatus.DROPPED
                    else -> true
                }
                if (!statusMatch) return@filter false

                // Search query
                if (appState.searchQuery.isNotBlank()) {
                    if (!book.title.contains(appState.searchQuery, ignoreCase = true)) {
                        return@filter false
                    }
                }

                // Format filter
                if (appState.selectedFormatFilters.isNotEmpty()) {
                    val passFormat = appState.selectedFormatFilters.any { key ->
                        when (key) {
                            "vn" -> book.isVN
                            "hybrid" -> book.isHybridFormat
                            "series" -> book.isSeries && !book.isHybridFormat
                            "web" -> book.isWeb && !book.isHybridFormat
                            "single" -> book.isSingle
                            else -> false
                        }
                    }
                    if (!passFormat) return@filter false
                }

                true
            }.let { list ->
                if (currentTabIndex == 0 && settings.sortByStatus) {
                    list.sortedBy { it.status.sortPriority }
                } else {
                    list
                }
            }
        }

        // Book list or Empty state
        if (filteredBooks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(colors.accentDim),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Список пуст",
                        style = TextStyle(
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = colors.textFg
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Нажмите + чтобы добавить тайтл",
                        style = TextStyle(
                            fontFamily = PlusJakartaSansFamily,
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    )
                }
            }
        } else {
            val listState = rememberLazyListState()

            // Bounce scroll effect
            var overscrollOffset by remember { mutableStateOf(0f) }
            val coroutineScope = rememberCoroutineScope()

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(0, overscrollOffset.roundToInt()) }
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            val isAtTop = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
                            if (isAtTop && delta > 0) {
                                overscrollOffset = (overscrollOffset + delta * 0.4f).coerceAtMost(100f)
                            } else if (overscrollOffset > 0) {
                                overscrollOffset = (overscrollOffset + delta * 0.4f).coerceAtLeast(0f)
                            }
                        },
                        onDragStopped = {
                            coroutineScope.launch {
                                overscrollOffset = 0f
                            }
                        }
                    )
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }
                items(filteredBooks, key = { it.id }) { book ->
                    BookCard(
                        book = book,
                        settings = settings,
                        onClick = { onEditBook(book) },
                        onLongClick = { bookToDelete = book }
                    )
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }

    // Format Filter Bottom Sheet
    if (showFormatFilterSheet) {
        FormatFilterBottomSheet(
            selectedFilters = appState.selectedFormatFilters,
            enableVN = settings.enableVN,
            onApply = { newFilters ->
                onFilterChanged(newFilters)
                showFormatFilterSheet = false
            },
            onDismissRequest = { showFormatFilterSheet = false }
        )
    }

    // Share Options Bottom Sheet
    if (showShareSheet) {
        ShareBottomSheet(
            onSelectOption = { shareType ->
                showShareSheet = false
                onOpenShareCard(shareType)
            },
            onDismissRequest = { showShareSheet = false }
        )
    }

    // Delete Book Confirmation Dialog
    bookToDelete?.let { book ->
        AlertDialog(
            onDismissRequest = { bookToDelete = null },
            containerColor = colors.cardBg,
            shape = RoundedCornerShape(RadiusLarge),
            title = {
                Text(
                    text = "Удалить тайтл?",
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = colors.textFg
                    )
                )
            },
            text = {
                Text(
                    text = "«${book.title}» будет удалён без возможности восстановления.",
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteBook(book.id)
                        bookToDelete = null
                    }
                ) {
                    Text(
                        text = "Удалить",
                        style = TextStyle(
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            color = colors.cDropped
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { bookToDelete = null }
                ) {
                    Text(
                        text = "Отмена",
                        style = TextStyle(
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                    )
                }
            }
        )
    }
}

@Composable
private fun IconButtonContainer(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    val colors = LocalReadTrackerColors.current
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (colors.screenBg == Color.Black || colors.cardBg == Color(0xFF1C1C1E) || colors.cardBg == Color(0xFF141414)) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FormatFilterBottomSheet(
    selectedFilters: Set<String>,
    enableVN: Boolean,
    onApply: (Set<String>) -> Unit,
    onDismissRequest: () -> Unit
) {
    val colors = LocalReadTrackerColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var localFilters by remember { mutableStateOf(selectedFilters) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = colors.cardBg,
        shape = RoundedCornerShape(topStart = RadiusLarge, topEnd = RadiusLarge)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Фильтр по формату",
                style = TextStyle(
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = colors.textFg
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            val options = mutableListOf(
                "series" to "Серии",
                "web" to "Веб-новеллы",
                "single" to "Синглы",
                "hybrid" to "LN+WN Гибрид"
            )
            if (enableVN) {
                options.add("vn" to "Визуальные новеллы")
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { (key, label) ->
                    val isSelected = localFilters.contains(key)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) colors.accent else Color.White.copy(alpha = 0.06f))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) colors.accent else Color.White.copy(alpha = 0.10f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                localFilters = if (isSelected) localFilters - key else localFilters + key
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = label,
                            style = TextStyle(
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isSelected) Color.Black else Color.LightGray
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        localFilters = emptySet()
                        onApply(emptySet())
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textFg),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Сбросить",
                        style = TextStyle(
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                }

                Button(
                    onClick = { onApply(localFilters) },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Применить",
                        style = TextStyle(
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color.Black
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareBottomSheet(
    onSelectOption: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val colors = LocalReadTrackerColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = colors.cardBg,
        shape = RoundedCornerShape(topStart = RadiusLarge, topEnd = RadiusLarge)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.3f))
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Поделиться",
                style = TextStyle(
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = colors.textFg
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            ShareOptionTile(
                title = "Аналитика",
                subtitle = "Карточка со статистикой",
                icon = Icons.Default.Analytics,
                iconColor = colors.accent,
                onClick = { onSelectOption("analytics") }
            )

            Spacer(modifier = Modifier.height(10.dp))

            ShareOptionTile(
                title = "Список тайтлов",
                subtitle = "Все тайтлы в одной карточке",
                icon = Icons.Default.FormatListBulleted,
                iconColor = colors.cReading,
                onClick = { onSelectOption("library") }
            )
        }
    }
}

@Composable
private fun ShareOptionTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    val colors = LocalReadTrackerColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RadiusMedium))
            .background(iconColor.copy(alpha = 0.08f))
            .border(width = 1.dp, color = iconColor.copy(alpha = 0.18f), shape = RoundedCornerShape(RadiusMedium))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(RadiusSmall))
                .background(iconColor.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = colors.textFg
                )
            )
            Text(
                text = subtitle,
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
            tint = iconColor,
            modifier = Modifier.size(15.dp)
        )
    }
}
