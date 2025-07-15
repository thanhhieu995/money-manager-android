package com.example.moneymanager.ui.main

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.example.moneymanager.R
import com.example.moneymanager.model.Account
import com.example.moneymanager.model.AppDatabase
import com.example.moneymanager.model.Category
import com.example.moneymanager.model.CategoryType
import com.example.moneymanager.ui.bottomNavigation.DailyNavigateFragment
import com.example.moneymanager.ui.bottomNavigation.StatisticFragment
import com.example.moneymanager.viewmodel.*
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
        defaultCategory()
        defaultAccount()
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
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_fragment_container, StatisticFragment())
                        .commit()
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

    private fun defaultCategory() {
        val daoCategory = AppDatabase.getDatabase(application).categoryDao()
        val factoryCategory = CategoryViewModelFactory(daoCategory)
        val categoryViewModel = ViewModelProvider(this, factoryCategory)[CategoryViewModel::class.java]
        val typeIncome = CategoryType.INCOME
        val typeExpense = CategoryType.EXPENSE
        categoryViewModel.getAll().observe(this) { listCategory ->
            if (listCategory.isEmpty()) {
                val defaultCategories = listOf(
                    // expense
                    Category(emoji = " \uD83E\uDD57", name = "Food", type = typeExpense),
                    Category(emoji = "🚗", name = "Transport", type = typeExpense),
                    Category(emoji = "🏠", name = "Household", type = typeExpense),
                    Category(emoji = "🐶", name = "Pets", type = typeExpense),
                    Category(emoji = "🎁", name = "Gift", type = typeExpense),
                    Category(emoji = "📚", name = "Education", type = typeExpense),
                    Category(emoji = "🏋️‍♂️", name = "Sport", type = typeExpense),
                    Category(emoji = "💄", name = "Beauty", type = typeExpense),
                    Category(emoji = "🧘‍♂️", name = "Health", type = typeExpense),
                    Category(emoji = "💻", name = "Investment", type = typeExpense),
                    Category(emoji = "🎨", name = "Culture", type = typeExpense),
                    Category(emoji = "🚲", name = "Bicycle", type = typeExpense),
                    Category(emoji = "\uD83C\uDF73 ☕", name = "Breakfast", type = typeExpense, parentId = 1),
                    Category(emoji = "\uD83E\uDD57 \uD83C\uDF71", name = "Lunch", type = typeExpense, parentId = 1),
                    Category(emoji = "\uD83C\uDF72 \uD83C\uDF56", name = "Dinner", type = typeExpense, parentId = 1),
                    // income
                    Category(emoji = "💸", name = "Allowance", type = typeIncome),
                    Category(emoji = "💼", name = "Salary", type = typeIncome),
                    Category(emoji = "🎁", name = "Bonus", type = typeIncome),
                )
                defaultCategories.forEach { categoryViewModel.insert(it) }
            }
        }
    }

    private fun defaultAccount() {
        val daoAccount = AppDatabase.getDatabase(application).accountDao()
        val factoryAccount = AccountViewModelFactory(daoAccount)
        val accountViewModel = ViewModelProvider(this, factoryAccount)[AccountViewModel::class.java]
        accountViewModel.getAllAccount().observe(this) { accounts ->
            if (accounts.isEmpty()) {
                val defaultAccounts = listOf(
                    Account(name = "Cash"),
                    Account(name = "Bank Account"),
                    Account(name = "Credit Card"),
                    Account(name = "E-Wallet"),
                    Account(name = "Crypto")
                )
                defaultAccounts.forEach { accountViewModel.insert(it) }
            }
        }
    }
}