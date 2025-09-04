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
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.color.MaterialColors
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.databinding.FragmentStatisticAccountBinding
import com.henrystudio.moneymanager.helper.FilterTransactions
import com.henrystudio.moneymanager.model.*
import com.henrystudio.moneymanager.viewmodel.TransactionViewModel
import com.henrystudio.moneymanager.viewmodel.TransactionViewModelFactory
import java.time.LocalDate

class StatisticAccountFragment : Fragment() {
    private var _binding : FragmentStatisticAccountBinding ?= null
    private val binding get() = _binding!!
    private lateinit var noDataText: TextView
    private lateinit var pieChart: PieChart
    private lateinit var recyclerView: RecyclerView
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
    private lateinit var adapter: CategoryStatAdapter
    private var categoryType = CategoryType.EXPENSE

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatisticAccountBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter.onClickListener = { categoryStat ->
            val intent = Intent(requireContext(), StatisticCategoryActivity::class.java)
            intent.putExtra("item_click_statistic_category_name", categoryStat.name)
            intent.putExtra("item_click_statistic_category_type", categoryType)
            intent.putExtra("item_click_statistic_filterOption", filterOptionTemp)
            intent.putExtra("item_click_statistic_keyWord", KeyFilter.Account)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.no_animation)
            true
        }
        viewModel.combinedFilter.observe(viewLifecycleOwner) { (type, list) ->
            categoryType = type
            updateCircleChartByAccount(type, list)
        }

        viewModel.allTransactions.observe(viewLifecycleOwner) { transactionList->
            allTransactions = transactionList
        }

        viewModel.filterOption.observe(viewLifecycleOwner) { filterOption ->
            filterOptionTemp = filterOption
            getListUpdateChart(filterOption)
        }

        viewModel.currentFilterDate.observe(viewLifecycleOwner) { filterDate ->
            viewModel.setFilter(filterOptionTemp.type, filterDate)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun init() {
        noDataText = binding.fragmentStatisticAccountNoDataText
        pieChart = binding.fragmentStatisticAccountPieChart
        recyclerView = binding.fragmentStatisticAccountRecyclerView
        adapter = CategoryStatAdapter(emptyList())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateCircleChartByAccount(statType: CategoryType, list: List<Transaction>) {
        val filtered = when (statType) {
            CategoryType.EXPENSE -> list.filter { !it.isIncome }
            CategoryType.INCOME -> list.filter { it.isIncome }
        }

        // 🟢 Nhóm theo account thay vì category
        val accountTotals = filtered
            .groupBy { it.account ?: "Unknown" } // field accountName cần có trong Transaction
            .map { (account, transactions) ->
                CategoryTotal(
                    categoryName = account,
                    totalAmount = transactions.sumOf { it.amount }
                )
            }

        val totalAmount = accountTotals.sumOf { it.totalAmount }.takeIf { it > 0 } ?: 1f

        val pieEntries = accountTotals.map {
            PieEntry(((it.totalAmount.toFloat() / totalAmount.toFloat()) * 100f), it.categoryName)
        }

        drawPieChart(pieEntries, accountTotals, statType)
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

        val centerTextValue = if (categoryType == CategoryType.EXPENSE) requireContext().getString(R.string.Expense) else requireContext().getString(R.string.Income)
        val color = if (categoryType == CategoryType.EXPENSE) Color.RED else ContextCompat.getColor(requireContext(), R.color.income)

        val spannable = SpannableString(centerTextValue).apply {
            setSpan(
                ForegroundColorSpan(color),
                0, centerTextValue.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        val holeColor = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorSurface, Color.WHITE)

        pieChart.apply {
            setUsePercentValues(true)
            setHoleColor(holeColor)
            isDrawHoleEnabled = true
            setCenterTextSize(16f)
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(12f)
            description.isEnabled = false
            legend.isEnabled = false

            if (entries.isNotEmpty()) {
                data = pieData
                centerText = spannable
                noDataText.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                visibility = View.VISIBLE
                invalidate()
            } else {
                clear()
                noDataText.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
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
        adapter.submitList(statList)
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
        viewModel.setStatisticTransactionFilter(list)
        filteredListTransaction = list
        currentStatType = viewModel.statisticCategoryType.value ?: CategoryType.EXPENSE
        if (filterOption.type == FilterPeriodStatistic.Trend) {
            updateLineChart(currentStatType, list) // Hàm riêng để vẽ biểu đồ đường
        } else {
            updateCircleChartByAccount(currentStatType, list) // Hàm biểu đồ tròn đã có sẵn
        }
    }

    private fun updateLineChart(categoryType: CategoryType, list: List<Transaction>) {}
}