package com.henrystudio.moneymanager.presentation.calendar.model

import com.henrystudio.moneymanager.data.model.Transaction

sealed class CalendarDayItem {
    data class Header(
        val date: String,
        val income: Double,
        val expense: Double
    ): CalendarDayItem()

    data class TransactionItem(
        val transaction: Transaction
    ): CalendarDayItem()
}
