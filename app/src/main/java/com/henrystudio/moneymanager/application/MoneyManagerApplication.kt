package com.henrystudio.moneymanager.application

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.google.android.gms.ads.MobileAds

class MoneyManagerApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        MobileAds.initialize(this) {}

        // 1. Lấy ngôn ngữ đã lưu
        val lang = LocaleHelper.getLanguage(this)

        // 2. Set lại cho AppCompatDelegate
        val appLocale = LocaleListCompat.forLanguageTags(lang)
        AppCompatDelegate.setApplicationLocales(appLocale)

        // 3. Theme theo hệ thống
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
}