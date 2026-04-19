package com.henrystudio.moneymanager.presentation.bookmark.ui.addBookmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.domain.usecase.transaction.TransactionUseCases
import com.henrystudio.moneymanager.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddBookmarkViewModel @Inject constructor(
    private val transactionUseCases: TransactionUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddBookmarkUiState())
    val uiState: StateFlow<AddBookmarkUiState> = _uiState.asStateFlow()

    private val _event = Channel<AddBookmarkEvent>()
    val event = _event.receiveAsFlow()

    fun setState(state: UiState<List<Transaction>>) {
        _uiState.update {
            it.copy(
                state = state
            )
        }
    }

    private fun emitEvent(event: AddBookmarkEvent) {
        viewModelScope.launch {
            _event.send(event)
        }
    }

    fun onAction(action: AddBookmarkAction) {
        when (action) {
            is AddBookmarkAction.BookmarkClicked -> {
                // Handle bookmark click, e.g., save the transaction as a bookmark
                // This is where you would add logic to save the transaction to bookmarks
                bookmarkTransaction(action.transaction)
                emitEvent(AddBookmarkEvent.NavigationBack)
            }
        }
    }

    private fun bookmarkTransaction(transaction: Transaction) {
        // Implement the logic to bookmark the transaction
        // This could involve updating a database or shared preferences
       viewModelScope.launch{
           transactionUseCases.updateTransactionsUseCase(
               transaction.copy(isBookmarked = true)
           )
       }
    }
}
