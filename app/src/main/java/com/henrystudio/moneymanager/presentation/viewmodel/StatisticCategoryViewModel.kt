package com.henrystudio.moneymanager.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.core.util.FilterTransactions
import com.henrystudio.moneymanager.data.model.Category
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.domain.usecase.category.CategoryUseCases
import com.henrystudio.moneymanager.domain.usecase.transaction.TransactionUseCases
import com.henrystudio.moneymanager.presentation.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

data class StatisticCategoryUiState(
    val categoryName: String = "",
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val filterOption: FilterOption = FilterOption(FilterPeriodStatistic.Monthly, LocalDate.now()),
    val keyFilter: KeyFilter = KeyFilter.Time,
    val chartPoints: List<LineChartPoint> = emptyList(),
    val listChildCategoryStat: List<CategoryStat> = emptyList(),
    val currentMonthText: String = "",
    val categorySumAmount: Double = 0.0,
    val isDailyVisible: Boolean = false,
    val isChartVisible: Boolean = true,
    val currentIndex: Int = 0,
    val parentId: Int = -1,
    val isLoading: Boolean = false
)

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class StatisticCategoryViewModel @Inject constructor(
    private val transactionUseCases: TransactionUseCases,
    private val categoryUseCases: CategoryUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticCategoryUiState())
    val uiState: StateFlow<StatisticCategoryUiState> = _uiState.asStateFlow()

    private val _initParams = MutableStateFlow<Triple<String, TransactionType, FilterOption>?>(null)
    private var allTransactions: List<Transaction> = emptyList()
    private var allCategories: List<Category> = emptyList()

    init {
        viewModelScope.launch {
            combine(
                transactionUseCases.getTransactionsUseCase(),
                categoryUseCases.getAllCategories(),
                _initParams.filterNotNull(),
                _uiState.map { it.keyFilter }.distinctUntilChanged()
            ) { transactions, categories, params, keyFilter ->
                allTransactions = transactions
                allCategories = categories
                
                val (name, type, filter) = params
                val state = _uiState.value
                
                var parentId = state.parentId
                if (parentId == -1) {
                    val nameOnly = name.substringBefore("/").replace(Regex("^[^\\p{L}\\p{N}]+"), "").trim()
                    parentId = categories.find { it.name.replace(Regex("^[^\\p{L}\\p{N}]+"), "").trim() == nameOnly }?.id ?: -1
                }

                val locale = Helper.getAppLocale()
                val chartPoints = calculateChartPoints(transactions, name, type == TransactionType.INCOME, filter, keyFilter, locale)
                
                val targetIndex = if (state.chartPoints.isEmpty()) findInitialIndex(chartPoints, filter) else state.currentIndex.coerceIn(0, chartPoints.lastIndex.coerceAtLeast(0))
                
                updateStateWithPoint(state.copy(
                    categoryName = name,
                    transactionType = type,
                    filterOption = filter,
                    parentId = parentId,
                    chartPoints = chartPoints
                ), targetIndex, locale)
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun init(categoryName: String, transactionType: TransactionType, filterOption: FilterOption, keyFilter: KeyFilter) {
        _uiState.update { it.copy(keyFilter = keyFilter) }
        _initParams.value = Triple(categoryName, transactionType, filterOption)
    }

    fun updateSelectionMode(enabled: Boolean) {
        _uiState.update { it.copy(isChartVisible = !enabled) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun selectPoint(index: Int) {
        val locale = Helper.getAppLocale()
        _uiState.update { updateStateWithPoint(it, index, locale) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateStateWithPoint(state: StatisticCategoryUiState, index: Int, locale: Locale): StatisticCategoryUiState {
        val point = state.chartPoints.getOrNull(index) ?: return state.copy(currentIndex = index)
        
        val colors = listOf(android.graphics.Color.RED, android.graphics.Color.BLUE, android.graphics.Color.GREEN, android.graphics.Color.MAGENTA, android.graphics.Color.CYAN)
        val filtered = if (state.parentId != -1) {
            FilterTransactions.filterTransactionsByCategoryName(allTransactions, state.categoryName)
        } else {
            FilterTransactions.filterTransactionsByNoteName(allTransactions, state.categoryName)
        }

        val transactionsInPeriod = getTransactionsForPeriod(filtered, state.filterOption, point.date)
        val childCategories = if (state.parentId != -1) allCategories.filter { it.parentId == state.parentId } else emptyList()
        val stats = Helper.convertToCategoryStats(childCategories, transactionsInPeriod, state.transactionType == TransactionType.INCOME, colors)
        
        return state.copy(
            currentIndex = index,
            listChildCategoryStat = stats,
            currentMonthText = getMonthText(point, state.filterOption.type, locale),
            categorySumAmount = point.amount,
            isDailyVisible = stats.isEmpty() && state.parentId == -1 && state.keyFilter != KeyFilter.Time
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getTransactionsForPeriod(transactions: List<Transaction>, option: FilterOption, date: LocalDate): List<Transaction> {
        return when (option.type) {
            FilterPeriodStatistic.Weekly -> FilterTransactions.filterTransactionsByWeek(transactions, date)
            FilterPeriodStatistic.Monthly -> FilterTransactions.filterTransactionsByMonth(transactions, date)
            FilterPeriodStatistic.Yearly -> FilterTransactions.filterTransactionsByYear(transactions, date)
            else -> FilterTransactions.filterTransactionsByMonth(transactions, date)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun findInitialIndex(chartPoints: List<LineChartPoint>, filterOption: FilterOption): Int {
        val targetDate = filterOption.date
        val index = when (filterOption.type) {
            FilterPeriodStatistic.Weekly -> chartPoints.indexOfFirst { it.date == targetDate.with(DayOfWeek.MONDAY) }
            FilterPeriodStatistic.Monthly -> chartPoints.indexOfFirst { it.date.month == targetDate.month && it.date.year == targetDate.year }
            FilterPeriodStatistic.Yearly -> chartPoints.indexOfFirst { it.date.year == targetDate.year }
            else -> -1
        }
        return if (index == -1) chartPoints.lastIndex.coerceAtLeast(0) else index
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getMonthText(point: LineChartPoint, type: FilterPeriodStatistic, locale: Locale): String {
        return when (type) {
            FilterPeriodStatistic.Monthly -> "${point.date.month.getDisplayName(TextStyle.FULL, locale)} ${point.date.year}"
            FilterPeriodStatistic.Weekly -> {
                val end = point.date.plusDays(6)
                "${point.date.format(DateTimeFormatter.ofPattern("dd/MM"))} ~ ${end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
            }
            FilterPeriodStatistic.Yearly -> point.date.year.toString()
            else -> point.label
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateChartPoints(
        transactions: List<Transaction>,
        categoryName: String,
        isIncome: Boolean,
        filterOption: FilterOption,
        keyFilter: KeyFilter,
        locale: Locale
    ): List<LineChartPoint> {
        val filtered = transactions.filter { tx ->
            val txDate = Helper.epochMillisToLocalDate(tx.date)
            if (tx.isIncome != isIncome) return@filter false
            when (keyFilter) {
                KeyFilter.CategoryParent -> tx.categoryParentId.toString() == categoryName
                KeyFilter.CategorySub -> (tx.categoryChildId?.toString() ?: "") == categoryName
                KeyFilter.Note -> tx.note.trim().equals(categoryName.trim(), ignoreCase = true)
                KeyFilter.Account -> tx.account.trim().equals(categoryName.trim(), ignoreCase = true)
                else -> true
            }
        }
        val grouped = when (filterOption.type) {
            FilterPeriodStatistic.Weekly -> filtered
                .filter { Helper.epochMillisToLocalDate(it.date).year == filterOption.date.year }
                .groupBy { Helper.epochMillisToLocalDate(it.date).with(DayOfWeek.MONDAY).format(DateTimeFormatter.ofPattern("dd/MM")) }
            FilterPeriodStatistic.Monthly -> filtered
                .filter { Helper.epochMillisToLocalDate(it.date).year == filterOption.date.year }
                .groupBy { Helper.epochMillisToLocalDate(it.date).monthValue.toString() }
            else -> filtered.groupBy { Helper.epochMillisToLocalDate(it.date).year.toString() }
        }
        return grouped.map { (label, group) ->
            val lastTxDate = Helper.epochMillisToLocalDate(group.last().date)
            val pointDate = if (filterOption.type == FilterPeriodStatistic.Weekly) lastTxDate.with(DayOfWeek.MONDAY) else lastTxDate
            LineChartPoint(label = label, amount = group.sumOf { it.amount }, date = pointDate)
        }.sortedBy { it.date }
    }

    fun bindTransactions(transactionsFlow: Flow<List<Transaction>>) {

        viewModelScope.launch {
            combine(
                transactionsFlow,
                categoryUseCases.getAllCategories(),
                _initParams.filterNotNull(),
                _uiState.map { it.keyFilter }.distinctUntilChanged()
            ) { transactions, categories, params, keyFilter ->

                allTransactions = transactions
                allCategories = categories

                val (name, type, filter) = params
                val state = _uiState.value

                var parentId = state.parentId
                if (parentId == -1) {
                    val nameOnly = name.substringBefore("/")
                        .replace(Regex("^[^\\p{L}\\p{N}]+"), "")
                        .trim()

                    parentId = categories.find {
                        it.name.replace(Regex("^[^\\p{L}\\p{N}]+"), "")
                            .trim() == nameOnly
                    }?.id ?: -1
                }

                val locale = Helper.getAppLocale()

                val chartPoints = calculateChartPoints(
                    transactions,
                    name,
                    type == TransactionType.INCOME,
                    filter,
                    keyFilter,
                    locale
                )

                val targetIndex =
                    if (state.chartPoints.isEmpty())
                        findInitialIndex(chartPoints, filter)
                    else
                        state.currentIndex.coerceIn(
                            0,
                            chartPoints.lastIndex.coerceAtLeast(0)
                        )

                updateStateWithPoint(
                    state.copy(
                        categoryName = name,
                        transactionType = type,
                        filterOption = filter,
                        parentId = parentId,
                        chartPoints = chartPoints
                    ),
                    targetIndex,
                    locale
                )

            }.collect {
                _uiState.value = it
            }
        }
    }
}
