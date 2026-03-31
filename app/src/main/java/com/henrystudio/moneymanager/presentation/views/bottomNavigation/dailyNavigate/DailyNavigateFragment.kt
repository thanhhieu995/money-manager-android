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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
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
import com.henrystudio.moneymanager.presentation.addtransaction.AddTransactionActivity
import com.henrystudio.moneymanager.presentation.viewmodel.DailyNavigateViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.SharedTransactionViewModel
import com.henrystudio.moneymanager.presentation.views.bookmark.BookmarkActivity
import com.henrystudio.moneymanager.presentation.views.daily.DailyFragment
import com.henrystudio.moneymanager.presentation.addtransaction.model.UiState
import com.henrystudio.moneymanager.presentation.views.main.ViewPagerAdapter
import com.henrystudio.moneymanager.presentation.views.monthly.MonthlyFragment
import com.henrystudio.moneymanager.presentation.views.search.SearchActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Month
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

    private val sharedViewModel: SharedTransactionViewModel by activityViewModels()
    private val viewModel: DailyNavigateViewModel by viewModels()

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
        viewPager.offscreenPageLimit = 1

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
                delay(100)
                combine(
                    sharedViewModel.groupedTransactionsState,
                    sharedViewModel.currentFilterDate,
                    sharedViewModel.currentDailyNavigateTabPosition,
                    sharedViewModel.selectionMode,
                    sharedViewModel.selectedTransactions
                ) { groupsState, date, tabPosition, selectionMode, selected ->
                    if (groupsState is UiState.Success) {
                        viewModel.updateFrom(
                            groupsState.data,
                            date,
                            tabPosition,
                            selectionMode,
                            selected
                        )
                    }
                }.flowOn(Dispatchers.Default).collect { }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    monthText.text = state.monthLabel
                    incomeCountAll.text = Helper.formatCurrency(state.incomeSum)
                    expenseCountAll.text = Helper.formatCurrency(state.expenseSum)
                    totalCount.text = Helper.formatCurrency(state.totalSum)
                    layoutEdit.visibility = if (state.selectionMode) View.VISIBLE else View.GONE
                    binding.fragmentDailyNavigateLayoutEditLineTwoSelectedCount.text =
                        "${state.selectedCount} ${requireContext().getString(R.string.selected)}"
                    binding.fragmentDailyNavigateLayoutEditLineTwoSelectedTotal.text =
                        "${requireContext().getString(R.string.Total)} : ${Helper.formatCurrency(state.selectedTotal)}"
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

        btnEditClose.setOnClickListener {
            sharedViewModel.exitSelectionMode()
        }

        btnEditDelete.setOnClickListener {
            val selected = sharedViewModel.selectedTransactions.value
            if (selected.isNotEmpty()) {
                sharedViewModel.deleteAll(selected)
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
