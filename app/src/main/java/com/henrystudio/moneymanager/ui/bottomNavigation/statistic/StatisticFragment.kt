package com.henrystudio.moneymanager.ui.bottomNavigation.statistic

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.databinding.FragmentStatisticBinding
import com.henrystudio.moneymanager.helper.FilterTransactions
import com.henrystudio.moneymanager.helper.Helper
import com.henrystudio.moneymanager.helper.Helper.Companion.updateMonthText
import com.henrystudio.moneymanager.model.*
import com.henrystudio.moneymanager.viewmodel.TransactionViewModel
import com.henrystudio.moneymanager.viewmodel.TransactionViewModelFactory
import java.time.LocalDate
import java.util.*

class StatisticFragment : Fragment() {
    private var _binding: FragmentStatisticBinding? = null
    private val binding get() = _binding!!
    private lateinit var monthBack: ImageView
    private lateinit var monthNext: ImageView
    private lateinit var monthText: TextView
    private lateinit var noDataText: TextView
    private lateinit var toggleGroupButton: MaterialButtonToggleGroup
    private lateinit var incomeBtn: MaterialButton
    private lateinit var expenseBtn: MaterialButton
    private var allTransactions: List<Transaction> = emptyList()
    private var filteredListTransaction : List<Transaction> = emptyList()
    private var currentStatType = CategoryType.EXPENSE
    @RequiresApi(Build.VERSION_CODES.O)
    private var filterOptionTemp : FilterOption = FilterOption(FilterPeriodStatistic.Monthly, LocalDate.now())

    private val viewModel : TransactionViewModel by activityViewModels {
        TransactionViewModelFactory(
            AppDatabase.getDatabase(requireActivity().application).transactionDao()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatisticBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

        toggleGroupButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                currentStatType = when (checkedId) {
                    binding.fragmentStatisticBtnIncome.id -> CategoryType.INCOME
                    else -> CategoryType.EXPENSE
                }
                updateCircleChart(currentStatType, filteredListTransaction)
            }
        }

        viewModel.allTransactions.observe(viewLifecycleOwner) { transactionList->
            allTransactions = transactionList
        }

        viewModel.currentFilterDate.observe(viewLifecycleOwner) { filterDate ->
            viewModel.setFilter(filterOptionTemp.type, filterDate)
        }

        viewModel.filterOption.observe(viewLifecycleOwner) { filterOption ->
            filterOptionTemp = filterOption
            getListUpdateChart(filterOption)
        }

        monthBack.setOnClickListener {
            when(filterOptionTemp.type) {
                FilterPeriodStatistic.Monthly -> viewModel.changeMonth(-1)
                FilterPeriodStatistic.Weekly -> viewModel.changeWeek(-1)
                FilterPeriodStatistic.Yearly -> viewModel.changeYear(-1)
                FilterPeriodStatistic.List -> {}
                FilterPeriodStatistic.Trend -> {}
            }
            updateMonthText(filterOptionTemp, monthText)
        }

        monthNext.setOnClickListener {
            when(filterOptionTemp.type) {
                FilterPeriodStatistic.Monthly -> viewModel.changeMonth(1)
                FilterPeriodStatistic.Weekly -> viewModel.changeWeek(1)
                FilterPeriodStatistic.Yearly -> viewModel.changeYear(1)
                FilterPeriodStatistic.List -> {}
                FilterPeriodStatistic.Trend -> {}
            }
            updateMonthText(filterOptionTemp, monthText)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateLineChart(categoryType: CategoryType, list: List<Transaction>) {}

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateCircleChart(statType: CategoryType, list: List<Transaction>) {
        val filtered = when (statType) {
            CategoryType.EXPENSE -> list.filter { !it.isIncome }
            CategoryType.INCOME -> list.filter { it.isIncome }
        }

        val categoryTotals = filtered
            .groupBy { it.categoryParentName }
            .map { (category, transactions) ->
                CategoryTotal(
                    categoryName = category,
                    totalAmount = transactions.sumOf { it.amount }
                )
            }

        val totalAmount = categoryTotals.sumOf { it.totalAmount }.takeIf { it > 0 } ?: 1f

        val pieEntries = categoryTotals.map {
            PieEntry(((it.totalAmount.toFloat() / totalAmount.toFloat()) * 100f), it.categoryName)
        }

        drawPieChart(pieEntries, categoryTotals, statType)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun drawPieChart(entries: List<PieEntry>, categoryTotals: List<CategoryTotal>, categoryType: CategoryType) {
        val dataSet = PieDataSet(entries, "")
        val colors = listOf(
            Color.parseColor("#FF6F61"),
            Color.parseColor("#6A1B9A"),
            Color.parseColor("#039BE5"),
            Color.parseColor("#43A047"),
            Color.parseColor("#FFB74D"),
            Color.parseColor("#26A69A")
        )
        dataSet.colors = colors
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 14f

        val pieData = PieData(dataSet)
        pieData.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${Math.round(value)}%"
            }
        })

