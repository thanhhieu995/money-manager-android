package com.henrystudio.moneymanager.presentation.statistic

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
import com.henrystudio.moneymanager.databinding.FragmentStatisticStatsBinding
import com.henrystudio.moneymanager.presentation.model.CategoryStat
import com.henrystudio.moneymanager.presentation.model.KeyFilter
import com.henrystudio.moneymanager.presentation.model.TransactionType
import com.henrystudio.moneymanager.presentation.viewmodel.SharedTransactionViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.StatisticStatsViewModel
import com.henrystudio.moneymanager.presentation.model.UiState
import com.henrystudio.moneymanager.presentation.views.bottomNavigation.statistic.CategoryStatAdapter
import com.henrystudio.moneymanager.presentation.views.bottomNavigation.statistic.StatisticCategoryActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StatisticStatsFragment : Fragment() {
    private var _binding: FragmentStatisticStatsBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedTransactionViewModel by activityViewModels()
    private val viewModel: StatisticStatsViewModel by viewModels()
    private lateinit var adapter: CategoryStatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = CategoryStatAdapter(emptyList())
        binding.fragmentStatisticStatsRecyclerView.adapter = adapter
        binding.fragmentStatisticStatsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter.onClickListener = { categoryStat ->
            val state = viewModel.uiState.value
            val intent = Intent(requireContext(), StatisticCategoryActivity::class.java).apply {
                putExtra("item_click_statistic_category_name", categoryStat.name)
                putExtra("item_click_statistic_filterOption", state.filterOption)
                putExtra("item_click_statistic_keyWord", KeyFilter.CategoryParent)
            }
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.no_animation)
            true
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    sharedViewModel.statisticTransactionType.collect { type ->
                        viewModel.updateTransactionType(type)
                    }
                }
                launch {
                    sharedViewModel.allTransactionsState.collect { state ->
                        viewModel.updateAllTransactions(if (state is UiState.Success) state.data else emptyList())
                    }
                }
                launch {
                    sharedViewModel.categoriesState.collect { categories ->
                        viewModel.updateCategories(categories)
                    }
                }
                launch {
                    sharedViewModel.filterOption.collectLatest { filterOption ->
                        viewModel.updateFilterOption(filterOption)
                    }
                }
                launch {
                    viewModel.uiState.collect { state ->
                        updateUi(state)
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateUi(state: com.henrystudio.moneymanager.presentation.viewmodel.StatisticStatsUiState) {
        adapter.submitList(state.stats)
        updateCircleChart(state.transactionType, state.stats)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateCircleChart(statType: TransactionType, stats: List<CategoryStat>) {
        val pieEntries = stats.map { PieEntry(it.percent, it.name) }
        drawPieChart(pieEntries, stats, statType)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun drawPieChart(entries: List<PieEntry>, stats: List<CategoryStat>, transactionType: TransactionType) {
        val dataSet = PieDataSet(entries, "")
        dataSet.colors = stats.map { it.color }
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 14f

        val pieData = PieData(dataSet)
        pieData.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String = "${Math.round(value)}%"
        })

        val centerTextValue = if (transactionType == TransactionType.EXPENSE) requireContext().getString(R.string.Expense) else requireContext().getString(R.string.Income)
        val color = if (transactionType == TransactionType.EXPENSE) Color.RED else ContextCompat.getColor(requireContext(), R.color.income)

        val spannable = SpannableString(centerTextValue).apply {
            setSpan(ForegroundColorSpan(color), 0, centerTextValue.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        val holeColor = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorSurface, Color.WHITE)

        binding.fragmentStatisticPieChart.apply {
            setUsePercentValues(true)
            isDrawHoleEnabled = true
            setHoleColor(holeColor)
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
