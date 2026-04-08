package com.henrystudio.moneymanager.presentation.daily.model

import android.content.Context
import com.henrystudio.moneymanager.data.model.Transaction
import java.time.LocalDate

sealed class DailyAction {
    data class OnScrollStopped(val date: LocalDate) : DailyAction()
    data class OnTransactionClick(val transaction: Transaction) : DailyAction()
    data class OnLongClick(val transaction: Transaction) : DailyAction()
}
