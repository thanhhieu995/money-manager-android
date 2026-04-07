package com.henrystudio.moneymanager.domain.repository

import java.time.LocalDate

interface LastDateRepository {
    fun saveLastDate(date: LocalDate)
    fun getLastDate(): LocalDate?
}