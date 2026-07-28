package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.ui.components.rememberBouncyOverscrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppSettings
import com.example.data.Book
import com.example.data.BookStatus
import com.example.data.VolumeEntry
import com.example.data.fmtNum
import com.example.ui.components.BookCoverImage
import com.example.ui.components.CardGroup
import com.example.ui.components.CardGroupDivider
import com.example.ui.components.CustomSwitch
import com.example.ui.components.SectionLabel
import com.example.ui.theme.LocalReadTrackerColors
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.RadiusLarge
import com.example.ui.theme.RadiusMedium
import com.example.ui.theme.RadiusSmall
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBookScreen(
    initialBook: Book?,
    settings: AppSettings,
    onSaveBook: (Book) -> Unit,
    onShowSnack: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalReadTrackerColors.current
    val isEditMode = (initialBook != null)

    var currentStep by remember { mutableIntStateOf(0) }

    // Form fields
    var id by remember { mutableStateOf(initialBook?.id ?: System.currentTimeMillis().toString()) }
    var title by remember { mutableStateOf(initialBook?.title ?: "") }
    var status by remember { mutableStateOf(initialBook?.status ?: BookStatus.READING) }

    // Format flags
    var isSeries by remember { mutableStateOf(initialBook?.isSeries ?: false) }
    var isWeb by remember { mutableStateOf(initialBook?.isWeb ?: false) }
    var isSingle by remember { mutableStateOf(initialBook?.isSingle ?: false) }
    var isVN by remember { mutableStateOf(initialBook?.isVN ?: false) }
    var isHybridFormat by remember { mutableStateOf(initialBook?.isHybridFormat ?: false) }
    var countVolumes by remember { mutableStateOf(initialBook?.countVolumes ?: true) }

    var isOngoing by remember { mutableStateOf(initialBook?.isOngoing ?: false) }
    var useDetailedVolumes by remember { mutableStateOf(initialBook?.useDetailedVolumes ?: false) }

    // Number fields (stored as String for input fields)
    var wordsStr by remember { mutableStateOf(initialBook?.words?.toString() ?: "") }
    var volumesStr by remember { mutableStateOf(initialBook?.volumes?.toString() ?: "") }
    var totalVolumesInSeriesStr by remember { mutableStateOf(initialBook?.totalVolumesInSeries?.toString() ?: "") }
    var webChaptersStr by remember { mutableStateOf(initialBook?.webChapters?.toString() ?: "") }
    var totalWebChaptersStr by remember { mutableStateOf(initialBook?.totalWebChapters?.toString() ?: "") }
    var endingsReadStr by remember { mutableStateOf(initialBook?.endingsRead?.toString() ?: "") }
    var endingsTotalStr by remember { mutableStateOf(initialBook?.endingsTotal?.toString() ?: "") }
    var totalWordsInBookStr by remember { mutableStateOf(initialBook?.totalWordsInBook?.toString() ?: "") }
    var hybridWebChaptersStr by remember { mutableStateOf(initialBook?.hybridWebChapters?.toString() ?: "") }
    var hybridTotalWebChaptersStr by remember { mutableStateOf(initialBook?.hybridTotalWebChapters?.toString() ?: "") }

    var rating by remember { mutableStateOf(initialBook?.rating) }
    var startVolumeStr by remember { mutableStateOf(initialBook?.startVolume?.toString() ?: "") }
    var startChapterStr by remember { mutableStateOf(initialBook?.startChapter?.toString() ?: "") }

    var currentBookmark by remember { mutableStateOf(initialBook?.currentBookmark ?: "") }
    var coverUrl by remember { mutableStateOf(initialBook?.coverUrl ?: "") }
    var localImagePath by remember { mutableStateOf(initialBook?.localImagePath ?: "") }
    var coverColor by remember { mutableIntStateOf(initialBook?.coverColor ?: 0xFF607D8B.toInt()) }

    val volumeEntriesList = remember {
        mutableStateListOf<VolumeEntry>().apply {
            if (initialBook != null && initialBook.volumeEntries.isNotEmpty()) {
                addAll(initialBook.volumeEntries)
            }
        }
    }

    // Cover picker bottom sheet
    var showCoverPickerSheet by remember { mutableStateOf(false) }
    var showUrlDialog by remember { mutableStateOf(false) }

    // Format change confirmation dialog
    var pendingFormatSelection by remember { mutableStateOf<String?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.filesDir, "cover_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                localImagePath = file.absolutePath
                coverUrl = ""
            } catch (_: Exception) {
                onShowSnack("Ошибка загрузки изображения")
            }
        }
    }

    fun validateAndSave() {
        val readEndings = endingsReadStr.toIntOrNull()
        val totalEndings = endingsTotalStr.toIntOrNull()

        val readChapters = if (isHybridFormat) hybridWebChaptersStr.toIntOrNull() else webChaptersStr.toIntOrNull()
        val totalChapters = if (isHybridFormat) hybridTotalWebChaptersStr.toIntOrNull() else totalWebChaptersStr.toIntOrNull()

        val wordsRead = if (useDetailedVolumes && !isWeb) volumeEntriesList.sumOf { it.w } else wordsStr.toLongOrNull() ?: 0L
        val totalWords = totalWordsInBookStr.toLongOrNull()

        val volsRead = if (useDetailedVolumes) volumeEntriesList.size.toDouble() else volumesStr.toDoubleOrNull() ?: 0.0
        val totalVolsSeries = totalVolumesInSeriesStr.toIntOrNull()

        if (isVN && readEndings != null && totalEndings != null && readEndings > totalEndings) {
            onShowSnack("Пройдено концовок больше, чем всего концовок")
            return
        }

        if ((isWeb || isHybridFormat) && readChapters != null && totalChapters != null && readChapters > totalChapters) {
            onShowSnack("Прочитано глав больше, чем указано всего глав")
            return
        }

        if (settings.enableTotalWords && totalWords != null && wordsRead > totalWords) {
            onShowSnack("Слов прочитано больше, чем всего слов в тайтле")
            return
        }

        if (countVolumes && !isOngoing && totalVolsSeries != null && volsRead > totalVolsSeries) {
            onShowSnack("Указано томов больше, чем всего томов в серии")
            return
        }

        val finalBook = Book(
            id = id,
            title = title.trim().ifEmpty { "Без названия" },
            status = status,
            isSeries = isSeries,
            isWeb = isWeb,
            isSingle = isSingle,
            countVolumes = countVolumes,
            isOngoing = isOngoing,
            useDetailedVolumes = useDetailedVolumes,
            isVN = isVN,
            isHybridFormat = isHybridFormat,
            words = wordsStr.toLongOrNull(),
            volumes = volumesStr.toDoubleOrNull(),
            totalVolumesInSeries = totalVolumesInSeriesStr.toIntOrNull(),
            webChapters = webChaptersStr.toIntOrNull(),
            totalWebChapters = totalWebChaptersStr.toIntOrNull(),
            endingsRead = endingsReadStr.toIntOrNull(),
            endingsTotal = endingsTotalStr.toIntOrNull(),
            totalWordsInBook = totalWordsInBookStr.toLongOrNull(),
            hybridWebChapters = hybridWebChaptersStr.toIntOrNull(),
            hybridTotalWebChapters = hybridTotalWebChaptersStr.toIntOrNull(),
            rating = rating,
            startVolume = startVolumeStr.toIntOrNull(),
            startChapter = startChapterStr.toIntOrNull(),
            coverColor = coverColor,
            coverUrl = coverUrl.ifBlank { null },
            localImagePath = localImagePath.ifBlank { null },
            volumeEntries = volumeEntriesList.toList(),
            currentBookmark = currentBookmark.ifBlank { null }
        )

        onSaveBook(finalBook)
    }

    fun applyFormatChange(targetFormatKey: String) {
        when (targetFormatKey) {
            "hybrid" -> {
                isSeries = true
                isWeb = false
                isSingle = false
                isVN = false
                isHybridFormat = true
                countVolumes = true
            }
            "series" -> {
                isSeries = true
                isWeb = false
                isSingle = false
                isVN = false
                isHybridFormat = false
                countVolumes = true
            }
            "web" -> {
                isSeries = false
                isWeb = true
                isSingle = false
                isVN = false
                isHybridFormat = false
                countVolumes = false
            }
            "single" -> {
                isSeries = false
                isWeb = false
                isSingle = true
                isVN = false
                isHybridFormat = false
                countVolumes = true
            }
            "vn" -> {
                isSeries = false
                isWeb = false
                isSingle = false
                isVN = true
                isHybridFormat = false
                countVolumes = false
            }
        }
    }

    fun onRequestFormatChange(targetFormatKey: String) {
        val hasProgressData = (wordsStr.toLongOrNull() ?: 0L) > 0L ||
                (volumesStr.toDoubleOrNull() ?: 0.0) > 0.0 ||
                (webChaptersStr.toIntOrNull() ?: 0) > 0 ||
                (endingsReadStr.toIntOrNull() ?: 0) > 0 ||
                volumeEntriesList.isNotEmpty()

        if (hasProgressData) {
            pendingFormatSelection = targetFormatKey
        } else {
            applyFormatChange(targetFormatKey)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.screenBg)
            .imePadding()
    ) {
        // AppBar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (!isEditMode && currentStep > 0) {
                        currentStep--
                    } else {
                        onBack()
                    }
                }
            ) {
                Icon(
                    imageVector = if (!isEditMode && currentStep == 0) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = colors.textFg
                )
            }

            Text(
                text = if (isEditMode) "Редактировать" else "Добавить тайтл",
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

            if (isEditMode) {
                IconButton(onClick = { validateAndSave() }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = colors.accent
                    )
                }
            }
        }

        if (!isEditMode) {
            // 3-step indicator
            StepIndicator(
                currentStep = currentStep,
                onStepClick = { step -> if (step < currentStep) currentStep = step }
            )
        }

        // Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (isEditMode) {
                // Edit mode: single scroll view
                val bouncyState = rememberBouncyOverscrollState()
                LazyColumn(
                    state = bouncyState.listState,
                    modifier = bouncyState.modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))

                        // Compact Cover Picker (80x110dp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 80.dp, height = 110.dp)
                                    .clip(RoundedCornerShape(RadiusSmall))
                                    .background(colors.cardBg)
                                    .clickable { showCoverPickerSheet = true },
                                contentAlignment = Alignment.Center
                            ) {
                                val dummyBook = Book(
                                    coverColor = coverColor,
                                    coverUrl = coverUrl.ifBlank { null },
                                    localImagePath = localImagePath.ifBlank { null }
                                )
                                BookCoverImage(
                                    book = dummyBook,
                                    modifier = Modifier.matchParentSize()
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter)
                                        .background(Color.Black.copy(alpha = 0.55f))
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Изменить",
                                        style = TextStyle(
                                            fontFamily = PlusJakartaSansFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = Color.White
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                SectionLabel("НАЗВАНИЕ")
                                OutlinedTextField(
                                    value = title,
                                    onValueChange = { title = it },
                                    placeholder = { Text("Введите название...", color = colors.textSecondary) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(RadiusMedium),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = colors.cardBg,
                                        unfocusedContainerColor = colors.cardBg,
                                        focusedBorderColor = colors.accent,
                                        unfocusedBorderColor = colors.dividerColor,
                                        focusedTextColor = colors.textFg,
                                        unfocusedTextColor = colors.textFg
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        SectionLabel("СТАТУС")
                        StatusSelectorWrap(
                            currentStatus = status,
                            onSelectStatus = { status = it }
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        SectionLabel("ФОРМАТ ИЗДАНИЯ")
                        FormatSelectorBlock(
                            isSeries = isSeries,
                            isWeb = isWeb,
                            isSingle = isSingle,
                            isVN = isVN,
                            isHybridFormat = isHybridFormat,
                            enableHybrid = settings.enableHybrid,
                            enableVN = settings.enableVN,
                            onSelectFormat = { onRequestFormatChange(it) }
                        )

                        if (!isWeb && !isHybridFormat && !isVN) {
                            Spacer(modifier = Modifier.height(10.dp))
                            CardGroup {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Учитывать тома", style = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.textFg))
                                        Text("Отключите для изданий без томов", style = TextStyle(fontFamily = PlusJakartaSansFamily, fontSize = 12.sp, color = colors.textSecondary))
                                    }
                                    CustomSwitch(
                                        checked = countVolumes,
                                        onCheckedChange = { countVolumes = it }
                                    )
                                }
                            }
                        }

                        // Data fields (same as Step 3)
                        DataFieldsSection(
                            settings = settings,
                            isVN = isVN,
                            isWeb = isWeb,
                            isHybridFormat = isHybridFormat,
                            countVolumes = countVolumes,
                            useDetailedVolumes = useDetailedVolumes,
                            onToggleUseDetailed = { useDetailed ->
                                if (!useDetailed && volumeEntriesList.isNotEmpty()) {
                                    // Transfer volume sum to words/volumes
                                    wordsStr = volumeEntriesList.sumOf { it.w }.toString()
                                    volumesStr = volumeEntriesList.size.toString()
                                }
                                useDetailedVolumes = useDetailed
                            },
                            currentBookmark = currentBookmark,
                            onBookmarkChange = { currentBookmark = it },
                            startVolumeStr = startVolumeStr,
                            onStartVolumeChange = { startVolumeStr = it },
                            startChapterStr = startChapterStr,
                            onStartChapterChange = { startChapterStr = it },
                            rating = rating,
                            onRatingChange = { rating = it },
                            endingsReadStr = endingsReadStr,
                            onEndingsReadChange = { endingsReadStr = it },
                            endingsTotalStr = endingsTotalStr,
                            onEndingsTotalChange = { endingsTotalStr = it },
                            webChaptersStr = webChaptersStr,
                            onWebChaptersChange = { webChaptersStr = it },
                            totalWebChaptersStr = totalWebChaptersStr,
                            onTotalWebChaptersChange = { totalWebChaptersStr = it },
                            hybridWebChaptersStr = hybridWebChaptersStr,
                            onHybridWebChaptersChange = { hybridWebChaptersStr = it },
                            hybridTotalWebChaptersStr = hybridTotalWebChaptersStr,
                            onHybridTotalWebChaptersChange = { hybridTotalWebChaptersStr = it },
                            wordsStr = wordsStr,
                            onWordsChange = { wordsStr = it },
                            volumesStr = volumesStr,
                            onVolumesChange = { volumesStr = it },
                            totalWordsInBookStr = totalWordsInBookStr,
                            onTotalWordsInBookChange = { totalWordsInBookStr = it },
                            isOngoing = isOngoing,
                            onOngoingChange = { isOngoing = it },
                            totalVolumesInSeriesStr = totalVolumesInSeriesStr,
                            onTotalVolumesInSeriesChange = { totalVolumesInSeriesStr = it },
                            volumeEntriesList = volumeEntriesList
                        )

                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            } else {
                // Wizard step content
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f)) {
                        when (currentStep) {
                            0 -> Step0Title(
                                title = title,
                                onTitleChange = { title = it },
                                coverColor = coverColor,
                                coverUrl = coverUrl,
                                localImagePath = localImagePath,
                                onOpenCoverPicker = { showCoverPickerSheet = true }
                            )
                            1 -> Step1StatusFormat(
                                currentStatus = status,
                                onSelectStatus = { status = it },
                                isSeries = isSeries,
                                isWeb = isWeb,
                                isSingle = isSingle,
                                isVN = isVN,
                                isHybridFormat = isHybridFormat,
                                countVolumes = countVolumes,
                                onToggleCountVolumes = { countVolumes = it },
                                enableHybrid = settings.enableHybrid,
                                enableVN = settings.enableVN,
                                onRequestFormatChange = { onRequestFormatChange(it) }
                            )
                            else -> Step2Data(
                                settings = settings,
                                isVN = isVN,
                                isWeb = isWeb,
                                isHybridFormat = isHybridFormat,
                                countVolumes = countVolumes,
                                useDetailedVolumes = useDetailedVolumes,
                                onToggleUseDetailed = { useDetailed ->
                                    if (!useDetailed && volumeEntriesList.isNotEmpty()) {
                                        wordsStr = volumeEntriesList.sumOf { it.w }.toString()
                                        volumesStr = volumeEntriesList.size.toString()
                                    }
                                    useDetailedVolumes = useDetailed
                                },
                                currentBookmark = currentBookmark,
                                onBookmarkChange = { currentBookmark = it },
                                startVolumeStr = startVolumeStr,
                                onStartVolumeChange = { startVolumeStr = it },
                                startChapterStr = startChapterStr,
                                onStartChapterChange = { startChapterStr = it },
                                rating = rating,
                                onRatingChange = { rating = it },
                                endingsReadStr = endingsReadStr,
                                onEndingsReadChange = { endingsReadStr = it },
                                endingsTotalStr = endingsTotalStr,
                                onEndingsTotalChange = { endingsTotalStr = it },
                                webChaptersStr = webChaptersStr,
                                onWebChaptersChange = { webChaptersStr = it },
                                totalWebChaptersStr = totalWebChaptersStr,
                                onTotalWebChaptersChange = { totalWebChaptersStr = it },
                                hybridWebChaptersStr = hybridWebChaptersStr,
                                onHybridWebChaptersChange = { hybridWebChaptersStr = it },
                                hybridTotalWebChaptersStr = hybridTotalWebChaptersStr,
                                onHybridTotalWebChaptersChange = { hybridTotalWebChaptersStr = it },
                                wordsStr = wordsStr,
                                onWordsChange = { wordsStr = it },
                                volumesStr = volumesStr,
                                onVolumesChange = { volumesStr = it },
                                totalWordsInBookStr = totalWordsInBookStr,
                                onTotalWordsInBookChange = { totalWordsInBookStr = it },
                                isOngoing = isOngoing,
                                onOngoingChange = { isOngoing = it },
                                totalVolumesInSeriesStr = totalVolumesInSeriesStr,
                                onTotalVolumesInSeriesChange = { totalVolumesInSeriesStr = it },
                                volumeEntriesList = volumeEntriesList
                            )
                        }
                    }

                    // Bottom Next/Add button
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = colors.screenBg
                    ) {
                        Button(
                            onClick = {
                                if (currentStep < 2) {
                                    currentStep++
                                } else {
                                    validateAndSave()
                                }
                            },
                            shape = RoundedCornerShape(RadiusMedium),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .height(52.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (currentStep < 2) "Далее" else "Добавить",
                                    style = TextStyle(
                                        fontFamily = PlusJakartaSansFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = colors.accentOnColor
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = if (currentStep < 2) Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.Check,
                                    contentDescription = null,
                                    tint = colors.accentOnColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Cover picker bottom sheet
    if (showCoverPickerSheet) {
        CoverPickerBottomSheet(
            onFromGallery = {
                showCoverPickerSheet = false
                galleryLauncher.launch(arrayOf("image/*"))
            },
            onByUrl = {
                showCoverPickerSheet = false
                showUrlDialog = true
            },
            onDismissRequest = { showCoverPickerSheet = false }
        )
    }

    // URL Dialog
    if (showUrlDialog) {
        var tempUrl by remember { mutableStateOf(coverUrl) }
        AlertDialog(
            onDismissRequest = { showUrlDialog = false },
            containerColor = colors.cardBg,
            shape = RoundedCornerShape(RadiusLarge),
            title = {
                Text(
                    text = "URL обложки",
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = colors.textFg
                    )
                )
            },
            text = {
                OutlinedTextField(
                    value = tempUrl,
                    onValueChange = { tempUrl = it },
                    placeholder = { Text("Вставьте ссылку...", color = colors.textSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(RadiusMedium),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.cardBg,
                        unfocusedContainerColor = colors.cardBg,
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.dividerColor,
                        focusedTextColor = colors.textFg,
                        unfocusedTextColor = colors.textFg
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coverUrl = tempUrl.trim()
                        localImagePath = ""
                        showUrlDialog = false
                    }
                ) {
                    Text(
                        text = "Сохранить",
                        style = TextStyle(
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showUrlDialog = false }) {
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

    // Confirm Format Change Dialog
    pendingFormatSelection?.let { targetKey ->
        AlertDialog(
            onDismissRequest = { pendingFormatSelection = null },
            containerColor = colors.cardBg,
            shape = RoundedCornerShape(RadiusLarge),
            title = {
                Text(
                    text = "Сменить формат издания?",
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
                    text = "Введённые данные о словах, томах, главах или концовках относятся к другому формату и могут быть потеряны при смене.",
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 14.sp,
                        color = colors.textSecondary
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        applyFormatChange(targetKey)
                        pendingFormatSelection = null
                    }
                ) {
                    Text(
                        text = "Сменить",
                        style = TextStyle(
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            color = colors.cDropped
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingFormatSelection = null }) {
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
private fun StepIndicator(
    currentStep: Int,
    onStepClick: (Int) -> Unit
) {
    val colors = LocalReadTrackerColors.current
    val stepLabels = listOf("Тайтл", "Статус", "Данные")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (i in 0..2) {
                val barColor = when {
                    i <= currentStep -> colors.accent
                    else -> colors.textSecondary.copy(alpha = 0.25f)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(barColor)
                        .clickable(enabled = i < currentStep) { onStepClick(i) }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            stepLabels.forEachIndexed { i, label ->
                val textColor = when {
                    i == currentStep -> colors.accent
                    i < currentStep -> colors.textSecondary
                    else -> colors.textSecondary.copy(alpha = 0.50f)
                }
                Text(
                    text = label,
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = if (i == currentStep) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp,
                        color = textColor
                    ),
                    modifier = Modifier.clickable(enabled = i < currentStep) { onStepClick(i) }
                )
            }
        }
    }
}

@Composable
private fun Step0Title(
    title: String,
    onTitleChange: (String) -> Unit,
    coverColor: Int,
    coverUrl: String,
    localImagePath: String,
    onOpenCoverPicker: () -> Unit
) {
    val colors = LocalReadTrackerColors.current
    val bouncyState = rememberBouncyOverscrollState()

    LazyColumn(
        state = bouncyState.listState,
        modifier = bouncyState.modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Vertical portrait cover area 140x195dp
            Box(
                modifier = Modifier
                    .size(width = 140.dp, height = 195.dp)
                    .clip(RoundedCornerShape(RadiusLarge))
                    .background(colors.cardBg)
                    .clickable { onOpenCoverPicker() }
                    .drawWithContent {
                        drawContent()
                        if (coverUrl.isBlank() && localImagePath.isBlank()) {
                            val stroke = Stroke(
                                width = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                            )
                            drawRoundRect(
                                color = colors.accent.copy(alpha = 0.40f),
                                style = stroke,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                    RadiusLarge.toPx(),
                                    RadiusLarge.toPx()
                                )
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                val hasImage = coverUrl.isNotBlank() || localImagePath.isNotBlank()
                if (hasImage) {
                    val dummyBook = Book(
                        coverColor = coverColor,
                        coverUrl = coverUrl.ifBlank { null },
                        localImagePath = localImagePath.ifBlank { null }
                    )
                    BookCoverImage(book = dummyBook, modifier = Modifier.matchParentSize())

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.72f))
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Изменить",
                            style = TextStyle(
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Обложка",
                            style = TextStyle(
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = colors.accent
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            SectionLabel("НАЗВАНИЕ")
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                placeholder = { Text("Введите название...", color = colors.textSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(RadiusMedium),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.cardBg,
                    unfocusedContainerColor = colors.cardBg,
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.dividerColor,
                    focusedTextColor = colors.textFg,
                    unfocusedTextColor = colors.textFg
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Обложка необязательна",
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                )
            }
        }
    }
}

@Composable
private fun Step1StatusFormat(
    currentStatus: BookStatus,
    onSelectStatus: (BookStatus) -> Unit,
    isSeries: Boolean,
    isWeb: Boolean,
    isSingle: Boolean,
    isVN: Boolean,
    isHybridFormat: Boolean,
    countVolumes: Boolean,
    onToggleCountVolumes: (Boolean) -> Unit,
    enableHybrid: Boolean,
    enableVN: Boolean,
    onRequestFormatChange: (String) -> Unit
) {
    val colors = LocalReadTrackerColors.current
    val bouncyState = rememberBouncyOverscrollState()

    LazyColumn(
        state = bouncyState.listState,
        modifier = bouncyState.modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionLabel("СТАТУС")
            StatusSelectorVertical(
                currentStatus = currentStatus,
                onSelectStatus = onSelectStatus
            )

            Spacer(modifier = Modifier.height(20.dp))
            SectionLabel("ФОРМАТ ИЗДАНИЯ")
            FormatSelectorBlock(
                isSeries = isSeries,
                isWeb = isWeb,
                isSingle = isSingle,
                isVN = isVN,
                isHybridFormat = isHybridFormat,
                enableHybrid = enableHybrid,
                enableVN = enableVN,
                onSelectFormat = onRequestFormatChange
            )

            if (!isWeb && !isHybridFormat && !isVN) {
                Spacer(modifier = Modifier.height(10.dp))
                CardGroup {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Учитывать тома", style = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.textFg))
                            Text("Отключите для изданий без томов", style = TextStyle(fontFamily = PlusJakartaSansFamily, fontSize = 12.sp, color = colors.textSecondary))
                        }
                        CustomSwitch(
                            checked = countVolumes,
                            onCheckedChange = onToggleCountVolumes
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusSelectorVertical(
    currentStatus: BookStatus,
    onSelectStatus: (BookStatus) -> Unit
) {
    val colors = LocalReadTrackerColors.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BookStatus.entries.forEach { st ->
            val isSelected = (st == currentStatus)
            val statusCol = colors.getColorForStatus(st)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(RadiusMedium))
                    .background(if (isSelected) statusCol.copy(alpha = 0.10f) else colors.cardBg)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) statusCol else colors.dividerColor,
                        shape = RoundedCornerShape(RadiusMedium)
                    )
                    .clickable { onSelectStatus(st) }
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(statusCol)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = st.label,
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 15.sp,
                        color = if (isSelected) statusCol else colors.textFg
                    ),
                    modifier = Modifier.weight(1f)
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = statusCol,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusSelectorWrap(
    currentStatus: BookStatus,
    onSelectStatus: (BookStatus) -> Unit
) {
    val colors = LocalReadTrackerColors.current

    @OptIn(ExperimentalLayoutApi::class)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BookStatus.entries.forEach { st ->
            val isSelected = (st == currentStatus)
            val statusCol = colors.getColorForStatus(st)

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) statusCol.copy(alpha = 0.14f) else colors.cardBg)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) statusCol else colors.dividerColor,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onSelectStatus(st) }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusCol)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = st.label,
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp,
                        color = if (isSelected) statusCol else colors.textFg
                    )
                )
            }
        }
    }
}

@Composable
private fun FormatSelectorBlock(
    isSeries: Boolean,
    isWeb: Boolean,
    isSingle: Boolean,
    isVN: Boolean,
    isHybridFormat: Boolean,
    enableHybrid: Boolean,
    enableVN: Boolean,
    onSelectFormat: (String) -> Unit
) {
    val colors = LocalReadTrackerColors.current

    CardGroup {
        if (enableHybrid) {
            FormatRadioRow(
                title = "LN+WN Гибрид",
                subtitle = "Комплексный формат (LN тома + WN онгоинг главы)",
                icon = Icons.Default.Bolt,
                selected = isHybridFormat,
                onClick = { onSelectFormat("hybrid") }
            )
            CardGroupDivider()
        }

        FormatRadioRow(
            title = "Серия томов",
            subtitle = "Серийное издание печатных томов (LN / Книги)",
            icon = Icons.Default.Layers,
            selected = isSeries && !isHybridFormat,
            onClick = { onSelectFormat("series") }
        )
        CardGroupDivider()

        FormatRadioRow(
            title = "Веб-новелла",
            subtitle = "Азиатские веб-романы, разбитые строго по главам (WN)",
            icon = Icons.Default.Language,
            selected = isWeb && !isHybridFormat,
            onClick = { onSelectFormat("web") }
        )
        CardGroupDivider()

        FormatRadioRow(
            title = "Сингл (Одиночное)",
            subtitle = "Одиночный роман (Ваншот / Том-сингл)",
            icon = Icons.Default.MenuBook,
            selected = isSingle,
            onClick = { onSelectFormat("single") }
        )

        if (enableVN) {
            CardGroupDivider()
            FormatRadioRow(
                title = "Визуальная новелла (VN)",
                subtitle = "Прогресс по пройденным концовкам",
                icon = Icons.Default.VideogameAsset,
                selected = isVN,
                onClick = { onSelectFormat("vn") }
            )
        }
    }
}

@Composable
private fun FormatRadioRow(
    title: String,
    subtitle: String,
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
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = colors.accent)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) colors.accent else colors.textSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.textFg))
            Text(subtitle, style = TextStyle(fontFamily = PlusJakartaSansFamily, fontSize = 12.sp, color = colors.textSecondary))
        }
    }
}

@Composable
private fun Step2Data(
    settings: AppSettings,
    isVN: Boolean,
    isWeb: Boolean,
    isHybridFormat: Boolean,
    countVolumes: Boolean,
    useDetailedVolumes: Boolean,
    onToggleUseDetailed: (Boolean) -> Unit,
    currentBookmark: String,
    onBookmarkChange: (String) -> Unit,
    startVolumeStr: String,
    onStartVolumeChange: (String) -> Unit,
    startChapterStr: String,
    onStartChapterChange: (String) -> Unit,
    rating: Int?,
    onRatingChange: (Int?) -> Unit,
    endingsReadStr: String,
    onEndingsReadChange: (String) -> Unit,
    endingsTotalStr: String,
    onEndingsTotalChange: (String) -> Unit,
    webChaptersStr: String,
    onWebChaptersChange: (String) -> Unit,
    totalWebChaptersStr: String,
    onTotalWebChaptersChange: (String) -> Unit,
    hybridWebChaptersStr: String,
    onHybridWebChaptersChange: (String) -> Unit,
    hybridTotalWebChaptersStr: String,
    onHybridTotalWebChaptersChange: (String) -> Unit,
    wordsStr: String,
    onWordsChange: (String) -> Unit,
    volumesStr: String,
    onVolumesChange: (String) -> Unit,
    totalWordsInBookStr: String,
    onTotalWordsInBookChange: (String) -> Unit,
    isOngoing: Boolean,
    onOngoingChange: (Boolean) -> Unit,
    totalVolumesInSeriesStr: String,
    onTotalVolumesInSeriesChange: (String) -> Unit,
    volumeEntriesList: MutableList<VolumeEntry>
) {
    val bouncyState = rememberBouncyOverscrollState()
    LazyColumn(
        state = bouncyState.listState,
        modifier = bouncyState.modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            DataFieldsSection(
                settings = settings,
                isVN = isVN,
                isWeb = isWeb,
                isHybridFormat = isHybridFormat,
                countVolumes = countVolumes,
                useDetailedVolumes = useDetailedVolumes,
                onToggleUseDetailed = onToggleUseDetailed,
                currentBookmark = currentBookmark,
                onBookmarkChange = onBookmarkChange,
                startVolumeStr = startVolumeStr,
                onStartVolumeChange = onStartVolumeChange,
                startChapterStr = startChapterStr,
                onStartChapterChange = onStartChapterChange,
                rating = rating,
                onRatingChange = onRatingChange,
                endingsReadStr = endingsReadStr,
                onEndingsReadChange = onEndingsReadChange,
                endingsTotalStr = endingsTotalStr,
                onEndingsTotalChange = onEndingsTotalChange,
                webChaptersStr = webChaptersStr,
                onWebChaptersChange = onWebChaptersChange,
                totalWebChaptersStr = totalWebChaptersStr,
                onTotalWebChaptersChange = onTotalWebChaptersChange,
                hybridWebChaptersStr = hybridWebChaptersStr,
                onHybridWebChaptersChange = onHybridWebChaptersChange,
                hybridTotalWebChaptersStr = hybridTotalWebChaptersStr,
                onHybridTotalWebChaptersChange = onHybridTotalWebChaptersChange,
                wordsStr = wordsStr,
                onWordsChange = onWordsChange,
                volumesStr = volumesStr,
                onVolumesChange = onVolumesChange,
                totalWordsInBookStr = totalWordsInBookStr,
                onTotalWordsInBookChange = onTotalWordsInBookChange,
                isOngoing = isOngoing,
                onOngoingChange = onOngoingChange,
                totalVolumesInSeriesStr = totalVolumesInSeriesStr,
                onTotalVolumesInSeriesChange = onTotalVolumesInSeriesChange,
                volumeEntriesList = volumeEntriesList
            )
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun DataFieldsSection(
    settings: AppSettings,
    isVN: Boolean,
    isWeb: Boolean,
    isHybridFormat: Boolean,
    countVolumes: Boolean,
    useDetailedVolumes: Boolean,
    onToggleUseDetailed: (Boolean) -> Unit,
    currentBookmark: String,
    onBookmarkChange: (String) -> Unit,
    startVolumeStr: String,
    onStartVolumeChange: (String) -> Unit,
    startChapterStr: String,
    onStartChapterChange: (String) -> Unit,
    rating: Int?,
    onRatingChange: (Int?) -> Unit,
    endingsReadStr: String,
    onEndingsReadChange: (String) -> Unit,
    endingsTotalStr: String,
    onEndingsTotalChange: (String) -> Unit,
    webChaptersStr: String,
    onWebChaptersChange: (String) -> Unit,
    totalWebChaptersStr: String,
    onTotalWebChaptersChange: (String) -> Unit,
    hybridWebChaptersStr: String,
    onHybridWebChaptersChange: (String) -> Unit,
    hybridTotalWebChaptersStr: String,
    onHybridTotalWebChaptersChange: (String) -> Unit,
    wordsStr: String,
    onWordsChange: (String) -> Unit,
    volumesStr: String,
    onVolumesChange: (String) -> Unit,
    totalWordsInBookStr: String,
    onTotalWordsInBookChange: (String) -> Unit,
    isOngoing: Boolean,
    onOngoingChange: (Boolean) -> Unit,
    totalVolumesInSeriesStr: String,
    onTotalVolumesInSeriesChange: (String) -> Unit,
    volumeEntriesList: MutableList<VolumeEntry>
) {
    val colors = LocalReadTrackerColors.current

    // 1. Bookmark
    if (settings.showBookmarks) {
        SectionLabel("ЗАКЛАДКА")
        OutlinedTextField(
            value = currentBookmark,
            onValueChange = onBookmarkChange,
            leadingIcon = { Icon(Icons.Default.Bookmark, null, tint = colors.accent, modifier = Modifier.size(18.dp)) },
            placeholder = { Text("Впишите главу/том, например: 1.4 глава, 1х3.3", color = colors.textSecondary, fontSize = 13.sp) },
            singleLine = true,
            shape = RoundedCornerShape(RadiusMedium),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.cardBg,
                unfocusedContainerColor = colors.cardBg,
                focusedBorderColor = colors.accent,
                unfocusedBorderColor = colors.dividerColor,
                focusedTextColor = colors.textFg,
                unfocusedTextColor = colors.textFg
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))
    }

    // 2. Adaptation Start
    if (settings.enableAdaptationStart) {
        SectionLabel("СТАРТ ПОСЛЕ АДАПТАЦИИ")
        if (isWeb || isHybridFormat) {
            OutlinedTextField(
                value = startChapterStr,
                onValueChange = { onStartChapterChange(it.filter { c -> c.isDigit() }) },
                leadingIcon = { Icon(Icons.Default.PlayArrow, null, tint = colors.accent, modifier = Modifier.size(18.dp)) },
                placeholder = { Text("Начальная глава (с какой начали)", color = colors.textSecondary, fontSize = 13.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(RadiusMedium),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.cardBg,
                    unfocusedContainerColor = colors.cardBg,
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.dividerColor,
                    focusedTextColor = colors.textFg,
                    unfocusedTextColor = colors.textFg
                ),
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            OutlinedTextField(
                value = startVolumeStr,
                onValueChange = { onStartVolumeChange(it.filter { c -> c.isDigit() }) },
                leadingIcon = { Icon(Icons.Default.PlayArrow, null, tint = colors.accent, modifier = Modifier.size(18.dp)) },
                placeholder = { Text("Начальный том (с какого начали)", color = colors.textSecondary, fontSize = 13.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(RadiusMedium),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.cardBg,
                    unfocusedContainerColor = colors.cardBg,
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.dividerColor,
                    focusedTextColor = colors.textFg,
                    unfocusedTextColor = colors.textFg
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
    }

    // 3. Rating
    if (settings.enableRating) {
        SectionLabel("ОЦЕНКА ТАЙТЛА")
        CardGroup {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val maxStars = if (settings.ratingScale == 5) 5 else 10
                val starSize = if (settings.ratingScale == 5) 32.dp else 24.dp

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (starIdx in 1..maxStars) {
                        val isActive = if (settings.ratingScale == 5) {
                            val rVal = rating ?: 0
                            kotlin.math.round(rVal / 2.0).toInt() >= starIdx
                        } else {
                            (rating ?: 0) >= starIdx
                        }

                        Icon(
                            imageVector = if (isActive) Icons.Default.Star else Icons.Default.StarOutline,
                            contentDescription = null,
                            tint = if (isActive) colors.accent else colors.textSecondary,
                            modifier = Modifier
                                .size(starSize)
                                .clickable {
                                    val newRating = if (settings.ratingScale == 5) starIdx * 2 else starIdx
                                    onRatingChange(newRating)
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayRatingText = if (rating != null) {
                        if (settings.ratingScale == 5) "${kotlin.math.round(rating / 2.0).toInt()} из 5" else "$rating из 10"
                    } else "не выбрано"

                    Text(
                        text = "Выбрано: $displayRatingText",
                        style = TextStyle(
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.accent
                        )
                    )

                    Text(
                        text = "Сбросить",
                        style = TextStyle(
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = colors.cDropped
                        ),
                        modifier = Modifier.clickable { onRatingChange(null) }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }

    // 4. Progress VN
    if (isVN) {
        SectionLabel("ПРОГРЕСС КОНЦОВОК")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = endingsReadStr,
                onValueChange = { onEndingsReadChange(it.filter { c -> c.isDigit() }) },
                label = { Text("Пройдено концовок", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.VideogameAsset, null, tint = colors.textSecondary, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(RadiusMedium),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.cardBg,
                    unfocusedContainerColor = colors.cardBg,
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.dividerColor,
                    focusedTextColor = colors.textFg,
                    unfocusedTextColor = colors.textFg
                ),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = endingsTotalStr,
                onValueChange = { onEndingsTotalChange(it.filter { c -> c.isDigit() }) },
                label = { Text("Всего концовок", fontSize = 12.sp) },
                placeholder = { Text("Необяз.", color = colors.textSecondary, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.BookmarkBorder, null, tint = colors.textSecondary, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(RadiusMedium),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.cardBg,
                    unfocusedContainerColor = colors.cardBg,
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.dividerColor,
                    focusedTextColor = colors.textFg,
                    unfocusedTextColor = colors.textFg
                ),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
    }

    // 5. Progress Chapters (web / hybrid)
    if (isWeb || isHybridFormat) {
        val labelTitle = if (isHybridFormat) "ПРОГРЕСС ВЕБ-ГЛАВ (В ГИБРИДЕ)" else "ПРОГРЕСС ГЛАВ ВЕБ-НОВЕЛЛЫ"
        SectionLabel(labelTitle)

        val readChaptersVal = if (isHybridFormat) hybridWebChaptersStr else webChaptersStr
        val totalChaptersVal = if (isHybridFormat) hybridTotalWebChaptersStr else totalWebChaptersStr

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = readChaptersVal,
                onValueChange = {
                    val cleaned = it.filter { c -> c.isDigit() }
                    if (isHybridFormat) onHybridWebChaptersChange(cleaned) else onWebChaptersChange(cleaned)
                },
                label = { Text("Прочитано глав", fontSize = 12.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(RadiusMedium),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.cardBg,
                    unfocusedContainerColor = colors.cardBg,
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.dividerColor,
                    focusedTextColor = colors.textFg,
                    unfocusedTextColor = colors.textFg
                ),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = totalChaptersVal,
                onValueChange = {
                    val cleaned = it.filter { c -> c.isDigit() }
                    if (isHybridFormat) onHybridTotalWebChaptersChange(cleaned) else onTotalWebChaptersChange(cleaned)
                },
                label = { Text("Всего глав", fontSize = 12.sp) },
                placeholder = { Text("Необяз.", color = colors.textSecondary, fontSize = 12.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(RadiusMedium),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.cardBg,
                    unfocusedContainerColor = colors.cardBg,
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.dividerColor,
                    focusedTextColor = colors.textFg,
                    unfocusedTextColor = colors.textFg
                ),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
    }

    // 6. Words & Volume Calculations (non-VN)
    if (!isVN) {
        SectionLabel("СЛОВА И РАСЧЁТЫ")

        if (!isWeb && countVolumes) {
            CardGroup {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Расчёт по томам", style = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.textFg))
                        Text(if (useDetailedVolumes) "Записывать слова каждого тома" else "Ввести суммарно по книге", style = TextStyle(fontFamily = PlusJakartaSansFamily, fontSize = 12.sp, color = colors.textSecondary))
                    }
                    CustomSwitch(
                        checked = useDetailedVolumes,
                        onCheckedChange = onToggleUseDetailed
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (useDetailedVolumes && !isWeb && countVolumes) {
            // List of Volume entries
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                volumeEntriesList.forEachIndexed { idx, entry ->
                    var volValStr by remember(entry.v) { mutableStateOf(if (entry.v % 1.0 == 0.0) entry.v.toLong().toString() else entry.v.toString()) }
                    var wordValStr by remember(entry.w) { mutableStateOf(entry.w.toString()) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = volValStr,
                            onValueChange = { input ->
                                val cleaned = input.filter { c -> c.isDigit() || c == '.' }
                                volValStr = cleaned
                                val parsedV = cleaned.toDoubleOrNull() ?: (idx + 1).toDouble()
                                volumeEntriesList[idx] = volumeEntriesList[idx].copy(v = parsedV)
                            },
                            label = { Text("Том", fontSize = 11.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(RadiusMedium),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.cardBg,
                                unfocusedContainerColor = colors.cardBg,
                                focusedBorderColor = colors.accent,
                                unfocusedBorderColor = colors.dividerColor,
                                focusedTextColor = colors.textFg,
                                unfocusedTextColor = colors.textFg
                            ),
                            modifier = Modifier.width(76.dp)
                        )

                        OutlinedTextField(
                            value = wordValStr,
                            onValueChange = { input ->
                                val cleaned = input.filter { c -> c.isDigit() }
                                wordValStr = cleaned
                                val parsedW = cleaned.toLongOrNull() ?: 0L
                                volumeEntriesList[idx] = volumeEntriesList[idx].copy(w = parsedW)
                            },
                            label = { Text("Слов", fontSize = 11.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(RadiusMedium),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.cardBg,
                                unfocusedContainerColor = colors.cardBg,
                                focusedBorderColor = colors.accent,
                                unfocusedBorderColor = colors.dividerColor,
                                focusedTextColor = colors.textFg,
                                unfocusedTextColor = colors.textFg
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.cDropped.copy(alpha = 0.10f))
                                .clickable { volumeEntriesList.removeAt(idx) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = null,
                                tint = colors.cDropped,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = {
                        val nextVolNum = (volumeEntriesList.lastOrNull()?.v ?: 0.0) + 1.0
                        volumeEntriesList.add(VolumeEntry(v = nextVolNum, w = 0L))
                    },
                    shape = RoundedCornerShape(RadiusMedium),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.accent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Добавить том",
                        style = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    )
                }

                // Summary Box
                val totalVols = volumeEntriesList.size
                val totalWordsSum = volumeEntriesList.sumOf { it.w }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(RadiusMedium))
                        .background(colors.accentDim)
                        .border(width = 1.dp, color = colors.accent.copy(alpha = 0.20f), shape = RoundedCornerShape(RadiusMedium))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Томов: $totalVols",
                            style = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = colors.accent)
                        )
                        Text(
                            text = "Слов: ${fmtNum(totalWordsSum, settings.shortenNumbers)}",
                            style = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = colors.accent)
                        )
                    }
                }
            }
        } else {
            // Simple words & volumes input
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = wordsStr,
                    onValueChange = { onWordsChange(it.filter { c -> c.isDigit() }) },
                    label = { Text("СЛОВ", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.TextFields, null, tint = colors.textSecondary, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(RadiusMedium),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.cardBg,
                        unfocusedContainerColor = colors.cardBg,
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.dividerColor,
                        focusedTextColor = colors.textFg,
                        unfocusedTextColor = colors.textFg
                    ),
                    modifier = Modifier.weight(1f)
                )

                if (countVolumes && !isWeb) {
                    OutlinedTextField(
                        value = volumesStr,
                        onValueChange = { onVolumesChange(it.filter { c -> c.isDigit() || c == '.' }) },
                        label = { Text("ТОМОВ", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Layers, null, tint = colors.textSecondary, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(RadiusMedium),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.cardBg,
                            unfocusedContainerColor = colors.cardBg,
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.dividerColor,
                            focusedTextColor = colors.textFg,
                            unfocusedTextColor = colors.textFg
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }

    // 7. Total Words in Book
    if (settings.enableTotalWords && !isVN) {
        SectionLabel("ВСЕГО СЛОВ В ТАЙТЛЕ")
        OutlinedTextField(
            value = totalWordsInBookStr,
            onValueChange = { onTotalWordsInBookChange(it.filter { c -> c.isDigit() }) },
            leadingIcon = { Icon(Icons.Default.TextSnippet, null, tint = colors.textSecondary, modifier = Modifier.size(18.dp)) },
            placeholder = { Text("Например: 1 200 000", color = colors.textSecondary, fontSize = 13.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(RadiusMedium),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.cardBg,
                unfocusedContainerColor = colors.cardBg,
                focusedBorderColor = colors.accent,
                unfocusedBorderColor = colors.dividerColor,
                focusedTextColor = colors.textFg,
                unfocusedTextColor = colors.textFg
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))
    }

    // 8. Total Volumes in Series
    if (countVolumes && !isVN) {
        SectionLabel("ВСЕГО ТОМОВ В СЕРИИ")
        CardGroup {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Онгоинг", style = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.textFg))
                    Text(if (isOngoing) "Отображается как 5/?" else "Кол-во томов известно", style = TextStyle(fontFamily = PlusJakartaSansFamily, fontSize = 12.sp, color = colors.textSecondary))
                }
                CustomSwitch(
                    checked = isOngoing,
                    onCheckedChange = onOngoingChange
                )
            }
        }

        if (!isOngoing) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = totalVolumesInSeriesStr,
                onValueChange = { onTotalVolumesInSeriesChange(it.filter { c -> c.isDigit() }) },
                placeholder = { Text("Необязательно — напр. 25", color = colors.textSecondary, fontSize = 13.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(RadiusMedium),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.cardBg,
                    unfocusedContainerColor = colors.cardBg,
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.dividerColor,
                    focusedTextColor = colors.textFg,
                    unfocusedTextColor = colors.textFg
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoverPickerBottomSheet(
    onFromGallery: () -> Unit,
    onByUrl: () -> Unit,
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
                    .background(colors.textSecondary.copy(alpha = 0.3f))
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Выбор обложки",
                style = TextStyle(
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = colors.textFg
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            CoverTile(
                title = "Из галереи",
                icon = Icons.Default.PhotoLibrary,
                onClick = onFromGallery
            )

            Spacer(modifier = Modifier.height(10.dp))

            CoverTile(
                title = "По URL",
                icon = Icons.Default.Link,
                onClick = onByUrl
            )
        }
    }
}

@Composable
private fun CoverTile(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val colors = LocalReadTrackerColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RadiusMedium))
            .background(colors.accentDim)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(RadiusSmall))
                .background(colors.accent.copy(alpha = 0.20f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            style = TextStyle(
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = colors.textFg
            )
        )
    }
}
