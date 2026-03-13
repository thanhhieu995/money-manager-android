package com.henrystudio.moneymanager.presentation.views.bottomNavigation.dailyNavigate

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
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.databinding.FragmentDailyNavigateBinding
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.core.util.Helper.Companion.getAppLocale
import com.henrystudio.moneymanager.core.util.MonthPickerDialogFragment
import com.henrystudio.moneymanager.core.util.FilterTransactions
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.data.model.TransactionGroup
import com.henrystudio.moneymanager.presentation.viewmodel.SharedTransactionViewModel
import com.henrystudio.moneymanager.presentation.views.addtransaction.AddTransactionActivity
import com.henrystudio.moneymanager.presentation.views.bookmark.BookmarkActivity
import com.henrystudio.moneymanager.presentation.views.daily.DailyFragment
import com.henrystudio.moneymanager.presentation.views.main.ViewPagerAdapter
import com.henrystudio.moneymanager.presentation.views.monthly.MonthlyFragment
import com.henrystudio.moneymanager.presentation.views.search.SearchActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

@AndroidEntryPoint
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
    private val sharedViewModel: SharedTransactionViewModel by activityViewModels()

    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var pageCallback: ViewPager2.OnPageChangeCallback
    private var isRestoring = false
    private var mediator: TabLayoutMediator? = null

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

        fun formatterMonth(): DateTimeFormatter {
            return DateTimeFormatter.ofPattern("MMM yyyy", getAppLocale())
        }

        fun formatterYear(): DateTimeFormatter {
            return DateTimeFormatter.ofPattern("yyyy", getAppLocale())
        }

        tabLayout = view.findViewById(R.id.fragment_daily_navigate_tabLayout)

        viewPager = view.findViewById(R.id.fragment_daily_navigate_viewPager)
        viewPagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter
        mediator = TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = requireContext().getString(R.string.daily)
                1 -> tab.text = requireContext().getString(R.string.calendar)
                2 -> tab.text = requireContext().getString(R.string.monthly)
            }
        }.also { it.attach() }
        viewPager.offscreenPageLimit = 3

        val savedPos = PrefsManager.getTabPosition(requireContext())
        viewPager.setCurrentItem(savedPos, false)
        isRestoring = true
        tabLayout.post {
            tabLayout.getTabAt(savedPos)?.select()
            viewPager.post {
                viewPager.setCurrentItem(savedPos, false)
                viewPager.post { isRestoring = false }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.currentDailyNavigateTabPosition.collectLatest { position ->
                    val filteredMonth = month?.let {
                        FilterTransactions.filterTransactionGroupByMonth(listTransactionGroup, it)
                    }
                    val filteredYear =
                        month?.let { FilterTransactions.filterTransactionGroupByYear(listTransactionGroup, it) }
                    when (position) {
                        0, 1 -> {
                            if (month != null) {
                                monthText.text = month!!.format(formatterMonth())
                                handleSummarySection(filteredMonth ?: emptyList())
                            }
                        }
                        2 -> {
                            if (month != null) {
                                monthText.text = month!!.format(formatterYear())
                                if (filteredYear != null) {
                                    handleSummarySection(filteredYear)
                                }
                            }
                        }
                    }
                }
            }
        }

        monthText.setOnClickListener {
            MonthPickerDialogFragment { month, year ->
                val monthName = Month.of(month).getDisplayName(TextStyle.SHORT, getAppLocale())
                monthText.text = "$monthName $year"
                val lastDay = LocalDate.of(year, month, 1).lengthOfMonth()
                val selectedDate = LocalDate.of(year, month, lastDay)
                sharedViewModel.setLocalDateCurrentFilterDate(selectedDate)
            }.show(parentFragmentManager, "monthPicker")
        }

        monthBack.setOnClickListener {
            val fragment = (viewPager.adapter as ViewPagerAdapter).getCurrentFragment(viewPager.currentItem)
            if (fragment is MonthlyFragment) {
                sharedViewModel.changeYear(-1)
            } else {
                sharedViewModel.changeMonth(-1)
            }
        }

        monthNext.setOnClickListener {
            val fragment = (viewPager.adapter as ViewPagerAdapter).getCurrentFragment(viewPager.currentItem)
            if (fragment is MonthlyFragment) {
                sharedViewModel.changeYear(1)
            } else {
                sharedViewModel.changeMonth(1)
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.combineGroupAndDate.collect { (groups, date) ->
                    month = date
                    listTransactionGroup = groups
                    val fragment = (viewPager.adapter as ViewPagerAdapter).getCurrentFragment(viewPager.currentItem)
                    val isMonthly = fragment is MonthlyFragment

                    monthText.text = date.format(if (isMonthly) formatterYear() else formatterMonth())

                    val filtered = if (isMonthly) {
                        FilterTransactions.filterTransactionGroupByYear(listTransactionGroup, date)
                    } else {
                        FilterTransactions.filterTransactionGroupByMonth(listTransactionGroup, date)
                    }

                    handleSummarySection(filtered)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.selectionMode.collect { enabled ->
                    layoutEdit.visibility = if (enabled) View.VISIBLE else View.GONE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.selectedTransactions.collect { selectedTransactions ->
                    selectedTransactionList = selectedTransactions
                    binding.fragmentDailyNavigateLayoutEditLineTwoSelectedCount.text =
                        "${selectedTransactions.size} ${requireContext().getString(R.string.selected)}"
                    val totalAmount = selectedTransactions.sumOf {
                        if (it.isIncome) it.amount else -it.amount
                    }
                    binding.fragmentDailyNavigateLayoutEditLineTwoSelectedTotal.text =
                        "${requireContext().getString(R.string.Total)} : ${Helper.formatCurrency(totalAmount)}"
                }
            }
        }

        btnEditClose.setOnClickListener {
            sharedViewModel.exitSelectionMode()
        }

        btnEditDelete.setOnClickListener {
            if (selectedTransactionList.isNotEmpty()) {
                sharedViewModel.deleteAll(selectedTransactionList)
                sharedViewModel.exitSelectionMode()
            }
        }

        layoutFunctionControl.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                layoutFunctionControl.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val height = layoutFunctionControl.height
                val params = layoutEdit.layoutParams
                params.height = height
                layoutEdit.layoutParams = params
            }
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.navigateToWeekFromMonthly.collect { event ->
                    event.getContentIfNotHandled()?.let { date ->
                        navigateToDailyTabAndScrollToWeek(date)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        pageCallback = object : ViewPager2.OnPageChangeCallback() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (isRestoring) return
                PrefsManager.saveTabPosition(requireContext(), position)
                 sharedViewModel.setCurrentDailyNavigateTab(position)
            }
        }
        viewPager.registerOnPageChangeCallback(pageCallback)
    }

    override fun onPause() {
        super.onPause()
        viewPager.unregisterOnPageChangeCallback(pageCallback)
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
        viewPager.setCurrentItem(0, true)
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
