package com.henrystudio.moneymanager.application

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        val lang = LocaleHelper.getLanguage(newBase)
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang))
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Khi hệ thống đổi giữa dark/light mode
        delegate.applyDayNight()
        // Hoặc nếu muốn chắc chắn toàn bộ giao diện load lại:
        // recreate()
    }
}