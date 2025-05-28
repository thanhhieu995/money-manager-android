package com.example.moneymanager.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.moneymanager.R
import com.example.moneymanager.ui.search.SearchActivity
import com.example.moneymanager.helper.Currency
import com.example.moneymanager.helper.FilterTransactions
import com.example.moneymanager.model.AppDatabase
import com.example.moneymanager.model.TransactionGroup
import com.example.moneymanager.ui.addtransaction.AddTransactionActivity
import com.example.moneymanager.ui.bookmark.BookmarkActivity
import com.example.moneymanager.ui.monthly.MonthlyFragment
import com.example.moneymanager.viewmodel.TransactionViewModel
import com.example.moneymanager.viewmodel.TransactionViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: TransactionViewModel
    private lateinit var transactionGroupAdapter: TransactionGroupAdapter
    private var currency = Currency()
    var listTransactionGroup : List<TransactionGroup> = listOf()
    private val filterTransactions = FilterTransactions()

    private lateinit var search: ImageView
    private lateinit var incomeCountAll: TextView
    private lateinit var expenseCountAll: TextView
    private lateinit var totalCount: TextView
    private lateinit var monthBack: ImageView
    private lateinit var monthNext: ImageView
    private lateinit var monthText: TextView
    private lateinit var bookmark: ImageView
    private lateinit var bottomNav: BottomNavigationView

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()

        val dao = AppDatabase.getDatabase(application).transactionDao()
        val factory = TransactionViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]
        transactionGroupAdapter = TransactionGroupAdapter()

        val formatterYear = DateTimeFormatter.ofPattern("yyyy", Locale.getDefault())
        val formatterMonth = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        var month = viewModel.currentMonthYear.value

        search.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        val tabLayout = findViewById<TabLayout>(R.id.main_tabLayout)

        val viewPager = findViewById<ViewPager2>(R.id.main_viewPager)
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Daily"
                1 -> tab.text = "Calendar"
                2 -> tab.text = "Monthly"
            }
        }.attach()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.setCurrentTab(position)
            }
        })

        viewModel.currentTabPosition.observe(this) { position ->
            val filteredMonth = filterTransactions.filterTransactionsByMonth(listTransactionGroup,
                month!!
            )
            val filteredYear =
                filterTransactions.filterTransactionsByYear(listTransactionGroup, month!!)
            when (position) {
                0 -> {
                    // Daily tab selected
                    if (month != null) {
                        monthText.text = month!!.format(formatterMonth)
                        handleSummarySection(filteredMonth)
                    }
                }
                1 -> {
                    // Calendar tab selected
                    if (month != null) {
                        monthText.text = month!!.format(formatterMonth)
                        handleSummarySection(filteredMonth)
                    }
                }
                2 -> {
                    // Monthly tab selected
                    if (month != null) {
                        monthText.text = month!!.format(formatterYear)
                        handleSummarySection(filteredYear)
                    }
                }
            }
        }

        viewModel.groupedTransactions.observe(this) {list ->
            listTransactionGroup = list
            if (month != null) {
                val filteredListMonth = filterTransactions.filterTransactionsByMonth(list, month!!)
                transactionGroupAdapter.submitList(filteredListMonth)
                handleSummarySection(filteredListMonth)
            }
        }

        monthBack.setOnClickListener {
            val fragment = (viewPager.adapter as ViewPagerAdapter).getCurrentFragment(viewPager.currentItem)
            if (fragment is MonthlyFragment) {
                viewModel.changeYear(-1)
            } else {
                viewModel.changeMonth(-1)
            }
        }

        monthNext.setOnClickListener {
            val fragment = (viewPager.adapter as ViewPagerAdapter).getCurrentFragment(viewPager.currentItem)
            if (fragment is MonthlyFragment) {
                viewModel.changeYear(1)
            } else {
                viewModel.changeMonth(1)
            }
        }

        viewModel.currentMonthYear.observe(this) { selectedMonth ->
            month = selectedMonth
            val fragment = (viewPager.adapter as ViewPagerAdapter).getCurrentFragment(viewPager.currentItem)
            val isMonthly = fragment is MonthlyFragment

            monthText.text = selectedMonth.format(if (isMonthly) formatterYear else formatterMonth)

            val filtered = if (isMonthly) {
                filterTransactions.filterTransactionsByYear(listTransactionGroup, selectedMonth)
            } else {
                filterTransactions.filterTransactionsByMonth(listTransactionGroup, selectedMonth)
            }

            handleSummarySection(filtered)
        }

        val btnAdd = findViewById<FloatingActionButton>(R.id.main_btn_add)
        btnAdd.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }

        bookmark.setOnClickListener {
            val intent = Intent(this, BookmarkActivity::class.java)
            startActivity(intent)
        }

        bottomNav.setOnClickListener { item ->
            when(item.id) {
                R.id.nav_daily -> {
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
    }

    private fun init() {
        search = findViewById(R.id.main_search)
        incomeCountAll = findViewById(R.id.main_income_count_all)
        expenseCountAll = findViewById(R.id.main_expense_count_all)
        totalCount = findViewById(R.id.main_total_count)
        monthBack = findViewById(R.id.main_month_back)
        monthNext = findViewById(R.id.main_month_next)
        monthText = findViewById(R.id.main_month_text)
        bookmark = findViewById(R.id.main_bookmark)
        bottomNav = findViewById(R.id.main_bottomBar)
    }

     private fun handleSummarySection(filtered: List<TransactionGroup>) {
        incomeCountAll.text = currency.formatCurrency(filtered.sumOf { it.income })
        expenseCountAll.text = currency.formatCurrency(filtered.sumOf { it.expense })
        totalCount.text = currency.formatCurrency(filtered.sumOf { it.income } - filtered.sumOf { it.expense })
    }
}