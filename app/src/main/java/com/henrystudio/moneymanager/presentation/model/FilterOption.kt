package com.henrystudio.moneymanager.presentation.model

import java.io.Serializable
import java.time.LocalDate

data class FilterOption(
    val type: FilterPeriodStatistic,
    val date: LocalDate
    ) : Serializable
