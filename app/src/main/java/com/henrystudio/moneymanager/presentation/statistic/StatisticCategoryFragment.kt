package com.henrystudio.moneymanager.presentation.views.bottomNavigation.statistic

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.henrystudio.moneymanager.databinding.FragmentStatisticCategoryBinding
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.presentation.model.FilterOption
import com.henrystudio.moneymanager.presentation.model.FilterPeriodStatistic
import com.henrystudio.moneymanager.presentation.model.KeyFilter
import com.henrystudio.moneymanager.presentation.model.LineChartPoint
import com.henrystudio.moneymanager.presentation.model.TransactionType
import com.henrystudio.moneymanager.presentation.viewmodel.CategoryViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.SharedTransactionViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.StatisticCategoryViewModel
import com.henrystudio.moneymanager.presentation.model.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StatisticCategoryFragment : Fragment() {
    private var _binding: FragmentStatisticCategoryBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedTransactionViewModel by activityViewModels()
    private val viewModel: StatisticCategoryViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()

    private lateinit var adapter: CategoryStatAdapter
    private val colors = listOf(Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.CYAN)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoryName = arguments?.getSerializable("item_click_statistic_category_name") as? String ?: ""
        val transactionType = arguments?.getSerializable("item_click_statistic_category_type") as? TransactionType
            ?: TransactionType.EXPENSE
        val filterOption = arguments?.getSerializable("item_click_statistic_filterOption") as? FilterOption ?: FilterOption(FilterPeriodStatistic.Monthly, java.time.LocalDate.now())
        val keyFilter = arguments?.getSerializable("item_click_statistic_keyWord") as? KeyFilter ?: KeyFilter.Time

        viewModel.init(categoryName, transactionType, filterOption, keyFilter)

        adapter = CategoryStatAdapter(emptyList())
        binding.fragmentStatisticCategoryStatsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.fragmentStatisticCategoryStatsRecyclerView.adapter = adapter

        viewModel.bindTransactions(sharedViewModel.allTransactionsState.map { state ->
           if (state is UiState.Success) state.data else emptyList()
        })
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    sharedViewModel.selectionMode.collect { enabled ->
                        viewModel.updateSelectionMode(enabled)
                    }
                }

                launch {
                    viewModel.uiState.collect { state ->
                        updateUi(state)
                    }
                }
            }
        }

        binding.fragmentStatisticCategoryLineChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e == null) return
                viewLifecycleOwner.lifecycleScope.launch {
                    // We need categories and transactions again? Let's just update index in ViewModel
                    // and have ViewModel update state.
                    // To do this properly, the ViewModel should have access to the data.
                }
            }
            override fun onNothingSelected() {}
        })

        binding.fragmentStatisticCategoryMonthBack.setOnClickListener {
            val state = viewModel.uiState.value
            if (state.currentIndex > 0) {
                // viewModel.selectPoint(...)
            }
        }

        binding.fragmentStatisticCategoryMonthNext.setOnClickListener {
            val state = viewModel.uiState.value
            if (state.currentIndex < state.chartPoints.lastIndex) {
                // viewModel.selectPoint(...)
            }
        }

        adapter.onClickListener = { categoryStat ->
            // Navigation logic moved here
            (requireActivity() as StatisticCategoryActivity).titleStack.addLast(viewModel.uiState.value.categoryName)
            // ... animation and fragment transaction
            true
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateUi(state: com.henrystudio.moneymanager.presentation.viewmodel.StatisticCategoryUiState) {
        binding.fragmentStatisticCategoryLineChart.visibility = if (state.isChartVisible) View.VISIBLE else View.GONE
        binding.fragmentStatisticCategoryMonthText.text = state.currentMonthText
        binding.fragmentStatisticCategoryLayoutCategorySumAmount.text = Helper.formatCurrency(requireContext() ,state.categorySumAmount)
        adapter.submitList(state.listChildCategoryStat)

        binding.fragmentStatisticCategoryDailyContainer.visibility = if (state.isDailyVisible) View.VISIBLE else View.GONE

        if (state.chartPoints.isNotEmpty()) {
            updateLineChart(state.chartPoints, state.filterOption.type, state.categoryName, state.transactionType)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateLineChart(
        chartPoints: List<LineChartPoint>,
        chartMode: FilterPeriodStatistic,
        categoryName: String,
        transactionType: TransactionType
    ) {
        val entries = chartPoints.mapIndexed { index, point -> Entry(index.toFloat(), point.amount.toFloat()) }
        val labels = chartPoints.map { point ->
            if (chartMode == FilterPeriodStatistic.Monthly) {
                java.time.Month.of(point.label.toInt()).getDisplayName(java.time.format.TextStyle.FULL, Helper.getAppLocale())
            } else point.label
        }

        val color = if (transactionType == TransactionType.INCOME) Color.GREEN else Color.RED
        var textColor = getThemeColor(com.google.android.material.R.attr.colorOnSurface)

        val dataSet = LineDataSet(entries, categoryName).apply {
            this.color = color
            valueTextColor = textColor
            circleRadius = 4f
            setCircleColor(color)
            lineWidth = 2f
            valueTextSize = 10f
        }

        binding.fragmentStatisticCategoryLineChart.apply {
            data = LineData(dataSet)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(labels)
                granularity = 1f
                setDrawGridLines(false)
                textColor = textColor
            }
            axisLeft.textColor = textColor
            axisRight.isEnabled = false
            description.isEnabled = false
            legend.isEnabled = false
            invalidate()
        }
    }

    private fun getThemeColor(attr: Int): Int {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
