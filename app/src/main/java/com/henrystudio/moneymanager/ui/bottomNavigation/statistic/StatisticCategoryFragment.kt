package com.henrystudio.moneymanager.ui.bottomNavigation.statistic

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.appbar.MaterialToolbar
import com.henrystudio.moneymanager.databinding.FragmentStatisticCategoryBinding
import com.henrystudio.moneymanager.helper.FilterTransactions
import com.henrystudio.moneymanager.helper.Helper
import com.henrystudio.moneymanager.helper.Helper.Companion.updateMonthText
import com.henrystudio.moneymanager.model.*
import com.henrystudio.moneymanager.viewmodel.CategoryViewModel
import com.henrystudio.moneymanager.viewmodel.CategoryViewModelFactory
import com.henrystudio.moneymanager.viewmodel.TransactionViewModel
import com.henrystudio.moneymanager.viewmodel.TransactionViewModelFactory
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*

class StatisticCategoryFragment : Fragment() {
    private lateinit var btnBack: ImageButton
    private var _binding: FragmentStatisticCategoryBinding? = null
    private val binding get() = _binding!!

    private var categoryName: String = ""
    private var categoryType: CategoryType = CategoryType.EXPENSE

    @RequiresApi(Build.VERSION_CODES.O)
    private var filterOption: FilterOption =
        FilterOption(FilterPeriodStatistic.Monthly, LocalDate.now())

    private var allTransactions: List<Transaction> = emptyList()
    private var allCategories: List<Category> = emptyList()
    private var listTransactionMonth: List<Transaction>? = null
    private var listChildCategories: List<Category> = emptyList()
    private var listChildCategoryStat : List<CategoryStat> = emptyList()
    private var parentId: Int = -1
    private lateinit var lineChart: LineChart
    private lateinit var recyclerView: RecyclerView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var monthBack: ImageView
    private lateinit var monthNext: ImageView
    private lateinit var monthText: TextView
    private lateinit var chartPoints: List<LineChartPoint>
    private var currentIndex = 0
    private lateinit var adapter: CategoryStatAdapter
    private val colors = listOf(Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.CYAN)
    @RequiresApi(Build.VERSION_CODES.O)
    private var filterOptionTemp : FilterOption = FilterOption(FilterPeriodStatistic.Monthly, LocalDate.now())


    val viewModel: TransactionViewModel by activityViewModels {
        TransactionViewModelFactory(
            AppDatabase.getDatabase(requireActivity().application).transactionDao()
        )
    }

