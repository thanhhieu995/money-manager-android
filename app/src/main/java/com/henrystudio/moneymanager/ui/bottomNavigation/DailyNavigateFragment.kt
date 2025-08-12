package com.henrystudio.moneymanager.ui.bottomNavigation

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.databinding.FragmentDailyNavigateBinding
import com.henrystudio.moneymanager.helper.Helper
import com.henrystudio.moneymanager.helper.MonthPickerDialogFragment
import com.henrystudio.moneymanager.model.AppDatabase
import com.henrystudio.moneymanager.model.Transaction
import com.henrystudio.moneymanager.model.TransactionGroup
import com.henrystudio.moneymanager.ui.addtransaction.AddTransactionActivity
import com.henrystudio.moneymanager.ui.bookmark.BookmarkActivity
import com.henrystudio.moneymanager.ui.daily.DailyFragment
import com.henrystudio.moneymanager.ui.main.ViewPagerAdapter
import com.henrystudio.moneymanager.ui.monthly.MonthlyFragment
import com.henrystudio.moneymanager.ui.search.SearchActivity
import com.henrystudio.moneymanager.viewmodel.TransactionViewModel
import com.henrystudio.moneymanager.viewmodel.TransactionViewModelFactory
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

class DailyNavigateFragment : Fragment() {

    private var _binding: FragmentDailyNavigateBinding? = null
    private val binding get() = _binding!!

    private lateinit var search: ImageView
    private lateinit var incomeCountAll: TextView
    private lateinit var expenseCountAll: TextView
    private lateinit var totalCount: TextView
    private lateinit var monthBack: ImageView
    private lateinit var monthNext: ImageView
    private lateinit var monthText: TextView
    private lateinit var bookmark: ImageView
    private lateinit var btnAdd: FloatingActionButton
    private lateinit var btnEditClose: ImageView
    private lateinit var btnEditDelete: ImageView
    private lateinit var layoutFunctionControl: LinearLayout
    private lateinit var layoutEdit: LinearLayout

