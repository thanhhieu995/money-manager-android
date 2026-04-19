package com.henrystudio.moneymanager.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henrystudio.moneymanager.data.model.Category
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.data.model.TransactionGroup
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.domain.usecase.category.CategoryUseCases
import com.henrystudio.moneymanager.domain.usecase.transaction.TransactionUseCases
import com.henrystudio.moneymanager.presentation.model.Event
import com.henrystudio.moneymanager.presentation.model.FilterOption
import com.henrystudio.moneymanager.presentation.model.FilterPeriodStatistic
import com.henrystudio.moneymanager.presentation.model.TransactionType
import com.henrystudio.moneymanager.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Collections.emptyList
import javax.inject.Inject

/**
 * Shared transaction-level state and operations used across multiple screens
 * (daily, monthly, statistics, search, etc.).
 */
@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class SharedTransactionViewModel @Inject constructor(
    private val transactionUseCases: TransactionUseCases,
    private val categoryUseCases: CategoryUseCases
) : ViewModel() {
    private val _openDetail = MutableSharedFlow<Transaction>()
    val openDetail = _openDetail.asSharedFlow()

    private fun emitOpenDetail(transaction: Transaction) {
        viewModelScope.launch {
            _openDetail.emit(transaction)
        }
    }

    val allTransactionsState: StateFlow<UiState<List<Transaction>>> =
        transactionUseCases.getTransactionsUseCase()
            .map { list ->
                if (list.isEmpty()) UiState.Empty
                else UiState.Success(list)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = UiState.Loading
            )

    val categoriesState: StateFlow<List<Category>> =
        categoryUseCases.getAllCategories()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val groupedTransactionsState: StateFlow<UiState<List<TransactionGroup>>> =
        transactionUseCases.getTransactionsGroupUseCase()
            .map { list ->
                if (list.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(list)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = UiState.Loading // 👈 QUAN TRỌNG
            )
    private val _currentFilterDate = MutableStateFlow(LocalDate.now())
    val currentFilterDate: StateFlow<LocalDate> = _currentFilterDate

    private val _currentDailyNavigateTabPosition = MutableStateFlow(0)
    val currentDailyNavigateTabPosition: StateFlow<Int> = _currentDailyNavigateTabPosition

    private val _selectionMode = MutableStateFlow(false)
    val selectionMode: StateFlow<Boolean> = _selectionMode

    private val _selectedTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val selectedTransactions: StateFlow<List<Transaction>> = _selectedTransactions

    private val _navigateToWeekFromMonthly = MutableSharedFlow<Event<LocalDate>>()
    val navigateToWeekFromMonthly: SharedFlow<Event<LocalDate>> =
        _navigateToWeekFromMonthly.asSharedFlow()

    private val _filterOption = MutableStateFlow(
        FilterOption(FilterPeriodStatistic.Monthly, LocalDate.now())
    )
    val filterOption: StateFlow<FilterOption> = _filterOption

    private val _statisticTransactionType = MutableStateFlow(TransactionType.EXPENSE)
    val statisticTransactionType: StateFlow<TransactionType> = _statisticTransactionType

    private val _statisticListTransactionFilter =
        MutableStateFlow<List<Transaction>>(emptyList())
    val statisticListTransactionFilter: StateFlow<List<Transaction>> =
        _statisticListTransactionFilter

    // Note screen: combined category type + filtered transactions
    val combinedFilter: Flow<Pair<TransactionType, List<Transaction>>> =
        combine(statisticTransactionType, statisticListTransactionFilter) { type, list ->
            type to list
        }

    // Daily / Monthly screens: grouped transactions + currently selected date
    val combineGroupAndDate: Flow<Pair<UiState<List<TransactionGroup>>, LocalDate>> =
        combine(groupedTransactionsState, currentFilterDate) { state, date ->
            state to date
        }.flowOn(Dispatchers.Default)

    private val _currentStatisticTabPosition = MutableStateFlow(0)
    val currentStatisticTabPosition: StateFlow<Int> = _currentStatisticTabPosition

    val uiDataFlow = combine(
        combineGroupAndDate,
        filterOption
    ) {
        (transactions, selectedMonth), option -> {
            Triple(transactions, selectedMonth, option)
        }
    }

    // CRUD operations
    fun insert(transaction: Transaction) = viewModelScope.launch {
        transactionUseCases.addTransactionUseCase(transaction)
    }

    fun delete(transaction: Transaction) = viewModelScope.launch {
        transactionUseCases.deleteTransactionUseCase(transaction)
    }

    fun deleteAll(transactionList: List<Transaction>) = viewModelScope.launch {
        transactionUseCases.deleteAllTransactionsUseCase(transactionList)
    }

    fun update(transaction: Transaction) = viewModelScope.launch {
        transactionUseCases.updateTransactionsUseCase(transaction)
    }

    fun setCurrentFilterDate(dateEpochMillis: Long) {
        _currentFilterDate.value = Helper.epochMillisToLocalDate(dateEpochMillis)
    }

    fun setLocalDateCurrentFilterDate(localDate: LocalDate) {
        _currentFilterDate.value = localDate
    }

    fun changeWeek(offset: Long) {
        _currentFilterDate.value = _currentFilterDate.value.plusWeeks(offset)
    }

    fun changeMonth(offset: Long) {
        _currentFilterDate.value = _currentFilterDate.value.plusMonths(offset)
    }

    fun changeYear(offset: Long) {
        _currentFilterDate.value = _currentFilterDate.value.plusYears(offset)
    }

    fun setCurrentDailyNavigateTab(position: Int) {
        _currentDailyNavigateTabPosition.value = position
    }

    fun toggleTransactionSelection(transaction: Transaction) {
        val currentTransaction = _selectedTransactions.value
        _selectedTransactions.value = if (currentTransaction.contains(transaction)) {
            currentTransaction - transaction
        } else {
            currentTransaction + transaction
        }
        _selectionMode.value = _selectedTransactions.value.isNotEmpty()
    }

    private fun clearSelection() {
        _selectedTransactions.value = emptyList()
    }

    fun enterSelectionMode() {
        _selectionMode.value = true
    }

    fun exitSelectionMode() {
        _selectionMode.value = false
        clearSelection()
    }

    fun navigateToWeekFromMonthly(date: LocalDate) {
        _currentFilterDate.value = date
        viewModelScope.launch {
            _navigateToWeekFromMonthly.emit(Event(date))
        }
    }

    fun setFilter(type: FilterPeriodStatistic, date: LocalDate) {
        _filterOption.value = FilterOption(type, date)
    }

    fun setStatisticTransactionType(type: TransactionType) {
        _statisticTransactionType.value = type
    }

    fun setStatisticTransactionFilter(list: List<Transaction>) {
        _statisticListTransactionFilter.value = list
    }

    fun setCurrentStatisticTab(position: Int) {
        _currentStatisticTabPosition.value = position
    }

    fun onAction(action: TransactionAction) {
        when (action) {

            is TransactionAction.OnTransactionLongClick -> {
                onTransactionLongClick(action.transaction)
            }

            is TransactionAction.OnTransactionClick -> {
               onTransactionClick(action.transaction)
            }

            else -> {}
        }
    }

    fun onTransactionClick(transaction: Transaction) {
        if (_selectionMode.value) {
            toggleTransactionSelection(transaction)
        } else {
            emitOpenDetail(transaction)
        }
    }

    fun onTransactionLongClick(transaction: Transaction) {
        enterSelectionMode()
        toggleTransactionSelection(transaction)
    }
}
