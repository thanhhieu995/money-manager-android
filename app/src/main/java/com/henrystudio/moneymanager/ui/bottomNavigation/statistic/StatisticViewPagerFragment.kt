package com.henrystudio.moneymanager.ui.bottomNavigation.statistic

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.henrystudio.moneymanager.ui.addtransaction.SharedTransactionHolder
import com.henrystudio.moneymanager.ui.bottomNavigation.dailyNavigate.PrefsManager
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
    private var selectedOption: FilterPeriodStatistic = FilterPeriodStatistic.Monthly
    private lateinit var adapter: StatisticPagerAdapter
    private var _binding: FragmentStatisticViewPagerBinding?= null
    private val binding get() = _binding!!
    private var listTransactionFilter: List<Transaction> = emptyList()

    private val viewModel : TransactionViewModel by activityViewModels {
        TransactionViewModelFactory(AppDatabase.getDatabase(requireActivity().application).transactionDao())
    }
    private lateinit var pageCallback: ViewPager2.OnPageChangeCallback
    private var isRestoring = false
    private var mediator: TabLayoutMediator? = null

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
        mediator = TabLayoutMediator(tabLayout, viewPager) {tab, position ->
            tab.text = when (position) {
                0 -> requireContext().getString(R.string.Stats)
                1 -> requireContext().getString(R.string.Acc)
                2 -> requireContext().getString(R.string.Note)
                else -> ""
            }
        }.also { it.attach() }

        val savedPos = PrefsManager.getStatisticTabPosition(requireContext())
        // setCurrentItem NGAY, trước attach mediator
        viewPager.setCurrentItem(savedPos, false)
        isRestoring = true
        // chọn tab (cập nhật cả TabLayout lẫn ViewPager)
        tabLayout.post {
            tabLayout.getTabAt(savedPos)?.select()
            viewPager.post {
                viewPager.setCurrentItem(savedPos, false)
                // cho phép callback hoạt động lại sau 1 frame
                viewPager.post { isRestoring = false }
            }
        }

        viewModel.setFilter(filterOptionTemp.type, filterOptionTemp.date)

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
            filterDropdown.text = getString(filterOption.type.stringRes)
            selectedOption = filterOption.type
            monthText.text = Helper.getUpdateMonthText(filterOption)
        }

        viewModel.statisticListTransactionFilter.observe(viewLifecycleOwner) { listFilter->
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        val shareDate = SharedTransactionHolder.currentFilterDate
        val shareFilterOption = SharedTransactionHolder.filterOption
        if (shareDate != null) {
            viewModel.setCurrentFilterDate(shareDate)
        }
        if (shareFilterOption != null) {
            viewModel.setFilter(shareFilterOption.type, shareFilterOption.date)
        }
        pageCallback = object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (isRestoring) return  // bỏ qua trigger do khôi phục
                PrefsManager.saveStatisticTabPosition(requireContext(), position)
                viewModel.setCurrentStatisticTab(position)
            }
        }
        viewPager.registerOnPageChangeCallback(pageCallback)
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

        // Map enum -> ImageView check
        val checkViews = mapOf(
            FilterPeriodStatistic.Weekly to view.findViewById<ImageView>(R.id.item_statistic_dialog_imgWeeklyCheck),
            FilterPeriodStatistic.Monthly to view.findViewById<ImageView>(R.id.item_statistic_dialog_imgMonthlyCheck),
            FilterPeriodStatistic.Yearly to view.findViewById<ImageView>(R.id.item_statistic_dialog_imgYearlyCheck),
            FilterPeriodStatistic.List to view.findViewById<ImageView>(R.id.item_statistic_dialog_imgListCheck),
            FilterPeriodStatistic.Trend to view.findViewById<ImageView>(R.id.item_statistic_dialog_imgTrendCheck),
        )

        // Map enum -> layoutId
        val optionLayouts = mapOf(
            FilterPeriodStatistic.Weekly to R.id.item_statistic_dialog_weeklyLayout,
            FilterPeriodStatistic.Monthly to R.id.item_statistic_dialog_monthlyLayout,
            FilterPeriodStatistic.Yearly to R.id.item_statistic_dialog_yearlyLayout,
            FilterPeriodStatistic.List to R.id.item_statistic_dialog_listLayout,
            FilterPeriodStatistic.Trend to R.id.item_statistic_dialog_trendLayout
        )

        // cập nhật text + check
        fun updateCheckMarks(selected: FilterPeriodStatistic) {
            filterDropdown.text = getString(selected.stringRes)

            checkViews.forEach { (option, imageView) ->
                imageView.visibility = if (option == selected) View.VISIBLE else View.GONE
            }

            bottomSheetDialog.dismiss()
        }

        // cập nhật trạng thái ban đầu
        updateCheckMarks(selectedOption)

        // gắn click listener
        optionLayouts.forEach { (filterPeriod, layoutId) ->
            view.findViewById<LinearLayout>(layoutId).setOnClickListener {
                when (filterPeriod) {
                    FilterPeriodStatistic.List -> {
                        selectedOption = filterOptionTemp.type
                        updateCheckMarks(selectedOption)
                        val intent = Intent(requireContext(), StatisticListActivity::class.java)
                        intent.putExtra("filterOption", filterOptionTemp)
                        intent.putExtra("categoryType", currentStatType)
                        intent.putExtra("currentFilterPeriodStatistic", filterPeriod)
                        startActivity(intent)
                        requireActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.no_animation)
                        bottomSheetDialog.dismiss()
                    }
                    FilterPeriodStatistic.Trend -> {
                        selectedOption = filterOptionTemp.type
                        updateCheckMarks(selectedOption)
                        val intent = Intent(requireContext(), StatisticTrendActivity::class.java)
                        intent.putExtra("filterOption", filterOptionTemp)
                        intent.putExtra("categoryType", currentStatType)
                        intent.putExtra("currentFilterPeriodStatistic", filterPeriod)
                        startActivity(intent)
                        requireActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.no_animation)
                        bottomSheetDialog.dismiss()
                    }
                    else -> {
                        selectedOption = filterPeriod
                        updateCheckMarks(filterPeriod)
                        viewModel.setFilter(filterPeriod, filterOptionTemp.date)
                    }
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
        incomeBtn.text = requireContext().getString(R.string.Income) + " " + Helper.formatCurrency(totalIncome)
        expenseBtn.text = requireContext().getString(R.string.exp) + " " + Helper.formatCurrency(totalExpense)
    }
}