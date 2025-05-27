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
import com.example.moneymanager.viewmodel.TransactionViewModel
import com.example.moneymanager.viewmodel.TransactionViewModelFactory
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

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

         search = findViewById(R.id.main_search)
         incomeCountAll = findViewById(R.id.main_income_count_all)
         expenseCountAll = findViewById(R.id.main_expense_count_all)
         totalCount = findViewById(R.id.main_total_count)
         monthBack = findViewById(R.id.main_month_back)
         monthNext = findViewById(R.id.main_month_next)
         monthText = findViewById(R.id.main_month_text)
         bookmark = findViewById(R.id.main_bookmark)

        search.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }


        val dao = AppDatabase.getDatabase(application).transactionDao()
        val factory = TransactionViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]
        transactionGroupAdapter = TransactionGroupAdapter()

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Daily"
                1 -> tab.text = "Calendar"
                2 -> tab.text = "Monthly"
            }
        }.attach()

        viewModel.groupedTransactions.observe(this) {list ->
            transactionGroupAdapter.submitList(list)
            listTransactionGroup = list
            incomeCountAll.text = currency.formatCurrency(list.sumOf { it.income })
            expenseCountAll.text = currency.formatCurrency(list.sumOf { it.expense })
            totalCount.text = currency.formatCurrency(list.sumOf { it.income } - list.sumOf { it.expense })
        }

        monthBack.setOnClickListener {
            viewModel.changeMonth(-1)
        }

        monthNext.setOnClickListener {
            viewModel.changeMonth(1)
        }

        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        viewModel.currentMonthYear.observe(this) { selectedMonth ->
            monthText.text = selectedMonth.format(formatter)
            val filtered = filterTransactions.filterTransactionsByMonth(listTransactionGroup, selectedMonth)
            incomeCountAll.text = currency.formatCurrency(filtered.sumOf { it.income })
            expenseCountAll.text = currency.formatCurrency(filtered.sumOf { it.expense })
            totalCount.text = currency.formatCurrency(filtered.sumOf { it.income } - filtered.sumOf { it.expense })
        }

        val btnAdd = findViewById<FloatingActionButton>(R.id.btn_add)
        btnAdd.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }

        bookmark.setOnClickListener {
            val intent = Intent(this, BookmarkActivity::class.java)
            startActivity(intent)
        }
    }
}