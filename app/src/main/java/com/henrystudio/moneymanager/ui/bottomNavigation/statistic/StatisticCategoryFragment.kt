package com.henrystudio.moneymanager.ui.bottomNavigation.statistic

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
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
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.databinding.FragmentStatisticCategoryBinding
import com.henrystudio.moneymanager.helper.FilterTransactions
import com.henrystudio.moneymanager.helper.Helper
import com.henrystudio.moneymanager.model.*
import com.henrystudio.moneymanager.ui.daily.DailyFragment
import com.henrystudio.moneymanager.viewmodel.CategoryViewModel
import com.henrystudio.moneymanager.viewmodel.CategoryViewModelFactory
import com.henrystudio.moneymanager.viewmodel.TransactionViewModel
import com.henrystudio.moneymanager.viewmodel.TransactionViewModelFactory
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

class StatisticCategoryFragment : Fragment() {
    private var _binding: FragmentStatisticCategoryBinding? = null
    private val binding get() = _binding!!

    private var categoryName: String = ""
    private var categoryType: CategoryType = CategoryType.EXPENSE

    private var allTransactions: List<Transaction> = emptyList()
    private var allCategories: List<Category> = emptyList()
    private var transactionList: List<Transaction> = emptyList()
    private var listTransactionsFilterCategoryName: List<Transaction> = emptyList()
    private var listChildCategories: List<Category> = emptyList()
    private var listChildCategoryStat: List<CategoryStat> = emptyList()
    private var childCategoryClick: Boolean = false
    private var parentId: Int = -1
    private lateinit var lineChart: LineChart
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutCategorySum: LinearLayout
    private lateinit var categorySumName: TextView
    private lateinit var categorySumAmount: TextView
    private lateinit var dailyContainer: FrameLayout
    private lateinit var monthBack: ImageView
    private lateinit var monthNext: ImageView
    private lateinit var monthText: TextView
    private lateinit var chartPoints: List<LineChartPoint>
    private var currentIndex = 0
    private lateinit var adapter: CategoryStatAdapter
    private val colors = listOf(Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.CYAN)

    @RequiresApi(Build.VERSION_CODES.O)
    private var filterOptionTemp: FilterOption =
        FilterOption(FilterPeriodStatistic.Monthly, LocalDate.now())


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

        categoryName = arguments?.getSerializable("item_click_statistic_category_name") as String
        categoryType =
            arguments?.getSerializable("item_click_statistic_category_type") as CategoryType
        filterOptionTemp =
            arguments?.getSerializable("item_click_statistic_filterOption") as FilterOption
        childCategoryClick = arguments?.getBoolean("item_click_statistic_category_child") as Boolean
        adapter = CategoryStatAdapter(listChildCategoryStat)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewModel.selectionMode.observe(viewLifecycleOwner) { enabled ->
            lineChart.visibility = if (enabled) View.GONE else View.VISIBLE
        }

        viewModel.allTransactions.observe(viewLifecycleOwner) { list ->
            allTransactions = list
            chartPoints = getCategoryLinePoints(
                list,
                categoryName,
                categoryType == CategoryType.INCOME,
                filterOptionTemp
            )

            // Cập nhật chart tại đây luôn
            updateLineChart(chartPoints, filterOptionTemp.type)
            if (chartPoints.isNotEmpty()) {
                selectCurrentPeriodPoint()
            }
        }

        viewModel.setFilter(filterOptionTemp.type, filterOptionTemp.date)

        clickLineChart()

        categoryViewModel.getAll().observe(viewLifecycleOwner) { list ->
            allCategories = list
            val name = categoryName.substringBefore("/")
            val nameOnly = name.replace(Regex("^[^\\p{L}\\p{N}]+"), "") // → "Transport"
            for (tx in allCategories) {
                val txName = tx.name.replace(Regex("^[^\\p{L}\\p{N}]+"), "")
                if (txName.trim() == nameOnly.trim()) {
                    parentId = tx.id
                }
            }
            if (parentId != -1) {
                categoryViewModel.getChildCategories(parentId)
                    .observe(viewLifecycleOwner) { listChild ->
                        listChildCategories = listChild
                        viewModel.allTransactions.observe(viewLifecycleOwner) { listTransactions ->
                            listTransactionsFilterCategoryName =
                                FilterTransactions.filterTransactionsByCategoryName(
                                    listTransactions,
                                    categoryName
                                )
                            transactionList = getListTransactionsFilterType(
                                listTransactionsFilterCategoryName, filterOptionTemp,
                                filterOptionTemp.date
                            )
                            listChildCategoryStat = Helper.convertToCategoryStats(
                                listChild,
                                transactionList ?: emptyList(),
                                categoryType == CategoryType.INCOME,
                                colors
                            )
                            if (listChildCategoryStat.isNotEmpty()) {
                                layoutCategorySum.visibility = View.VISIBLE
                                recyclerView.visibility = View.VISIBLE
                                dailyContainer.visibility = View.GONE
                                adapter.submitList(listChildCategoryStat)
                            } else {
                                layoutCategorySum.visibility = View.GONE
                                recyclerView.visibility = View.GONE
                                dailyContainer.visibility = View.VISIBLE
                                // Gửi category id hoặc name vào DailyFragment nếu cần
                                val fragment = DailyFragment.newDailyInstance(
                                    categoryName = categoryName,
                                    childCategoryClick
                                )

                                childFragmentManager.beginTransaction()
                                    .replace(
                                        R.id.fragment_statistic_category_dailyContainer,
                                        fragment
                                    )
                                    .commit()
                            }
                        }
                    }
            }
        }

