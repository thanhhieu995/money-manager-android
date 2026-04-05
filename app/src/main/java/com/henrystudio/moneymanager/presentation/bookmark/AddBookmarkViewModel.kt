package com.henrystudio.moneymanager.presentation.bookmark

import androidx.lifecycle.ViewModel
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.bookmark.AddBookmarkUiState
import com.henrystudio.moneymanager.presentation.addtransaction.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AddBookmarkViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AddBookmarkUiState())
    val uiState: StateFlow<AddBookmarkUiState> = _uiState.asStateFlow()

    fun updateTransactions(list: List<Transaction>) {
        _uiState.update {
            it.copy(transactions = list)
        }
    }

    fun setLoading() {
        _uiState.update {
            it.copy(
                dataTransactionGroupState = UiState.Loading
            )
        }
    }

    fun setEmpty() {
        _uiState.update {
            it.copy(
                dataTransactionGroupState = UiState.Empty
            )
        }
    }
}
