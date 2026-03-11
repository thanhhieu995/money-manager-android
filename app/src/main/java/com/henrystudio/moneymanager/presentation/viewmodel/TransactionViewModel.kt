package com.henrystudio.moneymanager.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.henrystudio.moneymanager.data.model.CategoryType
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.data.model.TransactionGroup
import com.henrystudio.moneymanager.data.repository.TransactionRepositoryImpl
import com.henrystudio.moneymanager.presentation.model.Event
import com.henrystudio.moneymanager.presentation.model.FilterOption
import com.henrystudio.moneymanager.presentation.model.FilterPeriodStatistic
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class TransactionViewModel(private val repository: TransactionRepositoryImpl) : ViewModel() {
    val allTransactions: LiveData<List<Transaction>> = repository.allTransactions
    val groupedTransactions: LiveData<List<TransactionGroup>> = repository.getGroupedTransactions()
    @RequiresApi(Build.VERSION_CODES.O)
    private val _currentFilterDate = MutableLiveData(LocalDate.now())
    @RequiresApi(Build.VERSION_CODES.O)
    val currentFilterDate: LiveData<LocalDate> = _currentFilterDate
    private val _currentDailyNavigateTabPosition = MutableLiveData<Int>()
    val currentDailyNavigateTabPosition: LiveData<Int> get() = _currentDailyNavigateTabPosition
    private val _selectionMode = MutableLiveData<Boolean>(false)
    val selectionMode: LiveData<Boolean> = _selectionMode
    private val _selectedTransactions = MutableLiveData<List<Transaction>>(emptyList())
    val selectedTransactions: LiveData<List<Transaction>> = _selectedTransactions
    private val _navigateToWeekFromMonthly = MutableLiveData<Event<LocalDate>>()
    val navigateToWeekFromMonthly: LiveData<Event<LocalDate>> = _navigateToWeekFromMonthly
    private val _filterOption = MutableLiveData<FilterOption>()
    val filterOption: LiveData<FilterOption> = _filterOption
    private val _statisticCategoryType = MutableLiveData(CategoryType.EXPENSE)
    val statisticCategoryType: LiveData<CategoryType> = _statisticCategoryType
    private val _statisticListTransactionFilter = MutableLiveData<List<Transaction>>(emptyList())
    val statisticListTransactionFilter : LiveData<List<Transaction>> = _statisticListTransactionFilter

    // noteFragment
    val combinedFilter: MediatorLiveData<Pair<CategoryType, List<Transaction>>> = MediatorLiveData()

    init {
        var currentType: CategoryType? = null
        var currentList: List<Transaction>? = null

        val updateCombinedFilter = {
            if (currentType != null && currentList != null) {
                combinedFilter.value = Pair(currentType!!, currentList!!)
            }
        }

        combinedFilter.addSource(statisticCategoryType) { type ->
            currentType = type
            updateCombinedFilter()
        }

        combinedFilter.addSource(statisticListTransactionFilter) { list ->
            currentList = list
            updateCombinedFilter()
        }
    }

    val combineGroupAndDate = MediatorLiveData<Pair<List<TransactionGroup>, LocalDate>>()

    init {
        combineGroupAndDate.addSource(groupedTransactions) { transactions ->
            val date = currentFilterDate.value
            if (date != null) combineGroupAndDate.value = transactions to date
        }
        combineGroupAndDate.addSource(currentFilterDate) { date ->
            val transactions = groupedTransactions.value
            if (transactions != null) combineGroupAndDate.value = transactions to date
        }
    }

    private val _currentStatisticTabPosition = MutableLiveData<Int>()
    val currentStatisticTabPosition: LiveData<Int> = _currentStatisticTabPosition

    fun insert(transaction: Transaction) = viewModelScope.launch {
        repository.insert(transaction)
    }

    fun delete(transaction: Transaction) = viewModelScope.launch {
        repository.delete(transaction)
    }

    fun deleteAll(transactionList: List<Transaction>) = viewModelScope.launch {
        repository.deleteAll(transactionList)
    }

    fun update(transaction: Transaction) = viewModelScope.launch{
       repository.update(transaction)
    }

    fun getBookmarkedTransactions(): LiveData<List<Transaction>> {
        return repository.getBookmarkedTransactions()
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
        _currentFilterDate.value = _currentFilterDate.value?.plusWeeks(offset)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun changeMonth(offset: Long) {
        _currentFilterDate.value = _currentFilterDate.value?.plusMonths(offset)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun changeYear(offset: Long) {
        _currentFilterDate.value = _currentFilterDate.value?.plusYears(offset)
    }

    fun setCurrentDailyNavigateTab(position: Int) {
        _currentDailyNavigateTabPosition.value = position
    }

    fun toggleTransactionSelection(transaction: Transaction) {
        val currentTransaction = _selectedTransactions.value ?: emptyList()
        _selectedTransactions.value = if (currentTransaction.contains(transaction)) {
            currentTransaction - transaction
        } else {
            currentTransaction + transaction
        }
        _selectionMode.value = _selectedTransactions.value?.isNotEmpty()
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
        _navigateToWeekFromMonthly.value = Event(date) // Dùng để scroll sau khi cập nhật
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