package com.henrystudio.moneymanager.presentation.addtransaction.ui.addTransactionFragment

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.core.util.Helper.Companion.parseDisplayDateToLocalDate
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.domain.usecase.transaction.TransactionUseCases
import com.henrystudio.moneymanager.presentation.addtransaction.model.AddTransactionEvent
import com.henrystudio.moneymanager.presentation.addtransaction.model.FieldState
import com.henrystudio.moneymanager.presentation.addtransaction.model.FieldType
import com.henrystudio.moneymanager.presentation.addtransaction.model.FieldUiState
import com.henrystudio.moneymanager.presentation.model.SaveTransactionParams
import com.henrystudio.moneymanager.presentation.model.TransactionType
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
    private var categoryJustSelected = false
    private var accountJustSelected = false

    private fun emitEvent(event: AddTransactionEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveTransaction(
        params: SaveTransactionParams
    ) {
        if (params.category.isEmpty() || params.account.isEmpty()) {
            emitEvent(AddTransactionEvent.ShowToast("fill_required"))
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

    fun onTransactionTypeChanged(isIncome: Boolean) {
        val current = _uiState.value
        val amountState = validateAmount(current.amountRaw.text)

        _uiState.update {
            it.copy(
                isIncome = isIncome,

                amountRaw = current.amountRaw.copy(
                    state = amountState
                ),
                category = FieldUiState("", FieldState.IDLE),
                account = FieldUiState("", FieldState.IDLE),
                note = current.note.copy(
                    state = FieldState.IDLE
                )
            )
        }

        // lấy field trống đầu tiên
        val nextField = getNextEmptyField()
        // emit when field need focus
        nextField?.let {
            emitEvent(AddTransactionEvent.FocusField(it))
        }
    }

    fun onNoteChanged(value: String) {
        _uiState.update {
            it.copy(
                note = it.note.copy(
                text = value,
                state = FieldState.IDLE
                )
            )
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
        val amountState = validateRequired(state.amountRaw.text)
        val categoryState = validateRequired(state.category.text)
        val accountState = validateRequired(state.account.text)

        _uiState.update {
            it.copy(
                amountRaw = it.amountRaw.copy(
                    state = amountState,
                    isTouched = true
                ),
                category = it.category.copy(
                    state = categoryState,
                    isTouched = true
                ),
                account = it.account.copy(
                    state = accountState,
                    isTouched = true
                ),
                date = it.date
            )
        }

        when {
            amountState == FieldState.ERROR -> {
                emitEvent(AddTransactionEvent.FocusField(FieldType.AMOUNT))
                return
            }
            categoryState == FieldState.ERROR -> {
                emitEvent(AddTransactionEvent.FocusField(FieldType.CATEGORY))
                return
            }
            accountState == FieldState.ERROR -> {
                emitEvent(AddTransactionEvent.FocusField(FieldType.ACCOUNT))
                return
            }
            else -> {}
        }

        val params = SaveTransactionParams(
            amount = state.amountRaw.text,
            category = state.category.text,
            account = state.account.text,
            note = state.note.text,
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
                    category = FieldUiState(transaction.categoryParentName + "/" + transaction.categorySubName, FieldState.VALID),
                    account = FieldUiState(transaction.account, FieldState.VALID),
                    note = FieldUiState(transaction.note, FieldState.VALID),
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
                category = FieldUiState("", FieldState.IDLE),
                account = FieldUiState("", FieldState.IDLE),
                note = FieldUiState("", FieldState.IDLE),
                date = _uiState.value.date,
            )
        }
    }

    fun getNextEmptyField(): FieldType? {
        val state = _uiState.value
        return when {
            state.amountRaw.text.isEmpty() -> FieldType.AMOUNT
            state.category.text.isEmpty() -> FieldType.CATEGORY
            state.account.text.isEmpty() -> FieldType.ACCOUNT
            state.note.text.isEmpty() -> FieldType.NOTE
            else -> null
        }
    }

    fun onNextClicked() {
        Log.d("DEBUG", "NEXT CLICKED")
        val nextField = getNextEmptyField()
        emitEvent(AddTransactionEvent.FocusField(nextField))
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
                category = FieldUiState(current.categoryParentName + "/" + current.categorySubName, FieldState.VALID),
                account = FieldUiState(current.account, FieldState.VALID),
                note = FieldUiState(current.note, FieldState.VALID),
                isIncome = current.isIncome,

                // 🔥 QUAN TRỌNG
                existingTransaction = null, // → chuyển sang mode tạo mới
                isEditMode = false,

                // chỉ đổi ngày
                date = Helper.getFormattedDateToday()
            )
        }

        // focus lại amount cho UX đẹp
        emitEvent(AddTransactionEvent.FocusField(FieldType.AMOUNT))
    }

    fun onDeleteClicked() {
        val transaction = _uiState.value.existingTransaction ?: return
        deleteTransaction(transaction)

        emitEvent(AddTransactionEvent.ShowToast("transaction_delete"))
        emitEvent(AddTransactionEvent.NavigateBack)
    }

    fun onBookmarkClicked() {
        val transaction = _uiState.value.existingTransaction ?: return
        val update = transaction.copy(isBookmarked = true)
        updateTransaction(update)

        emitEvent(AddTransactionEvent.ShowToast("transaction_bookmark"))
        emitEvent(AddTransactionEvent.NavigateBack)
    }

    fun getSelectedTransactionType() : TransactionType {
        return if (_uiState.value.isIncome) TransactionType.INCOME else TransactionType.EXPENSE
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

    fun onAmountFocusChanged(hasFocus: Boolean) {
        val current = _uiState.value.amountRaw

        val newState = if (!hasFocus && current.isTouched) {
            validateAmount(current.text)
        } else current.state

        _uiState.update {
            it.copy(
                amountRaw = current.copy(
                    isFocused = hasFocus,
                    isTouched = true,
                    state = newState
                )
            )
        }
    }

    fun onAmountChanged(input: String) {
        val clean = input.replace("[^\\d]".toRegex(), "")

        val formatted = if (clean.isNotEmpty()) {
            val number = clean.toLongOrNull() ?: 0L
            Helper.formatCurrency(number.toDouble())
        } else ""

        val current = _uiState.value.amountRaw

        val newState = if (current.isTouched) {
            validateAmount(clean)
        } else FieldState.IDLE

        _uiState.update {
            it.copy(
                amountRaw = current.copy(
                    text = clean,
                    state = newState
                ),
                amountFormatted = formatted
            )
        }
    }

    fun onCategoryClicked() {
        val current = _uiState.value.category

        _uiState.update {
            it.copy(
                category = current.copy(
                    isTouched = true
                )
            )
        }

        emitEvent(AddTransactionEvent.OpenCategoryPicker)
    }

    fun onAccountClicked() {
        val current = _uiState.value.account

        _uiState.update {
            it.copy(
                account = current.copy(
                    isTouched = true
                )
            )
        }

        emitEvent(AddTransactionEvent.OpenAccountPicker)
    }

    fun onCategorySelected(text: String) {
        val current = _uiState.value.category

        categoryJustSelected = true

        _uiState.update {
            it.copy(
                category = current.copy(
                    text = text,
                    isTouched = true,
                    isFocused = false,
                    state = FieldState.VALID
                )
            )
        }
    }

    fun onAccountSelected(text: String) {
        val current = _uiState.value.account

        accountJustSelected = true

        _uiState.update {
            it.copy(
                account = current.copy(
                    text = text,
                    isTouched = true,
                    isFocused = false,
                    state = FieldState.VALID
                )
            )
        }
    }

    fun onCategoryDismissed() {
        val current = _uiState.value.category

        val newState = if (!categoryJustSelected && current.isTouched) {
            validateRequired(current.text)
        } else {
            current.state
        }

        _uiState.update {
            it.copy(
                category = current.copy(
                    isFocused = false,
                    isTouched = true,
                    state = newState
                )
            )
        }

        // reset flag
        categoryJustSelected = false
    }

    fun onAccountDismissed() {
        val current = _uiState.value.account

        val newState = if (!accountJustSelected && current.isTouched) {
            validateRequired(current.text)
        } else {
            current.state
        }

        _uiState.update {
            it.copy(
                account = current.copy(
                    isFocused = false,
                    isTouched = true,
                    state = newState
                )
            )
        }

        // reset flag
        accountJustSelected = false
    }

    fun onCategoryFocusChanged(hasFocus: Boolean) {
        val current = _uiState.value.category

        val newState = if (!hasFocus && current.isTouched) {
            validateRequired(current.text)
        } else current.state

        _uiState.update {
            it.copy(
                category = current.copy(
                    isFocused = hasFocus,
                    isTouched = true,
                    state = newState
                )
            )
        }
    }

    fun onAccountFocusChanged(hasFocus: Boolean) {
        val current = _uiState.value.account

        val newState = if (!hasFocus && current.isTouched) {
            validateRequired(current.text)
        } else current.state

        _uiState.update {
            it.copy(
                account = current.copy(
                    isFocused = hasFocus,
                    isTouched = true,
                    state = newState
                )
            )
        }
    }
}