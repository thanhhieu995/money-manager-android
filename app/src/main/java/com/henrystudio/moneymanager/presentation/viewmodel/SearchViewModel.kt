package com.henrystudio.moneymanager.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.views.search.FilterPeriodSearch
import com.henrystudio.moneymanager.presentation.views.search.SearchUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.Normalizer
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.regex.Pattern
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class SearchViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var allTransactions: List<Transaction> = emptyList()

    fun updateTransactions(transactions: List<Transaction>) {
        allTransactions = transactions
        applyFilter()
    }

    fun updateQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilter()
    }

    fun updateFilterPeriod(period: FilterPeriodSearch) {
        _uiState.update { it.copy(filterPeriod = period) }
        applyFilter()
    }

    fun updateSelected(selected: List<Transaction>) {
        val count = selected.size
        val total = selected.sumOf { if (it.isIncome) it.amount else -it.amount }
        _uiState.update {
            it.copy(
                selectedCount = count,
                selectedTotal = "Total: ${Helper.formatCurrency(total)}"
            )
        }
    }

    private fun applyFilter() {
        val queryRaw = _uiState.value.searchQuery.lowercase().trim()
        val period = _uiState.value.filterPeriod
        val currentDate = LocalDate.now()
        val startOfWeek = currentDate.with(DayOfWeek.MONDAY)
        val endOfWeek = startOfWeek.plusDays(6)
        val currentMonth = currentDate.monthValue
        val currentYear = currentDate.year

        val filtered = if (queryRaw.isEmpty()) {
            when (period) {
                FilterPeriodSearch.All -> allTransactions
                else -> allTransactions.filter { tx ->
                    val txDate = Helper.epochMillisToLocalDate(tx.date)
                    when (period) {
                        FilterPeriodSearch.Weekly ->
                            !txDate.isBefore(startOfWeek) && !txDate.isAfter(endOfWeek)
                        FilterPeriodSearch.Monthly ->
                            txDate.monthValue == currentMonth && txDate.year == currentYear
                        FilterPeriodSearch.Yearly -> txDate.year == currentYear
                        else -> true
                    }
                }
            }
        } else {
            val query = queryRaw.removeVietnameseDiacritics()
            allTransactions.filter { tx ->
                val txDate = Helper.epochMillisToLocalDate(tx.date)
                val note = tx.note.lowercase().removeVietnameseDiacritics()
                val dateStr = Helper.formatEpochMillisToDisplayDate(tx.date).lowercase().removeVietnameseDiacritics()
                val amount = Helper.formatCurrency(tx.amount).lowercase()
                val matchQuery = note.contains(query) || dateStr.contains(query) || amount.contains(query)
                val matchPeriod = when (period) {
                    FilterPeriodSearch.All -> true
                    FilterPeriodSearch.Weekly ->
                        !txDate.isBefore(startOfWeek) && !txDate.isAfter(endOfWeek)
                    FilterPeriodSearch.Monthly ->
                        txDate.monthValue == currentMonth && txDate.year == currentYear
                    FilterPeriodSearch.Yearly -> txDate.year == currentYear
                    else -> true
                }
                matchQuery && matchPeriod
            }
        }

        val incomeTotal = filtered.filter { it.isIncome }.sumOf { it.amount }
        val expenseTotal = filtered.filter { !it.isIncome }.sumOf { it.amount }
        val distinctNotes = allTransactions.map { it.note }.distinct()

        _uiState.update {
            it.copy(
                filteredTransactions = filtered,
                incomeTotal = Helper.formatCurrency(incomeTotal),
                expenseTotal = Helper.formatCurrency(expenseTotal),
                distinctNotes = distinctNotes,
                isEmpty = filtered.isEmpty()
            )
        }
    }

    private fun String.removeVietnameseDiacritics(): String {
        val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
        return Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(normalized).replaceAll("")
    }
}
