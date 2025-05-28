package com.example.moneymanager.ui.main

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.example.moneymanager.R
import com.example.moneymanager.model.AppDatabase
import com.example.moneymanager.ui.bottomNavigation.DailyNavigateFragment
import com.example.moneymanager.viewmodel.TransactionViewModel
import com.example.moneymanager.viewmodel.TransactionViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.time.format.DateTimeFormatter
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var viewModel: TransactionViewModel
    @RequiresApi(Build.VERSION_CODES.O)
    private val formatterMonth = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
        val dao = AppDatabase.getDatabase(application).transactionDao()
        val factory = TransactionViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]

        viewModel.currentMonthYear.observe(this) { month ->
            bottomNav.menu.findItem(R.id.nav_daily).title = month.format(formatterMonth)
        }

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