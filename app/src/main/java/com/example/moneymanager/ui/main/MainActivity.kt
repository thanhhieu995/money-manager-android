package com.example.moneymanager.ui.main

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.example.moneymanager.R
import com.example.moneymanager.ui.bottomNavigation.DailyNavigateFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()

        bottomNav.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_daily -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_fragment_container, DailyNavigateFragment())
                        .commit()
                    true
                }
                R.id.nav_stats -> {
                    true
                }
                R.id.nav_accounts -> {
                    true
                }
                R.id.nav_more -> {
                    true
                }
                else -> false
            }
        }
        bottomNav.selectedItemId = R.id.nav_daily // phải đặt sau listener
    }

    private fun init() {
        bottomNav = findViewById(R.id.main_bottomBar)
    }
}