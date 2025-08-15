package com.henrystudio.moneymanager.ui.bottomNavigation.statistic

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.model.FilterOption
import com.henrystudio.moneymanager.model.FilterPeriodStatistic

class StatisticListTrendActivity : AppCompatActivity() {
    private lateinit var imgClose: ImageView
    private lateinit var adapter: StatisticListTrendAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var filterOption: FilterOption
    private lateinit var currentFilterPeriod: FilterPeriodStatistic
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistic_list_trend)
        init()
        filterOption = intent.getSerializableExtra("filterOption") as FilterOption
        currentFilterPeriod = intent.getSerializableExtra("currentFilterPeriodStatistic") as FilterPeriodStatistic
        imgClose.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.no_animation, R.anim.slide_out_bottom)
        }
        adapter = StatisticListTrendAdapter(this, filterOption, currentFilterPeriod)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) {tab, position ->
            tab.text = when(position) {
                0 -> "Weekly"
                1 -> "Monthly"
                2 -> "Yearly"
                else -> ""
            }
        }.attach()
        // ✅ Chuyển tab dựa vào oldFilterPeriod
        viewPager.currentItem = getTabPosition(filterOption.type)
    }

    private fun getTabPosition(oldFilterPeriod: FilterPeriodStatistic) : Int {
        return when (oldFilterPeriod) {
            FilterPeriodStatistic.Weekly -> 0
            FilterPeriodStatistic.Monthly -> 1
            FilterPeriodStatistic.Yearly -> 2
            else -> 0
        }
    }

    private fun init() {
        imgClose = findViewById(R.id.activity_statistic_list_trend_imgClose)
        viewPager = findViewById(R.id.activity_statistic_list_trend_viewPager)
        tabLayout = findViewById(R.id.activity_statistic_list_trend_tabLayout)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        overridePendingTransition(R.anim.no_animation, R.anim.slide_out_bottom)
    }
}