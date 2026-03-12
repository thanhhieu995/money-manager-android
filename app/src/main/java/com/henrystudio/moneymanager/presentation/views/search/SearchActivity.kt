package com.henrystudio.moneymanager.presentation.views.search

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.*
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.viewmodel.TransactionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {
    private val transactionViewModel: TransactionViewModel by viewModels()
    private lateinit var transactionAdapter: TransactionAdapter
    private var selectedOption: FilterPeriodSearch = FilterPeriodSearch.All
    private var keySearch = ""
    private var allSelectedTransactions: List<Transaction> = emptyList()
    private var transactions : List<Transaction> = emptyList()
    private lateinit var searchTitle: TextView
    private lateinit var searchArrange: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var btnCancel: TextView
    private lateinit var searchInput: AutoCompleteTextView
    private lateinit var incomeCount: TextView
    private lateinit var expenseCount: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutEdit: LinearLayout
    private lateinit var layoutControl: ConstraintLayout
    private lateinit var btnCloseLayoutEdit: ImageView
    private lateinit var btnDeleteLayoutEdit: ImageView
    private lateinit var tvSelectedEdit: TextView
    private lateinit var tvTotalAmountEdit: TextView
    private lateinit var tvNoData: TextView

    private var shouldAnimateExit = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        init()
        onBackPressedDispatcher.addCallback(this) {
            finish()
            overridePendingTransition(R.anim.no_animation, R.anim.slide_out_bottom)
        }
        transactionAdapter = TransactionAdapter(emptyList())

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = transactionAdapter

        searchTitle.text = getString(R.string.all)

        btnBack.setOnClickListener {
            shouldAnimateExit = true
            finish()
        }

        btnCancel.setOnClickListener {
            shouldAnimateExit = true
            finish()
        }

        searchArrange.setOnClickListener {
            searchArrange(keySearch)
        }

        transactionAdapter.clickListener = object : TransactionAdapter.OnTransactionClickListener{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onTransactionClick(transaction: Transaction): Boolean {
                if (transactionViewModel.selectionMode.value) {
                    transactionViewModel.toggleTransactionSelection(transaction)
                    transactionAdapter.notifyDataSetChanged()
                } else {
                    Helper.openTransactionDetail(this@SearchActivity, transaction)
                }
                return true
            }
        }

        transactionAdapter.longClickListener = object: TransactionAdapter.OnTransactionLongClickListener{
            override fun onTransactionLongClick(transaction: Transaction): Boolean {
                transactionViewModel.enterSelectionMode()
                transactionViewModel.toggleTransactionSelection(transaction)
                transactionAdapter.notifyDataSetChanged()
                return true
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                transactionViewModel.selectionMode.collect { enable ->
                    layoutEdit.visibility = if (enable) View.VISIBLE else View.GONE
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                transactionViewModel.selectedTransactions.collect { selectedTransactionList ->
                    allSelectedTransactions = selectedTransactionList
                    tvSelectedEdit.text = "${selectedTransactionList.size} selected"
                    val selectedTransactions = transactions.filter { selectedTransactionList.contains(it) }
                    val totalAmount = selectedTransactions.sumOf {
                        if (it.isIncome) it.amount else -it.amount
                    }
                    tvTotalAmountEdit.text = "Total: ${Helper.formatCurrency(totalAmount)}"
                }
            }
        }

        transactionAdapter.isSelected = { transaction ->
            transactionViewModel.selectionMode.value &&
                    transactionViewModel.selectedTransactions.value.contains(transaction)
        }

        btnCloseLayoutEdit.setOnClickListener {
            transactionViewModel.exitSelectionMode()
            transactionAdapter.notifyDataSetChanged()
        }

        btnDeleteLayoutEdit.setOnClickListener {
            if (allSelectedTransactions.isNotEmpty()) {
                transactionViewModel.deleteAll(allSelectedTransactions)
                transactionViewModel.exitSelectionMode()
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                transactionViewModel.allTransactions.collect { transactionList ->
                    transactions = transactionList
                    transactionAdapter.transactions = transactionList
                    transactionAdapter.filter.filter(keySearch)

                    val contents = transactionList.map { it.note }.distinct()
                    val arrayAdapter = ArrayAdapter(
                        this@SearchActivity,
                        android.R.layout.simple_dropdown_item_1line,
                        contents
                    )
                    searchInput.setAdapter(arrayAdapter)
                    // Khi người dùng chọn 1 gợi ý
                    searchInput.setOnItemClickListener { _, _, position, _ ->
                        val selected = arrayAdapter.getItem(position)
                        keySearch = selected.toString()
                        searchInput.setText(selected)
                        searchInput.setSelection(selected?.length ?: 0)
                        transactionAdapter.transactions = transactionList
                        transactionAdapter.filter.filter(selected)
                    }

                    tvNoData.visibility = if(transactionList.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }

        transactionAdapter.setOnFilterResultListener(object :
            TransactionAdapter.OnFilterResultListener {
            override fun onFilterResult(filteredList: List<Transaction>) {
                // TODO: cập nhật UI khác nếu cần
                var totalIncome = 0.0
                var totalExpense = 0.0
                for (tx in filteredList) {
                    if (tx.isIncome) {
                        totalIncome += tx.amount
                    } else {
                        totalExpense += tx.amount
                    }
                }
                incomeCount.text = Helper.formatCurrency(totalIncome)
                expenseCount.text = Helper.formatCurrency(totalExpense)
                tvNoData.visibility = if(filteredList.isEmpty()) View.VISIBLE else View.GONE
                transactionAdapter.updateList(filteredList)
            }
        })

        layoutControl.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                layoutControl.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val height = layoutControl.height
                val params = layoutEdit.layoutParams
                params.height = height
                layoutEdit.layoutParams = params
            }
        })
    }

    override fun onPause() {
        super.onPause()
        if (shouldAnimateExit) {
            overridePendingTransition(R.anim.no_animation, R.anim.slide_out_bottom)
            shouldAnimateExit = false
        }
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
        layoutEdit = findViewById(R.id.search_layout_edit)
        layoutControl = findViewById(R.id.search_layout_function)
        btnCloseLayoutEdit = findViewById(R.id.search_layout_edit_line_one_btn_close)
        tvSelectedEdit = findViewById(R.id.search_layout_edit_line_two_selected_count)
        tvTotalAmountEdit = findViewById(R.id.search_layout_edit_line_two_selected_total)
        btnDeleteLayoutEdit = findViewById(R.id.search_layout_edit_line_one_btn_delete)
        tvNoData = findViewById(R.id.search_no_data)
    }

    private fun searchArrange(query: String) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.item_search_arrange, null)
        bottomSheetDialog.setContentView(view)

        val checkViews = mapOf(
            FilterPeriodSearch.All to view.findViewById<ImageView>(R.id.search_optionTotalCheck),
            FilterPeriodSearch.Weekly to view.findViewById<ImageView>(R.id.search_optionWeeklyCheck),
            FilterPeriodSearch.Monthly to view.findViewById<ImageView>(R.id.search_optionMonthlyCheck),
            FilterPeriodSearch.Yearly to view.findViewById<ImageView>(R.id.search_optionYearlyCheck),
        )

        val optionLayouts = mapOf(
            FilterPeriodSearch.All to R.id.optionTotalLayout,
            FilterPeriodSearch.Weekly to R.id.optionWeeklyLayout,
            FilterPeriodSearch.Monthly to R.id.optionMonthlyLayout,
            FilterPeriodSearch.Yearly to R.id.optionYearlyLayout,
        )

        fun updateCheckMarks(selected: FilterPeriodSearch) {
            searchTitle.text = getString(selected.stringRes)
            checkViews.forEach { (option, imageView) ->
                imageView.visibility = if (option == selected) View.VISIBLE else View.GONE
            }
            bottomSheetDialog.dismiss()
        }

        updateCheckMarks(selectedOption) // cập nhật ban đầu

        optionLayouts.forEach { (filterPeriod, layoutId) ->
            view.findViewById<LinearLayout>(layoutId).setOnClickListener {
                selectedOption = filterPeriod
                updateCheckMarks(filterPeriod)
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
