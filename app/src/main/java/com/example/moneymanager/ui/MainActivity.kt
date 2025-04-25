package com.example.moneymanager.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanager.R
import com.example.moneymanager.model.AppDatabase
import com.example.moneymanager.viewmodel.TransactionViewModel
import com.example.moneymanager.viewmodel.TransactionViewModelFactory
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: TransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dao = AppDatabase.getInstance(application).transactionDao()
        val factory = TransactionViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]

        val noDataText = findViewById<TextView>(R.id.noDataText)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.addTab(tabLayout.newTab()) // Tab 0: Tất cả
        tabLayout.addTab(tabLayout.newTab()) // Tab 1: Thu
        tabLayout.addTab(tabLayout.newTab()) // Tab 2: Chi

        val recyclerView = findViewById<RecyclerView>(R.id.transactionList)
        val adapter = TransactionAdapter()
        recyclerView.adapter = adapter

        viewModel.transactions.observe(this) { transactions ->
            adapter.submitList(transactions)
            noDataText.visibility = if (transactions.isEmpty()) View.VISIBLE else View.GONE

            val incomeList = transactions.filter { it.amount > 0 }
            val expenseList = transactions.filter { it.amount < 0 }
            val totalIncome = incomeList.sumOf { it.amount }
            val totalExpense = expenseList.sumOf { it.amount }
            val totalAll = transactions.sumOf { it.amount }

            setCustomTab(tabLayout, 0, "Tất cả", totalAll, transactions.size)
            setCustomTab(tabLayout, 1, "Thu", totalIncome, incomeList.size)
            setCustomTab(tabLayout, 2, "Chi", totalExpense, expenseList.size)
        }
    }

    private fun setCustomTab(tabLayout: TabLayout, position: Int, title: String, amount: Double, count: Int) {
        val tab = tabLayout.getTabAt(position)
        val view = LayoutInflater.from(this).inflate(R.layout.custom_tab, null)
        view.findViewById<TextView>(R.id.tabTitle).text = title
        view.findViewById<TextView>(R.id.tabAmount).text = "₫${amount} (${count})"
        tab?.customView = view
    }
}