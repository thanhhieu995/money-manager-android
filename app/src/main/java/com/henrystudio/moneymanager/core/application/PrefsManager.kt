package com.henrystudio.moneymanager.core.application

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.henrystudio.moneymanager.presentation.main.MainTab
import com.henrystudio.moneymanager.presentation.setting.AppCurrency
import java.time.LocalDate

object PrefsManager {
    private const val PREF_NAME = "app_prefs"
    private const val KEY_TAB_POSITION = "tab_daily_position"
    private const val KEY_HAS_SEEN_ADD_TUTORIAL = "has_seen_add_tutorial"
    private const val PREF_SCROLL_NAME = "scroll_prefs"
    private const val KEY_LAST_DATE = "last_date"
    private const val PREF_STATISTIC = "app_pref_statistic"
    private const val KEY_STATISTIC_TAB_POSITION = "tab_statistic_position"
    private const val INSTALL_TIME = "install_time"
    private const val KEY_LANG = "app_language"
    private const val MAIN_LAST_TAB = "main_last_tab"
    private const val OPEN_COUNT = "open_count"
    private const val HAS_RATE = "has_rate"
    private const val KEY_CURRENCY = "currency"

    fun saveTabPosition(context: Context, position: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_TAB_POSITION, position).apply()
    }

    fun getTabPosition(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_TAB_POSITION, 0) // mặc định = 0
    }

    fun saveHasSeenAddTutorial(context: Context, hasSeen: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_HAS_SEEN_ADD_TUTORIAL, hasSeen).apply()
    }

    fun hasSeenAddTutorial(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_HAS_SEEN_ADD_TUTORIAL, false)
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

    fun saveStatisticTabPosition(context: Context, position: Int) {
        val prefs = context.getSharedPreferences(PREF_STATISTIC, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_STATISTIC_TAB_POSITION, position).apply()
    }

    fun getStatisticTabPosition(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_STATISTIC, Context.MODE_PRIVATE)
        return  prefs.getInt(KEY_STATISTIC_TAB_POSITION, 0)
    }

    fun setInstallTime(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (!prefs.contains(INSTALL_TIME)) {
            val currentTime = System.currentTimeMillis()
            prefs.edit().putLong(INSTALL_TIME, currentTime).apply()
        }
    }

    fun getInstallTime(context: Context): Long {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(INSTALL_TIME, 0L)
    }

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANG, "en") ?: "en"
    }

    fun saveLanguage(context: Context, lang: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, lang).apply()
    }

    fun saveMainSelectedTab(context: Context, tab: MainTab) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(MAIN_LAST_TAB, tab.route).apply()
    }

    fun getMainSelectedTab(context: Context): MainTab {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val route = prefs.getString(MAIN_LAST_TAB, MainTab.Daily.route) ?: MainTab.Daily.route
        return MainTab.fromRoute(route)
    }

    fun saveOpenCount(context: Context, count: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(OPEN_COUNT, count).apply()
    }

    fun getOpenCount(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(OPEN_COUNT, 0)
    }

    fun saveHasRate(context: Context, hasRate: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(HAS_RATE, hasRate).apply()
    }

    fun getHasRate(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(HAS_RATE, false)
    }

    fun saveCurrency(context: Context, currencyCode: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CURRENCY, currencyCode).apply()
    }

    fun getCurrency(context: Context): AppCurrency {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val code = prefs.getString(KEY_CURRENCY, "VND") ?: "VND"

        return AppCurrency.values().find { it.code == code } ?: AppCurrency.VND
    }
}
