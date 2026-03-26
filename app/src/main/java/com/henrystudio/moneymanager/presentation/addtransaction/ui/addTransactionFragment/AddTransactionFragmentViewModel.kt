package com.henrystudio.moneymanager.presentation.addtransaction.ui.addTransactionFragment

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.core.util.Helper.Companion.parseDisplayDateToLocalDate
import com.henrystudio.moneymanager.data.model.CategoryType
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.domain.usecase.transaction.TransactionUseCases
import com.henrystudio.moneymanager.presentation.addtransaction.model.AddTransactionEvent
import com.henrystudio.moneymanager.presentation.addtransaction.model.CategoryItem
import com.henrystudio.moneymanager.presentation.addtransaction.model.FieldState
import com.henrystudio.moneymanager.presentation.addtransaction.model.FieldType
import com.henrystudio.moneymanager.presentation.addtransaction.model.FieldUiState
import com.henrystudio.moneymanager.presentation.addtransaction.model.SaveResult
import com.henrystudio.moneymanager.presentation.model.SaveTransactionParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddTransactionFragmentViewModel @Inject constructor(
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

    private var isInitialized = false
    @RequiresApi(Build.VERSION_CODES.O)
    fun saveTransaction(
        params: SaveTransactionParams
    ) {
        if (params.category.isEmpty() || params.account.isEmpty()) {
            viewModelScope.launch {
                _event.emit(AddTransactionEvent.ShowToast("fill_required"))
            }
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
                // ✅ emit 1 event duy nhất
                _event.emit(
                    AddTransactionEvent.SaveCompleted(
                        date = params.date,
                        localDate = localDate,
                        closeAfterSave = params.closeAfterSave
                    )
                )
            } catch (e: Exception) {
                _event.emit(
                    AddTransactionEvent.ShowToast(e.message ?: "Unknown error")
                )
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

    fun onAmountChanged(input: String) {
        val clean = input.replace("[^\\d]".toRegex(), "")

        val formatted = if (clean.isNotEmpty()) {
            val number = clean.toLongOrNull() ?: 0L
            Helper.formatCurrency(number.toDouble())
        } else ""

        val current = uiState.value.amountRaw
        val isTouched = current.state != FieldState.IDLE
        val newState = if (!isTouched) {
            FieldState.IDLE
        } else {
            validateAmount(clean)
        }

        _uiState.update {
            it.copy(
                amountRaw = FieldUiState(clean, newState),
                amountFormatted = formatted
            )
        }
    }

    fun onTransactionTypeChanged(value: Boolean) {
        _uiState.update {
            it.copy(isIncome = value, category = "", account = "")
        }
    }

    fun onCategoryChanged(value: String) {
        _uiState.update {
            it.copy(category = value)
        }
    }

    fun onAccountChanged(value: String) {
        _uiState.update {
            it.copy(account = value)
        }
    }

    fun onNoteChanged(value: String) {
        _uiState.update {
            it.copy(note = value)
        }
    }

    fun onDateChanged(date: String) {
        _uiState.update {
            it.copy(date = date)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onSaveClicked(closeAfterSave: Boolean) {
        val state = _uiState.value

        val params = SaveTransactionParams(
            amount = state.amountRaw.text,
            category = state.category,
            account = state.account,
            note = state.note,
            date = state.date,
            isIncome = state.isIncome,
            existing = state.existingTransaction,
            closeAfterSave = closeAfterSave
        )

        saveTransaction(params)
    }

    fun initTransaction(transaction: Transaction?) {
        if (isInitialized) return
        isInitialized = true
        if (transaction != null) {
            _uiState.update {
                it.copy(
                    amountRaw = FieldUiState(transaction.amount.toLong().toString(), FieldState.VALID),
                    amountFormatted = Helper.formatCurrency(transaction.amount),
                    category = transaction.categoryParentName + "/" + transaction.categorySubName,
                    account = transaction.account,
                    note = transaction.note,
                    date = transaction.date,
                    isIncome = transaction.isIncome,
                    isEditMode = true,
                    isContinueVisible = false,
                    existingTransaction = transaction
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isEditMode = false,
                    isContinueVisible = true,
                    date = Helper.getFormattedDateToday(),
                    isIncome = false
                )
            }
        }
    }

    fun resetForm() {
        _uiState.update {
            it.copy(
                amountRaw = FieldUiState("", FieldState.IDLE),
                amountFormatted = "",
                category = "",
                account = "",
                note = "",
                date = "",
            )
        }
    }

    fun getNextEmptyField(): FieldType? {
        val state = _uiState.value
        return when {
            state.amountRaw.text.isEmpty() -> FieldType.AMOUNT
            state.category.isEmpty() -> FieldType.CATEGORY
            state.account.isEmpty() -> FieldType.ACCOUNT
            state.note.isEmpty() -> FieldType.NOTE
            else -> null
        }
    }

    fun onNextClicked() {
        val nextField = getNextEmptyField()
        viewModelScope.launch {
            _event.emit(AddTransactionEvent.FocusField(nextField))
        }
    }

    fun onUserStartEditing() {
        if(_uiState.value.isEditMode) {
            _uiState.update {
                it.copy(isEditMode = false)
            }
        }
    }

    fun onCopyClicked() {
        val current = _uiState.value.existingTransaction ?: return

        _uiState.update {
            it.copy(
                // giữ data cũ
                amountRaw = FieldUiState(current.amount.toLong().toString(), FieldState.VALID),
                amountFormatted = Helper.formatCurrency(current.amount),
                category = current.categoryParentName + "/" + current.categorySubName,
                account = current.account,
                note = current.note,
                isIncome = current.isIncome,

                // 🔥 QUAN TRỌNG
                existingTransaction = null, // → chuyển sang mode tạo mới
                isEditMode = false,

                // chỉ đổi ngày
                date = Helper.getFormattedDateToday()
            )
        }

        // focus lại amount cho UX đẹp
        viewModelScope.launch {
            _event.emit(AddTransactionEvent.FocusField(FieldType.AMOUNT))
        }
    }

    fun onDeleteClicked() {
        val transaction = _uiState.value.existingTransaction ?: return

        viewModelScope.launch {
            deleteTransaction(transaction)
            _event.emit(AddTransactionEvent.ShowToast("transaction_delete"))
            _event.emit(AddTransactionEvent.NavigateBack)
        }
    }

    fun onBookmarkClicked() {
        val transaction = _uiState.value.existingTransaction ?: return

        viewModelScope.launch {
            val update = transaction.copy(isBookmarked = true)
            updateTransaction(update)
            _event.emit(AddTransactionEvent.ShowToast("transaction_bookmark"))
            _event.emit(AddTransactionEvent.NavigateBack)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun handleSaveSuccess(date: String, closeAfterSave: Boolean) {
        viewModelScope.launch {
            val localDate = parseDisplayDateToLocalDate(date)
            // 🔥 emit event thay vì xử lý trực tiếp
            _event.emit(AddTransactionEvent.SaveCompleted(
                date = date,
                localDate = localDate,
                closeAfterSave = closeAfterSave
            ))
        }
    }

    fun getSelectedCategoryType() : CategoryType {
        return if (_uiState.value.isIncome) CategoryType.INCOME else CategoryType.EXPENSE
    }

    fun getTransactionColor(): Int {
        return if (_uiState.value.isIncome) R.color.income else R.color.red
    }

    fun formatCategoryDisplay(item: CategoryItem): String {
        val parentEmoji = item.parentEmoji ?: ""
        val parentName = item.parentName?.let { "$it/" } ?: ""
        return "$parentEmoji $parentName ${item.emoji} ${item.name}"
    }

    private fun validateAmount(text: String): FieldState {
        if (text.isEmpty()) return FieldState.ERROR
        return try {
            val value = text.replace(",", "").toDouble()
            if (value > 0) FieldState.VALID else FieldState.ERROR
        } catch (e: Exception) {
            FieldState.ERROR
        }
    }

    private fun validateRequired(text: String): FieldState {
        return if (text.isEmpty()) FieldState.ERROR else FieldState.VALID
    }

    fun onAmountTouched() {
        val current = uiState.value.amountRaw
        val newState = validateAmount(current.text)

        _uiState.value = uiState.value.copy(
            amountRaw = current.copy(state = newState)
        )
    }
}