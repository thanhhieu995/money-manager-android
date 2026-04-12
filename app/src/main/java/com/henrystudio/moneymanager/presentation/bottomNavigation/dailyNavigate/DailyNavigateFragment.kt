package com.henrystudio.moneymanager.presentation.bottomNavigation.dailyNavigate

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.core.util.MonthPickerDialogFragment
import com.henrystudio.moneymanager.databinding.FragmentDailyNavigateBinding
import com.henrystudio.moneymanager.presentation.addtransaction.AddTransactionActivity
import com.henrystudio.moneymanager.presentation.bookmark.BookmarkActivity
import com.henrystudio.moneymanager.presentation.daily.DailyFragment
import com.henrystudio.moneymanager.presentation.viewmodel.SharedTransactionViewModel
import com.henrystudio.moneymanager.presentation.views.bottomNavigation.dailyNavigate.PrefsManager
import com.henrystudio.moneymanager.presentation.views.main.ViewPagerAdapter
import com.henrystudio.moneymanager.presentation.views.search.SearchActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate

@AndroidEntryPoint
class DailyNavigateFragment : Fragment() {

    private var _binding: FragmentDailyNavigateBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedTransactionViewModel by activityViewModels()
    private val viewModel: DailyNavigateViewModel by viewModels()

    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var pageCallback: ViewPager2.OnPageChangeCallback
    private var isRestoring = false
    private var mediator: TabLayoutMediator? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDailyNavigateBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        registerPageCallback()
        bindViewModel()
        setupListeners()
        syncEditLayoutHeight()
        restoreSavedTab()
    }

    private fun registerPageCallback() {
        pageCallback = object : ViewPager2.OnPageChangeCallback() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (isRestoring) return
                viewModel.onAction(DailyNavigateAction.OnTabChanged(position))
            }
        }
        binding.fragmentDailyNavigateViewPager.registerOnPageChangeCallback(pageCallback)
    }

    override fun onDestroyView() {
        binding.fragmentDailyNavigateViewPager.unregisterOnPageChangeCallback(pageCallback)
        mediator?.detach()
        mediator = null
        _binding = null
        super.onDestroyView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun bindViewModel() {
        viewModel.bind(
            groupedTransactionsState = sharedViewModel.groupedTransactionsState,
            currentFilterDate = sharedViewModel.currentFilterDate,
            currentDailyNavigateTabPosition = sharedViewModel.currentDailyNavigateTabPosition,
            selectionMode = sharedViewModel.selectionMode,
            selectedTransactions = sharedViewModel.selectedTransactions
        )
        viewModel.bindNavigation(sharedViewModel.navigateToWeekFromMonthly)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        render(state)
                    }
                }

                launch {
                    viewModel.effect.collect { effect ->
                        handleEffect(effect)
                    }
                }
            }
        }
    }

    private fun setupViewPager() {
        viewPagerAdapter = ViewPagerAdapter(this)
        binding.fragmentDailyNavigateViewPager.adapter = viewPagerAdapter
        binding.fragmentDailyNavigateViewPager.offscreenPageLimit = 1

        mediator = TabLayoutMediator(
            binding.fragmentDailyNavigateTabLayout,
            binding.fragmentDailyNavigateViewPager
        ) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.daily)
                1 -> getString(R.string.calendar)
                else -> getString(R.string.monthly)
            }
        }.also { it.attach() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupListeners() {
        binding.fragmentDailyNavigateMonthText.setOnClickListener {
            MonthPickerDialogFragment { month, year ->
                viewModel.onAction(DailyNavigateAction.OnMonthPicked(month, year))
            }.show(parentFragmentManager, "monthPicker")
        }

        binding.fragmentDailyNavigateMonthBack.setOnClickListener {
            viewModel.onAction(DailyNavigateAction.OnPreviousPeriodClick)
        }

        binding.fragmentDailyNavigateMonthNext.setOnClickListener {
            viewModel.onAction(DailyNavigateAction.OnNextPeriodClick)
        }

        binding.fragmentDailyNavigateSearch.setOnClickListener {
            viewModel.onAction(DailyNavigateAction.OnSearchClick)
        }

        binding.fragmentDailyNavigateBookmark.setOnClickListener {
            viewModel.onAction(DailyNavigateAction.OnBookmarkClick)
        }

        binding.fragmentDailyNavigateBtnAdd.setOnClickListener {
            viewModel.onAction(DailyNavigateAction.OnAddTransactionClick)
        }

        binding.fragmentDailyNavigateLayoutEditLineOneBtnClose.setOnClickListener {
            viewModel.onAction(DailyNavigateAction.OnExitSelectionClick)
        }

        binding.fragmentDailyNavigateLayoutEditLineOneBtnDelete.setOnClickListener {
            viewModel.onAction(DailyNavigateAction.OnDeleteSelectionClick)
        }
    }

    private fun render(state: DailyNavigateUiState) {
        binding.fragmentDailyNavigateMonthText.text = state.monthLabel
        binding.fragmentDailyNavigateIncomeCountAll.text = Helper.formatCurrency(state.incomeSum)
        binding.fragmentDailyNavigateExpenseCountAll.text = Helper.formatCurrency(state.expenseSum)
        binding.fragmentDailyNavigateTotalCount.text = Helper.formatCurrency(state.totalSum)
        binding.fragmentDailyNavigateLayoutEdit.visibility =
            if (state.selectionMode) View.VISIBLE else View.GONE
        binding.fragmentDailyNavigateLayoutEditLineTwoSelectedCount.text =
            "${state.selectedCount} ${getString(R.string.selected)}"
        binding.fragmentDailyNavigateLayoutEditLineTwoSelectedTotal.text =
            "${getString(R.string.Total)} : ${Helper.formatCurrency(state.selectedTotal)}"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleEffect(effect: DailyNavigateEffect) {
        when (effect) {
            DailyNavigateEffect.OpenAddTransaction -> {
                startActivity(Intent(requireContext(), AddTransactionActivity::class.java))
                requireActivity().overridePendingTransition(
                    R.anim.slide_in_right,
                    R.anim.no_animation
                )
            }

            DailyNavigateEffect.OpenBookmark -> {
                startActivity(Intent(requireContext(), BookmarkActivity::class.java))
                requireActivity().overridePendingTransition(
                    R.anim.slide_in_bottom,
                    R.anim.no_animation
                )
            }

            DailyNavigateEffect.OpenSearch -> {
                startActivity(Intent(requireContext(), SearchActivity::class.java))
                requireActivity().overridePendingTransition(
                    R.anim.slide_in_bottom,
                    R.anim.no_animation
                )
            }

            is DailyNavigateEffect.ChangeMonth -> {
                sharedViewModel.changeMonth(effect.offset)
            }

            is DailyNavigateEffect.ChangeYear -> {
                sharedViewModel.changeYear(effect.offset)
            }

            is DailyNavigateEffect.DeleteSelectedTransactions -> {
                sharedViewModel.deleteAll(effect.transactions)
                sharedViewModel.exitSelectionMode()
            }

            DailyNavigateEffect.ExitSelectionMode -> {
                sharedViewModel.exitSelectionMode()
            }

            is DailyNavigateEffect.NavigateToDailyWeek -> {
                navigateToDailyTabAndScrollToWeek(effect.date)
            }

            is DailyNavigateEffect.PersistTabPosition -> {
                PrefsManager.saveTabPosition(requireContext(), effect.position)
            }

            is DailyNavigateEffect.UpdateCurrentFilterDate -> {
                sharedViewModel.setLocalDateCurrentFilterDate(effect.date)
            }

            is DailyNavigateEffect.UpdateCurrentTab -> {
                sharedViewModel.setCurrentDailyNavigateTab(effect.position)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun restoreSavedTab() {
        val savedPos = PrefsManager.getTabPosition(requireContext())
        sharedViewModel.setCurrentDailyNavigateTab(savedPos)
        binding.fragmentDailyNavigateViewPager.setCurrentItem(savedPos, false)
        isRestoring = true

        binding.fragmentDailyNavigateTabLayout.post {
            binding.fragmentDailyNavigateTabLayout.getTabAt(savedPos)?.select()
            binding.fragmentDailyNavigateViewPager.post {
                binding.fragmentDailyNavigateViewPager.setCurrentItem(savedPos, false)
                binding.fragmentDailyNavigateViewPager.post {
                    isRestoring = false
                }
            }
        }
    }

    private fun syncEditLayoutHeight() {
        binding.fragmentDailyNavigateLayoutFunctionControl.viewTreeObserver
            .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.fragmentDailyNavigateLayoutFunctionControl.viewTreeObserver
                        .removeOnGlobalLayoutListener(this)

                    val params = binding.fragmentDailyNavigateLayoutEdit.layoutParams
                    params.height = binding.fragmentDailyNavigateLayoutFunctionControl.height
                    binding.fragmentDailyNavigateLayoutEdit.layoutParams = params
                }
            })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun navigateToDailyTabAndScrollToWeek(weekStart: LocalDate) {
        binding.fragmentDailyNavigateViewPager.setCurrentItem(0, true)
        binding.fragmentDailyNavigateViewPager.postDelayed({
            val dailyFragment = viewPagerAdapter.getCurrentFragment(0) as? DailyFragment
            dailyFragment?.scrollToWeek(weekStart)
        }, 100)
    }
}
