package com.henrystudio.moneymanager.ui.bottomNavigation.statistic

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.helper.FilterTransactions
import com.henrystudio.moneymanager.helper.Helper
import com.henrystudio.moneymanager.model.AppDatabase
import com.henrystudio.moneymanager.model.FilterOption
import com.henrystudio.moneymanager.model.FilterPeriodStatistic
import com.henrystudio.moneymanager.model.TransactionGroup
import com.henrystudio.moneymanager.viewmodel.TransactionViewModel
import com.henrystudio.moneymanager.viewmodel.TransactionViewModelFactory
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*

class StatisticListTrendActivity : AppCompatActivity() {
    private lateinit var imgClose: ImageView
    private lateinit var monthText: TextView
    private lateinit var imgBack: ImageView
    private lateinit var imgNext: ImageView
    private lateinit var tvNoData: TextView
    private lateinit var incomeCountAll: TextView
    private lateinit var expenseCountAll: TextView
    private lateinit var totalCountAll: TextView
    private lateinit var layoutSummary: LinearLayout
    private lateinit var adapter: StatisticListTrendAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var filterOption: FilterOption
    private lateinit var currentFilterPeriod: FilterPeriodStatistic
    private lateinit var viewModel: TransactionViewModel
    private var allTransactionGroup : List<TransactionGroup> = emptyList()
    @RequiresApi(Build.VERSION_CODES.O)
    private var currentDate: LocalDate = LocalDate.now()
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistic_list_trend)
        init()
        val dao = AppDatabase.getDatabase(application).transactionDao()
        val factory = TransactionViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]
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

        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val filterMonth = FilterTransactions.filterTransactionGroupByMonth(allTransactionGroup, currentDate)
                val filterYear = FilterTransactions.filterTransactionGroupByYear(allTransactionGroup, currentDate)
                when(position) {
                    0 -> {
                        filterOption = FilterOption(FilterPeriodStatistic.Weekly, currentDate)
                        viewModel.setFilter(FilterPeriodStatistic.Weekly, currentDate)
                        handleSummarySection(filterMonth)
                    }
                    1 -> {
                        filterOption = FilterOption(FilterPeriodStatistic.Monthly, currentDate)
                        viewModel.setFilter(FilterPeriodStatistic.Monthly, currentDate)
                        handleSummarySection(filterYear)
                    }
                    2 -> {
                        filterOption = FilterOption(FilterPeriodStatistic.Yearly, currentDate)
                        viewModel.setFilter(FilterPeriodStatistic.Yearly, currentDate)
                    }
                }
            }
        })

        viewModel.setCurrentFilterDate(Helper.formatDateFromFilterOptionToDateDaily(filterOption.date.toString()))

        viewModel.currentFilterDate.observe(this) {date ->
            viewModel.setFilter(filterOption.type, date)
            currentDate = date
            initialSummarySection(allTransactionGroup)
        }

        viewModel.groupedTransactions.observe(this) { groups ->
            if (!groups.isNullOrEmpty()) {
                // chỉ chạy 1 lần để update summary khi mới có dữ liệu
                if (allTransactionGroup.isEmpty()) {
                    allTransactionGroup = groups
                    initialSummarySection(groups)
                } else {
                    allTransactionGroup = groups
                }
            }
        }

        viewModel.filterOption.observe(this) {option ->
            imgBack.visibility = if(option.type == FilterPeriodStatistic.Yearly) View.GONE else View.VISIBLE
            imgNext.visibility = if(option.type == FilterPeriodStatistic.Yearly) View.GONE else View.VISIBLE
            layoutSummary.visibility = if(option.type == FilterPeriodStatistic.Yearly) View.GONE else View.VISIBLE
            updateMonthTextListTrend(option, allTransactionGroup)
        }

        imgBack.setOnClickListener {
            when(filterOption.type) {
                FilterPeriodStatistic.Weekly -> viewModel.changeMonth(-1)
                FilterPeriodStatistic.Monthly -> viewModel.changeYear(-1)
                FilterPeriodStatistic.Yearly -> {}
                else -> viewModel.changeMonth(-1)
            }
        }

        imgNext.setOnClickListener {
            when(filterOption.type) {
                FilterPeriodStatistic.Weekly -> viewModel.changeMonth(1)
                FilterPeriodStatistic.Monthly -> viewModel.changeYear(1)
                FilterPeriodStatistic.Yearly -> {}
                else -> viewModel.changeMonth(1)
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
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        overridePendingTransition(R.anim.no_animation, R.anim.slide_out_bottom)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateMonthTextListTrend(filterOption: FilterOption, groups: List<TransactionGroup>) {
        monthText.text = when (filterOption.type) {
            FilterPeriodStatistic.Monthly -> filterOption.date.year.toString()
            FilterPeriodStatistic.Weekly -> {
                val formatterFirst = DateTimeFormatter.ofPattern("dd/MM")
                val formatterLast = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                // Ép tuần bắt đầu từ thứ Hai
                val weekFields = WeekFields.of(DayOfWeek.MONDAY, 1)
                // Ngày đầu tiên của tháng
                val firstDayOfMonth = LocalDate.of(filterOption.date.year, filterOption.date.month, 1)
                // Ngày cuối cùng của tháng
                val lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth())

                // Lùi về thứ Hai của tuần chứa ngày đầu tháng
                val firstMonday = firstDayOfMonth.with(weekFields.dayOfWeek(), 1)

                // Tiến tới Chủ Nhật của tuần chứa ngày cuối tháng
                val lastSunday = lastDayOfMonth.with(weekFields.dayOfWeek(), 7)

                "${firstMonday.format(formatterFirst)} ~ ${lastSunday.format(formatterLast)}"
            }
            FilterPeriodStatistic.Yearly -> getYearRangeFromTransactionGroups(groups)
            FilterPeriodStatistic.List -> "Not code now"
            FilterPeriodStatistic.Trend -> "Not code now"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getYearRangeFromTransactionGroups(groups: List<TransactionGroup>): String {
        if (groups.isEmpty()) return ""

        val formatter = DateTimeFormatter.ofPattern("dd/MM/yy (EEE)", Locale.ENGLISH)

        val minYear = groups.minOf { LocalDate.parse(it.date, formatter).year }
        val maxYear = groups.maxOf { LocalDate.parse(it.date, formatter).year }

        return if (minYear == maxYear) {
            "$minYear"
        } else {
            "$minYear ~ $maxYear"
        }
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
}