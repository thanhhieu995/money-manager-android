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
import com.henrystudio.moneymanager.databinding.FragmentStatisticAccountBinding
import com.henrystudio.moneymanager.data.model.CategoryType
import com.henrystudio.moneymanager.presentation.model.CategoryStat
import com.henrystudio.moneymanager.presentation.model.KeyFilter
import com.henrystudio.moneymanager.presentation.viewmodel.SharedTransactionViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.StatisticAccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StatisticAccountFragment : Fragment() {
    private var _binding: FragmentStatisticAccountBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedTransactionViewModel by activityViewModels()
    private val viewModel: StatisticAccountViewModel by viewModels()
    private lateinit var adapter: CategoryStatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = CategoryStatAdapter(emptyList())
        binding.fragmentStatisticAccountRecyclerView.adapter = adapter
        binding.fragmentStatisticAccountRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter.onClickListener = { categoryStat ->
            val state = viewModel.uiState.value
            val intent = Intent(requireContext(), StatisticCategoryActivity::class.java)
            intent.putExtra("item_click_statistic_category_name", categoryStat.name)
            intent.putExtra("item_click_statistic_category_type", state.categoryType)
            intent.putExtra("item_click_statistic_filterOption", state.filterOption)
            intent.putExtra("item_click_statistic_keyWord", KeyFilter.Account)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.no_animation)
            true
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    sharedViewModel.statisticCategoryType.collect { type ->
                        viewModel.updateCategoryType(type)
                    }
                }
                launch {
                    sharedViewModel.allTransactions.collect { transactionList ->
                        viewModel.updateAllTransactions(transactionList)
                    }
                }
                launch {
                    sharedViewModel.filterOption.collect { filterOption ->
                        viewModel.updateFilterOption(filterOption)
                    }
                }
                launch {
                    viewModel.uiState.collect { state ->
                        sharedViewModel.setStatisticTransactionFilter(state.filteredTransactions)
                        updateCircleChartByAccount(state.categoryType, state.stats)
                        adapter.submitList(state.stats)
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateCircleChartByAccount(statType: CategoryType, stats: List<CategoryStat>) {
        val pieEntries = stats.map { PieEntry(it.percent, it.name) }
        drawPieChart(pieEntries, stats, statType)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun drawPieChart(entries: List<PieEntry>, stats: List<CategoryStat>, categoryType: CategoryType) {
        val dataSet = PieDataSet(entries, "")
        dataSet.colors = stats.map { it.color }
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 14f

        val pieData = PieData(dataSet)
        pieData.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String = "${Math.round(value)}%"
        })

        val centerTextValue = if (categoryType == CategoryType.EXPENSE) requireContext().getString(R.string.Expense) else requireContext().getString(R.string.Income)
        val color = if (categoryType == CategoryType.EXPENSE) Color.RED else ContextCompat.getColor(requireContext(), R.color.income)

        val spannable = SpannableString(centerTextValue).apply {
            setSpan(ForegroundColorSpan(color), 0, centerTextValue.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        val holeColor = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorSurface, Color.WHITE)

        binding.fragmentStatisticAccountPieChart.apply {
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
                binding.fragmentStatisticAccountNoDataText.visibility = View.GONE
                binding.fragmentStatisticAccountRecyclerView.visibility = View.VISIBLE
                visibility = View.VISIBLE
                invalidate()
            } else {
                clear()
                binding.fragmentStatisticAccountNoDataText.visibility = View.VISIBLE
                binding.fragmentStatisticAccountRecyclerView.visibility = View.GONE
                visibility = View.INVISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
