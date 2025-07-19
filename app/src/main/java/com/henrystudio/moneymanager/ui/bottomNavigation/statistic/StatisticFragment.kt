package com.henrystudio.moneymanager.ui.bottomNavigation.statistic

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.henrystudio.moneymanager.databinding.FragmentStatisticBinding
import com.henrystudio.moneymanager.model.AppDatabase
import com.henrystudio.moneymanager.model.CategoryStat
import com.henrystudio.moneymanager.model.CategoryTotal
import com.henrystudio.moneymanager.model.CategoryType
import com.henrystudio.moneymanager.viewmodel.TransactionViewModel
import com.henrystudio.moneymanager.viewmodel.TransactionViewModelFactory

class StatisticFragment : Fragment() {
    private var _binding: FragmentStatisticBinding? = null
    private val binding get() = _binding!!
    var currentStatType = CategoryType.EXPENSE

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fragmentStatisticToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                currentStatType = when (checkedId) {
                    binding.fragmentStatisticBtnIncome.id -> CategoryType.INCOME
                    else -> CategoryType.EXPENSE
                }
                updateChart(currentStatType)
            }
        }

        viewModel.allTransactions.observe(viewLifecycleOwner) {
            updateChart(currentStatType)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateChart(statType: CategoryType) {
        val list = viewModel.allTransactions.value ?: return

        val filtered = when (statType) {
            CategoryType.EXPENSE -> list.filter { !it.isIncome }
            CategoryType.INCOME -> list.filter { it.isIncome }
        }

        val categoryTotals = filtered
            .groupBy { it.category }
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

        binding.fragmentStatisticPieChart.apply {
            data = pieData
            setUsePercentValues(true)
            isDrawHoleEnabled = true
            centerText = if (categoryType == CategoryType.EXPENSE) "Expense" else "Income"
            setCenterTextSize(16f)
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            description.isEnabled = false
            legend.isEnabled = false
            invalidate()
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
        binding.fragmentStatisticStatsRecyclerView.adapter = adapter
        binding.fragmentStatisticStatsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }
}