    private var month: LocalDate? = null
    private var listTransactionGroup: List<TransactionGroup> = listOf()
    private var selectedTransactionList: List<Transaction> = emptyList()
    private val viewModel: TransactionViewModel by activityViewModels {
        TransactionViewModelFactory(AppDatabase.getDatabase(requireActivity().application).transactionDao())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDailyNavigateBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

        val formatterYear = DateTimeFormatter.ofPattern("yyyy", Locale.getDefault())
        val formatterMonth = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

        val tabLayout = view.findViewById<TabLayout>(R.id.fragment_daily_navigate_tabLayout)

        val viewPager = view.findViewById<ViewPager2>(R.id.fragment_daily_navigate_viewPager)
        val viewPagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Daily"
                1 -> tab.text = "Calendar"
                2 -> tab.text = "Monthly"
            }
        }.attach()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.setCurrentDailyNavigateTab(position)
            }
        })

        viewModel.currentDailyNavigateTabPosition.observe(viewLifecycleOwner) { position ->
            viewPager.currentItem = position
            val filteredMonth = month?.let {
                com.henrystudio.moneymanager.helper.FilterTransactions.filterTransactionGroupByMonth(listTransactionGroup,
                    it
                )
            }
            val filteredYear =
                month?.let { com.henrystudio.moneymanager.helper.FilterTransactions.filterTransactionGroupByYear(listTransactionGroup, it) }
            when (position) {
                0 -> {
                    // Daily tab selected
                    if (month != null) {
                        monthText.text = month!!.format(formatterMonth)
                        handleSummarySection(filteredMonth?: emptyList())
                    }
                }
                1 -> {
                    // Calendar tab selected
                    if (month != null) {
                        monthText.text = month!!.format(formatterMonth)
                        handleSummarySection(filteredMonth ?: emptyList())
                    }
                }
                2 -> {
                    // Monthly tab selected
                    if (month != null) {
                        monthText.text = month!!.format(formatterYear)
                        if (filteredYear != null) {
                            handleSummarySection(filteredYear)
                        }
                    }
                }
            }
        }

        monthText.setOnClickListener {
            MonthPickerDialogFragment { month, year ->
                val monthName = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.getDefault())
                monthText.text = "$monthName $year"
                // Parse thành LocalDate (ngày 1 của tháng)
                val lastDay = LocalDate.of(year, month, 1).lengthOfMonth()
                val selectedDate = LocalDate.of(year, month, lastDay)
                viewModel.setLocalDateCurrentFilterDate(selectedDate)
            }.show(parentFragmentManager, "monthPicker")
        }

        monthBack.setOnClickListener {
            val fragment = (viewPager.adapter as ViewPagerAdapter).getCurrentFragment(viewPager.currentItem)
            if (fragment is MonthlyFragment) {
                viewModel.changeYear(-1)
            } else {
                viewModel.changeMonth(-1)
            }
        }

        monthNext.setOnClickListener {
            val fragment = (viewPager.adapter as ViewPagerAdapter).getCurrentFragment(viewPager.currentItem)
            if (fragment is MonthlyFragment) {
                viewModel.changeYear(1)
            } else {
                viewModel.changeMonth(1)
            }
        }

        search.setOnClickListener {
            startActivity(Intent(requireContext(), SearchActivity::class.java))
            requireActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.no_animation)
        }

        bookmark.setOnClickListener {
            startActivity(Intent(requireContext(), BookmarkActivity::class.java))
            requireActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.no_animation)
        }

        btnAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddTransactionActivity::class.java))
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.no_animation)
        }

        viewModel.currentFilterDate.observe(viewLifecycleOwner) { selectedMonth ->
            month = selectedMonth
            // Đổi tiêu đề của item "Daily"
            val fragment = (viewPager.adapter as ViewPagerAdapter).getCurrentFragment(viewPager.currentItem)
            val isMonthly = fragment is MonthlyFragment

            monthText.text = selectedMonth.format(if (isMonthly) formatterYear else formatterMonth)

            val filtered = if (isMonthly) {
                com.henrystudio.moneymanager.helper.FilterTransactions.filterTransactionGroupByYear(listTransactionGroup, selectedMonth)
            } else {
                com.henrystudio.moneymanager.helper.FilterTransactions.filterTransactionGroupByMonth(listTransactionGroup, selectedMonth)
            }

            handleSummarySection(filtered)
        }

        viewModel.groupedTransactions.observe(viewLifecycleOwner) { list ->
            listTransactionGroup = list
            month?.let {
                val filtered = com.henrystudio.moneymanager.helper.FilterTransactions.filterTransactionGroupByMonth(list, it)
                handleSummarySection(filtered)
            }
        }

        // observe selectionMode && id
        viewModel.selectionMode.observe(viewLifecycleOwner) { enabled ->
            layoutEdit.visibility = if (enabled) View.VISIBLE else View.GONE
        }

        viewModel.selectedTransactions.observe(viewLifecycleOwner) { selectedTransactions ->
            selectedTransactionList = selectedTransactions
            // Cập nhật số lượng và tổng tiền khi người dùng chọn giao dịch
            binding.fragmentDailyNavigateLayoutEditLineTwoSelectedCount.text =
                "${selectedTransactions.size} selected"
            val totalAmount = selectedTransactions.sumOf {
                if(it.isIncome) it.amount else -it.amount
            }
            binding.fragmentDailyNavigateLayoutEditLineTwoSelectedTotal.text =
                "Total: ${Helper.formatCurrency(totalAmount)}"
        }

        btnEditClose.setOnClickListener {
            viewModel.exitSelectionMode()
        }

        btnEditDelete.setOnClickListener {
            if (selectedTransactionList.isNotEmpty()) {
                viewModel.deleteAll(selectedTransactionList)
                viewModel.exitSelectionMode()
            }
        }

        layoutFunctionControl.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                layoutFunctionControl.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val height = layoutFunctionControl.height
                // Gán chiều cao cho layoutEdit
                val params = layoutEdit.layoutParams
                params.height = height
                layoutEdit.layoutParams = params
            }
        })

        viewModel.navigateToWeekFromMonthly.observe(viewLifecycleOwner) { date ->
            if (date != null) {
                // Gọi scroll đến tuần tương ứng
                navigateToDailyTabAndScrollToWeek(date)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handleSummarySection(filtered: List<TransactionGroup>) {
        incomeCountAll.text = Helper.formatCurrency(filtered.sumOf { it.income })
        expenseCountAll.text = Helper.formatCurrency(filtered.sumOf { it.expense })
        totalCount.text = Helper.formatCurrency(filtered.sumOf { it.income - it.expense })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun navigateToDailyTabAndScrollToWeek(weekStart: LocalDate) {
        val viewPager = binding.fragmentDailyNavigateViewPager
        viewPager.setCurrentItem(0, true) // 0 là tab "Daily"

        // Delay nhẹ để đợi Fragment trong ViewPager khởi tạo xong
        viewPager.postDelayed({
            val dailyFragment = (viewPager.adapter as ViewPagerAdapter)
                .getCurrentFragment(0) as? DailyFragment

            dailyFragment?.scrollToWeek(weekStart)
        }, 100)
    }

    private fun init() {
        search = binding.fragmentDailyNavigateSearch
        incomeCountAll = binding.fragmentDailyNavigateIncomeCountAll
        expenseCountAll = binding.fragmentDailyNavigateExpenseCountAll
        totalCount = binding.fragmentDailyNavigateTotalCount
        monthBack = binding.fragmentDailyNavigateMonthBack
        monthNext = binding.fragmentDailyNavigateMonthNext
        monthText = binding.fragmentDailyNavigateMonthText
        bookmark = binding.fragmentDailyNavigateBookmark
        btnAdd = binding.fragmentDailyNavigateBtnAdd
        btnEditClose = binding.fragmentDailyNavigateLayoutEditLineOneBtnClose
        btnEditDelete = binding.fragmentDailyNavigateLayoutEditLineOneBtnDelete
        layoutFunctionControl = binding.fragmentDailyNavigateLayoutFunctionControl
        layoutEdit = binding.fragmentDailyNavigateLayoutEdit
    }
}