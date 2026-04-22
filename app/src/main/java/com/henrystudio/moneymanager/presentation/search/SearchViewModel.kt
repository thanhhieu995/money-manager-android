package com.henrystudio.moneymanager.presentation.search

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.data.model.Transaction
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
                selectedTotal = total
            )
        }
    }

    private fun applyFilter() {
        val state = uiState.value
        val filterContext = createFilterContext(state)

        val filteredTransactions = filterTransactions(filterContext)
        val totals = calculateTotals(filteredTransactions)
        val distinctNotes = extractDistinctNotes()

        _uiState.update {
            it.copy(
                filteredTransactions = filteredTransactions,
                incomeTotal = totals.income,
                expenseTotal = totals.expense,
                distinctNotes = distinctNotes,
                isEmpty = filteredTransactions.isEmpty()
            )
        }
    }

    private fun createFilterContext(state: SearchUiState): FilterContext {
        val currentDate = LocalDate.now()
        val startOfWeek = currentDate.with(DayOfWeek.MONDAY)

        return FilterContext(
            query = state.searchQuery.lowercase().trim().removeVietnameseDiacritics(),
            period = state.filterPeriod,
            startOfWeek = startOfWeek,
            endOfWeek = startOfWeek.plusDays(6),
            currentMonth = currentDate.monthValue,
            currentYear = currentDate.year
        )
    }

    private fun filterTransactions(context: FilterContext): List<Transaction> {
        return allTransactions.filter { tx ->
            matchesPeriod(tx, context) && matchesQuery(tx, context.query)
        }
    }

    private fun matchesPeriod(tx: Transaction, context: FilterContext): Boolean {
        val txDate = Helper.epochMillisToLocalDate(tx.date)
        return when (context.period) {
            FilterPeriodSearch.All -> true
            FilterPeriodSearch.Weekly ->
                !txDate.isBefore(context.startOfWeek) && !txDate.isAfter(context.endOfWeek)
            FilterPeriodSearch.Monthly ->
                txDate.monthValue == context.currentMonth && txDate.year == context.currentYear
            FilterPeriodSearch.Yearly ->
                txDate.year == context.currentYear
        }
    }

    private fun matchesQuery(tx: Transaction, query: String): Boolean {
        if (query.isEmpty()) return true

        val note = tx.note.lowercase().removeVietnameseDiacritics()
        val date = Helper.formatEpochMillisToDisplayDate(tx.date)
            .lowercase()
            .removeVietnameseDiacritics()
        val amount = tx.amount.toString().lowercase().removeVietnameseDiacritics()

        return note.contains(query) || date.contains(query) || amount.contains(query)
    }

    private fun calculateTotals(transactions: List<Transaction>): Totals {
        val income = transactions.filter { it.isIncome }.sumOf { it.amount }
        val expense = transactions.filter { !it.isIncome }.sumOf { it.amount }
        return Totals(income, expense)
    }

    private fun extractDistinctNotes(): List<String> {
        return allTransactions.map { it.note }.distinct()
    }

    private data class FilterContext(
        val query: String,
        val period: FilterPeriodSearch,
        val startOfWeek: LocalDate,
        val endOfWeek: LocalDate,
        val currentMonth: Int,
        val currentYear: Int
    )

    private data class Totals(
        val income: Long,
        val expense: Long
    )

    private fun String.removeVietnameseDiacritics(): String {
        val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
        return Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(normalized).replaceAll("")
    }
}
