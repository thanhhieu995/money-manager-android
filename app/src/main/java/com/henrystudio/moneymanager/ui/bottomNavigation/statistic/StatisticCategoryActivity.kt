package com.henrystudio.moneymanager.ui.bottomNavigation.statistic

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.helper.Helper
import com.henrystudio.moneymanager.model.AppDatabase
import com.henrystudio.moneymanager.model.Transaction
import com.henrystudio.moneymanager.viewmodel.TransactionViewModel
import com.henrystudio.moneymanager.viewmodel.TransactionViewModelFactory

class StatisticCategoryActivity : AppCompatActivity() {
    private lateinit var toolbar: MaterialToolbar
    private lateinit var btnBack: ImageButton
    private lateinit var title: TextView
    private lateinit var container: FrameLayout
    private lateinit var statisticCategoryFragment: StatisticCategoryFragment
    private lateinit var layoutEdit: LinearLayout
    private lateinit var btnEditClose: ImageView
    private lateinit var btnEditDelete: ImageView
    private lateinit var selectedCount: TextView
    private lateinit var selectedTotal: TextView

    private var selectedTransactionList: List<Transaction> = emptyList()
    private lateinit var viewModel: TransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistic_category)

        val dao = AppDatabase.getDatabase(application).transactionDao()
        val factory = TransactionViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]
        init()

        btnBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right)
        }

        val name = intent.getStringExtra("item_click_statistic_category_name")
        val categoryType = intent.getSerializableExtra("item_click_statistic_category_type")
        val filterOption = intent.getSerializableExtra("item_click_statistic_filterOption")
        title.text = name
        val bundle =  Bundle().apply {
            putSerializable("item_click_statistic_category_name", name)
            putSerializable("item_click_statistic_category_type", categoryType)
            putSerializable("item_click_statistic_filterOption", filterOption)
        }
        statisticCategoryFragment.apply {
            arguments = bundle
        }
        supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(R.id.activity_statistic_category_container, statisticCategoryFragment)
            .commit()

        // observe selectionMode && id
        viewModel.selectionMode.observe(this) { enabled ->
            layoutEdit.visibility = if (enabled) View.VISIBLE else View.GONE
            toolbar.visibility = if (enabled) View.GONE else View.VISIBLE
        }

        viewModel.selectedTransactions.observe(this) { selectedTransactions ->
            selectedTransactionList = selectedTransactions
            // Cập nhật số lượng và tổng tiền khi người dùng chọn giao dịch
            selectedCount.text =
                "${selectedTransactions.size} selected"
            val totalAmount = selectedTransactions.sumOf {
                if(it.isIncome) it.amount else -it.amount
            }
            selectedTotal.text =
                "Total: ${Helper.formatCurrency(totalAmount)}"
        }

        btnEditClose.setOnClickListener {
            viewModel.exitSelectionMode()
        }

        btnEditDelete.setOnClickListener {
            if (selectedTransactionList.isNotEmpty()) {
                viewModel.deleteAll(selectedTransactionList)
                viewModel.exitSelectionMode()
            }
        }
    }

    private fun init() {
        toolbar = findViewById(R.id.activity_statistic_category_toolbar)
        btnBack = findViewById(R.id.activity_statistic_category_backButton)
        title = findViewById(R.id.activity_statistic_category_titleCurrent)
        container = findViewById(R.id.activity_statistic_category_container)
        layoutEdit = findViewById(R.id.activity_statistic_category_layout_edit)
        btnEditClose = findViewById(R.id.activity_statistic_category_layout_edit_line_one_btn_close)
        btnEditDelete = findViewById(R.id.activity_statistic_category_layout_edit_line_one_btn_delete)
        selectedCount = findViewById(R.id.activity_statistic_category_layout_edit_line_two_selected_count)
        selectedTotal = findViewById(R.id.activity_statistic_category_layout_edit_line_two_selected_total)
        statisticCategoryFragment = StatisticCategoryFragment()
    }
}