package com.henrystudio.moneymanager.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun updateFilterDate(date: LocalDate) {
        viewModelScope.launch(Dispatchers.Default) {
            val currentLocale: Locale = try {
                AppCompatDelegate.getApplicationLocales().let { locales ->
                    if (!locales.isEmpty) locales[0]!! else Locale.getDefault()
                }
            } catch (e: Exception) {
                Locale.getDefault()
            }
            val formatter = DateTimeFormatter.ofPattern("LLLL yyyy", currentLocale)
            _uiState.update {
                it.copy(bottomNavTitle = date.format(formatter))
            }
        }
    }
}

data class MainUiState(
    val bottomNavTitle: String = ""
)
