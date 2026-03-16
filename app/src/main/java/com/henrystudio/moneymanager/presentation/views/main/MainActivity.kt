package com.henrystudio.moneymanager.presentation.views.main

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.data.model.Account
import com.henrystudio.moneymanager.data.model.Category
import com.henrystudio.moneymanager.data.model.CategoryType
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.viewmodel.AccountViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.CategoryViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.MainViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.SharedTransactionViewModel
import com.henrystudio.moneymanager.presentation.views.bottomNavigation.dailyNavigate.DailyNavigateFragment
import com.henrystudio.moneymanager.presentation.views.bottomNavigation.statistic.StatisticViewPagerFragment
import com.henrystudio.moneymanager.presentation.views.setting.SettingFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import androidx.core.content.edit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var bottomNav: BottomNavigationView
    private val sharedViewModel: SharedTransactionViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()
    private val accountViewModel: AccountViewModel by viewModels()

    private lateinit var adView: AdView

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this) {}

        adView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val lastTab = prefs.getInt("last_selected_tab", R.id.nav_daily)

        init()
        defaultCategory()
        defaultAccount()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.currentFilterDate.collect { date ->
                    mainViewModel.updateFilterDate(date)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.uiState.collect { state ->
                    bottomNav.menu.findItem(R.id.nav_daily).title = state.bottomNavTitle
                }
            }
        }

        bottomNav.setOnItemSelectedListener { item ->
            prefs.edit { putInt("last_selected_tab", item.itemId) }
            when(item.itemId) {
                R.id.nav_daily -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_fragment_container, DailyNavigateFragment())
                        .commit()
                    true
                }
                R.id.nav_stats -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_fragment_container, StatisticViewPagerFragment())
                        .commit()
                    true
                }
                R.id.nav_more -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_fragment_container, SettingFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
        bottomNav.selectedItemId = lastTab
    }

    private fun init() {
        bottomNav = findViewById(R.id.main_bottomBar)
    }

    private fun defaultCategory() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

        if (!prefs.getBoolean("category_seeded", false)) {

            val typeIncome = CategoryType.INCOME
            val typeExpense = CategoryType.EXPENSE

            val defaultCategories = listOf(
                Category(emoji = "🥗", name = "Food", type = typeExpense),
                Category(emoji = "🎉", name = "Social Life", type = typeExpense),
                Category(emoji = "🚗", name = "Transport", type = typeExpense),
                Category(emoji = "🎨", name = "Culture", type = typeExpense),
                Category(emoji = "🏠", name = "Household", type = typeExpense),
                Category(emoji = "👕", name = "Apparel", type = typeExpense),
                Category(emoji = "💄", name = "Beauty", type = typeExpense),
                Category(emoji = "🧘‍♂️", name = "Health", type = typeExpense),
                Category(emoji = "📚", name = "Education", type = typeExpense),
                Category(emoji = "🐶", name = "Pets", type = typeExpense),
                Category(emoji = "🎁", name = "Gift", type = typeExpense),
                Category(emoji = "🏋️‍♂️", name = "Sport", type = typeExpense),
                Category(emoji = "💻", name = "Investment", type = typeExpense),
                Category(emoji = "🚲", name = "Bicycle", type = typeExpense),
                Category(emoji = "", name = "Other", type = typeExpense),

                Category(emoji = "💸", name = "Allowance", type = typeIncome),
                Category(emoji = "💼", name = "Salary", type = typeIncome),
                Category(emoji = "🎁", name = "Bonus", type = typeIncome),
                Category(emoji = "", name = "Other", type = typeIncome),
            )

            defaultCategories.forEach {
                categoryViewModel.insert(it)
            }

            prefs.edit {
                putBoolean("category_seeded", true)
            }
        }
    }

    private fun defaultAccount() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

        if (!prefs.getBoolean("account_seeded", false)) {

            val defaultAccounts = listOf(
                Account(name = "Cash"),
                Account(name = "Bank Account"),
                Account(name = "Credit Card"),
                Account(name = "E-Wallet"),
                Account(name = "Crypto")
            )

            defaultAccounts.forEach {
                accountViewModel.insert(it)
            }

            prefs.edit {
                putBoolean("account_seeded", true)
            }
        }
    }
}
