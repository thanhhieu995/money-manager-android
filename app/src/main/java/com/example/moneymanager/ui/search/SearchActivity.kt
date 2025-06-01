package com.example.moneymanager.ui.search

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanager.R
import com.example.moneymanager.helper.Helper
import com.example.moneymanager.model.AppDatabase
import com.example.moneymanager.model.Transaction
import com.example.moneymanager.viewmodel.TransactionViewModel
import com.example.moneymanager.viewmodel.TransactionViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog

class SearchActivity : AppCompatActivity() {
    private lateinit var viewModel: TransactionViewModel
    private var transactions: List<Transaction> = listOf()
    private lateinit var transactionAdapter : TransactionAdapter
    private var selectedOption: String = "All" // default
    var searchQuery = ""

    private lateinit var searchTitle: TextView
    private lateinit var searchArrange: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var btnCancel: TextView
    private lateinit var searchInput: AutoCompleteTextView
    private lateinit var incomeCount: TextView
    private lateinit var expenseCount: TextView
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        init()

        transactionAdapter = TransactionAdapter(transactions)

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
            searchArrange(searchQuery)
        }

        transactionAdapter.clickListener = object : TransactionAdapter.OnTransactionClickListener{
            override fun onTransactionClick(transaction: Transaction): Boolean {
                Helper.openTransactionDetail(this@SearchActivity, transaction)
                return true
            }
        }

        val dao = AppDatabase.getDatabase(application).transactionDao()
        val factory = TransactionViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]
        viewModel.allTransactions.observe(this) { it ->
            transactions = it

            val contents = transactions.map { it.note }.distinct()

            val arrayAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                contents
            )
            searchInput.setAdapter(arrayAdapter)

            // Khi người dùng chọn 1 gợi ý
            searchInput.setOnItemClickListener { _, _, position, _ ->
                val selected = arrayAdapter.getItem(position)
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

        transactionAdapter.setOnFilterResultListener(object :
            TransactionAdapter.OnFilterResultListener {
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

                incomeCount.text = Helper.formatCurrency(totalIncome)
                expenseCount.text = Helper.formatCurrency(totalExpense)
            }
        })
    }

    private fun init() {
        searchTitle = findViewById(R.id.search_title)
        searchArrange = findViewById(R.id.search_filter)
        btnBack = findViewById(R.id.search_back)
        btnCancel = findViewById(R.id.search_btnCancel)
        searchInput = findViewById(R.id.search_autoComplete)
        incomeCount = findViewById(R.id.search_income_count_all)
        expenseCount = findViewById(R.id.search_expense_count_all)
        recyclerView = findViewById(R.id.search_resultList)
    }

    private fun searchArrange(query: String) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.item_search_arrange, null)
        bottomSheetDialog.setContentView(view)

        val checkViews = mapOf(
            "All" to view.findViewById<ImageView>(R.id.search_optionTotalCheck),
            "Weekly" to view.findViewById<ImageView>(R.id.search_optionWeeklyCheck),
            "Monthly" to view.findViewById<ImageView>(R.id.search_optionMonthlyCheck),
            "Yearly" to view.findViewById<ImageView>(R.id.search_optionYearlyCheck),
        )

        val optionConfigs = listOf(
            Triple("All", R.id.optionTotalLayout, FilterPeriod.All),
            Triple("Weekly", R.id.optionWeeklyLayout, FilterPeriod.Weekly),
            Triple("Monthly", R.id.optionMonthlyLayout, FilterPeriod.Monthly),
            Triple("Yearly", R.id.optionYearlyLayout, FilterPeriod.Yearly)
        )

        fun updateCheckMarks(selected: String) {
            searchTitle.text = selected
            checkViews.forEach { (option, imageView) ->
                imageView.visibility = if (option == selected) View.VISIBLE else View.GONE
            }
        }

        updateCheckMarks(selectedOption) // cập nhật ban đầu

        optionConfigs.forEach { (optionName, layoutId, filterPeriod) ->
            view.findViewById<LinearLayout>(layoutId).setOnClickListener {
                selectedOption = optionName
                updateCheckMarks(optionName)
                transactionAdapter.filterPeriod = filterPeriod
                transactionAdapter.filter.filter(query)
                transactionAdapter.updateList(transactions)
            }
        }

        view.findViewById<TextView>(R.id.search_optionCancel).setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }
}