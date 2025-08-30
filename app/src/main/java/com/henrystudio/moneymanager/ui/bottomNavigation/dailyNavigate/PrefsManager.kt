package com.henrystudio.moneymanager.ui.bottomNavigation.dailyNavigate

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

object PrefsManager {
    private const val PREF_NAME = "app_prefs"
    private const val KEY_TAB_POSITION = "tab_daily_position"
    private const val PREF_SCROLL_NAME = "scroll_prefs"
    private const val KEY_LAST_DATE = "last_date"

    fun saveTabPosition(context: Context, position: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_TAB_POSITION, position).apply()
    }

    fun getTabPosition(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_TAB_POSITION, 0) // mặc định = 0
    }

    fun saveLastDate(context: Context, date: LocalDate) {
        val prefs = context.getSharedPreferences(PREF_SCROLL_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LAST_DATE, date.toString()).apply()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadLastDate(context: Context): LocalDate? {
        val prefs = context.getSharedPreferences(PREF_SCROLL_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LAST_DATE, null)?.let { LocalDate.parse(it) }
    }
}