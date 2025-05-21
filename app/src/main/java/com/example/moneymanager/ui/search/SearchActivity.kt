package com.example.moneymanager.ui.search

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
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
        val searchInput = findViewById<AutoCompleteTextView>(R.id.search_autoComplete)
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
        viewModel.allTransactions.observe(this) { it ->
            transactions = it

            val contents = transactions.map { it.content }.distinct()

            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                contents
            )
            searchInput.setAdapter(adapter)

            // Khi người dùng chọn 1 gợi ý
            searchInput.setOnItemClickListener { _, _, position, _ ->
                val selected = adapter.getItem(position)
                searchInput.setText(selected)
                searchInput.setSelection(selected?.length ?: 0)
                transactionAdapter.filter.filter(selected)
            }

            // Khi người dùng nhập văn bản
            searchInput.addTextChangedListener {
                if (it.isNullOrEmpty()) {
                    transactionAdapter.updateList(emptyList())
                    incomeCount.text = "0đ"
                    expenseCount.text = "0đ"
                } else {
                    transactionAdapter.filter.filter(it.toString())
                    transactionAdapter.updateList(transactions)
                }
            }
        }

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