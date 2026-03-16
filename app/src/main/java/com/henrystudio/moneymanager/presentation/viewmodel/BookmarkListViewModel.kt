package com.henrystudio.moneymanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.views.bookmark.BookmarkListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class BookmarkListViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(BookmarkListUiState())
    val uiState: StateFlow<BookmarkListUiState> = _uiState.asStateFlow()

    fun updateBookmarks(list: List<Transaction>) {
        _uiState.update {
            it.copy(bookmarks = list, isEmpty = list.isEmpty())
        }
    }
}
