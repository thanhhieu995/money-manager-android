package com.henrystudio.moneymanager.presentation.daily.model

sealed class DailyEvent{
    data class ScrollToPosition(val position: Int): DailyEvent()
}
