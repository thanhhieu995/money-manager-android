package com.henrystudio.moneymanager.ui.bottomNavigation.dailyNavigate

import android.content.Context

object PrefsManager {
    private const val PREF_NAME = "app_prefs"
    private const val KEY_TAB_POSITION = "tab_daily_position"

    fun saveTabPosition(context: Context, position: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_TAB_POSITION, position).apply()
    }

    fun getTabPosition(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_TAB_POSITION, 0) // mặc định = 0
    }
}