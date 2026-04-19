package com.henrystudio.moneymanager.presentation.bottomNavigation.dailyNavigate

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henrystudio.moneymanager.core.util.FilterTransactions
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.data.model.TransactionGroup
import com.henrystudio.moneymanager.presentation.model.UiState
import com.henrystudio.moneymanager.presentation.model.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class DailyNavigateViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(DailyNavigateUiState())
    val uiState: StateFlow<DailyNavigateUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<DailyNavigateEffect>()
    val effect = _effect.asSharedFlow()

    private var bindUiStateJob: Job? = null
    private var bindNavigationJob: Job? = null
    private var latestSelectedTransactions: List<Transaction> = emptyList()

    fun bind(
        groupedTransactionsState: Flow<UiState<List<TransactionGroup>>>,
        currentFilterDate: Flow<LocalDate>,
        currentDailyNavigateTabPosition: Flow<Int>,
        selectionMode: Flow<Boolean>,
        selectedTransactions: Flow<List<Transaction>>
    ) {
        if (bindUiStateJob != null) return

        bindUiStateJob = viewModelScope.launch {
            combine(
                groupedTransactionsState,
                currentFilterDate,
                currentDailyNavigateTabPosition,
                selectionMode,
                selectedTransactions
            ) { groupsState, date, tabPosition, isSelectionMode, selected ->
                latestSelectedTransactions = selected
                buildUiState(
                    groups = (groupsState as? UiState.Success)?.data.orEmpty(),
                    date = date,
                    tabPosition = tabPosition,
                    selectionMode = isSelectionMode,
                    selectedTransactions = selected
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun bindNavigation(navigateToWeekFromMonthly: Flow<Event<LocalDate>>) {
        if (bindNavigationJob != null) return

        bindNavigationJob = viewModelScope.launch {
            navigateToWeekFromMonthly.collect { event ->
                event.getContentIfNotHandled()?.let { date ->
                    emitEffect(DailyNavigateEffect.NavigateToDailyWeek(date))
                }
            }
        }
    }

    fun onAction(action: DailyNavigateAction) {
        when (action) {
            DailyNavigateAction.OnAddTransactionClick -> {
                emitEffect(DailyNavigateEffect.OpenAddTransaction)
            }

            DailyNavigateAction.OnBookmarkClick -> {
                emitEffect(DailyNavigateEffect.OpenBookmark)
            }

            DailyNavigateAction.OnSearchClick -> {
                emitEffect(DailyNavigateEffect.OpenSearch)
            }

            DailyNavigateAction.OnDeleteSelectionClick -> {
                if (latestSelectedTransactions.isNotEmpty()) {
                    emitEffect(
                        DailyNavigateEffect.DeleteSelectedTransactions(latestSelectedTransactions)
                    )
                }
            }

            DailyNavigateAction.OnExitSelectionClick -> {
                emitEffect(DailyNavigateEffect.ExitSelectionMode)
            }

            is DailyNavigateAction.OnMonthPicked -> {
                val lastDay = LocalDate.of(action.year, action.month, 1).lengthOfMonth()
                val selectedDate = LocalDate.of(action.year, action.month, lastDay)
                emitEffect(DailyNavigateEffect.UpdateCurrentFilterDate(selectedDate))
            }

            DailyNavigateAction.OnNextPeriodClick -> {
                if (_uiState.value.isYearlyView) {
                    emitEffect(DailyNavigateEffect.ChangeYear(1))
                } else {
                    emitEffect(DailyNavigateEffect.ChangeMonth(1))
                }
            }

            DailyNavigateAction.OnPreviousPeriodClick -> {
                if (_uiState.value.isYearlyView) {
                    emitEffect(DailyNavigateEffect.ChangeYear(-1))
                } else {
                    emitEffect(DailyNavigateEffect.ChangeMonth(-1))
                }
            }

            is DailyNavigateAction.OnTabChanged -> {
                emitEffect(DailyNavigateEffect.UpdateCurrentTab(action.position))
                if (action.shouldPersist) {
                    emitEffect(DailyNavigateEffect.PersistTabPosition(action.position))
                }
            }

            is DailyNavigateAction.OnNavigateToWeekRequested -> {
                emitEffect(DailyNavigateEffect.NavigateToDailyWeek(action.date))
            }
        }
    }

    private fun buildUiState(
        groups: List<TransactionGroup>,
        date: LocalDate,
        tabPosition: Int,
        selectionMode: Boolean,
        selectedTransactions: List<Transaction>
    ): DailyNavigateUiState {
        val isYearlyView = tabPosition == 2
        val filtered = if (isYearlyView) {
            FilterTransactions.filterTransactionGroupByYear(groups, date)
        } else {
            FilterTransactions.filterTransactionGroupByMonth(groups, date)
        }

        val incomeSum = filtered.sumOf { it.income }
        val expenseSum = filtered.sumOf { it.expense }
        val totalSum = incomeSum - expenseSum
        val locale = Locale.getDefault()
        val monthLabel = if (isYearlyView) {
            date.format(DateTimeFormatter.ofPattern("yyyy", locale))
        } else {
            date.format(DateTimeFormatter.ofPattern("MMM yyyy", locale))
        }
        val selectedTotal = selectedTransactions.sumOf {
            if (it.isIncome) it.amount else -it.amount
        }

        return DailyNavigateUiState(
            monthLabel = monthLabel,
            incomeSum = incomeSum,
            expenseSum = expenseSum,
            totalSum = totalSum,
            selectionMode = selectionMode,
            selectedCount = selectedTransactions.size,
            selectedTotal = selectedTotal,
            currentTabPosition = tabPosition,
            selectedDate = date,
            isYearlyView = isYearlyView
        )
    }

    private fun emitEffect(effect: DailyNavigateEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
}
