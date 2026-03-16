package com.henrystudio.moneymanager.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.domain.usecase.transaction.TransactionUseCases
import com.henrystudio.moneymanager.presentation.views.addtransaction.SaveResult
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

    init {
        loadNoteSuggestions()
    }

    private fun loadNoteSuggestions() {
        viewModelScope.launch {
            transactionUseCases.getTransactionsUseCase().collect { transactions ->
                val suggestions = transactions.map { it.note }.distinct()
                _uiState.update { it.copy(noteSuggestions = suggestions) }
            }
        }
    }

    fun getFormattedDateToday(): String {
        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd/MM/yy (EEE)", Helper.getAppLocale())
        return dateFormat.format(currentDate)
    }

    fun formatPickedDate(year: Int, month: Int, day: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        val dateFormat = SimpleDateFormat("dd/MM/yy (EEE)", Helper.getAppLocale())
        return dateFormat.format(calendar.time)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun parseDisplayDateToLocalDate(dateStr: String): LocalDate? {
        return try {
            val inputLocale = Helper.getAppLocale()
            val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yy (EEE)", inputLocale)
            LocalDate.parse(dateStr, inputFormatter)
        } catch (e: Exception) {
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveTransaction(
        amountStr: String,
        categoryStr: String,
        accountStr: String,
        noteStr: String,
        dateStr: String,
        isIncome: Boolean,
        existingTransaction: Transaction?,
        closeAfterSave: Boolean
    ) {
        if (categoryStr.isEmpty() || accountStr.isEmpty()) {
            _uiState.update { it.copy(saveResult = SaveResult.Error("fill_required")) }
            return
        }

        val amount = amountStr.replace("[^\\d]".toRegex(), "").toDoubleOrNull() ?: 0.0
        val categoryParts = Helper.splitCategoryName(categoryStr)
        
        val localDate = parseDisplayDateToLocalDate(dateStr) ?: LocalDate.now()
        val englishFormatter = DateTimeFormatter.ofPattern("dd/MM/yy (EEE)", Locale.ENGLISH)
        val dateForDb = localDate.format(englishFormatter)

        viewModelScope.launch {
            try {
                if (existingTransaction != null) {
                    val updated = existingTransaction.copy(
                        categoryParentName = categoryParts.parent,
                        categorySubName = categoryParts.sub,
                        note = noteStr.trim(),
                        account = accountStr,
                        amount = amount,
                        isIncome = isIncome,
                        date = dateForDb
                    )
                    transactionUseCases.updateTransactionsUseCase(updated)
                } else {
                    val newTransaction = Transaction(
                        title = "",
                        categoryParentName = categoryParts.parent,
                        categorySubName = categoryParts.sub,
                        note = noteStr.trim(),
                        account = accountStr,
                        amount = amount,
                        isIncome = isIncome,
                        date = dateForDb
                    )
                    transactionUseCases.addTransactionUseCase(newTransaction)
                }
                _uiState.update { it.copy(saveResult = SaveResult.Success(closeAfterSave)) }
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
