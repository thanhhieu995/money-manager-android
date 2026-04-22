package com.henrystudio.moneymanager.presentation.addtransaction.ui.addTransactionFragment

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.core.util.Helper.Companion.epochMillisToLocalDate
import com.henrystudio.moneymanager.core.util.Helper.Companion.toDisplayLabel
import com.henrystudio.moneymanager.core.util.Helper.Companion.localDateToStartOfDayEpochMillis
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.domain.usecase.category.CategoryUseCases
import com.henrystudio.moneymanager.presentation.addtransaction.model.CategoryItem
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
import javax.inject.Inject

@HiltViewModel
class AddTransactionFragmentViewModel @Inject constructor(
    private val transactionUseCases: TransactionUseCases,
    private val categoryUseCases: CategoryUseCases
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<AddTransactionEvent>()
    val event = _event.asSharedFlow()

    private var lastSelectedCategory : CategoryItem? = null
    private var categoriesById: Map<Int, com.henrystudio.moneymanager.data.model.Category> = emptyMap()
    private var originalTransaction: Transaction? = null
    private var newFormBaseline: NewFormBaseline? = null
    private var draftCategoryIdsForNew: Pair<Int, Int?>? = null

    private data class NewFormBaseline(
        val amountRaw: String,
        val categoryParent: String,
        val categoryChild: String,
        val account: String,
        val note: String,
        val date: LocalDate,
        val isIncome: Boolean
    )
    init {
        viewModelScope.launch {
            categoryUseCases.getAllCategories()
                .collect { categories ->
                    categoriesById = categories.associateBy { it.id }
                    // 🔥 FIX: nếu đang edit thì re-bind category
                    val existing = _uiState.value.existingTransaction
                    if (existing != null) {
                        updateCategoryFromExisting(existing)
                    }
                }
        }
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
        if (params.categoryParent.isEmpty() || params.account.isEmpty()) {
            emitEvent(AddTransactionEvent.ShowToast("fill_required"))
            return
        }

        val amount = params.amount.replace("[^\\d]".toRegex(), "").toLongOrNull() ?: 0L
        val localDate = epochMillisToLocalDate(params.date)
        val dateForDb = localDateToStartOfDayEpochMillis(localDate)

        viewModelScope.launch {
            try {
                if (params.existing != null) {
                    val ids = resolveCategoryIdsForSave(existing = params.existing)
                    val updated = params.existing.copy(
                        categoryParentId = ids.first,
                        categoryChildId = ids.second,
                        note = params.note.trim(),
                        account = params.account,
                        amount = amount,
                        isIncome = params.isIncome,
                        date = dateForDb,
                        createdAt = params.existing.createdAt,
                        updatedAt = System.currentTimeMillis()
                    )
                    transactionUseCases.updateTransactionsUseCase(updated)
                } else {
                    val ids = resolveCategoryIdsForSave(existing = null)
                    val newTransaction = Transaction(
                        title = "",
                        categoryParentId = ids.first,
                        categoryChildId = ids.second,
                        note = params.note.trim(),
                        account = params.account,
                        amount = amount,
                        isIncome = params.isIncome,
                        date = dateForDb,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = null
                    )
                    transactionUseCases.addTransactionUseCase(newTransaction)
                }

                // 🚀 chạy nền, không chờ
                launch {
                    bumpCategoryUsage(params)
                }

                // ✅ emit 1 event duy nhất
                _event.emit(
                    AddTransactionEvent.SaveCompleted(
                        dateEpochMillis = dateForDb,
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

    private suspend fun bumpCategoryUsage(params: SaveTransactionParams) {
        val ids = resolveCategoryIdsForSave(existing = params.existing)
        if (ids.first > 0) {
            categoryUseCases.increaseCategoryUsage(ids.first)
        }
        ids.second?.let { childId ->
            if (childId > 0) categoryUseCases.increaseCategoryUsage(childId)
        }
    }

    private fun resolveCategoryIdsForSave(existing: Transaction?): Pair<Int, Int?> {
        // ưu tiên item user vừa chọn
        lastSelectedCategory?.let { item ->
            val parentId = item.parentId ?: item.id
            val childId = if (item.parentId != null) item.id else null
            return parentId to childId
        }

        // edit mode: user không chọn lại -> dùng ID sẵn có
        if (existing != null) {
            return existing.categoryParentId to existing.categoryChildId
        }

        // create/copy mode: giữ lại ID category đã có sẵn (ví dụ từ Copy)
        draftCategoryIdsForNew?.let { return it }

        // fallback an toàn (không nên xảy ra vì params.categoryParent required)
        return 0 to null
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun onTransactionTypeChanged(isIncome: Boolean) {
        val current = _uiState.value
        val amountState = validateAmount(current.amountRaw.text)

        // Đổi type => category chọn trước đó không còn hợp lệ
        lastSelectedCategory = null
        draftCategoryIdsForNew = null

        _uiState.update {
            val next = it.copy(
                isIncome = isIncome,

                amountRaw = current.amountRaw.copy(
                    state = amountState
                ),
                category = CategorySelectionUiState(),
                account = FieldUiState("", FieldState.IDLE),
                note = current.note.copy(
                    state = FieldState.IDLE
                )
            )
            next.copy(isDirty = computeIsDirty(next))
        }

        // lấy field trống đầu tiên
        val nextField = getNextEmptyField()
        // emit when field need focus
        nextField?.let {
            emitEvent(AddTransactionEvent.FocusField(it))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onNoteChanged(value: String) {
        _uiState.update {
            val next = it.copy(
                note = it.note.copy(
                text = value,
                state = FieldState.IDLE
                )
            )
            next.copy(isDirty = computeIsDirty(next))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onDateChanged(date: LocalDate) {
        _uiState.update {
            val next = it.copy(date = date)
            next.copy(isDirty = computeIsDirty(next))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onSaveClicked(closeAfterSave: Boolean) {
        val state = _uiState.value
        val amountState = validateAmount(state.amountRaw.text)
        val categoryState = validateRequired(state.category.parent.text)
        val accountState = validateRequired(state.account.text)

        _uiState.update {
            it.copy(
                amountRaw = it.amountRaw.copy(
                    state = amountState,
                    isTouched = true
                ),
                category = it.category.copy(
                    parent = it.category.parent.copy(
                        state = categoryState,
                        isTouched = true
                    ),
                    child = it.category.child.copy(
                        state = categoryState,
                        isTouched = true
                    )
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
            categoryParent = state.category.parent.text,
            categoryChild = state.category.child.text,
            account = state.account.text,
            note = state.note.text,
            date = localDateToStartOfDayEpochMillis(state.date),
            isIncome = state.isIncome,
            existing = state.existingTransaction,
            closeAfterSave = closeAfterSave
        )

        saveTransaction(params)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initTransaction(transaction: Transaction?) {
        originalTransaction = transaction
        if (isInitialized) return
        isInitialized = true
        if (transaction != null) {
            newFormBaseline = null
            draftCategoryIdsForNew = null
            val parentLabel = categoriesById[transaction.categoryParentId]?.toDisplayLabel().orEmpty()
            val childLabel = transaction.categoryChildId?.let { categoriesById[it]?.toDisplayLabel() }.orEmpty()
            _uiState.update {
                it.copy(
                    amountRaw = FieldUiState(transaction.amount.toString(), FieldState.VALID),
                    amountFormatted = transaction.amount,
                    category = CategorySelectionUiState(
                        parent = FieldUiState(parentLabel, FieldState.VALID),
                        child = FieldUiState(childLabel, FieldState.VALID)
                    ),
                    account = FieldUiState(transaction.account, FieldState.VALID),
                    note = FieldUiState(transaction.note, FieldState.VALID),
                    date = epochMillisToLocalDate(transaction.date),
                    isIncome = transaction.isIncome,
                    // Edit mode: ban đầu chưa đổi gì -> chưa dirty
                    isDirty = false,
                    isContinueVisible = false,
                    existingTransaction = transaction
                )
            }
        } else {
            lastSelectedCategory = null
            draftCategoryIdsForNew = null
            _uiState.update {
                val next = it.copy(
                    isContinueVisible = true,
                    date = epochMillisToLocalDate(transaction?.date ?: System.currentTimeMillis()),
                    isIncome = false
                )
                next.copy(isDirty = computeIsDirty(next))
            }
            newFormBaseline = buildNewBaseline(_uiState.value)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun resetForm() {
        lastSelectedCategory = null
        draftCategoryIdsForNew = null
        if (_uiState.value.existingTransaction == null) {
            originalTransaction = null
        }
        _uiState.update {
            val next = it.copy(
                amountRaw = FieldUiState("", FieldState.IDLE),
                amountFormatted = 0L,
                category = CategorySelectionUiState(
                    parent = FieldUiState("", FieldState.IDLE),
                    child = FieldUiState("", FieldState.IDLE)
                ),
                account = FieldUiState("", FieldState.IDLE),
                note = FieldUiState("", FieldState.IDLE),
                date = _uiState.value.date,
            )
            next.copy(isDirty = computeIsDirty(next))
        }
        if (_uiState.value.existingTransaction == null) {
            newFormBaseline = buildNewBaseline(_uiState.value)
        }
    }

    fun getNextEmptyField(): FieldType? {
        val state = _uiState.value
        return when {
            state.amountRaw.text.isEmpty() -> FieldType.AMOUNT
            state.category.parent.text.isEmpty() -> FieldType.CATEGORY
            state.account.text.isEmpty() -> FieldType.ACCOUNT
            state.note.text.isEmpty() -> FieldType.NOTE
            else -> null
        }
    }

    fun onNextClicked() {
        val nextField = getNextEmptyField()
        emitEvent(AddTransactionEvent.FocusField(nextField))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onCopyClicked() {
        val current = _uiState.value.existingTransaction ?: return

        val parentLabel = categoriesById[current.categoryParentId]?.toDisplayLabel().orEmpty()
        val childLabel = current.categoryChildId?.let { categoriesById[it]?.toDisplayLabel() }.orEmpty()
        // chuyển sang mode tạo mới -> không so với original nữa
        originalTransaction = null
        lastSelectedCategory = null
        draftCategoryIdsForNew = current.categoryParentId to current.categoryChildId
        _uiState.update {
            val next = it.copy(
                // giữ data cũ
                amountRaw = FieldUiState(current.amount.toString(), FieldState.VALID),
                amountFormatted = current.amount,
                category = CategorySelectionUiState(
                    parent = FieldUiState(parentLabel, FieldState.VALID),
                    child = FieldUiState(childLabel, FieldState.VALID)
                ),
                account = FieldUiState(current.account, FieldState.VALID),
                note = FieldUiState(current.note, FieldState.VALID),
                isIncome = current.isIncome,

                // 🔥 QUAN TRỌNG
                existingTransaction = null, // → chuyển sang mode tạo mới

                // chỉ đổi ngày
                date = epochMillisToLocalDate(current.date).let { date ->
                    if (date.isBefore(LocalDate.now())) {
                        LocalDate.now()
                    } else date
                },
            )
            next.copy(isDirty = computeIsDirty(next))
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
        val update = transaction.copy(isBookmarked = true, bookmarkedAt = System.currentTimeMillis())
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun onAmountChanged(input: String) {
        val clean = input.replace("[^\\d]".toRegex(), "")

        val formatted = if (clean.isNotEmpty()) {
            val number = clean.toLongOrNull() ?: 0L
            number
        } else 0L

        val current = _uiState.value.amountRaw

        val newState = if (current.isTouched) {
            validateAmount(clean)
        } else FieldState.IDLE

        _uiState.update {
            val next = it.copy(
                amountRaw = current.copy(
                    text = clean,
                    state = newState
                ),
                amountFormatted = formatted,
            )
            next.copy(isDirty = computeIsDirty(next))
        }
    }

    fun onCategoryClicked() {
        val current = _uiState.value.category

        _uiState.update {
            it.copy(
                category = current.copy(
                    parent = current.parent.copy(
                        isTouched = true
                    ),
                    child = current.child.copy(
                        isTouched = true
                    )
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun onCategorySelected(categoryItem: CategoryItem) {
        val current = _uiState.value.category

        categoryJustSelected = true
        lastSelectedCategory = categoryItem
        // đã chọn lại category => lấy theo lựa chọn mới
        draftCategoryIdsForNew = null

        val parentName = categoryItem.parentName ?: categoryItem.name
        val parentEmoji = categoryItem.parentEmoji ?: categoryItem.emoji
        val childName = if (categoryItem.parentName != null) categoryItem.name else ""
        val childEmoji = if (categoryItem.parentName != null) categoryItem.emoji else ""

        val parentDisplay = listOf(parentEmoji, parentName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
        val childDisplay = listOf(childEmoji, childName)
            .filter { it.isNotBlank() }
            .joinToString(" ")

        _uiState.update {
            val next = it.copy(
                category = current.copy(
                    parent = current.parent.copy(
                        text = parentDisplay,
                        isTouched = true,
                        isFocused = false,
                        state = FieldState.VALID
                    ),
                    child = current.child.copy(
                        text = childDisplay,
                        isTouched = true,
                        isFocused = false,
                        state = if (childDisplay.isEmpty()) FieldState.IDLE else FieldState.VALID
                    )
                ),
            )
            next.copy(isDirty = computeIsDirty(next))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onAccountSelected(text: String) {
        val current = _uiState.value.account

        accountJustSelected = true

        _uiState.update {
            val next = it.copy(
                account = current.copy(
                    text = text,
                    isTouched = true,
                    isFocused = false,
                    state = FieldState.VALID
                )
            )
            next.copy(isDirty = computeIsDirty(next))
        }
    }

    fun onCategoryDismissed() {
        val current = _uiState.value.category

        val newState = if (!categoryJustSelected && current.parent.isTouched) {
            validateRequired(current.parent.text)
        } else {
            current.parent.state
        }

        _uiState.update {
            it.copy(
                category = current.copy(
                    parent = current.parent.copy(
                        isFocused = false,
                        isTouched = true,
                        state = newState
                    ),
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

        val newState = if (!hasFocus && current.parent.isTouched) {
            validateRequired(current.parent.text)
        } else current.parent.state

        _uiState.update {
            it.copy(
                category = current.copy(
                    parent = current.parent.copy(
                        isFocused = hasFocus,
                        isTouched = true,
                        state = newState
                    ),
                    child = current.child.copy(
                        isFocused = false
                    )
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

    private fun updateCategoryFromExisting(transaction: Transaction) {
        val parentLabel = categoriesById[transaction.categoryParentId]
            ?.toDisplayLabel().orEmpty()

        val childLabel = transaction.categoryChildId
            ?.let { categoriesById[it]?.toDisplayLabel() }
            .orEmpty()

        _uiState.update {
            it.copy(
                category = CategorySelectionUiState(
                    parent = FieldUiState(parentLabel, FieldState.VALID),
                    child = FieldUiState(childLabel, FieldState.VALID)
                )
            )
        }
    }

    private fun buildNewBaseline(state: AddTransactionUiState): NewFormBaseline {
        return NewFormBaseline(
            amountRaw = state.amountRaw.text,
            categoryParent = state.category.parent.text,
            categoryChild = state.category.child.text,
            account = state.account.text,
            note = state.note.text,
            date = state.date,
            isIncome = state.isIncome
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun computeIsDirty(state: AddTransactionUiState): Boolean {
        // Edit mode: so với transaction gốc
        originalTransaction?.let { original ->
            val currentAmount = state.amountRaw.text.toLongOrNull() ?: 0L

            val currentParentId = lastSelectedCategory?.parentId
                ?: original.categoryParentId

            val currentChildId = lastSelectedCategory?.id
                ?: original.categoryChildId

            return currentAmount != original.amount ||
                    state.account.text != original.account ||
                    state.note.text != original.note ||
                    state.isIncome != original.isIncome ||
                    currentParentId != original.categoryParentId ||
                    currentChildId != original.categoryChildId ||
                    state.date != epochMillisToLocalDate(original.date)
        }

        // Create mode (tạo mới / sau copy): luôn hiện layoutSave theo UI đang dùng
        return true
    }
}
