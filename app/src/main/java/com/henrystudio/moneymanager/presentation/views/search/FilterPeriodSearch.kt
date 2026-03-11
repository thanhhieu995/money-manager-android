package com.henrystudio.moneymanager.presentation.views.search

import com.henrystudio.moneymanager.R

enum class FilterPeriodSearch {
    All, Weekly, Monthly, Yearly
}

val FilterPeriodSearch.stringRes: Int
    get() = when(this) {
        FilterPeriodSearch.All -> R.string.all
        FilterPeriodSearch.Weekly -> R.string.weekly
        FilterPeriodSearch.Monthly -> R.string.monthly
        FilterPeriodSearch.Yearly -> R.string.yearly
    }