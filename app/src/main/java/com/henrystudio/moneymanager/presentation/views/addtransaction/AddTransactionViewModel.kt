package com.henrystudio.moneymanager.presentation.views.addtransaction

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.core.util.Helper.Companion.parseDisplayDateToLocalDate
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.domain.usecase.transaction.TransactionUseCases
import com.henrystudio.moneymanager.presentation.model.SaveTransactionParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

data class AddTransactionUiState(
    val noteSuggestions: List<String> = emptyList(),
    val saveResult: SaveResult? = null
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionUseCases: TransactionUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<AddTransactionEvent>()
    val event = _event.asSharedFlow()

    init {
        viewModelScope.launch {
            transactionUseCases.getTransactionsUseCase()
                .map { list -> list.map { it.note }.distinct() }
                .collect { suggestions ->
                    _uiState.update { it.copy(noteSuggestions = suggestions) }
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveTransaction(
        params: SaveTransactionParams
    ) {
        if (params.category.isEmpty() || params.account.isEmpty()) {
            _uiState.update { it.copy(saveResult = SaveResult.Error("fill_required")) }
            return
        }

        val amount = params.amount.replace("[^\\d]".toRegex(), "").toDoubleOrNull() ?: 0.0
        val categoryParts = Helper.splitCategoryName(params.category)
        
        val localDate = parseDisplayDateToLocalDate(params.date) ?: LocalDate.now()
        val englishFormatter = DateTimeFormatter.ofPattern("dd/MM/yy (EEE)", Locale.ENGLISH)
        val dateForDb = localDate.format(englishFormatter)

        viewModelScope.launch {
            try {
                if (params.existing != null) {
                    val updated = params.existing.copy(
                        categoryParentName = categoryParts.parent,
                        categorySubName = categoryParts.sub,
                        note = params.note.trim(),
                        account = params.account,
                        amount = amount,
                        isIncome = params.isIncome,
                        date = dateForDb
                    )
                    transactionUseCases.updateTransactionsUseCase(updated)
                } else {
                    val newTransaction = Transaction(
                        title = "",
                        categoryParentName = categoryParts.parent,
                        categorySubName = categoryParts.sub,
                        note = params.note.trim(),
                        account = params.account,
                        amount = amount,
                        isIncome = params.isIncome,
                        date = dateForDb
                    )
                    transactionUseCases.addTransactionUseCase(newTransaction)
                }
                if (params.closeAfterSave) {
                    _event.emit(AddTransactionEvent.NavigateBackToDaily)
                }
                _uiState.update { it.copy(saveResult = SaveResult.Success(params.closeAfterSave)) }
            } catch (e: Exception) {
                _uiState.update { it.copy(saveResult = SaveResult.Error(e.message ?: "Unknown Error")) }
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionUseCases.deleteTransactionUseCase(transaction)
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionUseCases.updateTransactionsUseCase(transaction)
        }
    }

    fun clearSaveResult() {
        _uiState.update { it.copy(saveResult = null) }
    }
}
