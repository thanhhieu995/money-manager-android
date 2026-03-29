package com.henrystudio.moneymanager.presentation.views.bottomNavigation.statistic

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.appbar.MaterialToolbar
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.viewmodel.SharedTransactionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.ArrayDeque

@AndroidEntryPoint
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
    private val sharedViewModel: SharedTransactionViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistic_category)

        init()
        btnBack.setOnClickListener {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
                val previousTitle = if (titleStack.isNotEmpty()) titleStack.removeLast() else ""
                animateBackTitleTransition(previousTitle)
            } else {
                finish()
            }
            overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right)
        }

        val name = intent.getStringExtra("item_click_statistic_category_name")
        val transactionType = intent.getSerializableExtra("item_click_statistic_category_type")
        val filterOption = intent.getSerializableExtra("item_click_statistic_filterOption")
        val keyFilter = intent.getSerializableExtra("item_click_statistic_keyWord")
        if (name != null) {
            updateTransactionTitle(name)
        }
        val bundle =  Bundle().apply {
            putSerializable("item_click_statistic_category_name", name)
            putSerializable("item_click_statistic_category_type", transactionType)
            putSerializable("item_click_statistic_filterOption", filterOption)
            putSerializable("item_click_statistic_keyWord", keyFilter)
        }
        statisticCategoryFragment.apply {
            arguments = bundle
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.activity_statistic_category_container, statisticCategoryFragment)
            .commit()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.selectionMode.collect { enabled ->
                    layoutEdit.visibility = if (enabled) View.VISIBLE else View.GONE
                    toolbar.visibility = if (enabled) View.GONE else View.VISIBLE
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.selectedTransactions.collect { selectedTransactions ->
                    selectedTransactionList = selectedTransactions
                    selectedCount.text =
                        "${selectedTransactions.size} ${getString(R.string.selected)}"
                    val totalAmount = selectedTransactions.sumOf {
                        if (it.isIncome) it.amount else -it.amount
                    }
                    selectedTotal.text =
                        "${getString(R.string.Total)} : ${Helper.formatCurrency(totalAmount)}"
                }
            }
        }

        btnEditClose.setOnClickListener {
            sharedViewModel.exitSelectionMode()
        }

        btnEditDelete.setOnClickListener {
            if (selectedTransactionList.isNotEmpty()) {
                sharedViewModel.deleteAll(selectedTransactionList)
                sharedViewModel.exitSelectionMode()
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

            titleView.translationX = screenWidth.toFloat()

            titleView.animate()
                .translationX(offsetToCenter)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction {
                    titleCurrent.text = newText
                    titleCurrent.translationX = 0f
                    titleView.visibility = View.GONE
                    titleView.translationX = 0f
                }
                .start()
        }
    }

    private fun animateBackTitleTransition(previousTitle: String) {
        val toolbarWidth = toolbar.width.toFloat()

        titleCurrent.animate()
            .translationX(toolbarWidth)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                titleCurrent.visibility = View.GONE
                titleCurrent.translationX = 0f
            }
            .start()

        titleIncoming.text = previousTitle
        titleIncoming.visibility = View.VISIBLE
        titleIncoming.translationX = -toolbarWidth

        titleIncoming.animate()
            .translationX(0f)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                titleCurrent.text = previousTitle
                titleCurrent.translationX = 0f
                titleCurrent.visibility = View.VISIBLE
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
