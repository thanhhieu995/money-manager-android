package com.henrystudio.moneymanager.ui.bottomNavigation

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.henrystudio.moneymanager.databinding.FragmentStatisticBinding
import com.henrystudio.moneymanager.model.AppDatabase
import com.henrystudio.moneymanager.model.CategoryTotal
import com.henrystudio.moneymanager.viewmodel.TransactionViewModel
import com.henrystudio.moneymanager.viewmodel.TransactionViewModelFactory

class StatisticFragment : Fragment() {
    private var _binding: FragmentStatisticBinding? = null
    private val binding get() = _binding!!
    private var transactionViewModel: TransactionViewModel? = null
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

        val dao = AppDatabase.getDatabase(requireActivity().application).transactionDao()
        val factory = TransactionViewModelFactory(dao)
        transactionViewModel = ViewModelProvider(requireActivity(), factory)[TransactionViewModel::class.java]

        transactionViewModel!!.allTransactions.observe(viewLifecycleOwner) { list ->
            val expenseTransactions = list.filter { !it.isIncome } // chỉ lấy chi tiêu

            // Gom nhóm theo categoryName
            val categoryTotals = expenseTransactions
                .groupBy { it.category }
                .map { (category, transactions) ->
                    CategoryTotal(
                        categoryName = category,
                        totalAmount = transactions.sumOf { it.amount }
                    )
                }

            // Tổng tất cả chi tiêu
            val totalExpenses = categoryTotals.sumOf { it.totalAmount }

            // Chuyển thành dữ liệu cho PieChart (MPAndroidChart)
            val pieEntries = categoryTotals.map {
                PieEntry(((it.totalAmount / totalExpenses) * 100f).toFloat(), it.categoryName)
            }
            drawPieChart(pieEntries)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun drawPieChart(entries: List<PieEntry>) {
        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(
            Color.parseColor("#FF6F61"),
            Color.parseColor("#6A1B9A"),
            Color.parseColor("#039BE5"),
            Color.parseColor("#43A047"),
            Color.parseColor("#FFB74D"),
            Color.parseColor("#26A69A")
        )
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 14f

        val pieData = PieData(dataSet)

        binding.fragmentStatisticPieChart.apply {
            data = pieData
            setUsePercentValues(true)
            isDrawHoleEnabled = true
            centerText = "Expenses"
            setCenterTextSize(16f)
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            description.isEnabled = false
            legend.isEnabled = false
            invalidate() // refresh
        }
    }
}