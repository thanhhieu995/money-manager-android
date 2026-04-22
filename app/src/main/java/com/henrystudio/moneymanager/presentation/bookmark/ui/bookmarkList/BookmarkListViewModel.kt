package com.henrystudio.moneymanager.presentation.bookmark.ui.bookmarkList

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.core.util.Helper.Companion.epochMillisToLocalDate
import com.henrystudio.moneymanager.core.util.Helper.Companion.formatEpochMillisToDateKey
import com.henrystudio.moneymanager.domain.usecase.category.CategoryUseCases
import com.henrystudio.moneymanager.domain.usecase.transaction.TransactionUseCases
import com.henrystudio.moneymanager.presentation.bookmark.model.BookmarkItemUi
import com.henrystudio.moneymanager.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarkListViewModel @Inject constructor(
    private val transactionUseCases: TransactionUseCases,
    private val categoryUseCases: CategoryUseCases
) : ViewModel() {

    @RequiresApi(Build.VERSION_CODES.O)
    val uiState =
        combine(
            transactionUseCases.getBookmarkedTransactionsUseCase(),
            categoryUseCases.getAllCategories()
        ) { transactions, categories ->
            val map = categories.associateBy { it.id }
            val bookmarks = transactions.filter { it.isBookmarked }
                .map { tx ->
                    val (label, _) = Helper.resolveTransactionCategoryLabels(tx, map)
                    BookmarkItemUi(
                        transaction = tx,
                        date = epochMillisToLocalDate(tx.date),
                        category = label,
                        content = tx.note,
                        account = tx.account,
                        amount = tx.amount,
                        isIncome = tx.isIncome
                    )
                }
            BookmarkListUiState(
                bookmarks = bookmarks,
            )
        }
            .map { state ->
                if (state.bookmarks.isEmpty()) UiState.Empty
                else UiState.Success(state)
            }
            .onStart { emit(UiState.Loading) }
            .catch { emit(UiState.Error(it.message ?: "Unknown error")) }
            . stateIn(
                scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = UiState.Loading
    )

    fun onAction(action: BookmarkListAction) {
        when (action) {
            is BookmarkListAction.DeleteBookmark -> {
                // Handle delete bookmark action
                // This is where you would add logic to remove the transaction from bookmarks
                viewModelScope.launch {
                    transactionUseCases.updateTransactionsUseCase(
                        action.transaction.copy(isBookmarked = false, bookmarkedAt = null)
                    )
                }
            }
        }
    }
}
