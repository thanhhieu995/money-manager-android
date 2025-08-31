package com.henrystudio.moneymanager.application

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class MoneyManagerApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    override fun attachBaseContext(base: Context?) {
        val lang = base?.let { LocaleHelper.getLanguage(it) }
        super.attachBaseContext(base?.let { lang?.let { it1 -> LocaleHelper.setLocale(it, it1) } })
    }
}