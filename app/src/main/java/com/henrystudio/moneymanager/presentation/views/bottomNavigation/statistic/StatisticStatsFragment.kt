package com.henrystudio.moneymanager.presentation.views.bottomNavigation.statistic

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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.color.MaterialColors
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.data.local.AppDatabase
import com.henrystudio.moneymanager.databinding.FragmentStatisticStatsBinding
import com.henrystudio.moneymanager.core.util.FilterTransactions
import com.henrystudio.moneymanager.data.model.CategoryType
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.data.repository.TransactionRepositoryImpl
import com.henrystudio.moneymanager.presentation.model.CategoryStat
import com.henrystudio.moneymanager.presentation.model.CategoryTotal
import com.henrystudio.moneymanager.presentation.model.FilterOption
import com.henrystudio.moneymanager.presentation.model.FilterPeriodStatistic
import com.henrystudio.moneymanager.presentation.model.KeyFilter
import com.henrystudio.moneymanager.presentation.viewmodel.TransactionViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.TransactionViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate

@AndroidEntryPoint
class StatisticStatsFragment : Fragment() {
    private var _binding: FragmentStatisticStatsBinding? = null
    private val binding get() = _binding!!
    private lateinit var noDataText: TextView
    private var allTransactions: List<Transaction> = emptyList()
    private var filteredListTransaction : List<Transaction> = emptyList()
    private var currentStatType = CategoryType.EXPENSE
    @RequiresApi(Build.VERSION_CODES.O)
    private var filterOptionTemp : FilterOption =
        FilterOption(FilterPeriodStatistic.Monthly, LocalDate.now())

    private val transactionViewModel : TransactionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatisticStatsBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    transactionViewModel.statisticCategoryType.collect { type ->
                        updateCircleChart(type, filteredListTransaction)
                    }
                }
                launch { transactionViewModel.allTransactions.collect { transactionList->
                    allTransactions = transactionList
                    }
                }

                launch { transactionViewModel.filterOption.collect { filterOption ->
                    filterOptionTemp = filterOption
                    getListUpdateChart(filterOption)
                    }
                }

                launch {
                    transactionViewModel.currentFilterDate.collect { filterDate ->
                        transactionViewModel.setFilter(filterOptionTemp.type, filterDate)
                    }
                }
            }
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

        binding.fragmentStatisticPieChart.apply {
            setUsePercentValues(true)
            isDrawHoleEnabled = true
            setCenterTextSize(16f)
            setHoleColor(holeColor)
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
            intent.putExtra("item_click_statistic_category_type", categoryType)
            intent.putExtra("item_click_statistic_filterOption", filterOptionTemp)
            intent.putExtra("item_click_statistic_keyWord", KeyFilter.CategoryParent)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.no_animation)
            true
        }
        binding.fragmentStatisticStatsRecyclerView.adapter = adapter
        binding.fragmentStatisticStatsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    fun init() {
        noDataText = binding.fragmentStatisticNoDataText
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
        transactionViewModel.setStatisticTransactionFilter(list)
        filteredListTransaction = list
        currentStatType = transactionViewModel.statisticCategoryType.value ?: CategoryType.EXPENSE
        if (filterOption.type == FilterPeriodStatistic.Trend) {
            updateLineChart(currentStatType, list) // Hàm riêng để vẽ biểu đồ đường
        } else {
            updateCircleChart(currentStatType, list) // Hàm biểu đồ tròn đã có sẵn
        }
    }
}