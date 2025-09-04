package com.henrystudio.moneymanager.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.ui.main.MainActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        // Thời gian chờ cho màn hình splash (ví dụ: 1 giây)
        val splashTimeOut: Long = 1000

        Handler(Looper.getMainLooper()).postDelayed({
            // Tạo Intent để chuyển sang MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            // Kết thúc SplashActivity để người dùng không thể quay lại
            finish()
        }, splashTimeOut)
    }
}