    private val categoryViewModel: CategoryViewModel by activityViewModels {
        CategoryViewModelFactory(
            AppDatabase.getDatabase(requireActivity().application).categoryDao()
        )
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private val formatterWeek: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")
    @RequiresApi(Build.VERSION_CODES.O)
    private val formatterMonth: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/yyyy")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentStatisticCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        categoryName = arguments?.getSerializable("item_click_statistic_category_name") as String
        categoryType =
            arguments?.getSerializable("item_click_statistic_category_type") as CategoryType
        filterOption = arguments?.getSerializable("item_click_filterOption") as FilterOption
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        // Đặt title ở đây nếu cần
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Toolbar in Fragment"
        toolbar.title = "new title"

        adapter = CategoryStatAdapter(listChildCategoryStat)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        categoryViewModel.getAll().observe(viewLifecycleOwner) { list ->
            allCategories = list
            val name = categoryName.substringBefore("/")
            for (tx in allCategories) {
                if ((tx.emoji + " " + tx.name).trim() == name.trim()) {
                    parentId = tx.id
                }
            }
            if (parentId != -1) {
                categoryViewModel.getChildCategories(parentId).observe(viewLifecycleOwner) { listChild ->
                    listChildCategories = listChild
                    listTransactionMonth = FilterTransactions.filterTransactionsByCategoryNameAndMonth(viewModel.allTransactions.value?: emptyList(),
                        categoryName, LocalDate.now())
                    listChildCategoryStat = Helper.convertToCategoryStats(listChild,
                        listTransactionMonth ?: emptyList(), categoryType == CategoryType.INCOME, colors)

                    adapter.submitList(listChildCategoryStat)
                }
            }
        }

        viewModel.filterOption.observe(viewLifecycleOwner) { option ->
            filterOptionTemp = option
        }

        viewModel.allTransactions.observe(viewLifecycleOwner) { list ->
            allTransactions = list
            chartPoints = getCategoryLinePoints(
                list,
                categoryName,
                categoryType == CategoryType.INCOME,
                filterOption
            )

            // Cập nhật chart tại đây luôn
            updateLineChart(chartPoints, filterOption.type)
            if (chartPoints.isNotEmpty()) {
                currentIndex = chartPoints.lastIndex // hoặc 0 tùy mặc định
                showChartAt(currentIndex)
            }
            clickLineChart()
        }

        binding.fragmentStatisticCategoryMonthBack.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                highlightChartPoint(currentIndex)
            }
        }

        binding.fragmentStatisticCategoryMonthNext.setOnClickListener {
            if (currentIndex < chartPoints.lastIndex) {
                currentIndex++
                highlightChartPoint(currentIndex)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateLineChart(chartPoints: List<LineChartPoint>, chartMode: FilterPeriodStatistic) {
        val entries = chartPoints.mapIndexed { index, point ->
            Entry(index.toFloat(), point.amount.toFloat())
        }

        val labels = chartPoints.map { point ->
            when(chartMode) {
                FilterPeriodStatistic.Monthly -> {
                    val month = Month.of(point.label.toInt())
                    month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                }
                FilterPeriodStatistic.Weekly -> {
                    point.label
                }
                FilterPeriodStatistic.Yearly -> {
                    point.label
                }
                FilterPeriodStatistic.List -> {
                    val month = Month.of(point.label.toInt())
                    month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                }
                FilterPeriodStatistic.Trend -> {
                    val month = Month.of(point.label.toInt())
                    month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                }
            }
        }

        val dataSet = LineDataSet(entries, categoryName)
        dataSet.color = Color.BLUE
        dataSet.valueTextColor = Color.BLACK
        dataSet.circleRadius = 4f
        dataSet.setCircleColor(Color.BLUE)
        dataSet.lineWidth = 2f
        dataSet.valueTextSize = 10f

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // Cấu hình trục X
        with(lineChart.xAxis) {
            position = XAxis.XAxisPosition.BOTTOM  // 👈 chuyển xuống dưới
            valueFormatter = IndexAxisValueFormatter(labels) // 👈 dùng nhãn tháng
            granularity = 1f
            setDrawGridLines(false)
            textSize = 12f
            labelRotationAngle = 0f // hoặc 45f nếu label dài
        }

        // Các thiết lập khác (nếu cần)
        lineChart.axisRight.isEnabled = false // tắt trục Y bên phải
        lineChart.description.isEnabled = false // tắt description mặc định
        lineChart.legend.isEnabled = false
        lineChart.invalidate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCategoryLinePoints(
        transactions: List<Transaction>,
        categoryName: String,
        isIncome: Boolean,
        filterOption: FilterOption
    ): List<LineChartPoint> {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yy", Locale.getDefault())
        val filtered = transactions.filter {
            it.categoryParentName.equals(categoryName, ignoreCase = true) &&
                    it.isIncome == isIncome
        }

        val grouped: Map<String, List<Transaction>> = when (filterOption.type) {
            FilterPeriodStatistic.Weekly -> {
                // Nhóm theo ngày (dd/MM)
                filtered.groupBy {
                    val dateSub = it.date.substringBefore(" ")
                    val date = LocalDate.parse(dateSub, formatter).with(DayOfWeek.MONDAY)
                    date.format(DateTimeFormatter.ofPattern("dd/MM"))
                }
            }

            FilterPeriodStatistic.Monthly -> {
                // Nhóm theo tháng trong năm (Tháng 1, Tháng 2, ...)
                filtered.groupBy {
                    val dateSub = it.date.substringBefore(" ")
                    val date = LocalDate.parse(dateSub, formatter)
                    date.monthValue.toString()
                }
            }

            FilterPeriodStatistic.Yearly -> {
                // Nhóm theo năm
                filtered.groupBy {
                    val dateSub = it.date.substringBefore(" ")
                    val date = LocalDate.parse(dateSub, formatter)
                    date.year.toString()
                }
            }
            FilterPeriodStatistic.Trend -> {
                filtered.groupBy {
                    val dateSub = it.date.substringBefore(" ")
                    val date = LocalDate.parse(dateSub, formatter)
                    date.year.toString()
                }
            }
            FilterPeriodStatistic.List -> {
                filtered.groupBy {
                    val dateSub = it.date.substringBefore(" ")
                    val date = LocalDate.parse(dateSub, formatter)
                    date.year.toString()
                }
            }
        }

        return grouped.entries.map { (label, group) ->
            val anyDate = group.first().date.substringBefore(" ")
            val localDate = LocalDate.parse(anyDate, formatter)

            val chartDate = when (filterOption.type) {
                FilterPeriodStatistic.Weekly -> localDate
                FilterPeriodStatistic.Monthly -> localDate.withDayOfMonth(1) // đại diện cho đầu tháng
                FilterPeriodStatistic.Yearly -> localDate.withDayOfYear(1)   // đại diện cho đầu năm
                else -> localDate
            }

            LineChartPoint(label = label, amount = group.sumOf { it.amount }, date = chartDate)
        }.sortedBy { it.date }
    }

    private fun init() {
        toolbar = binding.fragmentStatisticCategoryToolbar
        monthBack = binding.fragmentStatisticCategoryMonthBack
        monthNext = binding.fragmentStatisticCategoryMonthNext
        monthText = binding.fragmentStatisticCategoryMonthText
        btnBack = binding.fragmentStatisticCategoryBackButton
        lineChart = binding.fragmentStatisticCategoryLineChart
        recyclerView = binding.fragmentStatisticCategoryStatsRecyclerView
    }

    private fun clickLineChart() {
        lineChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e == null) return

                val xIndex = e.x.toInt() // Vị trí trên trục X
                val point = chartPoints.getOrNull(xIndex)
                showChartAt(xIndex)
                currentIndex = xIndex
                listTransactionMonth = point?.let {
                    FilterTransactions.filterTransactionsByCategoryNameAndMonth(
                        allTransactions,
                        categoryName,
                        it.date
                    )
                }
                listChildCategoryStat = Helper.convertToCategoryStats(listChildCategories, listTransactionMonth?: allTransactions, categoryType == CategoryType.INCOME, colors)
                adapter.submitList(listChildCategoryStat)
            }

            override fun onNothingSelected() {
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showChartAt(index: Int) {
        val point = chartPoints[index]

        val label = when (filterOption.type) {
            FilterPeriodStatistic.Monthly -> {
                val date = point.date
                "${date.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${date.year}"
            }
            FilterPeriodStatistic.Weekly -> {
                val start = point.date
                val end = start.plusDays(6)
                "${start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} ~ ${end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
            }
            FilterPeriodStatistic.Yearly -> point.date.year.toString()
            else -> point.label
        }

        binding.fragmentStatisticCategoryMonthText.text = label

        // Enable / disable back/next button
        binding.fragmentStatisticCategoryMonthBack.isEnabled = index > 0
        binding.fragmentStatisticCategoryMonthNext.isEnabled = index < chartPoints.lastIndex
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun highlightChartPoint(index: Int) {
        lineChart.highlightValue(Highlight(index.toFloat(), 0f, 0)) // Highlight theo x index
        lineChart.centerViewToAnimated(index.toFloat(), 0f, YAxis.AxisDependency.LEFT, 500)

        showChartAt(index) // Cập nhật text + disable nút nếu cần
    }
}