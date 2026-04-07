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
import com.henrystudio.moneymanager.presentation.addtransaction.model.UiState
import com.henrystudio.moneymanager.presentation.daily.model.DailyEvent
import com.henrystudio.moneymanager.presentation.daily.model.DailyTransactionGroupUi
import com.henrystudio.moneymanager.presentation.daily.model.DailyTransactionUi
import com.henrystudio.moneymanager.presentation.daily.model.ParamsProcessData
import com.henrystudio.moneymanager.presentation.model.FilterOption
import com.henrystudio.moneymanager.presentation.model.FilterPeriodStatistic
import com.henrystudio.moneymanager.presentation.model.KeyFilter
import com.henrystudio.moneymanager.presentation.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private var currentList: List<DailyTransactionGroupUi> = emptyList()

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
        val currentGroups = _uiState.value.transactions

        val updatedGroups = currentGroups.map { group ->
            group.copy(
                transactions = group.transactions.map { item ->
                    item.copy(
                        isSelected = selectedTransactions.any { it.id == item.transaction.id }
                    )
                }
            )
        }

        _uiState.update {
            it.copy(
                selectionMode = selectionMode,
                selectedTransactions = selectedTransactions,
                transactions = updatedGroups
            )
        }
    }

    fun updateSelectedDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun setLoading(selectedMonth: LocalDate) {
        _uiState.update {
            it.copy(
                dataTransactionGroupState = UiState.Loading,
                selectedDate = selectedMonth
            )
        }
    }

    fun setEmpty(selectedMonth: LocalDate) {
        _uiState.update {
            it.copy(
                dataTransactionGroupState = UiState.Empty,
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
                if (_uiState.value.transactions.isEmpty()) {
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
                        dataTransactionGroupState = if (uiList.isEmpty()) {
                            UiState.Empty
                        } else {
                            UiState.Success(uiList)
                        },
                        transactions = uiList,
                        selectedDate = selectedMonth,
                        isEmpty = uiList.isEmpty(),
                        isYearly = filterOption.type == FilterPeriodStatistic.Yearly
                    )
                }
            }

            is UiState.Error -> {
                _uiState.update {
                    it.copy(
                        dataTransactionGroupState = state,
                        selectedDate = selectedMonth
                    )
                }
            }
        }
    }

    private fun mapToUi(
        groups: List<TransactionGroup>,
        selected: List<Transaction>
    ): List<DailyTransactionGroupUi> {

        return groups.map { group ->
            DailyTransactionGroupUi(
                id = group.id,
                date = group.date,
                income = group.income,
                expense = group.expense,
                transactions = group.transactions.map { tx ->
                    DailyTransactionUi(
                        transaction = tx,
                        isSelected = selected.any { it.id == tx.id }
                    )
                }
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun findPositionForDate(transactions: List<DailyTransactionGroupUi>, date: LocalDate): Int {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yy")
        return transactions.indexOfFirst { tx ->
            val cleanedDate = tx.date.substringBefore(" ")
            val txDate = LocalDate.parse(cleanedDate, formatter)
            txDate == date
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

    fun onParseStringToLocaleDate(dateString: String) : LocalDate {
        return Helper.parseStringToLocalDate(dateString)
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
        viewModelScope.launch {
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
        viewModelScope.launch {
            flow.collect { (mode, selected) ->
                updateSelection(
                    mode,
                    selected
                )
            }
        }
    }
}