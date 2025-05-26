package com.example.moneymanager.ui.search

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanager.R
import com.example.moneymanager.model.AppDatabase
import com.example.moneymanager.model.Transaction
import com.example.moneymanager.viewmodel.TransactionViewModel
import com.example.moneymanager.viewmodel.TransactionViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog

class SearchActivity : AppCompatActivity() {
    private lateinit var viewModel: TransactionViewModel
    private var transactions : List<Transaction> = listOf()
    private var transactionAdapter = TransactionAdapter(emptyList())
    private var selectedOption: String = "All" // default
    var searchQuery = ""
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val searchTitle = findViewById<TextView>(R.id.search_title)
        val searchArrange = findViewById<ImageView>(R.id.search_filter)
        val btnBack = findViewById<ImageView>(R.id.search_back)
        val btnCancel = findViewById<TextView>(R.id.search_btnCancel)
        val searchInput = findViewById<AutoCompleteTextView>(R.id.search_autoComplete)
        val incomeCount = findViewById<TextView>(R.id.search_income_count_all)
        val expenseCount = findViewById<TextView>(R.id.search_expense_count_all)
        val recyclerView = findViewById<RecyclerView>(R.id.search_resultList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = transactionAdapter

        searchTitle.text = "All"

        btnBack.setOnClickListener {
            finish()
        }

        btnCancel.setOnClickListener {
            finish()
        }

        searchArrange.setOnClickListener {
            searchArrange(searchQuery, searchTitle)
        }

        val dao = AppDatabase.getDatabase(application).transactionDao()
        val factory = TransactionViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]
        viewModel.allTransactions.observe(this) { it ->
            transactions = it

            val contents = transactions.map { it.note }.distinct()

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
                searchQuery = it.toString()
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

    @SuppressLint("MissingInflatedId")
    private fun searchArrange(query: String, searchTitle: TextView) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.item_search_arrange, null)
        bottomSheetDialog.setContentView(view)
        // Dấu tick
        val checkViews = mapOf(
            "All" to view.findViewById<ImageView>(R.id.search_optionTotalCheck),
            "Weekly" to view.findViewById<ImageView>(R.id.search_optionWeeklyCheck),
            "Monthly" to view.findViewById<ImageView>(R.id.search_optionMonthlyCheck),
            "Yearly" to view.findViewById<ImageView>(R.id.search_optionYearlyCheck),
        )
        // Hiển thị tick đúng vị trí
        fun updateCheckMarks(selected: String) {
            searchTitle.text = selected
            checkViews.forEach { (option, imageView) ->
                imageView.visibility = if (option == selected) View.VISIBLE else View.GONE
            }
        }

        updateCheckMarks(selectedOption) // cập nhật ban đầu

        view.findViewById<LinearLayout>(R.id.optionTotalLayout).setOnClickListener {
            selectedOption = "All"
            updateCheckMarks(selectedOption)
            transactionAdapter.filterPeriod = FilterPeriod.All
            transactionAdapter.filter.filter(query)
            transactionAdapter.updateList(transactions)
        }

        view.findViewById<LinearLayout>(R.id.optionWeeklyLayout).setOnClickListener {
            selectedOption = "Weekly"
            updateCheckMarks(selectedOption)
            transactionAdapter.filterPeriod = FilterPeriod.Weekly
            transactionAdapter.filter.filter(query)
            transactionAdapter.updateList(transactions)
        }

        view.findViewById<LinearLayout>(R.id.optionMonthlyLayout).setOnClickListener {
            selectedOption = "Monthly"
            updateCheckMarks(selectedOption)
            transactionAdapter.filterPeriod = FilterPeriod.Monthly
            transactionAdapter.filter.filter(query)
            transactionAdapter.updateList(transactions)
        }

        view.findViewById<LinearLayout>(R.id.optionYearlyLayout).setOnClickListener {
            selectedOption = "Yearly"
            updateCheckMarks(selectedOption)
            transactionAdapter.filterPeriod = FilterPeriod.Yearly
            transactionAdapter.filter.filter(query)
            transactionAdapter.updateList(transactions)
        }

        // Tương tự với Monthly, Annually, Period...

        view.findViewById<TextView>(R.id.search_optionCancel).setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }
}