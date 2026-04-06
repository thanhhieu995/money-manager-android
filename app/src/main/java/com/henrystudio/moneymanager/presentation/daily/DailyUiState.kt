package com.henrystudio.moneymanager.presentation.daily

import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.addtransaction.model.UiState
import com.henrystudio.moneymanager.presentation.daily.model.DailyTransactionGroupUi
import java.time.LocalDate

data class DailyUiState(
    val transactions: List<DailyTransactionGroupUi> = emptyList(), // 👈 đổi sang UI model
    val selectedDate: LocalDate = LocalDate.now(),
    val selectionMode: Boolean = false,
    val selectedTransactions: List<Transaction> = emptyList(), // 👈 giữ lại
    val isEmpty: Boolean = false,
    val dataTransactionGroupState: UiState<List<DailyTransactionGroupUi>> = UiState.Loading,
    val isYearly: Boolean = false
)