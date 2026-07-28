package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Book
import com.example.ui.screens.AddEditBookScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ShareCardScreen
import com.example.ui.theme.LocalReadTrackerColors
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.ReadTrackerTheme
import com.example.ui.theme.RadiusMedium
import kotlinx.coroutines.launch

enum class ScreenRoute {
    LIBRARY,
    ANALYTICS,
    ADD_EDIT,
    SETTINGS,
    SHARE_CARD
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainContainer(viewModel: AppStateViewModel) {
    val appState by viewModel.state.collectAsState()
    val settings = appState.settings

    var currentRoute by remember { mutableStateOf(ScreenRoute.LIBRARY) }
    var bookToEdit by remember { mutableStateOf<Book?>(null) }
    var activeShareType by remember { mutableStateOf("analytics") }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    BackHandler(enabled = currentRoute != ScreenRoute.LIBRARY) {
        when (currentRoute) {
            ScreenRoute.SETTINGS -> currentRoute = ScreenRoute.ANALYTICS
            ScreenRoute.ADD_EDIT -> currentRoute = ScreenRoute.LIBRARY
            ScreenRoute.ANALYTICS -> currentRoute = ScreenRoute.LIBRARY
            ScreenRoute.SHARE_CARD -> currentRoute = ScreenRoute.LIBRARY
            else -> currentRoute = ScreenRoute.LIBRARY
        }
    }

    fun showSnack(msg: String) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(msg)
        }
    }

    ReadTrackerTheme(settings = settings) {
        val colors = LocalReadTrackerColors.current
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = colors.screenBg,
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            },
            bottomBar = {
                if (!settings.hideBottomBar && (currentRoute == ScreenRoute.LIBRARY || currentRoute == ScreenRoute.ANALYTICS)) {
                    Surface(
                        color = colors.screenBg,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Library Tab
                            val isLibrarySelected = (currentRoute == ScreenRoute.LIBRARY)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { currentRoute = ScreenRoute.LIBRARY },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isLibrarySelected) colors.accentDim else Color.Transparent)
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = if (isLibrarySelected) colors.accent else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Библиотека",
                                        style = TextStyle(
                                            fontFamily = PlusJakartaSansFamily,
                                            fontWeight = if (isLibrarySelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = if (isLibrarySelected) colors.accent else Color.Gray
                                        )
                                    )
                                }
                            }

                            // Analytics Tab
                            val isAnalyticsSelected = (currentRoute == ScreenRoute.ANALYTICS)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { currentRoute = ScreenRoute.ANALYTICS },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isAnalyticsSelected) colors.accentDim else Color.Transparent)
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BarChart,
                                        contentDescription = null,
                                        tint = if (isAnalyticsSelected) colors.accent else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Аналитика",
                                        style = TextStyle(
                                            fontFamily = PlusJakartaSansFamily,
                                            fontWeight = if (isAnalyticsSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = if (isAnalyticsSelected) colors.accent else Color.Gray
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            },
            floatingActionButton = {
                if (currentRoute == ScreenRoute.LIBRARY) {
                    FloatingActionButton(
                        onClick = {
                            bookToEdit = null
                            currentRoute = ScreenRoute.ADD_EDIT
                        },
                        containerColor = colors.accent,
                        contentColor = Color.Black,
                        shape = CircleShape,
                        elevation = if (settings.fabGlow) {
                            FloatingActionButtonDefaults.elevation(defaultElevation = 12.dp, pressedElevation = 16.dp)
                        } else {
                            FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp, pressedElevation = 8.dp)
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .then(
                                if (settings.fabGlow) {
                                    Modifier.shadow(
                                        elevation = 16.dp,
                                        shape = CircleShape,
                                        ambientColor = colors.accent.copy(alpha = 0.5f),
                                        spotColor = colors.accent.copy(alpha = 0.6f)
                                    )
                                } else Modifier
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Добавить тайтл",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.End
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (settings.disableAnimations) {
                    when (currentRoute) {
                        ScreenRoute.LIBRARY -> LibraryScreen(
                            appState = appState,
                            onTabSelected = { viewModel.updateTab(it) },
                            onSearchChanged = { viewModel.updateSearchQuery(it) },
                            onFilterChanged = { viewModel.updateFormatFilters(it) },
                            onEditBook = { book ->
                                bookToEdit = book
                                currentRoute = ScreenRoute.ADD_EDIT
                            },
                            onDeleteBook = { viewModel.deleteBook(it) },
                            onOpenAnalytics = { currentRoute = ScreenRoute.ANALYTICS },
                            onOpenShareCard = { shareType ->
                                activeShareType = shareType
                                currentRoute = ScreenRoute.SHARE_CARD
                            }
                        )
                        ScreenRoute.ANALYTICS -> AnalyticsScreen(
                            appState = appState,
                            onOpenSettings = { currentRoute = ScreenRoute.SETTINGS }
                        )
                        ScreenRoute.ADD_EDIT -> AddEditBookScreen(
                            initialBook = bookToEdit,
                            settings = settings,
                            onSaveBook = { book ->
                                viewModel.addOrUpdateBook(book)
                                currentRoute = ScreenRoute.LIBRARY
                            },
                            onShowSnack = { showSnack(it) },
                            onBack = { currentRoute = ScreenRoute.LIBRARY }
                        )
                        ScreenRoute.SETTINGS -> SettingsScreen(
                            appState = appState,
                            onUpdateSettings = { viewModel.updateSettings(it) },
                            onImportLibrary = { books, replace -> viewModel.importLibrary(books, replace) },
                            onImportSettingsJson = { jsonStr -> viewModel.importSettingsJson(jsonStr) },
                            onResetColors = { viewModel.resetColors() },
                            onShowSnack = { showSnack(it) },
                            onBack = { currentRoute = ScreenRoute.ANALYTICS }
                        )
                        ScreenRoute.SHARE_CARD -> ShareCardScreen(
                            shareType = activeShareType,
                            appState = appState,
                            onShowSnack = { showSnack(it) },
                            onBack = { currentRoute = ScreenRoute.LIBRARY }
                        )
                    }
                } else {
                    AnimatedContent(
                        targetState = currentRoute,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "screen_transition"
                    ) { targetRoute ->
                        when (targetRoute) {
                            ScreenRoute.LIBRARY -> LibraryScreen(
                                appState = appState,
                                onTabSelected = { viewModel.updateTab(it) },
                                onSearchChanged = { viewModel.updateSearchQuery(it) },
                                onFilterChanged = { viewModel.updateFormatFilters(it) },
                                onEditBook = { book ->
                                    bookToEdit = book
                                    currentRoute = ScreenRoute.ADD_EDIT
                                },
                                onDeleteBook = { viewModel.deleteBook(it) },
                                onOpenAnalytics = { currentRoute = ScreenRoute.ANALYTICS },
                                onOpenShareCard = { shareType ->
                                    activeShareType = shareType
                                    currentRoute = ScreenRoute.SHARE_CARD
                                }
                            )
                            ScreenRoute.ANALYTICS -> AnalyticsScreen(
                                appState = appState,
                                onOpenSettings = { currentRoute = ScreenRoute.SETTINGS }
                            )
                            ScreenRoute.ADD_EDIT -> AddEditBookScreen(
                                initialBook = bookToEdit,
                                settings = settings,
                                onSaveBook = { book ->
                                    viewModel.addOrUpdateBook(book)
                                    currentRoute = ScreenRoute.LIBRARY
                                },
                                onShowSnack = { showSnack(it) },
                                onBack = { currentRoute = ScreenRoute.LIBRARY }
                            )
                            ScreenRoute.SETTINGS -> SettingsScreen(
                                appState = appState,
                                onUpdateSettings = { viewModel.updateSettings(it) },
                                onImportLibrary = { books, replace -> viewModel.importLibrary(books, replace) },
                                onImportSettingsJson = { jsonStr -> viewModel.importSettingsJson(jsonStr) },
                                onResetColors = { viewModel.resetColors() },
                                onShowSnack = { showSnack(it) },
                                onBack = { currentRoute = ScreenRoute.ANALYTICS }
                            )
                            ScreenRoute.SHARE_CARD -> ShareCardScreen(
                                shareType = activeShareType,
                                appState = appState,
                                onShowSnack = { showSnack(it) },
                                onBack = { currentRoute = ScreenRoute.LIBRARY }
                            )
                        }
                    }
                }
            }
        }
    }
}
