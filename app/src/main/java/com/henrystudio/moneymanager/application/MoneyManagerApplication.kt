package com.henrystudio.moneymanager.application

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class MoneyManagerApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
}