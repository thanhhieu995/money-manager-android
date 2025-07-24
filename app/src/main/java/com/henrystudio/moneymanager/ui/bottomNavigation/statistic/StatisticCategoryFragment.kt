package com.henrystudio.moneymanager.ui.bottomNavigation.statistic

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.henrystudio.moneymanager.databinding.FragmentStatisticCategoryBinding
import com.henrystudio.moneymanager.helper.FilterTransactions
import com.henrystudio.moneymanager.helper.Helper
import com.henrystudio.moneymanager.model.*
import com.henrystudio.moneymanager.viewmodel.CategoryViewModel
import com.henrystudio.moneymanager.viewmodel.CategoryViewModelFactory
import com.henrystudio.moneymanager.viewmodel.TransactionViewModel
import com.henrystudio.moneymanager.viewmodel.TransactionViewModelFactory
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
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

    val viewModel: TransactionViewModel by activityViewModels {
        TransactionViewModelFactory(
            AppDatabase.getDatabase(requireActivity().application).transactionDao()
        )
    }

    val categoryViewModel: CategoryViewModel by activityViewModels {
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
        btnBack = binding.fragmentStatisticCategoryBackButton
        lineChart = binding.fragmentStatisticCategoryLineChart
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        val colors = listOf(Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.CYAN)

        categoryName = arguments?.getSerializable("item_click_statistic_category_name") as String
        categoryType =
            arguments?.getSerializable("item_click_statistic_category_type") as CategoryType
        filterOption = arguments?.getSerializable("item_click_filterOption") as FilterOption

        categoryViewModel.getAll().observe(viewLifecycleOwner) { list ->
            allCategories = list
            val name = categoryName.substringBefore("/")
            for (tx in allCategories) {
                if ((tx.emoji + " " + tx.name).trim() == name.trim()) {
                    parentId = tx.id
                }
            }
            if (parentId != -1) {
                categoryViewModel.getChildCategories(parentId).observe(viewLifecycleOwner) { list ->
                    listChildCategories = list
                }
            }
        }

        viewModel.allTransactions.observe(viewLifecycleOwner) { list ->
            allTransactions = list
            val chartPoints = getCategoryLinePoints(
                list,
                categoryName,
                categoryType == CategoryType.INCOME,
                filterOption
            )

            // Cập nhật chart tại đây luôn
            updateLineChart(chartPoints)

            lineChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    if (e == null) return

                    val xIndex = e.x.toInt() // Vị trí trên trục X
                    val point = chartPoints.getOrNull(xIndex)
                    point?.let {
                        val selectedDate = it
                        // Ví dụ xử lý thêm:
                        // openDetailForDate(selectedDate)
                    }
                    listTransactionMonth = point?.let {
                        FilterTransactions.filterTransactionsByCategoryNameAndMonth(
                            allTransactions,
                            categoryName,
                            it.date
                        )
                    }
                    listChildCategoryStat = Helper.convertToCategoryStats(allCategories, listTransactionMonth?: allTransactions, categoryType == CategoryType.INCOME, colors)
                }

                override fun onNothingSelected() {
                }
            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateLineChart(chartPoints: List<LineChartPoint>) {
        val entries = chartPoints.mapIndexed { index, point ->
            Entry(index.toFloat(), point.amount.toFloat())
        }

        val labels = chartPoints.map { point ->
            val month = Month.of(point.label.toInt())
            // Hiển thị tháng tên tiếng Anh, ví dụ "April"
            month.getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH)
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
                    val date = LocalDate.parse(dateSub, formatter)
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

        return grouped.entries.sortedBy { it.key }.map { (label, group) ->
            val anyDate = group.first().date.substringBefore(" ")
            val localDate = LocalDate.parse(anyDate, formatter)

            val chartDate = when (filterOption.type) {
                FilterPeriodStatistic.Weekly -> localDate
                FilterPeriodStatistic.Monthly -> localDate.withDayOfMonth(1) // đại diện cho đầu tháng
                FilterPeriodStatistic.Yearly -> localDate.withDayOfYear(1)   // đại diện cho đầu năm
                else -> localDate
            }

            LineChartPoint(label = label, amount = group.sumOf { it.amount }, date = chartDate)
        }
    }
}