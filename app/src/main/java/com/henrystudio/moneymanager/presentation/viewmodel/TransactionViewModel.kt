package com.henrystudio.moneymanager.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.util.Pair
import androidx.lifecycle.*
import com.henrystudio.moneymanager.data.model.CategoryType
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.data.model.TransactionGroup
import com.henrystudio.moneymanager.domain.usecase.transaction.TransactionUseCases
import com.henrystudio.moneymanager.presentation.model.Event
import com.henrystudio.moneymanager.presentation.model.FilterOption
import com.henrystudio.moneymanager.presentation.model.FilterPeriodStatistic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Collections.emptyList
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class TransactionViewModel @Inject constructor (private val transactionUseCases: TransactionUseCases) : ViewModel() {
    val allTransactions: Flow<List<Transaction>> = transactionUseCases.getTransactionsUseCase()
    val groupedTransactions: StateFlow<List<TransactionGroup>> = transactionUseCases.getTransactionsGroupUseCase().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    @RequiresApi(Build.VERSION_CODES.O)
    private val _currentFilterDate = MutableStateFlow(LocalDate.now())
    @RequiresApi(Build.VERSION_CODES.O)
    val currentFilterDate: StateFlow<LocalDate> = _currentFilterDate

    private val _currentDailyNavigateTabPosition = MutableStateFlow(0)
    val currentDailyNavigateTabPosition: StateFlow<Int> = _currentDailyNavigateTabPosition

    private val _selectionMode = MutableStateFlow(false)
    val selectionMode: StateFlow<Boolean> = _selectionMode

    private val _selectedTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val selectedTransactions: StateFlow<List<Transaction>> = _selectedTransactions

    private val _navigateToWeekFromMonthly = MutableSharedFlow<Event<LocalDate>>()
    val navigateToWeekFromMonthly: SharedFlow<Event<LocalDate>> = _navigateToWeekFromMonthly.asSharedFlow()

    private val _filterOption = MutableStateFlow<FilterOption>(
        FilterOption(FilterPeriodStatistic.Monthly, LocalDate.now())
    )
    val filterOption: StateFlow<FilterOption> = _filterOption
    private val _statisticCategoryType = MutableStateFlow(CategoryType.EXPENSE)
    val statisticCategoryType: StateFlow<CategoryType> = _statisticCategoryType
    private val _statisticListTransactionFilter = MutableStateFlow<List<Transaction>>(emptyList())
    val statisticListTransactionFilter : StateFlow<List<Transaction>> = _statisticListTransactionFilter

    // noteFragment
    val combinedFilter: Flow<Pair<CategoryType, List<Transaction>>> =  combine(
        statisticCategoryType,
        statisticListTransactionFilter
    ) { type, list ->
        Pair(type, list)
    }

    val combineGroupAndDate: Flow<Pair<List<TransactionGroup>, LocalDate>> =
        combine(
            groupedTransactions,
            currentFilterDate
        ) { transactions, date ->
            Pair(transactions, date)
        }

    private val _currentStatisticTabPosition = MutableStateFlow(0)
    val currentStatisticTabPosition: StateFlow<Int> = _currentStatisticTabPosition

    fun insert(transaction: Transaction) = viewModelScope.launch {
        transactionUseCases.addTransactionUseCase(transaction)
    }

    fun delete(transaction: Transaction) = viewModelScope.launch {
        transactionUseCases.deleteTransactionUseCase(transaction)
    }

    fun deleteAll(transactionList: List<Transaction>) = viewModelScope.launch {
        transactionUseCases.deleteAllTransactionsUseCase(transactionList)
    }

    fun update(transaction: Transaction) = viewModelScope.launch{
       transactionUseCases.updateTransactionsUseCase(transaction)
    }

    suspend fun getBookmarkedTransactions(): Flow<List<Transaction>> {
        return transactionUseCases.getBookmarkedTransactionsUseCase()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setCurrentFilterDate(date: String) {
        val cleanedDate = date.substringBefore(" ")
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yy")
        val localDate = LocalDate.parse(cleanedDate, formatter)

        _currentFilterDate.value = localDate
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setLocalDateCurrentFilterDate(localDate: LocalDate) {
        _currentFilterDate.value = localDate
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun changeWeek(offset: Long) {
        _currentFilterDate.value = _currentFilterDate.value.plusWeeks(offset)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun changeMonth(offset: Long) {
        _currentFilterDate.value = _currentFilterDate.value.plusMonths(offset)
    }

    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun navigateToWeekFromMonthly(date: LocalDate) {
        _currentFilterDate.value = date.withDayOfMonth(date.dayOfMonth)
        viewModelScope.launch {
            _navigateToWeekFromMonthly.emit(Event(date)) // Dùng để scroll sau khi cập nhật
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setFilter(type: FilterPeriodStatistic, date: LocalDate) {
        _filterOption.value = FilterOption(type, date)
    }

    fun setStatisticCategoryType(type: CategoryType) {
        _statisticCategoryType.value = type
    }

    fun setStatisticTransactionFilter(list: List<Transaction>) {
        _statisticListTransactionFilter.value = list
    }

    fun setCurrentStatisticTab(position: Int) {
        _currentStatisticTabPosition.value = position
    }
}
