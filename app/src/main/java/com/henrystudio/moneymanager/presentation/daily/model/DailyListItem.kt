package com.henrystudio.moneymanager.presentation.daily.model

import com.henrystudio.moneymanager.data.model.Transaction

sealed class DailyListItem {
    data class Header(
        val id: Int,
        val date: String,
        val income: Double,
        val expense: Double
    ) : DailyListItem()

    data class TransactionItem(
        val transaction: Transaction,
        val isSelected: Boolean
    ) : DailyListItem()
}
