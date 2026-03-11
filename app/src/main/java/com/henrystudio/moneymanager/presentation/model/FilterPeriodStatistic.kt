package com.henrystudio.moneymanager.presentation.model

import com.henrystudio.moneymanager.R

enum class FilterPeriodStatistic {
    Weekly, Monthly, Yearly, List, Trend
}

// Extension property để map enum -> strings.xml
val FilterPeriodStatistic.stringRes: Int
    get() = when (this) {
        FilterPeriodStatistic.Weekly -> R.string.weekly
        FilterPeriodStatistic.Monthly -> R.string.monthly
        FilterPeriodStatistic.Yearly -> R.string.yearly
        FilterPeriodStatistic.List -> R.string.list
        FilterPeriodStatistic.Trend -> R.string.trend
    }