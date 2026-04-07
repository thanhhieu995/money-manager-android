package com.henrystudio.moneymanager.data.repository

import android.content.Context
import com.henrystudio.moneymanager.domain.repository.LastDateRepository
import java.time.LocalDate

class LastDateRepositoryImpl(
    val context: Context
) : LastDateRepository{
    private val prefs = context.getSharedPreferences("scroll_prefs", Context.MODE_PRIVATE)
    override fun saveLastDate(date: LocalDate) {
        prefs.edit().putString("last_date", date.toString()).apply()
    }

    override fun getLastDate(): LocalDate? {
        return prefs.getString("last_date", null)?.let { LocalDate.parse(it) }
    }
}