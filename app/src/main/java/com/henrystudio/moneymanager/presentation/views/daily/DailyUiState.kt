package com.henrystudio.moneymanager.presentation.views.daily

import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.data.model.TransactionGroup
import java.time.LocalDate

data class DailyUiState(
    val transactions: List<TransactionGroup> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val selectionMode: Boolean = false,
    val selectedTransactions: List<Transaction> = emptyList(),
    val isEmpty: Boolean = false,
    val isYearly: Boolean = false
)