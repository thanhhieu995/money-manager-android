package com.henrystudio.moneymanager.ui.bottomNavigation.statistic

import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.application.BaseActivity
import com.henrystudio.moneymanager.helper.Helper
import com.henrystudio.moneymanager.model.*
import com.henrystudio.moneymanager.ui.setting.LanguagePref
import com.henrystudio.moneymanager.viewmodel.TransactionViewModel
import com.henrystudio.moneymanager.viewmodel.TransactionViewModelFactory
import java.time.LocalDate

class StatisticTrendActivity : BaseActivity() {
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
        // Lấy ngôn ngữ đã lưu
        val lang = LanguagePref.getLanguage(this)
        if (lang != null) {
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(lang)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
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
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.weekly)))
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.monthly)))
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.yearly)))
        val filter = mapPositionToFilter(getTabPosition(filterOption.type), filterOption.date)

        val bundle = Bundle().apply {
            putSerializable("item_click_statistic_category_name", Helper.getUpdateMonthText(filter))
            putSerializable("item_click_statistic_category_type",categoryType)
            putSerializable("item_click_statistic_filterOption", filter)
            putSerializable("item_click_statistic_keyWord", KeyFilter.Time)
        }
        // gắn fragment cố định 1 lần duy nhất
        fragment = StatisticCategoryFragment().apply {
            arguments = bundle
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.activity_statistic_trend_fragmentContainer, fragment)
            .commit()
        viewModel.currentFilterDate.observe(this) { date ->
            currentDate = date
        }
        tabLayout.getTabAt(getTabPosition(filterOption.type))?.select()
        btnClose.setOnClickListener {
            onBackAnimation()
        }
        // lắng nghe đổi tab để update fragment
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position ?: 0

                val filterTemp = mapPositionToFilter(position, currentDate)
                viewModel.setFilter(filterTemp.type, currentDate)
                val bundleTab = Bundle().apply {
                    putSerializable("item_click_statistic_category_name", Helper.getUpdateMonthText(filter))
                    putSerializable("item_click_statistic_category_type",categoryType)
                    putSerializable("item_click_statistic_filterOption", filterTemp)
                    putSerializable("item_click_statistic_keyWord", KeyFilter.Time)
                }
                // gắn fragment cố định 1 lần duy nhất
                fragment = StatisticCategoryFragment().apply {
                    arguments = bundleTab
                }
                supportFragmentManager.beginTransaction()
                    .replace(R.id.activity_statistic_trend_fragmentContainer, fragment)
                    .commit()
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