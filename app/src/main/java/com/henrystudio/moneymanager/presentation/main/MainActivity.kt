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
import com.henrystudio.moneymanager.presentation.viewmodel.MainViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.SharedTransactionViewModel
import com.henrystudio.moneymanager.presentation.bottomNavigation.dailyNavigate.DailyNavigateFragment
import com.henrystudio.moneymanager.presentation.views.bottomNavigation.statistic.StatisticViewPagerFragment
import com.henrystudio.moneymanager.presentation.views.setting.SettingFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var bottomNav: BottomNavigationView
    private val sharedViewModel: SharedTransactionViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var adView: AdView

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val lastTab = prefs.getInt("last_selected_tab", R.id.nav_daily)

        init()
        initAds()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.currentFilterDate.collect { date ->
                    mainViewModel.updateFilterDate(date)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.uiState.collectLatest { state ->
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
        lifecycleScope.launch {
            bottomNav.selectedItemId = lastTab
        }
    }

    private fun init() {
        bottomNav = findViewById(R.id.main_bottomBar)
    }

    private fun initAds() {
        lifecycleScope.launch(Dispatchers.IO) {
            MobileAds.initialize(this@MainActivity) {}
            withContext(Dispatchers.Main) {
                adView = findViewById(R.id.adView)
                val adRequest = AdRequest.Builder().build()
                adView.loadAd(adRequest)
            }
        }
    }
}
