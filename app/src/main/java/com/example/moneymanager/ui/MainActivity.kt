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
        tabLayout.addTab(tabLayout.newTab().setText("Daily"))
        tabLayout.addTab(tabLayout.newTab().setText("Calender"))
        tabLayout.addTab(tabLayout.newTab().setText("Monthly"))

        val recyclerView = findViewById<RecyclerView>(R.id.transactionList)
        val adapter = TransactionAdapter()
        recyclerView.adapter = adapter

        viewModel.transactions.observe(this) { transactions ->
            adapter.submitList(transactions)
            noDataText.visibility = if (transactions.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}