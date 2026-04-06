package com.henrystudio.moneymanager.presentation.daily

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.data.model.TransactionGroup
import com.henrystudio.moneymanager.domain.usecase.transaction.TransactionUseCases
import com.henrystudio.moneymanager.presentation.addtransaction.model.UiState
import com.henrystudio.moneymanager.presentation.daily.model.DailyTransactionGroupUi
import com.henrystudio.moneymanager.presentation.daily.model.DailyTransactionUi
import com.henrystudio.moneymanager.presentation.model.FilterOption
import com.henrystudio.moneymanager.presentation.model.FilterPeriodStatistic
import com.henrystudio.moneymanager.presentation.model.KeyFilter
import com.henrystudio.moneymanager.presentation.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class DailyViewModel @Inject constructor(
    private val transactionUseCases: TransactionUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyUiState())
    val uiState: StateFlow<DailyUiState> = _uiState.asStateFlow()

    private var lastSelected: List<Transaction> = emptyList()

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
}