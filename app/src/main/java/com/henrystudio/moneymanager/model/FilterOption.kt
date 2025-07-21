package com.henrystudio.moneymanager.model

import com.henrystudio.moneymanager.ui.search.FilterPeriod
import java.time.LocalDate

data class FilterOption(
    val type: FilterPeriod,
    val date: LocalDate
    )
