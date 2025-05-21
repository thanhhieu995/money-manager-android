package com.example.moneymanager.ui.search

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanager.R
import com.example.moneymanager.model.AppDatabase
import com.example.moneymanager.model.Transaction
import com.example.moneymanager.viewmodel.TransactionViewModel
import com.example.moneymanager.viewmodel.TransactionViewModelFactory

class SearchActivity : AppCompatActivity() {
    private lateinit var viewModel: TransactionViewModel
    private var transactions : List<Transaction> = listOf()
    private var transactionAdapter = TransactionAdapter(emptyList())
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val btnBack = findViewById<ImageView>(R.id.search_back)
        val btnCancel = findViewById<TextView>(R.id.search_btnCancel)
        val searchView = findViewById<SearchView>(R.id.search_searchView)
        val incomeCount = findViewById<TextView>(R.id.search_income_count_all)
        val expenseCount = findViewById<TextView>(R.id.search_expense_count_all)
        val recyclerView = findViewById<RecyclerView>(R.id.search_resultList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = transactionAdapter

        btnBack.setOnClickListener {
            finish()
        }

        btnCancel.setOnClickListener {
            finish()
        }

        val dao = AppDatabase.getDatabase(application).transactionDao()
        val factory = TransactionViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]
        viewModel.allTransactions.observe(this) {
            transactions = it
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    transactionAdapter.updateList(emptyList())
                    incomeCount.text = "0đ"
                    expenseCount.text = "0đ"
                } else {
                    transactionAdapter.filter.filter(newText)
                    transactionAdapter.updateList(transactions)
                }
                return true
            }
        })

        transactionAdapter.setOnFilterResultListener(object : TransactionAdapter.OnFilterResultListener {
            override fun onFilterResult(filteredList: List<Transaction>) {
                // TODO: cập nhật UI khác nếu cần
                var totalIncome: Double = 0.0
                var totalExpense: Double = 0.0

                for (tx in filteredList) {
                    if (tx.isIncome) {
                        totalIncome += tx.amount
                    } else {
                        totalExpense += tx.amount
                    }
                }

                incomeCount.text = transactionAdapter.formatCurrency(totalIncome)
                expenseCount.text = transactionAdapter.formatCurrency(totalExpense)
            }
        })
    }
}