        monthBack.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                highlightChartPoint(currentIndex)
            }
        }

        monthNext.setOnClickListener {
            if (currentIndex < chartPoints.lastIndex) {
                currentIndex++
                highlightChartPoint(currentIndex)
            }
        }
        // click into child category item
        adapter.onClickListener = { categoryStat ->
            (requireActivity() as StatisticCategoryActivity).titleStack.addLast(categoryName)
            val titleCurrent = (requireActivity() as StatisticCategoryActivity).titleCurrent
            val titleIncoming = (requireActivity() as StatisticCategoryActivity).titleIncoming
            (requireActivity() as StatisticCategoryActivity).animateTitleToLeftOfIcon(titleCurrent)
            (requireActivity() as StatisticCategoryActivity).animateIncomingTitleToCenter(
                titleIncoming,
                categoryStat.name
            )
            // Gửi category id hoặc name vào DailyFragment nếu cần
            val fragment = StatisticCategoryFragment()
            val bundle = Bundle().apply {
                putSerializable("item_click_statistic_category_name", categoryStat.name)
                putSerializable("item_click_statistic_category_type", categoryType)
                putSerializable("item_click_statistic_filterOption", filterOptionTemp)
                putBoolean("item_click_statistic_category_child", true)
            }
            fragment.apply {
                arguments = bundle
            }
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,  // enter
                    R.anim.no_animation,    // exit
                    R.anim.no_animation,    // popEnter (khi quay lại)
                    R.anim.slide_out_right  // popExit (khi quay lại)
                )
                .replace(R.id.activity_statistic_category_container, fragment)
                .addToBackStack(null)
                .commit()
            true
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateLineChart(
        chartPoints: List<LineChartPoint>,
        chartMode: FilterPeriodStatistic
    ) {
        val entries = chartPoints.mapIndexed { index, point ->
            Entry(index.toFloat(), point.amount.toFloat())
        }

        val labels = chartPoints.map { point ->
            when (chartMode) {
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

        lineChart.setTouchEnabled(true)
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)
        lineChart.setPinchZoom(true)

        lineChart.xAxis.isGranularityEnabled = true
        lineChart.xAxis.granularity = 1f // mỗi bước 1 label
        lineChart.setVisibleXRangeMaximum(5f) // CHỈ HIỆN 5 điểm 1 lần

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
        val filtered = if (childCategoryClick) transactions.filter {
            it.categorySubName.trim()
                .equals(categoryName.trim(), ignoreCase = true) && it.isIncome == isIncome
        }
        else transactions.filter {
            it.categoryParentName.equals(categoryName, ignoreCase = true) &&
                    it.isIncome == isIncome
        }

        val grouped: Map<String, List<Transaction>> = when (filterOption.type) {
            FilterPeriodStatistic.Weekly -> {
                val targetYear = filterOption.date.year
                // Nhóm theo ngày (dd/MM)
                filtered.filter {
                    val dateSub = it.date.substringBefore(" ")
                    val date = LocalDate.parse(dateSub, formatter)
                    date.with(DayOfWeek.MONDAY).year == targetYear // 🟢 chỉ lấy giao dịch trong năm cần
                }
                    .groupBy {
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
                FilterPeriodStatistic.Weekly -> localDate.with(DayOfWeek.MONDAY)
                FilterPeriodStatistic.Monthly -> localDate.withDayOfMonth(1) // đại diện cho đầu tháng
                FilterPeriodStatistic.Yearly -> localDate.withDayOfMonth(1)   // đại diện cho đầu năm
                else -> localDate
            }

            LineChartPoint(label = label, amount = group.sumOf { it.amount }, date = chartDate)
        }.sortedBy { it.date }
    }

    private fun init() {
        monthBack = binding.fragmentStatisticCategoryMonthBack
        monthNext = binding.fragmentStatisticCategoryMonthNext
        monthText = binding.fragmentStatisticCategoryMonthText
        lineChart = binding.fragmentStatisticCategoryLineChart
        layoutCategorySum = binding.fragmentStatisticCategoryLayoutCategorySum
        categorySumName = binding.fragmentStatisticCategoryLayoutCategorySumName
        categorySumAmount = binding.fragmentStatisticCategoryLayoutCategorySumAmount
        recyclerView = binding.fragmentStatisticCategoryStatsRecyclerView
        dailyContainer = binding.fragmentStatisticCategoryDailyContainer
    }

    private fun clickLineChart() {
        lineChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e == null) return

                val xIndex = e.x.toInt() // Vị trí trên trục X
                updateDateList(xIndex)
                showChartAt(xIndex)
            }

            override fun onNothingSelected() {
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showChartAt(index: Int) {
        val point = chartPoints[index]
        viewModel.setLocalDateCurrentFilterDate(point.date)
        val label = when (filterOptionTemp.type) {
            FilterPeriodStatistic.Monthly -> {
                val date = point.date
                "${date.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${date.year}"
            }
            FilterPeriodStatistic.Weekly -> {
                val start = point.date
                val end = start.plusDays(6)
                "${start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} ~ ${
                    end.format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    )
                }"
            }
            FilterPeriodStatistic.Yearly -> point.date.year
            else -> point.label
        }

        monthText.text = label.toString()

        // Enable / disable back/next button
        binding.fragmentStatisticCategoryMonthBack.isEnabled = index > 0
        binding.fragmentStatisticCategoryMonthNext.isEnabled = index < chartPoints.lastIndex
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun highlightChartPoint(index: Int) {
        lineChart.highlightValue(Highlight(index.toFloat(), 0f, 0)) // Highlight theo x index
        lineChart.centerViewToAnimated(index.toFloat(), 0f, YAxis.AxisDependency.LEFT, 500)
        showChartAt(index) // Cập nhật text + disable nút nếu cần
        updateDateList(index)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun selectCurrentPeriodPoint() {
        val today = LocalDate.now()
        val localDate = when (filterOptionTemp.type) {
            FilterPeriodStatistic.Weekly -> filterOptionTemp.date.with(DayOfWeek.MONDAY)
            FilterPeriodStatistic.Monthly -> filterOptionTemp.date.withDayOfMonth(1)
            FilterPeriodStatistic.Yearly -> filterOptionTemp.date.withDayOfMonth(1)
            else -> today
        }
        val index = when (filterOptionTemp.type) {
            FilterPeriodStatistic.Weekly -> chartPoints.indexOfFirst { it.date == localDate }
            FilterPeriodStatistic.Monthly -> chartPoints.indexOfFirst { it.date.month == localDate.month && it.date.year == localDate.year }
            FilterPeriodStatistic.Yearly -> chartPoints.indexOfFirst { it.date.year == localDate.year }
            else -> -1
        }
        if (index != -1) {
            currentIndex = index
            highlightChartPoint(index)
        } else {
            // Optional: chọn điểm gần nhất trước hôm nay
            val fallbackIndex = chartPoints.indexOfLast { it.date.isBefore(localDate) }
            if (fallbackIndex != -1) {
                currentIndex = fallbackIndex
                highlightChartPoint(fallbackIndex)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateDateList(index: Int) {
        val point = chartPoints.getOrNull(index)
        currentIndex = index
        transactionList = point?.let {
            getListTransactionsFilterType(
                listTransactionsFilterCategoryName,
                filterOptionTemp,
                it.date
            )
        } ?: emptyList()
        listChildCategoryStat = Helper.convertToCategoryStats(
            listChildCategories,
            transactionList,
            categoryType == CategoryType.INCOME,
            colors
        )
        updateCategorySum(categoryName, point?.amount ?: -1.0)
        adapter.submitList(listChildCategoryStat)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getListTransactionsFilterType(
        listTransactionsFilterCategoryName: List<Transaction>,
        filterOption: FilterOption,
        date: LocalDate
    ): List<Transaction> {
        return when (filterOption.type) {
            FilterPeriodStatistic.Weekly -> {
                FilterTransactions.filterTransactionsByWeek(
                    listTransactionsFilterCategoryName,
                    date
                )
            }
            FilterPeriodStatistic.Monthly -> {
                FilterTransactions.filterTransactionsByMonth(
                    listTransactionsFilterCategoryName,
                    date
                )
            }
            FilterPeriodStatistic.Yearly -> {
                FilterTransactions.filterTransactionsByYear(
                    listTransactionsFilterCategoryName,
                    date
                )
            }
            else -> {
                FilterTransactions.filterTransactionsByMonth(
                    listTransactionsFilterCategoryName,
                    date
                )
            }
        }
    }

    private fun updateCategorySum(name: String, amount: Double) {
        categorySumName.text = name
        categorySumAmount.text = Helper.formatCurrency(amount)
    }
}