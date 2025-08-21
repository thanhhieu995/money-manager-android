package com.henrystudio.moneymanager.ui.bottomNavigation.statistic

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.helper.Helper
import com.henrystudio.moneymanager.model.*
import com.henrystudio.moneymanager.viewmodel.TransactionViewModel
import com.henrystudio.moneymanager.viewmodel.TransactionViewModelFactory
import java.time.LocalDate

class StatisticTrendActivity : AppCompatActivity() {
    private lateinit var btnClose: ImageView
    private lateinit var tabLayout: TabLayout
    private lateinit var fragment: StatisticCategoryFragment
    private lateinit var filterOption: FilterOption
    private lateinit var categoryType: CategoryType
    private lateinit var currentFilterPeriod: FilterPeriodStatistic
    private lateinit var viewModel: TransactionViewModel
    private lateinit var currentDate : LocalDate
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistic_trend)

        init()
        onBackPressedDispatcher.addCallback(this) {
            onBackAnimation()
        }
        val dao = AppDatabase.getDatabase(application).transactionDao()
        val factory = TransactionViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]
        filterOption = intent.getSerializableExtra("filterOption") as FilterOption
        categoryType = intent.getSerializableExtra("categoryType") as CategoryType
        currentFilterPeriod = intent.getSerializableExtra("currentFilterPeriodStatistic") as FilterPeriodStatistic

        // add 3 tab
        tabLayout.addTab(tabLayout.newTab().setText("Weekly"))
        tabLayout.addTab(tabLayout.newTab().setText("Monthly"))
        tabLayout.addTab(tabLayout.newTab().setText("Yearly"))
        // gắn fragment cố định 1 lần duy nhất
        fragment = StatisticCategoryFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.activity_statistic_trend_fragmentContainer, fragment)
            .commit()
        viewModel.currentFilterDate.observe(this) { date ->
            currentDate = date
        }
        tabLayout.getTabAt(getTabPosition(filterOption.type))?.select().apply {
            Handler().postDelayed({val filter = mapPositionToFilter(getTabPosition(filterOption.type), currentDate)
                fragment.update(
                    filter,
                    Helper.getUpdateMonthText(filter),
                    categoryType,
                    KeyFilter.Time
                )}, 100)
        }
        btnClose.setOnClickListener {
            onBackAnimation()
        }
        // lắng nghe đổi tab để update fragment
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position ?: 0

                val filter = mapPositionToFilter(position, currentDate)
                fragment.update(
                    filter,
                    Helper.getUpdateMonthText(filter),
                    categoryType,
                    KeyFilter.Time
                )
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    fun mapPositionToFilter(position: Int, date: LocalDate): FilterOption {
        return when (position) {
            0 -> FilterOption(FilterPeriodStatistic.Weekly, date)
            1 -> FilterOption(FilterPeriodStatistic.Monthly, date)
            2 -> FilterOption(FilterPeriodStatistic.Yearly, date)
            else -> FilterOption(FilterPeriodStatistic.Monthly, date)
        }
    }

    private fun getTabPosition(oldFilterPeriod: FilterPeriodStatistic) : Int {
        return when (oldFilterPeriod) {
            FilterPeriodStatistic.Weekly -> 0
            FilterPeriodStatistic.Monthly -> 1
            FilterPeriodStatistic.Yearly -> 2
            else -> 0
        }
    }

    private fun onBackAnimation() {
        finish()
        overridePendingTransition(R.anim.no_animation, R.anim.slide_out_bottom)
    }

    private fun init() {
        tabLayout = findViewById(R.id.activity_statistic_trend_tabLayout)
        btnClose = findViewById(R.id.activity_statistic_trend_imgClose)
    }
}