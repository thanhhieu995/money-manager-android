package com.henrystudio.moneymanager.presentation.bottomNavigation

import com.henrystudio.moneymanager.data.model.Transaction
import java.time.LocalDate

sealed interface DailyNavigateEffect {
    data object OpenSearch : DailyNavigateEffect
    data object OpenBookmark : DailyNavigateEffect
    data object OpenAddTransaction : DailyNavigateEffect
    data class ChangeMonth(val offset: Long) : DailyNavigateEffect
    data class ChangeYear(val offset: Long) : DailyNavigateEffect
    data class UpdateCurrentFilterDate(val date: LocalDate) : DailyNavigateEffect
    data object ExitSelectionMode : DailyNavigateEffect
    data class DeleteSelectedTransactions(val transactions: List<Transaction>) : DailyNavigateEffect
    data class PersistTabPosition(val position: Int) : DailyNavigateEffect
    data class UpdateCurrentTab(val position: Int) : DailyNavigateEffect
    data class NavigateToDailyWeek(val date: LocalDate) : DailyNavigateEffect
}
