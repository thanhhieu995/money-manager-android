package com.example.moneymanager.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.moneymanager.R
import com.example.moneymanager.helper.Currency
import com.example.moneymanager.model.AppDatabase
import com.example.moneymanager.ui.addtransaction.AddTransactionActivity
import com.example.moneymanager.viewmodel.TransactionViewModel
import com.example.moneymanager.viewmodel.TransactionViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: TransactionViewModel
    private lateinit var transactionGroupAdapter: TransactionGroupAdapter
    private var currency = Currency()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val incomeCountAll = findViewById<TextView>(R.id.main_income_count_all)
        val expenseCountAll = findViewById<TextView>(R.id.main_expense_count_all)
        val totalCount = findViewById<TextView>(R.id.main_total_count)

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

            incomeCountAll.text = currency.formatCurrency(list.sumOf { it.income })
            expenseCountAll.text = currency.formatCurrency(list.sumOf { it.expense })
            totalCount.text = currency.formatCurrency(list.sumOf { it.income } - list.sumOf { it.expense })
        }

        val btnAdd = findViewById<FloatingActionButton>(R.id.btn_add)
        btnAdd.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }
    }
}