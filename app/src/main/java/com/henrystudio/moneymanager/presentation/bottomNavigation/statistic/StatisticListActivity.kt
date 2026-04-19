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
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.presentation.model.FilterOption
import com.henrystudio.moneymanager.presentation.model.FilterPeriodStatistic
import com.henrystudio.moneymanager.presentation.model.TransactionType
import com.henrystudio.moneymanager.presentation.viewmodel.SharedTransactionViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.StatisticListViewModel
import com.henrystudio.moneymanager.presentation.model.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
    private val sharedViewModel: SharedTransactionViewModel by viewModels()
    private val viewModel: StatisticListViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistic_list)
        init()
        onBackPressedDispatcher.addCallback(this) {
            onBackAnimation()
        }

        val filterOption = intent.getSerializableExtra("filterOption") as FilterOption
        val transactionType = intent.getSerializableExtra("transactionType") as TransactionType
        val currentFilterPeriod = intent.getSerializableExtra("currentFilterPeriodStatistic") as FilterPeriodStatistic
        viewModel.setArgs(filterOption, transactionType, currentFilterPeriod)

        imgClose.setOnClickListener {
            onBackAnimation()
        }
        
        adapter = StatisticListAdapter(this, filterOption)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.weekly)
                1 -> getString(R.string.monthly)
                2 -> getString(R.string.yearly)
                else -> ""
            }
        }.attach()
        viewPager.offscreenPageLimit = 3
        viewPager.currentItem = viewModel.getTabPosition(filterOption.type)

        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.onTabSelected(position)
                viewModel.getFilterOption()?.let { opt ->
                    sharedViewModel.setFilter(opt.type, opt.date)
                }
            }
        })

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.currentFilterDate.collect { date ->
                    viewModel.updateCurrentDate(date)
                    viewModel.getFilterOption()?.let { opt ->
                        sharedViewModel.setFilter(opt.type, date)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.groupedTransactionsState.collect { state ->
                    if (state is UiState.Success) {
                        viewModel.updateGroups(state.data)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    state.filterOption?.let { opt ->
                        adapter.updateFilterOption(opt)
                    }
                    state.currentFilterPeriod?.let { period ->
                        layoutControl.visibility = if (period == FilterPeriodStatistic.Trend) View.GONE else View.VISIBLE
                    }
                    monthText.text = state.monthLabel
                    incomeCountAll.text = state.incomeSum
                    expenseCountAll.text = state.expenseSum
                    totalCountAll.text = state.totalSum
                    imgBack.visibility = if (state.showBack) View.VISIBLE else View.GONE
                    imgNext.visibility = if (state.showNext) View.VISIBLE else View.GONE
                    layoutSummary.visibility = if (state.showSummary) View.VISIBLE else View.GONE
                }
            }
        }

        sharedViewModel.setCurrentFilterDate(Helper.localDateToStartOfDayEpochMillis(filterOption.date))

        imgBack.setOnClickListener {
            viewModel.getFilterOption()?.let { opt ->
                when (opt.type) {
                    FilterPeriodStatistic.Weekly -> sharedViewModel.changeMonth(-1)
                    FilterPeriodStatistic.Monthly -> sharedViewModel.changeYear(-1)
                    else -> sharedViewModel.changeMonth(-1)
                }
            }
        }

        imgNext.setOnClickListener {
            viewModel.getFilterOption()?.let { opt ->
                when (opt.type) {
                    FilterPeriodStatistic.Weekly -> sharedViewModel.changeMonth(1)
                    FilterPeriodStatistic.Monthly -> sharedViewModel.changeYear(1)
                    else -> sharedViewModel.changeMonth(1)
                }
            }
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

    fun onBackAnimation() {
        finish()
        overridePendingTransition(R.anim.no_animation, R.anim.slide_out_bottom)
    }
}
