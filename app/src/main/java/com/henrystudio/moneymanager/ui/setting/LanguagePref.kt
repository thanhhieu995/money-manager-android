package com.henrystudio.moneymanager.ui.setting

import android.content.Context

object LanguagePref {
    private const val PREF_NAME = "app_prefs"
    private const val KEY_LANG = "app_language"

    fun saveLanguage(context: Context, langCode: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, langCode).apply()
    }

    fun getLanguage(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANG, null) // null = chưa chọn
    }
}