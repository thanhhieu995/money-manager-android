package com.henrystudio.moneymanager.presentation.daily

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.data.model.TransactionGroup
import com.henrystudio.moneymanager.domain.usecase.appstate.PreferenceUseCases
import com.henrystudio.moneymanager.domain.usecase.transaction.TransactionUseCases
import com.henrystudio.moneymanager.presentation.model.UiState
import com.henrystudio.moneymanager.presentation.daily.model.DailyAction
import com.henrystudio.moneymanager.presentation.daily.model.DailyEvent
import com.henrystudio.moneymanager.presentation.daily.model.DailyHeaderUi
import com.henrystudio.moneymanager.presentation.daily.model.DailyListItem
import com.henrystudio.moneymanager.presentation.daily.model.ParamsProcessData
import com.henrystudio.moneymanager.presentation.model.FilterOption
import com.henrystudio.moneymanager.presentation.model.FilterPeriodStatistic
import com.henrystudio.moneymanager.presentation.model.KeyFilter
import com.henrystudio.moneymanager.presentation.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class DailyViewModel @Inject constructor(
    private val transactionUseCases: TransactionUseCases,
    private val preferenceUseCases: PreferenceUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyUiState())
    val uiState: StateFlow<DailyUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<DailyEvent>()
    val event = _event.asSharedFlow()

    private var currentList: List<DailyListItem> = emptyList()
    private var bindProcessDataJob: Job? = null
    private var bindSelectionJob: Job? = null

    private val paramsFlow = MutableStateFlow(ParamsProcessData())
    private val selectionFlow =
        MutableStateFlow<Pair<Boolean, List<Transaction>>>(false to emptyList())

    private fun emitEvent(newEvent: DailyEvent) {
        viewModelScope.launch {
            _event.emit(newEvent)
        }
    }

    fun updateSelection(
        selectionMode: Boolean,
        selectedTransactions: List<Transaction>
    ) {
        val items = _uiState.value.dailyListItems

        val updateItems = items.map { item ->
            when (item) {
                is DailyListItem.Header -> item

                is DailyListItem.TransactionItem -> {
                    item.copy(isSelected = selectedTransactions.any {it.id == item.transaction.id })
                }
            }
        }

        _uiState.update {
            it.copy(
                selectionMode = selectionMode,
                selectedTransactions = selectedTransactions,
                dailyListItems = updateItems
            )
        }
    }

    fun updateSelectedDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun setLoading(selectedMonth: LocalDate) {
        _uiState.update {
            it.copy(
                dailyListItemState = UiState.Loading,
                selectedDate = selectedMonth
            )
        }
    }

    fun setEmpty(selectedMonth: LocalDate) {
        _uiState.update {
            it.copy(
                dailyListItemState = UiState.Empty,
                selectedDate = selectedMonth
            )
        }
    }

    fun processData(
        state: UiState<List<TransactionGroup>>,
        filterOption: FilterOption,
        selectedMonth: LocalDate,
        categoryName: String?,
        transactionType: TransactionType,
        keyFilter: KeyFilter,
        isFromMainActivity: Boolean
    ) {
        when (state) {
            is UiState.Loading -> {
                if (_uiState.value.dailyListItems.isEmpty()) {
                    setLoading(selectedMonth)
                }
            }

            is UiState.Empty -> {
                setEmpty(selectedMonth)
            }

            is UiState.Success -> {
                val filteredList = transactionUseCases.filterTransactionGroupsUseCase(
                    transactions = state.data,
                    filterOption = filterOption,
                    selectedMonth = selectedMonth,
                    categoryName = categoryName,
                    transactionType = transactionType,
                    keyFilter = keyFilter,
                    isFromMainActivity = isFromMainActivity
                )

                val selected = _uiState.value.selectedTransactions

                val uiList = mapToUi(filteredList, selected)

                currentList = uiList

                _uiState.update {
                    it.copy(
                        dailyListItemState = if (uiList.isEmpty()) {
                            UiState.Empty
                        } else {
                            UiState.Success(uiList)
                        },
                        dailyListItems = uiList,
                        selectedDate = selectedMonth,
                        isEmpty = uiList.isEmpty(),
                        isYearly = filterOption.type == FilterPeriodStatistic.Yearly
                    )
                }
            }

            is UiState.Error -> {
                _uiState.update {
                    it.copy(
                        dailyListItemState = state,
                        selectedDate = selectedMonth
                    )
                }
            }
        }
    }

    private fun mapToUi(
        groups: List<TransactionGroup>,
        selected: List<Transaction>
    ): List<DailyListItem> {

        val selectedIds = selected.map { it.id }.toSet()

        val result = mutableListOf<DailyListItem>()

        groups.forEach { group ->
            // 🔥 Header item
            result.add(
                DailyListItem.Header(
                    id = group.id,
                    date = group.date,
                    income = group.income,
                    expense = group.expense
                )
            )

            // 🔥 SORT transaction mới nhất lên trên
            val sortedTransactions = group.transactions
                .sortedByDescending { it.updatedAt ?: it.createdAt} // 👈 QUAN TRỌNG
            // 🔥 Transaction items
            sortedTransactions.forEach { tx ->
                result.add(
                    DailyListItem.TransactionItem(
                        transaction = tx,
                        isSelected = selectedIds.contains(tx.id)
                    )
                )
            }
        }

        return result
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun findPositionForDate(items: List<DailyListItem>, date: LocalDate): Int {
        return items.indexOfFirst { item ->
            if (item is DailyListItem.Header) {
                val txDate = Helper.epochMillisToLocalDate(item.date)
                txDate == date
            } else {
                false
            }
        }
    }

    fun onScrollStopped(date: LocalDate) {
        preferenceUseCases.saveLastDate(date)
    }

    fun handleInitialScroll(
        isNavigateFromMonthly: Boolean
    ) {
        val lastDate = preferenceUseCases.getLastDate() ?: return
        if (isNavigateFromMonthly) return

        val position = findPositionForDate(currentList, lastDate)
        if (position != -1) {
            emitEvent(DailyEvent.ScrollToPosition(position))
        }
    }

    fun onParseEpochMillisToLocalDate(epochMillis: Long) : LocalDate {
        return Helper.epochMillisToLocalDate(epochMillis)
    }

    fun onFormatCurrency(amount: Double): String {
        return Helper.formatCurrency(amount)
    }

    fun onOpenTransactionDetail(context: Context, transaction: Transaction) {
        Helper.openTransactionDetail(context, transaction)
    }

    fun setParamsProcessData(
        categoryName: String?,
        transactionType: TransactionType,
        keyFilter: KeyFilter,
        isFromMainActivity: Boolean
    ) {
        paramsFlow.value = ParamsProcessData(
            categoryName = categoryName,
            transactionType = transactionType,
            keyFilter = keyFilter,
            isFromMainActivity = isFromMainActivity
        )
    }

    fun bindProcessData(flow: Flow<Triple<UiState<List<TransactionGroup>>, LocalDate, FilterOption>>) {
        if (bindProcessDataJob != null) return

        bindProcessDataJob = viewModelScope.launch {
            combine(flow, paramsFlow) { triple, params ->
                Pair(triple, params)
            }.collect { (triple, params) ->
                val (state, selectedMonth, option) = triple
                processData(
                    state = state,
                    filterOption = option,
                    selectedMonth = selectedMonth,
                    categoryName = params.categoryName,
                    transactionType = params.transactionType ?: TransactionType.EXPENSE,
                    keyFilter = params.keyFilter ?: KeyFilter.CategoryParent,
                    isFromMainActivity = params.isFromMainActivity
                )
            }
        }
    }

    fun bindSelection(flow: Flow<Pair<Boolean, List<Transaction>>>) {
        if (bindSelectionJob != null) return

        bindSelectionJob = viewModelScope.launch {
            flow.collect { (mode, selected) ->
                updateSelection(
                    mode,
                    selected
                )
            }
        }
    }

    fun mapHeader(item: DailyListItem.Header): DailyHeaderUi {
        val localDate = Helper.epochMillisToLocalDate(item.date)
        val formatter = DateTimeFormatter.ofPattern("dd EEE")

        return DailyHeaderUi(
            dateText = localDate.format(formatter),
            incomeText = Helper.formatCurrency(item.income),
            expenseText = Helper.formatCurrency(item.expense)
        )
    }

    fun onAction(action: DailyAction) {
        when (action) {
            is DailyAction.OnScrollStopped -> {
                preferenceUseCases.saveLastDate(action.date)
            }

            else -> {}
        }
    }
}
