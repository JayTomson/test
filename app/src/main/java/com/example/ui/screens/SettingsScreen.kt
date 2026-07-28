package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.AppSettings
import com.example.data.Book
import com.example.ui.AppState
import com.example.ui.components.CardGroup
import com.example.ui.components.CardGroupDivider
import com.example.ui.components.ColorPickerBottomSheet
import com.example.ui.components.CustomSwitch
import com.example.ui.components.SectionLabel
import com.example.ui.components.rememberBouncyOverscrollState
import com.example.ui.theme.LocalReadTrackerColors
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.RadiusLarge
import com.example.ui.theme.RadiusMedium
import com.example.ui.theme.RadiusSmall
import kotlinx.serialization.json.Json
import java.io.File

@Composable
fun SettingsScreen(
    appState: AppState,
    onUpdateSettings: (AppSettings) -> Unit,
    onImportLibrary: (List<Book>, Boolean) -> Unit,
    onImportSettingsJson: (String) -> Boolean,
    onResetColors: () -> Unit,
    onShowSnack: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalReadTrackerColors.current
    val settings = appState.settings

    var showHelpDialog by remember { mutableStateOf(false) }
    var activeColorPickerTarget by remember { mutableStateOf<String?>(null) }

    // Library Import Dialog State
    var pendingImportBooks by remember { mutableStateOf<List<Book>?>(null) }

    // JSON file picker for Library Import
    val libraryImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val jsonStr = inputStream?.bufferedReader()?.use { reader -> reader.readText() } ?: ""
                val (parsedList, corrupted) = Book.parseLibrary(jsonStr)
                if (corrupted || parsedList.isEmpty()) {
                    onShowSnack("Ошибка импорта: неверный формат файла")
                } else {
                    pendingImportBooks = parsedList
                }
            } catch (e: Exception) {
                onShowSnack("Ошибка импорта: ${e.message}")
            }
        }
    }

    // JSON file picker for Settings Import
    val settingsImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val jsonStr = inputStream?.bufferedReader()?.use { reader -> reader.readText() } ?: ""
                val success = onImportSettingsJson(jsonStr)
                if (success) {
                    onShowSnack("Настройки успешно импортированы")
                } else {
                    onShowSnack("Ошибка импорта настроек: неверный формат файла")
                }
            } catch (e: Exception) {
                onShowSnack("Ошибка экспорта/импорта: ${e.message}")
            }
        }
    }

    fun exportLibraryJson() {
        try {
            val jsonStr = Book.encodeLibrary(appState.books)
            val file = File(context.cacheDir, "readtracker_library_export.json")
            file.writeText(jsonStr)

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "ReadTracker — экспорт библиотеки")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Экспорт библиотеки"))
        } catch (e: Exception) {
            onShowSnack("Ошибка экспорта/импорта: ${e.message}")
        }
    }

    fun exportSettingsJson() {
        try {
            val jsonStr = AppSettings.encodeSettings(settings)
            val file = File(context.cacheDir, "readtracker_settings_export.json")
            file.writeText(jsonStr)

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "ReadTracker — экспорт настроек")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Экспорт настроек"))
        } catch (e: Exception) {
            onShowSnack("Ошибка экспорта/импорта: ${e.message}")
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
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = colors.textFg
                )
            }

            Text(
                text = "Настройки",
                style = TextStyle(
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    letterSpacing = (-0.5).sp,
                    color = colors.textFg
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
        }

        val bouncyState = rememberBouncyOverscrollState()

        LazyColumn(
            state = bouncyState.listState,
            modifier = bouncyState.modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))

                // Help Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(RadiusMedium))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    colors.accent.copy(alpha = 0.16f),
                                    colors.accent.copy(alpha = 0.04f)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = colors.accent.copy(alpha = 0.28f),
                            shape = RoundedCornerShape(RadiusMedium)
                        )
                        .clickable { showHelpDialog = true }
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(RadiusSmall))
                                .background(colors.accent.copy(alpha = 0.20f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Как узнать количество слов в книге?",
                                style = TextStyle(
                                    fontFamily = PlusJakartaSansFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = colors.textFg
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Нажмите — пошаговая инструкция",
                                style = TextStyle(
                                    fontFamily = PlusJakartaSansFamily,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Theme Mode
                SectionLabel("ТЕМА")
                CardGroup {
                    ThemeOptionRow(
                        title = "AMOLED",
                        icon = Icons.Default.DarkMode,
                        selected = settings.themeMode == 0,
                        onClick = { onUpdateSettings(settings.copy(themeMode = 0)) }
                    )
                    CardGroupDivider()
                    ThemeOptionRow(
                        title = "Тёмная",
                        icon = Icons.Default.NightlightRound,
                        selected = settings.themeMode == 1,
                        onClick = { onUpdateSettings(settings.copy(themeMode = 1)) }
                    )
                    CardGroupDivider()
                    ThemeOptionRow(
                        title = "Светлая",
                        icon = Icons.Default.WbSunny,
                        selected = settings.themeMode == 2,
                        onClick = { onUpdateSettings(settings.copy(themeMode = 2)) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Card Style
                SectionLabel("ВИД КАРТОЧЕК")
                CardGroup {
                    val cardStyles = listOf(
                        0 to Pair("Компактный", "Цветная полоска и вся информация в две строки"),
                        1 to Pair("С обложкой", "Обложка слева, бейджи и метрики справа"),
                        2 to Pair("Минимал", "Одна строка на тайтл — максимальная плотность"),
                        3 to Pair("Развёрнутый", "Крупный заголовок и прогресс-бар чтения")
                    )

                    cardStyles.forEachIndexed { idx, (styleCode, pair) ->
                        CardStyleOptionRow(
                            styleCode = styleCode,
                            title = pair.first,
                            subtitle = pair.second,
                            selected = settings.cardStyle == styleCode,
                            onClick = { onUpdateSettings(settings.copy(cardStyle = styleCode)) }
                        )
                        if (idx < cardStyles.lastIndex) {
                            CardGroupDivider()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Interface Switches
                SectionLabel("ИНТЕРФЕЙС")
                CardGroup {
                    SwitchRow(
                        title = "Исправление заголовка «Библиотека»",
                        subtitle = if (settings.fixLibraryTitle) "Крупное плотное начертание, не обрезается на узких экранах" else "Стандартное начертание",
                        checked = settings.fixLibraryTitle,
                        onCheckedChange = { onUpdateSettings(settings.copy(fixLibraryTitle = it)) }
                    )
                    CardGroupDivider()
                    SwitchRow(
                        title = "Выравнивание фильтров",
                        subtitle = if (settings.alignFilters) "Вкладки «Все», «Читаю»… начинаются ровно под заголовком" else "Вкладки у самого края экрана",
                        checked = settings.alignFilters,
                        onCheckedChange = { onUpdateSettings(settings.copy(alignFilters = it)) }
                    )
                    CardGroupDivider()
                    SwitchRow(
                        title = "Текст статуса в списке",
                        subtitle = if (settings.showStatusLabel) "Подписи «Читаю», «Брошено» и т.д. в карточках" else "Только цветной индикатор",
                        checked = settings.showStatusLabel,
                        onCheckedChange = { onUpdateSettings(settings.copy(showStatusLabel = it)) }
                    )
                    CardGroupDivider()
                    SwitchRow(
                        title = "Сокращать числа",
                        subtitle = if (settings.shortenNumbers) "150K вместо 150 000 в статистике и карточках" else "Точное отображение чисел",
                        checked = settings.shortenNumbers,
                        onCheckedChange = { onUpdateSettings(settings.copy(shortenNumbers = it)) }
                    )
                    CardGroupDivider()
                    SwitchRow(
                        title = "Отключить анимации",
                        subtitle = if (settings.disableAnimations) "Переходы между экранами мгновенные" else "Обычные системные переходы",
                        checked = settings.disableAnimations,
                        onCheckedChange = { onUpdateSettings(settings.copy(disableAnimations = it)) }
                    )
                    CardGroupDivider()
                    SwitchRow(
                        title = "Свечение кнопки «+»",
                        subtitle = if (settings.fabGlow) "Цветная тень под кнопкой добавления" else "Тень отключена",
                        checked = settings.fabGlow,
                        onCheckedChange = { onUpdateSettings(settings.copy(fabGlow = it)) }
                    )
                    CardGroupDivider()
                    SwitchRow(
                        title = "Кнопка «Поделиться»",
                        subtitle = if (settings.showShareButton) "Иконка экспорта карточек в шапке библиотеки" else "Кнопка скрыта",
                        checked = settings.showShareButton,
                        onCheckedChange = { onUpdateSettings(settings.copy(showShareButton = it)) }
                    )
                    CardGroupDivider()
                    SwitchRow(
                        title = "Скрыть нижний бар",
                        subtitle = if (settings.hideBottomBar) "Аналитика открывается кнопкой в шапке" else "Вкладки «Библиотека» и «Аналитика» внизу",
                        checked = settings.hideBottomBar,
                        onCheckedChange = { onUpdateSettings(settings.copy(hideBottomBar = it)) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Functions
                SectionLabel("ФУНКЦИИ")
                CardGroup {
                    SwitchRow(
                        title = "Закладки",
                        subtitle = if (settings.showBookmarks) "Заметка «где остановился» в карточке тайтла" else "Поле закладки скрыто",
                        checked = settings.showBookmarks,
                        onCheckedChange = { onUpdateSettings(settings.copy(showBookmarks = it)) }
                    )

                    if (settings.showBookmarks) {
                        CardGroupDivider()
                        DropdownRow(
                            title = "Расположение закладки",
                            valueText = if (settings.bookmarkPosition == 0) "Снизу" else "В ряд",
                            options = listOf("Снизу" to 0, "В ряд" to 1),
                            onSelectOption = { onUpdateSettings(settings.copy(bookmarkPosition = it)) }
                        )
                    }

                    CardGroupDivider()
                    SwitchRow(
                        title = "Старт после адаптации",
                        subtitle = if (settings.enableAdaptationStart) "Метка тома/главы, с которых вы начали читать" else "Метка старта скрыта",
                        checked = settings.enableAdaptationStart,
                        onCheckedChange = { onUpdateSettings(settings.copy(enableAdaptationStart = it)) }
                    )
                    CardGroupDivider()
                    SwitchRow(
                        title = "Гибридный формат LN+WN",
                        subtitle = if (settings.enableHybrid) "Тома лайт-новеллы и главы веб-новеллы в одной карточке" else "LN и WN ведутся отдельными карточками",
                        checked = settings.enableHybrid,
                        onCheckedChange = { onUpdateSettings(settings.copy(enableHybrid = it)) }
                    )
                    CardGroupDivider()
                    SwitchRow(
                        title = "Визуальные новеллы (VN)",
                        subtitle = if (settings.enableVN) "Отдельный формат с учётом пройденных концовок" else "Формат VN скрыт",
                        checked = settings.enableVN,
                        onCheckedChange = { onUpdateSettings(settings.copy(enableVN = it)) }
                    )
                    CardGroupDivider()
                    SwitchRow(
                        title = "Всего слов в тайтле",
                        subtitle = if (settings.enableTotalWords) "Поле «всего слов» и прогресс прочитано/всего" else "Только прочитанные слова",
                        checked = settings.enableTotalWords,
                        onCheckedChange = { onUpdateSettings(settings.copy(enableTotalWords = it)) }
                    )
                    CardGroupDivider()
                    SwitchRow(
                        title = "Оценка тайтлов",
                        subtitle = if (settings.enableRating) "Звёздный рейтинг в карточке тайтла" else "Рейтинг скрыт",
                        checked = settings.enableRating,
                        onCheckedChange = { onUpdateSettings(settings.copy(enableRating = it)) }
                    )

                    if (settings.enableRating) {
                        CardGroupDivider()
                        DropdownRow(
                            title = "Шкала оценки",
                            valueText = if (settings.ratingScale == 5) "5 звёзд" else "10 звёзд",
                            options = listOf("5 звёзд" to 5, "10 звёзд" to 10),
                            onSelectOption = { onUpdateSettings(settings.copy(ratingScale = it)) }
                        )
                    }

                    CardGroupDivider()
                    SwitchRow(
                        title = "Поиск и фильтр",
                        subtitle = if (settings.enableSearch) "Строка поиска и фильтр по формату над списком" else "Панель поиска скрыта",
                        checked = settings.enableSearch,
                        onCheckedChange = { onUpdateSettings(settings.copy(enableSearch = it)) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Analytics Settings
                SectionLabel("АНАЛИТИКА")
                CardGroup {
                    Text(
                        text = "Режим аналитики",
                        style = TextStyle(
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.textFg
                        ),
                        modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 4.dp)
                    )

                    val modes = listOf(
                        0 to "Серии + тома",
                        1 to "Серии + веб",
                        3 to "Серии + веб + VN",
                        2 to "Все метрики (Серии, веб, тома и визуальные новеллы)"
                    )

                    modes.forEach { (modeCode, modeLabel) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUpdateSettings(settings.copy(statsMode = modeCode)) }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.statsMode == modeCode,
                                onClick = { onUpdateSettings(settings.copy(statsMode = modeCode)) },
                                colors = RadioButtonDefaults.colors(selectedColor = colors.accent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = modeLabel,
                                style = TextStyle(
                                    fontFamily = PlusJakartaSansFamily,
                                    fontSize = 14.sp,
                                    color = colors.textFg
                                )
                            )
                        }
                    }

                    CardGroupDivider()
                    SwitchRow(
                        title = "Широкие карточки метрик",
                        subtitle = if (settings.stackedStats) "Метрики в столбик, крупнее" else "Метрики в один ряд",
                        checked = settings.stackedStats,
                        onCheckedChange = { onUpdateSettings(settings.copy(stackedStats = it)) }
                    )
                    CardGroupDivider()
                    SwitchRow(
                        title = "Главы веб-романов",
                        subtitle = if (settings.showWebChapters) "Прогресс X/Y гл. в карточках" else "Прогресс глав скрыт",
                        checked = settings.showWebChapters,
                        onCheckedChange = { onUpdateSettings(settings.copy(showWebChapters = it)) }
                    )
                    CardGroupDivider()
                    SwitchRow(
                        title = "Сортировка «Все» по статусу",
                        subtitle = if (settings.sortByStatus) "Читаю → активные → Завершено" else "Обычный порядок добавления",
                        checked = settings.sortByStatus,
                        onCheckedChange = { onUpdateSettings(settings.copy(sortByStatus = it)) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reorderable Analytics Sections
                Text(
                    text = "Разделы аналитики",
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = colors.textFg
                    )
                )
                Text(
                    text = "Включённые можно перетаскивать за ⠿, чтобы поменять порядок",
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                CardGroup {
                    val availableSections = listOf("topWords" to "Топ-5 по словам", "wordsByStatus" to "Слова по статусам")
                    val currentSections = settings.statsSections.toMutableList()

                    if (currentSections.isEmpty()) {
                        Text(
                            text = "Разделы не выбраны",
                            style = TextStyle(
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 13.sp,
                                color = colors.textSecondary
                            ),
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    availableSections.forEachIndexed { idx, (secKey, secLabel) ->
                        val isEnabled = currentSections.contains(secKey)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isEnabled,
                                onCheckedChange = { checked ->
                                    val updatedList = currentSections.toMutableList()
                                    if (checked) {
                                        if (!updatedList.contains(secKey)) updatedList.add(secKey)
                                    } else {
                                        updatedList.remove(secKey)
                                    }
                                    onUpdateSettings(settings.copy(statsSections = updatedList))
                                },
                                colors = CheckboxDefaults.colors(checkedColor = colors.accent)
                            )

                            Text(
                                text = secLabel,
                                style = TextStyle(
                                    fontFamily = PlusJakartaSansFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = colors.textFg
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            if (isEnabled) {
                                Icon(
                                    imageVector = Icons.Default.DragHandle,
                                    contentDescription = null,
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        if (idx < availableSections.lastIndex) {
                            CardGroupDivider()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Custom Colors
                SectionLabel("ПЕРСОНАЛИЗАЦИЯ")
                CardGroup {
                    val colorTiles = listOf(
                        "accent" to ("Основной цвет (акцент)" to settings.accent),
                        "cPlanned" to ("Статус: В планах" to settings.cPlanned),
                        "cReading" to ("Статус: Читаю" to settings.cReading),
                        "cPaused" to ("Статус: На паузе" to settings.cPaused),
                        "cCompleted" to ("Статус: Завершено" to settings.cCompleted),
                        "cDropped" to ("Статус: Брошено" to settings.cDropped),
                        "tagSeries" to ("Тег «Серия»" to settings.tagSeries),
                        "tagWeb" to ("Тег «Веб»" to settings.tagWeb),
                        "tagSingle" to ("Тег «Сингл»" to settings.tagSingle),
                        "tagHybrid" to ("Тег «LN+WN»" to settings.tagHybrid),
                        "tagOngoing" to ("Тег «Онг.»" to settings.tagOngoing)
                    )

                    colorTiles.forEachIndexed { idx, (key, pair) ->
                        ColorTileRow(
                            title = pair.first,
                            argb = pair.second,
                            onClick = { activeColorPickerTarget = key }
                        )
                        if (idx < colorTiles.lastIndex) {
                            CardGroupDivider()
                        }
                    }

                    CardGroupDivider()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onResetColors() }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.cDropped.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                tint = colors.cDropped,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Сбросить цвета",
                                style = TextStyle(
                                    fontFamily = PlusJakartaSansFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = colors.cDropped
                                )
                            )
                            Text(
                                text = "Вернуть стандартную палитру",
                                style = TextStyle(
                                    fontFamily = PlusJakartaSansFamily,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Data Actions
                SectionLabel("ДАННЫЕ")
                CardGroup {
                    DataActionTile(
                        title = "Экспорт библиотеки",
                        subtitle = "Сохранить тайтлы в JSON-файл",
                        icon = Icons.Default.UploadFile,
                        iconColor = colors.cReading,
                        onClick = { exportLibraryJson() }
                    )
                    CardGroupDivider()

                    DataActionTile(
                        title = "Импорт библиотеки",
                        subtitle = "Загрузить тайтлы из JSON-файла",
                        icon = Icons.Default.DownloadForOffline,
                        iconColor = colors.cPlanned,
                        onClick = { libraryImportLauncher.launch(arrayOf("application/json", "*/*")) }
                    )
                    CardGroupDivider()

                    DataActionTile(
                        title = "Экспорт настроек",
                        subtitle = "Все параметры и цвета в JSON-файл",
                        icon = Icons.Default.Tune,
                        iconColor = colors.cCompleted,
                        onClick = { exportSettingsJson() }
                    )
                    CardGroupDivider()

                    DataActionTile(
                        title = "Импорт настроек",
                        subtitle = "Применить параметры из JSON-файла",
                        icon = Icons.Default.SettingsBackupRestore,
                        iconColor = colors.cPaused,
                        onClick = { settingsImportLauncher.launch(arrayOf("application/json", "*/*")) }
                    )
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    // Color Picker Sheet
    activeColorPickerTarget?.let { targetKey ->
        val currentTargetColor = when (targetKey) {
            "cPlanned" -> settings.cPlanned
            "cReading" -> settings.cReading
            "cPaused" -> settings.cPaused
            "cCompleted" -> settings.cCompleted
            "cDropped" -> settings.cDropped
            "tagSeries" -> settings.tagSeries
            "tagWeb" -> settings.tagWeb
            "tagSingle" -> settings.tagSingle
            "tagHybrid" -> settings.tagHybrid
            "tagOngoing" -> settings.tagOngoing
            else -> settings.accent
        }

        ColorPickerBottomSheet(
            title = "Выбор цвета",
            initialColor = currentTargetColor,
            customColors = settings.customColors,
            onColorSelected = { newArgb ->
                val updated = when (targetKey) {
                    "cPlanned" -> settings.copy(cPlanned = newArgb)
                    "cReading" -> settings.copy(cReading = newArgb)
                    "cPaused" -> settings.copy(cPaused = newArgb)
                    "cCompleted" -> settings.copy(cCompleted = newArgb)
                    "cDropped" -> settings.copy(cDropped = newArgb)
                    "tagSeries" -> settings.copy(tagSeries = newArgb)
                    "tagWeb" -> settings.copy(tagWeb = newArgb)
                    "tagSingle" -> settings.copy(tagSingle = newArgb)
                    "tagHybrid" -> settings.copy(tagHybrid = newArgb)
                    "tagOngoing" -> settings.copy(tagOngoing = newArgb)
                    else -> settings.copy(accent = newArgb)
                }
                onUpdateSettings(updated)
            },
            onAddCustomColor = { newArgb ->
                if (!settings.customColors.contains(newArgb)) {
                    val updatedCustom = settings.customColors + newArgb
                    onUpdateSettings(settings.copy(customColors = updatedCustom))
                }
            },
            onDeleteCustomColor = { argb ->
                val updatedCustom = settings.customColors - argb
                onUpdateSettings(settings.copy(customColors = updatedCustom))
                onShowSnack("Свой цвет удалён")
            },
            onDismissRequest = { activeColorPickerTarget = null }
        )
    }

    // Help Instruction Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            containerColor = colors.cardBg,
            shape = RoundedCornerShape(RadiusLarge),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.accentDim),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Подсчёт слов",
                        style = TextStyle(
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = colors.textFg
                        )
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HelpBlock(
                        icon = Icons.Default.WarningAmber,
                        color = colors.cPaused,
                        title = "Moon+ Reader — счётчик врёт",
                        text = "Встроенный подсчёт слов в Moon+ Reader некорректный и сильно искажает статистику. Ориентироваться на него не стоит."
                    )
                    HelpBlock(
                        icon = Icons.Default.MenuBook,
                        color = colors.cReading,
                        title = "Calibre + плагин Count Pages",
                        text = "Надёжный способ: загрузите книгу в Calibre и установите плагин Count Pages — он точно считает слова по файлу."
                    )
                    HelpBlock(
                        icon = Icons.Default.Android,
                        color = colors.cPlanned,
                        title = "На Android — Winlator",
                        text = "Calibre — программа для ПК. На Android её можно запустить через эмулятор Winlator. Желательно отключить OpenGL в настройках эмулятора, чтобы Count Pages работал стабильно."
                    )
                    HelpBlock(
                        icon = Icons.Default.LocalLibrary,
                        color = colors.cCompleted,
                        title = "Читали печатную книгу?",
                        text = "Просто найдите epub-файл того же перевода и издателя и посчитайте слова в нём — объём совпадёт с достаточной точностью."
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text(
                        text = "Понятно",
                        style = TextStyle(
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent
                        )
                    )
                }
            }
        )
    }

    // Library Import Option Dialog
    pendingImportBooks?.let { importedList ->
        AlertDialog(
            onDismissRequest = { pendingImportBooks = null },
            containerColor = colors.cardBg,
            shape = RoundedCornerShape(RadiusLarge),
            title = {
                Text(
                    text = "Импорт библиотеки",
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
                    text = "Будет загружено ${importedList.size} тайтлов. Дополнить текущую библиотеку или полностью заменить её?",
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 14.sp,
                        color = colors.textSecondary
                    )
                )
            },
            confirmButton = {
                Row {
                    TextButton(
                        onClick = {
                            onImportLibrary(importedList, false)
                            onShowSnack("Добавлено/обновлено ${importedList.size} тайтлов")
                            pendingImportBooks = null
                        }
                    ) {
                        Text(
                            text = "Дополнить",
                            style = TextStyle(
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Bold,
                                color = colors.accent
                            )
                        )
                    }

                    TextButton(
                        onClick = {
                            onImportLibrary(importedList, true)
                            onShowSnack("Загружено ${importedList.size} тайтлов")
                            pendingImportBooks = null
                        }
                    ) {
                        Text(
                            text = "Заменить",
                            style = TextStyle(
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Bold,
                                color = colors.cDropped
                            )
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportBooks = null }) {
                    Text(
                        text = "Отмена",
                        style = TextStyle(
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textSecondary
                        )
                    )
                }
            }
        )
    }
}

@Composable
private fun ThemeOptionRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalReadTrackerColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) colors.accent else colors.textSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = TextStyle(
                fontFamily = PlusJakartaSansFamily,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp,
                color = if (selected) colors.accent else colors.textFg
            ),
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = if (selected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (selected) colors.accent else colors.textSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun CardStyleOptionRow(
    styleCode: Int,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalReadTrackerColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mini preview 46x38dp
        Box(
            modifier = Modifier
                .size(width = 46.dp, height = 38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (colors.screenBg == Color.Black || colors.cardBg == Color(0xFF1C1C1E) || colors.cardBg == Color(0xFF141414)) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.04f))
                .border(
                    width = if (selected) 1.5.dp else 1.dp,
                    color = if (selected) colors.accent.copy(alpha = 0.60f) else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            val lineColor = if (selected) colors.accent else colors.textSecondary
            val bgLine = if (colors.screenBg == Color.Black || colors.cardBg == Color(0xFF1C1C1E) || colors.cardBg == Color(0xFF141414)) Color.White.copy(alpha = 0.24f) else Color.Black.copy(alpha = 0.26f)

            when (styleCode) {
                0 -> {
                    // Compact: 3x20 strip + 2 lines
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(width = 3.dp, height = 20.dp).clip(RoundedCornerShape(1.dp)).background(lineColor))
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Box(modifier = Modifier.size(width = 24.dp, height = 4.dp).clip(RoundedCornerShape(2.dp)).background(bgLine))
                            Spacer(modifier = Modifier.height(3.dp))
                            Box(modifier = Modifier.size(width = 16.dp, height = 3.dp).clip(RoundedCornerShape(2.dp)).background(bgLine))
                        }
                    }
                }
                1 -> {
                    // Cover: 11x16 rect + 2 lines
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(width = 11.dp, height = 16.dp).clip(RoundedCornerShape(2.dp)).background(lineColor.copy(alpha = 0.3f)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Box(modifier = Modifier.size(width = 20.dp, height = 4.dp).clip(RoundedCornerShape(2.dp)).background(bgLine))
                            Spacer(modifier = Modifier.height(3.dp))
                            Box(modifier = Modifier.size(width = 14.dp, height = 3.dp).clip(RoundedCornerShape(2.dp)).background(bgLine))
                        }
                    }
                }
                2 -> {
                    // Minimal: dot 6dp + line
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(lineColor))
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(modifier = Modifier.size(width = 24.dp, height = 4.dp).clip(RoundedCornerShape(2.dp)).background(bgLine))
                    }
                }
                else -> {
                    // Expanded: short block, line, progress bar 60%
                    Column {
                        Box(modifier = Modifier.size(width = 28.dp, height = 4.dp).clip(RoundedCornerShape(2.dp)).background(bgLine))
                        Spacer(modifier = Modifier.height(3.dp))
                        Box(modifier = Modifier.size(width = 32.dp, height = 3.dp).clip(RoundedCornerShape(2.dp)).background(lineColor))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.textFg))
            Text(subtitle, style = TextStyle(fontFamily = PlusJakartaSansFamily, fontSize = 12.sp, color = colors.textSecondary))
        }

        Icon(
            imageVector = if (selected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (selected) colors.accent else colors.textSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = LocalReadTrackerColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = colors.textFg
                )
            )
            Text(
                text = subtitle,
                style = TextStyle(
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        CustomSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun <T> DropdownRow(
    title: String,
    valueText: String,
    options: List<Pair<String, T>>,
    onSelectOption: (T) -> Unit
) {
    val colors = LocalReadTrackerColors.current
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = colors.textFg
                )
            )

            Text(
                text = valueText,
                style = TextStyle(
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = colors.accent
                )
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(colors.cardBg)
        ) {
            options.forEach { (label, value) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = label,
                            style = TextStyle(
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Bold,
                                color = colors.textFg
                            )
                        )
                    },
                    onClick = {
                        onSelectOption(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ColorTileRow(
    title: String,
    argb: Int,
    onClick: () -> Unit
) {
    val colors = LocalReadTrackerColors.current
    val swatchColor = Color(argb)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(swatchColor)
                .border(
                    width = 1.dp,
                    color = if (colors.screenBg == Color.Black || colors.cardBg == Color(0xFF1C1C1E) || colors.cardBg == Color(0xFF141414)) Color.White.copy(alpha = 0.24f) else Color.Black.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(7.dp)
                )
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            style = TextStyle(
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = colors.textFg
            ),
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun DataActionTile(
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
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(RadiusSmall))
                .background(iconColor.copy(alpha = 0.12f)),
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
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = colors.textFg
                )
            )
            Text(
                text = subtitle,
                style = TextStyle(
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun HelpBlock(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    title: String,
    text: String
) {
    val colors = LocalReadTrackerColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RadiusMedium))
            .background(color.copy(alpha = 0.07f))
            .border(width = 1.dp, color = color.copy(alpha = 0.18f), shape = RoundedCornerShape(RadiusMedium))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = color
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = text,
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                )
            }
        }
    }
}