        val centerTextValue = if (categoryType == CategoryType.EXPENSE) "Expense" else "Income"
        val color = if (categoryType == CategoryType.EXPENSE) Color.RED else ContextCompat.getColor(requireContext(), R.color.income)

        val spannable = SpannableString(centerTextValue).apply {
            setSpan(
                ForegroundColorSpan(color),
                0, centerTextValue.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        binding.fragmentStatisticPieChart.apply {
            setUsePercentValues(true)
            isDrawHoleEnabled = true
            setCenterTextSize(16f)
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(12f)
            description.isEnabled = false
            legend.isEnabled = false

            if (entries.isNotEmpty()) {
                data = pieData
                centerText = spannable
                binding.fragmentStatisticNoDataText.visibility = View.GONE
                binding.fragmentStatisticStatsRecyclerView.visibility = View.VISIBLE
                visibility = View.VISIBLE
                invalidate()
            } else {
                clear()
                binding.fragmentStatisticNoDataText.visibility = View.VISIBLE
                binding.fragmentStatisticStatsRecyclerView.visibility = View.GONE
                visibility = View.INVISIBLE
            }
        }

        val total = categoryTotals.sumOf { it.totalAmount }

        val statList = categoryTotals.mapIndexed { index, cat ->
            CategoryStat(
                name = cat.categoryName ?: "Unknown",
                percent = (cat.totalAmount / total * 100f).toFloat(),
                amount = cat.totalAmount.toFloat(),
                color = colors[index % colors.size]
            )
        }

        val adapter = CategoryStatAdapter(statList)
        adapter.onClickListener = { categoryStat ->
            val intent = Intent(requireContext(), StatisticCategoryActivity::class.java)
            intent.putExtra("item_click_statistic_category_name", categoryStat.name)
            intent.putExtra("item_click_statistic_category_amount", categoryStat.amount)
            intent.putExtra("item_click_statistic_category_type", categoryType)
            intent.putExtra("item_click_statistic_filterOption", filterOptionTemp)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.no_animation)
            true
        }
        binding.fragmentStatisticStatsRecyclerView.adapter = adapter
        binding.fragmentStatisticStatsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    fun init() {
        monthNext = binding.fragmentStatisticMonthNext
        monthBack = binding.fragmentStatisticMonthBack
        monthText = binding.fragmentStatisticMonthText
        noDataText = binding.fragmentStatisticNoDataText
        toggleGroupButton = binding.fragmentStatisticToggleGroup
        incomeBtn = binding.fragmentStatisticBtnIncome
        expenseBtn = binding.fragmentStatisticBtnExpense
    }

    private fun updateTextButton(filteredList: List<Transaction>) {
        val incomeList = filteredList.filter { it.isIncome }
        val expenseList = filteredList.filter { !it.isIncome }

        val totalIncome = incomeList.sumOf { it.amount }
        val totalExpense = expenseList.sumOf { it.amount }
        incomeBtn.text = "Income " + Helper.formatCurrency(totalIncome)
        expenseBtn.text = "Exp " + Helper.formatCurrency(totalExpense)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getListUpdateChart(filterOption: FilterOption) {
        val list = when (filterOption.type) {
            FilterPeriodStatistic.Monthly -> FilterTransactions.filterTransactionsByMonth(allTransactions, filterOption.date)
            FilterPeriodStatistic.Weekly -> FilterTransactions.filterTransactionsByWeek(allTransactions, filterOption.date)
            FilterPeriodStatistic.Yearly -> FilterTransactions.filterTransactionsByYear(allTransactions, filterOption.date)
            FilterPeriodStatistic.List -> emptyList()
            FilterPeriodStatistic.Trend -> emptyList()
        }
        updateMonthText(filterOption, monthText)
        updateTextButton(list)
        filteredListTransaction = list
        if (filterOption.type == FilterPeriodStatistic.Trend) {
            updateLineChart(currentStatType, list) // Hàm riêng để vẽ biểu đồ đường
        } else {
            updateCircleChart(currentStatType, list) // Hàm biểu đồ tròn đã có sẵn
        }
    }
}