package com.henrystudio.moneymanager.ui.search

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.*
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.application.BaseActivity
import com.henrystudio.moneymanager.helper.Helper
import com.henrystudio.moneymanager.model.AppDatabase
import com.henrystudio.moneymanager.model.Transaction
import com.henrystudio.moneymanager.ui.setting.LanguagePref
import com.henrystudio.moneymanager.viewmodel.TransactionViewModel
import com.henrystudio.moneymanager.viewmodel.TransactionViewModelFactory

class SearchActivity : BaseActivity() {
    private lateinit var viewModel: TransactionViewModel
    private lateinit var transactionAdapter: TransactionAdapter
    private var selectedOption: String = "All" // default
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
        // Lấy ngôn ngữ đã lưu
        val lang = LanguagePref.getLanguage(this)
        if (lang != null) {
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(lang)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }

        init()
        onBackPressedDispatcher.addCallback(this) {
            finish()
            overridePendingTransition(R.anim.no_animation, R.anim.slide_out_bottom)
        }
        transactionAdapter = TransactionAdapter(emptyList())

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = transactionAdapter

        val dao = AppDatabase.getDatabase(application).transactionDao()
        val factory = TransactionViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]

        searchTitle.text = "All"

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
                if (viewModel.selectionMode.value == true) {
                    viewModel.toggleTransactionSelection(transaction)
                    transactionAdapter.notifyDataSetChanged()
                } else {
                    Helper.openTransactionDetail(this@SearchActivity, transaction)
                }
                return true
            }
        }

        transactionAdapter.longClickListener = object: TransactionAdapter.OnTransactionLongClickListener{
            override fun onTransactionLongClick(transaction: Transaction): Boolean {
                viewModel.enterSelectionMode()
                viewModel.toggleTransactionSelection(transaction)
                transactionAdapter.notifyDataSetChanged()
                return true
            }
        }

        viewModel.selectionMode.observe(this) { enable ->
            layoutEdit.visibility = if (enable) View.VISIBLE else View.GONE
        }

        viewModel.selectedTransactions.observe(this) { selectedTransactionList ->
            allSelectedTransactions = selectedTransactionList
            tvSelectedEdit.text = "${selectedTransactionList.size} selected"
            val selectedTransactions = transactions?.filter { selectedTransactionList.contains(it) } ?: emptyList()
            val totalAmount = selectedTransactions.sumOf {
                if (it.isIncome) it.amount else -it.amount
            }
            tvTotalAmountEdit.text = "Total: ${Helper.formatCurrency(totalAmount)}"
        }

        transactionAdapter.isSelected = {transaction ->
            viewModel.selectionMode.value == true &&
                    viewModel.selectedTransactions.value?.contains(transaction) == true
        }

        btnCloseLayoutEdit.setOnClickListener {
            viewModel.exitSelectionMode()
            transactionAdapter.notifyDataSetChanged()
        }

        btnDeleteLayoutEdit.setOnClickListener {
            if (allSelectedTransactions.isNotEmpty()) {
                viewModel.deleteAll(allSelectedTransactions)
                viewModel.exitSelectionMode()
            }
        }

        viewModel.allTransactions.observe(this) { transactionList ->
            transactions = transactionList
            transactionAdapter.transactions = transactionList
            transactionAdapter.filter.filter(keySearch)

            val contents = transactionList.map { it.note }.distinct()
            val arrayAdapter = ArrayAdapter(
                this,
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
            "All" to view.findViewById<ImageView>(R.id.search_optionTotalCheck),
            "Weekly" to view.findViewById<ImageView>(R.id.search_optionWeeklyCheck),
            "Monthly" to view.findViewById<ImageView>(R.id.search_optionMonthlyCheck),
            "Yearly" to view.findViewById<ImageView>(R.id.search_optionYearlyCheck),
        )

        val optionConfigs = listOf(
            Triple("All", R.id.optionTotalLayout, FilterPeriodSearch.All),
            Triple("Weekly", R.id.optionWeeklyLayout, FilterPeriodSearch.Weekly),
            Triple("Monthly", R.id.optionMonthlyLayout, FilterPeriodSearch.Monthly),
            Triple("Yearly", R.id.optionYearlyLayout, FilterPeriodSearch.Yearly)
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