package com.henrystudio.moneymanager.ui.bottomNavigation.statistic

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.core.database.AppDatabase
import com.henrystudio.moneymanager.features.transaction.data.local.Transaction
import com.henrystudio.moneymanager.repository.TransactionRepository
import com.henrystudio.moneymanager.viewmodel.TransactionViewModel
import com.henrystudio.moneymanager.viewmodel.TransactionViewModelFactory

class StatisticCategoryActivity : AppCompatActivity() {
    private lateinit var toolbar: MaterialToolbar
    private lateinit var btnBack: ImageButton
    lateinit var titleCurrent: TextView
    lateinit var titleIncoming: TextView
    private lateinit var container: FrameLayout
    private lateinit var statisticCategoryFragment: StatisticCategoryFragment
    private lateinit var layoutEdit: LinearLayout
    private lateinit var btnEditClose: ImageView
    private lateinit var btnEditDelete: ImageView
    private lateinit var selectedCount: TextView
    private lateinit var selectedTotal: TextView
    val titleStack = ArrayDeque<String>()

    private var selectedTransactionList: List<Transaction> = emptyList()
    private lateinit var viewModel: TransactionViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistic_category)

        val database = AppDatabase.getDatabase(application)
        val transactionRepository = TransactionRepository(database.transactionDao())
        val transactionFactory = TransactionViewModelFactory(transactionRepository)
        viewModel = ViewModelProvider(this, transactionFactory)[TransactionViewModel::class.java]
        init()

        btnBack.setOnClickListener {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
                val previousTitle = if (titleStack.isNotEmpty()) titleStack.removeLast() else ""
                // Áp dụng animation cho title khi quay lại
                animateBackTitleTransition(previousTitle)
            } else {
                finish()
            }
            overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right)
        }

        val name = intent.getStringExtra("item_click_statistic_category_name")
        val categoryType = intent.getSerializableExtra("item_click_statistic_category_type")
        val filterOption = intent.getSerializableExtra("item_click_statistic_filterOption")
        val keyFilter = intent.getSerializableExtra("item_click_statistic_keyWord")
        if (name != null) {
            updateTransactionTitle(name)
        }
        val bundle =  Bundle().apply {
            putSerializable("item_click_statistic_category_name", name)
            putSerializable("item_click_statistic_category_type", categoryType)
            putSerializable("item_click_statistic_filterOption", filterOption)
            putSerializable("item_click_statistic_keyWord", keyFilter)
        }
        statisticCategoryFragment.apply {
            arguments = bundle
        }
        supportFragmentManager.beginTransaction()
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
                "${selectedTransactions.size} ${getString(R.string.selected)}"
            val totalAmount = selectedTransactions.sumOf {
                if(it.isIncome) it.amount else -it.amount
            }
            selectedTotal.text =
                "${getString(R.string.Total)} : ${Helper.formatCurrency(totalAmount)}"
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
        titleCurrent = findViewById(R.id.activity_statistic_category_titleCurrent)
        titleIncoming = findViewById(R.id.activity_statistic_category_titleIncoming)
        btnBack = findViewById(R.id.activity_statistic_category_backButton)
        container = findViewById(R.id.activity_statistic_category_container)
        layoutEdit = findViewById(R.id.activity_statistic_category_layout_edit)
        btnEditClose = findViewById(R.id.activity_statistic_category_layout_edit_line_one_btn_close)
        btnEditDelete = findViewById(R.id.activity_statistic_category_layout_edit_line_one_btn_delete)
        selectedCount = findViewById(R.id.activity_statistic_category_layout_edit_line_two_selected_count)
        selectedTotal = findViewById(R.id.activity_statistic_category_layout_edit_line_two_selected_total)
        statisticCategoryFragment = StatisticCategoryFragment()
    }

    private fun updateTransactionTitle(title: String) {
        titleCurrent.text = title
    }

    fun animateIncomingTitleToCenter(titleView: TextView, newText: String) {
        titleView.text = newText
        titleView.visibility = View.VISIBLE
        titleView.alpha = 1f

        titleView.post {
            val screenWidth = toolbar.width
            val centerX = screenWidth / 2f
            val textCenterX = titleView.left + titleView.width / 2f
            val offsetToCenter = centerX - textCenterX

            // Bắt đầu từ bên phải ngoài màn hình
            titleView.translationX = screenWidth.toFloat()

            // Animate vào giữa
            titleView.animate()
                .translationX(offsetToCenter)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction {
                    // Sau khi vào giữa, cập nhật lại titleCurrent
                    titleCurrent.text = newText
                    titleCurrent.translationX = 0f

                    // Ẩn và reset titleIncoming
                    titleView.visibility = View.GONE
                    titleView.translationX = 0f
                }
                .start()
        }
    }

    private fun animateBackTitleTransition(previousTitle: String) {
        val toolbarWidth = toolbar.width.toFloat()

        // 1. Animate titleCurrent trượt sang phải và ẩn
        titleCurrent.animate()
            .translationX(toolbarWidth) // trượt ra bên phải
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                titleCurrent.visibility = View.GONE
                titleCurrent.translationX = 0f // reset để dùng lại
            }
            .start()

        // 2. titleIncoming xuất hiện từ bên trái
        titleIncoming.text = previousTitle
        titleIncoming.visibility = View.VISIBLE
        titleIncoming.translationX = -toolbarWidth // bắt đầu từ ngoài trái

        // 3. Animate titleIncoming vào giữa
        titleIncoming.animate()
            .translationX(0f)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                // Khi animation kết thúc: cập nhật titleCurrent
                titleCurrent.text = previousTitle
                titleCurrent.translationX = 0f
                titleCurrent.visibility = View.VISIBLE

                // Reset titleIncoming
                titleIncoming.visibility = View.GONE
                titleIncoming.translationX = 0f
            }
            .start()
    }

    fun animateTitleToLeftOfIcon(titleView: TextView) {
        titleView.post {
            val iconStart = btnBack.left
            val titleCenterX = titleView.left + titleView.width / 2f
            val iconCenterX = iconStart + btnBack.width / 2f
            val targetTranslationX = iconCenterX - titleCenterX

            titleView.animate()
                .translationX(targetTranslationX)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }
    }
}