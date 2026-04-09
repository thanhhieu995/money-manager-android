package com.henrystudio.moneymanager.presentation.daily

import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.addtransaction.model.UiState
import com.henrystudio.moneymanager.presentation.daily.model.DailyListItem
import java.time.LocalDate

data class DailyUiState(
    val dailyListItems: List<DailyListItem> = emptyList(), // 👈 đổi sang UI model
    val selectedDate: LocalDate = LocalDate.now(),
    val selectionMode: Boolean = false,
    val selectedTransactions: List<Transaction> = emptyList(), // 👈 giữ lại
    val isEmpty: Boolean = false,
    val dailyListItemState: UiState<List<DailyListItem>> = UiState.Loading,
    val isYearly: Boolean = false
)