package com.henrystudio.moneymanager.presentation.views.bottomNavigation.statistic

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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.databinding.FragmentStatisticViewPagerBinding
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.presentation.addtransaction.components.viewholder.SharedTransactionHolder
import com.henrystudio.moneymanager.presentation.model.FilterOption
import com.henrystudio.moneymanager.presentation.model.FilterPeriodStatistic
import com.henrystudio.moneymanager.presentation.model.TransactionType
import com.henrystudio.moneymanager.presentation.model.stringRes
import com.henrystudio.moneymanager.presentation.viewmodel.SharedTransactionViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.StatisticViewPagerViewModel
import com.henrystudio.moneymanager.core.application.PrefsManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate

@AndroidEntryPoint
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
    private var _binding: FragmentStatisticViewPagerBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedTransactionViewModel by activityViewModels()
    private val viewModel: StatisticViewPagerViewModel by viewModels()
    private lateinit var pageCallback: ViewPager2.OnPageChangeCallback
    private var isRestoring = false
    private var mediator: TabLayoutMediator? = null

    @RequiresApi(Build.VERSION_CODES.O)
    private var filterOptionTemp: FilterOption =
        FilterOption(FilterPeriodStatistic.Monthly, LocalDate.now())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticViewPagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        adapter = StatisticPagerAdapter(this)
        viewPager.adapter = adapter
        mediator = TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> requireContext().getString(R.string.Stats)
                1 -> requireContext().getString(R.string.Acc)
                2 -> requireContext().getString(R.string.Note)
                else -> ""
            }
        }.also { it.attach() }

        val savedPos = PrefsManager.getStatisticTabPosition(requireContext())
        viewPager.setCurrentItem(savedPos, false)
        isRestoring = true
        tabLayout.post {
            tabLayout.getTabAt(savedPos)?.select()
            viewPager.post {
                viewPager.setCurrentItem(savedPos, false)
                viewPager.post { isRestoring = false }
            }
        }

        sharedViewModel.setFilter(filterOptionTemp.type, filterOptionTemp.date)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    sharedViewModel.statisticListTransactionFilter.collect { list ->
                        viewModel.updateFilteredTransactions(list)
                    }
                }
                launch {
                    sharedViewModel.statisticTransactionType.collect { type ->
                        viewModel.updateTransactionType(type)
                    }
                }
                launch {
                    sharedViewModel.filterOption.collect { filterOption ->
                        filterOptionTemp = filterOption
                        viewModel.updateFilterOption(filterOption)
                    }
                }
                launch {
                    sharedViewModel.currentStatisticTabPosition.collect { position ->
                        viewModel.updateTabPosition(position)
                    }
                }
                launch {
                    viewModel.uiState.collect { state ->
                        filterDropdown.text = getString(state.filterOption.type.stringRes)
                        selectedOption = state.filterOption.type
                        monthText.text = Helper.getUpdateMonthText(state.filterOption)
                        toggleGroupButton.check(
                            if (state.transactionType == TransactionType.INCOME) incomeBtn.id else expenseBtn.id
                        )
                        incomeBtn.text = getString(R.string.Income) + " " + Helper.formatCurrency(requireContext() ,state.totalIncome)
                        expenseBtn.text = getString(R.string.exp) + " " + Helper.formatCurrency(requireContext(), state.totalExpense)
                        viewPager.currentItem = state.currentTabPosition
                    }
                }
            }
        }

        filterDropdown.setOnClickListener {
            showDialogOption()
        }
        toggleGroupButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val type = when (checkedId) {
                    incomeBtn.id -> TransactionType.INCOME
                    else -> TransactionType.EXPENSE
                }
                sharedViewModel.setStatisticTransactionType(type)
            }
        }

        monthBack.setOnClickListener {
            when (filterOptionTemp.type) {
                FilterPeriodStatistic.Monthly -> sharedViewModel.changeMonth(-1)
                FilterPeriodStatistic.Weekly -> sharedViewModel.changeWeek(-1)
                FilterPeriodStatistic.Yearly -> sharedViewModel.changeYear(-1)
                else -> {}
            }
        }

        monthNext.setOnClickListener {
            when (filterOptionTemp.type) {
                FilterPeriodStatistic.Monthly -> sharedViewModel.changeMonth(1)
                FilterPeriodStatistic.Weekly -> sharedViewModel.changeWeek(1)
                FilterPeriodStatistic.Yearly -> sharedViewModel.changeYear(1)
                else -> {}
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        val shareDate = SharedTransactionHolder.currentFilterDate
        val shareFilterOption = SharedTransactionHolder.filterOption
        if (shareDate != null) {
            sharedViewModel.setCurrentFilterDate(shareDate)
        }
        if (shareFilterOption != null) {
            sharedViewModel.setFilter(shareFilterOption.type, shareFilterOption.date)
        }
        pageCallback = object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (isRestoring) return
                PrefsManager.saveStatisticTabPosition(requireContext(), position)
                sharedViewModel.setCurrentStatisticTab(position)
            }
        }
        viewPager.registerOnPageChangeCallback(pageCallback)
    }

    override fun onPause() {
        super.onPause()
        viewPager.unregisterOnPageChangeCallback(pageCallback)
    }

    private fun init() {
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
            FilterPeriodStatistic.Weekly to view.findViewById<ImageView>(R.id.item_statistic_dialog_imgWeeklyCheck),
            FilterPeriodStatistic.Monthly to view.findViewById<ImageView>(R.id.item_statistic_dialog_imgMonthlyCheck),
            FilterPeriodStatistic.Yearly to view.findViewById<ImageView>(R.id.item_statistic_dialog_imgYearlyCheck),
            FilterPeriodStatistic.List to view.findViewById<ImageView>(R.id.item_statistic_dialog_imgListCheck),
            FilterPeriodStatistic.Trend to view.findViewById<ImageView>(R.id.item_statistic_dialog_imgTrendCheck),
        )

        val optionLayouts = mapOf(
            FilterPeriodStatistic.Weekly to R.id.item_statistic_dialog_weeklyLayout,
            FilterPeriodStatistic.Monthly to R.id.item_statistic_dialog_monthlyLayout,
            FilterPeriodStatistic.Yearly to R.id.item_statistic_dialog_yearlyLayout,
            FilterPeriodStatistic.List to R.id.item_statistic_dialog_listLayout,
            FilterPeriodStatistic.Trend to R.id.item_statistic_dialog_trendLayout
        )

        fun updateCheckMarks(selected: FilterPeriodStatistic) {
            filterDropdown.text = getString(selected.stringRes)
            checkViews.forEach { (option, imageView) ->
                imageView.visibility = if (option == selected) View.VISIBLE else View.GONE
            }
            bottomSheetDialog.dismiss()
        }

        updateCheckMarks(selectedOption)

        optionLayouts.forEach { (filterPeriod, layoutId) ->
            view.findViewById<LinearLayout>(layoutId).setOnClickListener {
                when (filterPeriod) {
                    FilterPeriodStatistic.List -> {
                        selectedOption = filterOptionTemp.type
                        updateCheckMarks(selectedOption)
                        val intent = Intent(requireContext(), StatisticListActivity::class.java)
                        intent.putExtra("filterOption", filterOptionTemp)
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
                        intent.putExtra("currentFilterPeriodStatistic", filterPeriod)
                        startActivity(intent)
                        requireActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.no_animation)
                        bottomSheetDialog.dismiss()
                    }
                    else -> {
                        selectedOption = filterPeriod
                        updateCheckMarks(filterPeriod)
                        sharedViewModel.setFilter(filterPeriod, filterOptionTemp.date)
                    }
                }
            }
        }

        view.findViewById<TextView>(R.id.item_statistic_dialog_optionCancel).setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
