package com.henrystudio.moneymanager.presentation.bottomNavigation

import java.time.LocalDate

sealed interface DailyNavigateAction {
    data object OnSearchClick : DailyNavigateAction
    data object OnBookmarkClick : DailyNavigateAction
    data object OnAddTransactionClick : DailyNavigateAction
    data object OnPreviousPeriodClick : DailyNavigateAction
    data object OnNextPeriodClick : DailyNavigateAction
    data class OnMonthPicked(val month: Int, val year: Int) : DailyNavigateAction
    data object OnExitSelectionClick : DailyNavigateAction
    data object OnDeleteSelectionClick : DailyNavigateAction
    data class OnTabChanged(val position: Int, val shouldPersist: Boolean = true) :
        DailyNavigateAction
    data class OnNavigateToWeekRequested(val date: LocalDate) : DailyNavigateAction
}
