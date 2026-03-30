package com.henrystudio.moneymanager.presentation.views.search

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.henrystudio.moneymanager.presentation.viewmodel.SearchViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.SharedTransactionViewModel
import com.henrystudio.moneymanager.presentation.views.daily.DataTransactionGroupState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {
    private val sharedViewModel: SharedTransactionViewModel by viewModels()
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var transactionAdapter: TransactionAdapter
    private var allSelectedTransactions: List<Transaction> = emptyList()
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
            searchArrange()
        }

        transactionAdapter.clickListener = object : TransactionAdapter.OnTransactionClickListener{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onTransactionClick(transaction: Transaction): Boolean {
                if (sharedViewModel.selectionMode.value) {
                    sharedViewModel.toggleTransactionSelection(transaction)
                    transactionAdapter.notifyDataSetChanged()
                } else {
                    Helper.openTransactionDetail(this@SearchActivity, transaction)
                }
                return true
            }
        }

        transactionAdapter.longClickListener = object: TransactionAdapter.OnTransactionLongClickListener{
            override fun onTransactionLongClick(transaction: Transaction): Boolean {
                sharedViewModel.enterSelectionMode()
                sharedViewModel.toggleTransactionSelection(transaction)
                transactionAdapter.notifyDataSetChanged()
                return true
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.selectionMode.collect { enable ->
                    layoutEdit.visibility = if (enable) View.VISIBLE else View.GONE
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.selectedTransactions.collect { selectedTransactionList ->
                    allSelectedTransactions = selectedTransactionList
                    viewModel.updateSelected(selectedTransactionList)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    tvSelectedEdit.text = "${state.selectedCount} selected"
                    tvTotalAmountEdit.text = state.selectedTotal
                }
            }
        }

        transactionAdapter.isSelected = { transaction ->
            sharedViewModel.selectionMode.value &&
                    sharedViewModel.selectedTransactions.value.contains(transaction)
        }

        btnCloseLayoutEdit.setOnClickListener {
            sharedViewModel.exitSelectionMode()
            transactionAdapter.notifyDataSetChanged()
        }

        btnDeleteLayoutEdit.setOnClickListener {
            if (allSelectedTransactions.isNotEmpty()) {
                sharedViewModel.deleteAll(allSelectedTransactions)
                sharedViewModel.exitSelectionMode()
            }
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateQuery(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.allTransactionsState.collect { state ->
                    viewModel.updateTransactions(
                        if (state is DataTransactionGroupState.Success) state.data else emptyList()
                    )
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    transactionAdapter.updateList(state.filteredTransactions)
                    incomeCount.text = state.incomeTotal
                    expenseCount.text = state.expenseTotal
                    tvNoData.visibility = if (state.isEmpty) View.VISIBLE else View.GONE
                    val arrayAdapter = ArrayAdapter(
                        this@SearchActivity,
                        android.R.layout.simple_dropdown_item_1line,
                        state.distinctNotes
                    )
                    searchInput.setAdapter(arrayAdapter)
                    searchTitle.text = getString(state.filterPeriod.stringRes)
                }
            }
        }

        searchInput.setOnItemClickListener { _, _, position, _ ->
            val adapter = searchInput.adapter as? ArrayAdapter<*>
            val selected = adapter?.getItem(position)?.toString() ?: ""
            searchInput.setText(selected)
            searchInput.setSelection(selected.length)
            viewModel.updateQuery(selected)
        }

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

    private fun searchArrange() {
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
            viewModel.updateFilterPeriod(selected)
            checkViews.forEach { (option, imageView) ->
                imageView.visibility = if (option == selected) View.VISIBLE else View.GONE
            }
            bottomSheetDialog.dismiss()
        }

        viewModel.uiState.value.filterPeriod.let { current ->
            checkViews.forEach { (option, imageView) ->
                imageView.visibility = if (option == current) View.VISIBLE else View.GONE
            }
        }

        optionLayouts.forEach { (filterPeriod, layoutId) ->
            view.findViewById<LinearLayout>(layoutId).setOnClickListener {
                updateCheckMarks(filterPeriod)
            }
        }

        view.findViewById<TextView>(R.id.search_optionCancel).setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }
}
