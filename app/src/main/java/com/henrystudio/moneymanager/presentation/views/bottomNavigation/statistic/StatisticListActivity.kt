package com.henrystudio.moneymanager.presentation.views.bottomNavigation.statistic

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.core.util.FilterTransactions
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.data.model.CategoryType
import com.henrystudio.moneymanager.data.model.TransactionGroup
import com.henrystudio.moneymanager.presentation.model.FilterOption
import com.henrystudio.moneymanager.presentation.model.FilterPeriodStatistic
import com.henrystudio.moneymanager.presentation.viewmodel.SharedTransactionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*

@AndroidEntryPoint
class StatisticListActivity : AppCompatActivity() {
    private lateinit var imgClose: ImageView
    private lateinit var monthText: TextView
    private lateinit var imgBack: ImageView
    private lateinit var imgNext: ImageView
    private lateinit var tvNoData: TextView
    private lateinit var incomeCountAll: TextView
    private lateinit var expenseCountAll: TextView
    private lateinit var totalCountAll: TextView
    private lateinit var layoutSummary: LinearLayout
    private lateinit var layoutControl: LinearLayout
    private lateinit var adapter: StatisticListAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var filterOption: FilterOption
    private lateinit var categoryType: CategoryType
    private lateinit var currentFilterPeriod: FilterPeriodStatistic
    private val sharedViewModel: SharedTransactionViewModel by viewModels()
    private var allTransactionGroup : List<TransactionGroup> = emptyList()
    @RequiresApi(Build.VERSION_CODES.O)
    private var currentDate: LocalDate = LocalDate.now()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistic_list)
        init()
        onBackPressedDispatcher.addCallback(this) {
            onBackAnimation()
        }

        filterOption = intent.getSerializableExtra("filterOption") as FilterOption
        categoryType = intent.getSerializableExtra("categoryType") as CategoryType
        currentFilterPeriod = intent.getSerializableExtra("currentFilterPeriodStatistic") as FilterPeriodStatistic
        
        imgClose.setOnClickListener {
            onBackAnimation()
        }
        
        adapter = StatisticListAdapter(this, filterOption)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) {tab, position ->
            tab.text = when(position) {
                0 -> getString(R.string.weekly)
                1 -> getString(R.string.monthly)
                2 -> getString(R.string.yearly)
                else -> ""
            }
        }.attach()
        viewPager.offscreenPageLimit = 3
        viewPager.currentItem = getTabPosition(filterOption.type)

        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val filterMonth = FilterTransactions.filterTransactionGroupByMonth(allTransactionGroup, currentDate)
                val filterYear = FilterTransactions.filterTransactionGroupByYear(allTransactionGroup, currentDate)

                filterOption = mapPositionToFilter(position, currentDate)
                sharedViewModel.setFilter(filterOption.type, currentDate)
                when (position) {
                    0 -> handleSummarySection(filterMonth)
                    1 -> handleSummarySection(filterYear)
                    2 -> {}
                }
            }
        })

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    sharedViewModel.currentFilterDate.collect { date ->
                        sharedViewModel.setFilter(filterOption.type, date)
                        currentDate = date
                        initialSummarySection(allTransactionGroup)
                    }
                }

                launch {
                    sharedViewModel.groupedTransactions.collect { groups ->
                        layoutControl.visibility = when(currentFilterPeriod) {
                            FilterPeriodStatistic.Trend -> View.GONE
                            else -> View.VISIBLE
                        }
                        if (groups.isNotEmpty()) {
                            if (allTransactionGroup.isEmpty()) {
                                allTransactionGroup = groups
                                initialSummarySection(groups)
                                updateMonthTextList(filterOption, allTransactionGroup)
                            } else {
                                allTransactionGroup = groups
                            }
                        }
                    }
                }

                launch {
                    sharedViewModel.filterOption.collect { option ->
                        imgBack.visibility = if(option.type == FilterPeriodStatistic.Yearly) View.GONE else View.VISIBLE
                        imgNext.visibility = if(option.type == FilterPeriodStatistic.Yearly) View.GONE else View.VISIBLE
                        layoutSummary.visibility = if(option.type == FilterPeriodStatistic.Yearly) View.GONE else View.VISIBLE
                        updateMonthTextList(option, allTransactionGroup)
                    }
                }
            }
        }

        sharedViewModel.setCurrentFilterDate(Helper.formatDateFromFilterOptionToDateDaily(filterOption.date.toString()))

        imgBack.setOnClickListener {
            when(filterOption.type) {
                FilterPeriodStatistic.Weekly -> sharedViewModel.changeMonth(-1)
                FilterPeriodStatistic.Monthly -> sharedViewModel.changeYear(-1)
                else -> sharedViewModel.changeMonth(-1)
            }
        }

        imgNext.setOnClickListener {
            when(filterOption.type) {
                FilterPeriodStatistic.Weekly -> sharedViewModel.changeMonth(1)
                FilterPeriodStatistic.Monthly -> sharedViewModel.changeYear(1)
                else -> sharedViewModel.changeMonth(1)
            }
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

    private fun init() {
        imgClose = findViewById(R.id.activity_statistic_list_trend_imgClose)
        viewPager = findViewById(R.id.activity_statistic_list_trend_viewPager)
        tabLayout = findViewById(R.id.activity_statistic_list_trend_tabLayout)
        monthText = findViewById(R.id.activity_statistic_list_trend_month_text)
        imgBack = findViewById(R.id.activity_statistic_list_trend_month_back)
        imgNext = findViewById(R.id.activity_statistic_list_trend_month_next)
        tvNoData = findViewById(R.id.activity_statistic_list_trend_tv_noData)
        incomeCountAll = findViewById(R.id.activity_statistic_list_trend_income_count_all)
        expenseCountAll = findViewById(R.id.activity_statistic_list_trend_expense_count_all)
        totalCountAll = findViewById(R.id.activity_statistic_list_trend_total_count)
        layoutSummary = findViewById(R.id.activity_statistic_list_trend_summarySection)
        layoutControl = findViewById(R.id.activity_statistic_list_trend_control)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateMonthTextList(filterOption: FilterOption, groups: List<TransactionGroup>) {
        monthText.text = when (filterOption.type) {
            FilterPeriodStatistic.Monthly -> filterOption.date.year.toString()
            FilterPeriodStatistic.Weekly -> {
                val formatterFirst = DateTimeFormatter.ofPattern("dd/MM")
                val formatterLast = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val weekFields = WeekFields.of(DayOfWeek.MONDAY, 1)
                val firstDayOfMonth = LocalDate.of(filterOption.date.year, filterOption.date.month, 1)
                val lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth())
                val firstMonday = firstDayOfMonth.with(weekFields.dayOfWeek(), 1)
                val lastSunday = lastDayOfMonth.with(weekFields.dayOfWeek(), 7)
                "${firstMonday.format(formatterFirst)} ~ ${lastSunday.format(formatterLast)}"
            }
            FilterPeriodStatistic.Yearly -> getYearRangeFromTransactionGroups(groups)
            else -> "N/A"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getYearRangeFromTransactionGroups(groups: List<TransactionGroup>): String {
        if (groups.isEmpty()) return ""
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yy (EEE)", Locale.ENGLISH)
        val minYear = groups.minOf { LocalDate.parse(it.date, formatter).year }
        val maxYear = groups.maxOf { LocalDate.parse(it.date, formatter).year }
        return if (minYear == maxYear) "$minYear" else "$minYear ~ $maxYear"
    }

    private fun handleSummarySection(filtered: List<TransactionGroup>) {
        incomeCountAll.text = Helper.formatCurrency(filtered.sumOf { it.income })
        expenseCountAll.text = Helper.formatCurrency(filtered.sumOf { it.expense })
        totalCountAll.text = Helper.formatCurrency(filtered.sumOf { it.income - it.expense })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initialSummarySection(groups: List<TransactionGroup>) {
        when (getTabPosition(filterOption.type)) {
            0 -> {
                val filterMonth = FilterTransactions.filterTransactionGroupByMonth(groups, currentDate)
                handleSummarySection(filterMonth)
            }
            1 -> {
                val filterYear = FilterTransactions.filterTransactionGroupByYear(groups, currentDate)
                handleSummarySection(filterYear)
            }
            2 -> {}
        }
    }

    fun onBackAnimation() {
        finish()
        overridePendingTransition(R.anim.no_animation, R.anim.slide_out_bottom)
    }

    fun mapPositionToFilter(position: Int, date: LocalDate): FilterOption {
        return when (position) {
            0 -> FilterOption(FilterPeriodStatistic.Weekly, date)
            1 -> FilterOption(FilterPeriodStatistic.Monthly, date)
            2 -> FilterOption(FilterPeriodStatistic.Yearly, date)
            else -> FilterOption(FilterPeriodStatistic.Monthly, date)
        }
    }
}
