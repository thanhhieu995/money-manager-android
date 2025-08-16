package com.henrystudio.moneymanager.ui.bottomNavigation.statistic

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.databinding.FragmentStatisticViewPagerBinding
import com.henrystudio.moneymanager.helper.Helper
import com.henrystudio.moneymanager.model.*
import com.henrystudio.moneymanager.viewmodel.TransactionViewModel
import com.henrystudio.moneymanager.viewmodel.TransactionViewModelFactory
import java.time.LocalDate

class StatisticViewPagerFragment : Fragment() {
    private lateinit var tabLayout: TabLayout
    private lateinit var filterDropdown: TextView
    private lateinit var monthBack: ImageView
    private lateinit var monthNext: ImageView
    private lateinit var monthText: TextView
    private lateinit var toggleGroupButton: MaterialButtonToggleGroup
    private lateinit var incomeBtn: MaterialButton
    private lateinit var expenseBtn: MaterialButton
    private lateinit var viewPager: ViewPager2
    private var selectedOption: String = "Monthly"
    private lateinit var adapter: StatisticPagerAdapter
    private var _binding: FragmentStatisticViewPagerBinding?= null
    private val binding get() = _binding!!
    private var listTransactionFilter: List<Transaction> = emptyList()

    private val viewModel : TransactionViewModel by activityViewModels {
        TransactionViewModelFactory(AppDatabase.getDatabase(requireActivity().application).transactionDao())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private var filterOptionTemp : FilterOption = FilterOption(FilterPeriodStatistic.Monthly, LocalDate.now())
    private var currentStatType = CategoryType.EXPENSE

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatisticViewPagerBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        init()
        adapter = StatisticPagerAdapter(this)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) {tab, position ->
            tab.text = when (position) {
                0 -> "Stats"
                1 -> "Acc"
                2 -> "Note"
                else -> ""
            }
        }.attach()

        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.setCurrentStatisticTab(position)
            }
        })

        viewModel.statisticListTransactionFilter.observe(viewLifecycleOwner) {list ->
            listTransactionFilter = list
        }

        viewModel.currentStatisticTabPosition.observe(viewLifecycleOwner) {position ->
            viewPager.currentItem = position
            when(position) {
                0 -> {}
                1 -> {}
                2 -> {}
            }
        }

        viewModel.statisticCategoryType.observe(viewLifecycleOwner) {type ->
            currentStatType = type
            toggleGroupButton.check(
                if (currentStatType == CategoryType.INCOME) incomeBtn.id else expenseBtn.id
            )
        }

        filterDropdown.setOnClickListener {
            showDialogOption()
        }
        toggleGroupButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                currentStatType = when (checkedId) {
                    incomeBtn.id -> CategoryType.INCOME
                    else -> CategoryType.EXPENSE
                }
                viewModel.setStatisticCategoryType(currentStatType)
            }
        }

        viewModel.filterOption.observe(viewLifecycleOwner) { filterOption ->
            filterOptionTemp = filterOption
            filterDropdown.text = filterOption.type.toString()
            selectedOption = filterOption.type.toString()
            Helper.updateMonthText(filterOption, monthText)
        }

        viewModel.statisticListTransactionFilter.observe(viewLifecycleOwner) {listFilter->
            updateTextButton(listFilter)
        }

        monthBack.setOnClickListener {
            when(filterOptionTemp.type) {
                FilterPeriodStatistic.Monthly -> viewModel.changeMonth(-1)
                FilterPeriodStatistic.Weekly -> viewModel.changeWeek(-1)
                FilterPeriodStatistic.Yearly -> viewModel.changeYear(-1)
                FilterPeriodStatistic.List -> {}
                FilterPeriodStatistic.Trend -> {}
            }
        }

        monthNext.setOnClickListener {
            when(filterOptionTemp.type) {
                FilterPeriodStatistic.Monthly -> viewModel.changeMonth(1)
                FilterPeriodStatistic.Weekly -> viewModel.changeWeek(1)
                FilterPeriodStatistic.Yearly -> viewModel.changeYear(1)
                FilterPeriodStatistic.List -> {}
                FilterPeriodStatistic.Trend -> {}
            }
        }
    }

    fun init() {
        tabLayout = binding.fragmentStatisticViewPagerTabLayout
        viewPager = binding.fragmentStatisticViewPager
        filterDropdown = binding.statisticFilterDropdown
        monthBack = binding.fragmentStatisticViewPagerMonthBack
        monthNext = binding.fragmentStatisticViewPagerMonthNext
        monthText = binding.fragmentStatisticViewPagerMonthText
        toggleGroupButton = binding.fragmentStatisticViewPagerToggleGroup
        incomeBtn = binding.fragmentStatisticViewPagerBtnIncome
        expenseBtn = binding.fragmentStatisticViewPagerBtnExpense
    }

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDialogOption() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.item_statistic_dialog, null)
        bottomSheetDialog.setContentView(view)
        val checkViews = mapOf(
            "Weekly" to view.findViewById<ImageView>(R.id.item_statistic_dialog_imgWeeklyCheck),
            "Monthly" to view.findViewById<ImageView>(R.id.item_statistic_dialog_imgMonthlyCheck),
            "Yearly" to view.findViewById<ImageView>(R.id.item_statistic_dialog_imgYearlyCheck),
            "List" to view.findViewById<ImageView>(R.id.item_statistic_dialog_imgListCheck),
            "Trend" to view.findViewById<ImageView>(R.id.item_statistic_dialog_imgTrendCheck),
        )
        val optionConfigs = listOf(
            Triple("Weekly", R.id.item_statistic_dialog_weeklyLayout, FilterPeriodStatistic.Weekly),
            Triple("Monthly", R.id.item_statistic_dialog_monthlyLayout, FilterPeriodStatistic.Monthly),
            Triple("Yearly", R.id.item_statistic_dialog_yearlyLayout, FilterPeriodStatistic.Yearly),
            Triple("List", R.id.item_statistic_dialog_listLayout, FilterPeriodStatistic.List),
            Triple("Trend", R.id.item_statistic_dialog_trendLayout, FilterPeriodStatistic.Trend)
        )

        fun updateCheckMarks(selected: String) {
            filterDropdown.text = selected
            checkViews.forEach { (option, imageView) ->
                imageView.visibility = if (option == selected) View.VISIBLE else View.GONE
            }
        }
        updateCheckMarks(selectedOption) // cập nhật ban đầu
        optionConfigs.forEach { (optionName, layoutId, filterPeriod) ->
            view.findViewById<LinearLayout>(layoutId).setOnClickListener {
                if (filterPeriod == FilterPeriodStatistic.List || filterPeriod == FilterPeriodStatistic.Trend) {
                    val intent = Intent(requireContext(), StatisticListTrendActivity::class.java)
                    intent.putExtra("filterOption", filterOptionTemp)
                    intent.putExtra("currentFilterPeriodStatistic", filterPeriod)
                    startActivity(intent)
                    requireActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.no_animation)
                    bottomSheetDialog.dismiss()
                } else {
                    selectedOption = optionName
                    updateCheckMarks(optionName)
                    viewModel.setFilter(filterPeriod, filterOptionTemp.date)
                }
            }
        }
        view.findViewById<TextView>(R.id.item_statistic_dialog_optionCancel).setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    private fun updateTextButton(filteredList: List<Transaction>) {
        val incomeList = filteredList.filter { it.isIncome }
        val expenseList = filteredList.filter { !it.isIncome }

        val totalIncome = incomeList.sumOf { it.amount }
        val totalExpense = expenseList.sumOf { it.amount }
        incomeBtn.text = "Income " + Helper.formatCurrency(totalIncome)
        expenseBtn.text = "Exp " + Helper.formatCurrency(totalExpense)
    }
}