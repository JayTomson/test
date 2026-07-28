package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.AppSettings
import com.example.data.Book
import com.example.data.MetricsCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppState(
    val books: List<Book> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val metrics: MetricsCache = MetricsCache(),
    val isCorruptedBackup: Boolean = false,
    val searchQuery: String = "",
    val selectedFormatFilters: Set<String> = emptySet(),
    val selectedTab: Int = 0,
    val snackMessage: String? = null
)

class AppStateViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.booksFlow.collectLatest { (booksList, corrupted) ->
                _state.update { curr ->
                    val newMetrics = MetricsCache.calculate(booksList)
                    curr.copy(
                        books = booksList,
                        metrics = newMetrics,
                        isCorruptedBackup = corrupted
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.settingsFlow.collectLatest { settingsObj ->
                _state.update { curr ->
                    curr.copy(
                        settings = settingsObj,
                        selectedTab = settingsObj.savedTabIndex.coerceIn(0, 5)
                    )
                }
            }
        }
    }

    fun addOrUpdateBook(book: Book) {
        viewModelScope.launch {
            val currentList = _state.value.books.toMutableList()
            val existingIndex = currentList.indexOfFirst { it.id == book.id }
            if (existingIndex >= 0) {
                currentList[existingIndex] = book
            } else {
                currentList.add(0, book)
            }
            repository.saveBooks(currentList)
        }
    }

    fun deleteBook(bookId: String) {
        viewModelScope.launch {
            val currentList = _state.value.books.filterNot { it.id == bookId }
            repository.saveBooks(currentList)
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            repository.saveSettings(newSettings)
        }
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun updateFormatFilters(filters: Set<String>) {
        _state.update { it.copy(selectedFormatFilters = filters) }
    }

    fun updateTab(index: Int) {
        _state.update { it.copy(selectedTab = index) }
        viewModelScope.launch {
            val updatedSettings = _state.value.settings.copy(savedTabIndex = index)
            repository.saveSettings(updatedSettings)
        }
    }

    fun importLibrary(newBooks: List<Book>, replace: Boolean) {
        viewModelScope.launch {
            val finalBooks = if (replace) {
                newBooks
            } else {
                val existingMap = _state.value.books.associateBy { it.id }.toMutableMap()
                for (nb in newBooks) {
                    existingMap[nb.id] = nb
                }
                existingMap.values.toList()
            }
            repository.saveBooks(finalBooks)
        }
    }

    fun importSettingsJson(jsonStr: String): Boolean {
        return try {
            val newSettings = AppSettings.parseSettings(jsonStr)
            updateSettings(newSettings)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun resetColors() {
        val default = AppSettings()
        val reset = _state.value.settings.copy(
            accent = default.accent,
            cPlanned = default.cPlanned,
            cReading = default.cReading,
            cPaused = default.cPaused,
            cCompleted = default.cCompleted,
            cDropped = default.cDropped,
            tagSeries = default.tagSeries,
            tagWeb = default.tagWeb,
            tagSingle = default.tagSingle,
            tagHybrid = default.tagHybrid,
            tagOngoing = default.tagOngoing
        )
        updateSettings(reset)
    }

    fun showSnack(msg: String) {
        _state.update { it.copy(snackMessage = msg) }
    }

    fun dismissSnack() {
        _state.update { it.copy(snackMessage = null) }
    }
}
