package com.henrystudio.moneymanager.model

import java.time.LocalDate

data class FilterOption(
    val type: FilterPeriodStatistic,
    val date: LocalDate
    )
