package com.henrystudio.moneymanager.presentation.bottomNavigation

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
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
import com.henrystudio.moneymanager.core.application.PrefsManager
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.core.util.MonthPickerDialogFragment
import com.henrystudio.moneymanager.databinding.FragmentDailyNavigateBinding
import com.henrystudio.moneymanager.presentation.addtransaction.AddTransactionActivity
import com.henrystudio.moneymanager.presentation.model.UiState
import com.henrystudio.moneymanager.presentation.bookmark.BookmarkActivity
import com.henrystudio.moneymanager.presentation.daily.DailyFragment
import com.henrystudio.moneymanager.presentation.viewmodel.SharedTransactionViewModel
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
    private var fabTouchOffsetX = 0f
    private var fabTouchOffsetY = 0f
    private var isDraggingFab = false
    private var isAddTutorialVisible = false

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
        Log.d("DailyNavigateFragment", "onViewCreated called")
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

                launch {
                    sharedViewModel.allTransactionsState.collect { state ->
                        when (state) {
                            is UiState.Empty -> maybeShowAddTutorial()
                            is UiState.Success -> hideAddTutorial(markSeen = false)
                            else -> Unit
                        }
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
            if (isDraggingFab) return@setOnClickListener
            hideAddTutorial()
            viewModel.onAction(DailyNavigateAction.OnAddTransactionClick)
        }

        binding.fragmentDailyNavigateBtnAdd.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    val parentLocation = IntArray(2)
                    binding.root.getLocationOnScreen(parentLocation)
                    fabTouchOffsetX = event.rawX - parentLocation[0] - view.x
                    fabTouchOffsetY = event.rawY - parentLocation[1] - view.y
                    isDraggingFab = false
                    view.bringToFront()
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val parentLocation = IntArray(2)
                    binding.root.getLocationOnScreen(parentLocation)
                    val nextX = event.rawX - parentLocation[0] - fabTouchOffsetX
                    val nextY = event.rawY - parentLocation[1] - fabTouchOffsetY

                    if (!isDraggingFab && (
                            kotlin.math.abs(nextX - view.x) > 10f ||
                                kotlin.math.abs(nextY - view.y) > 10f
                            )
                    ) {
                        isDraggingFab = true
                    }

                    if (isDraggingFab) {
                        moveFabWithinParent(
                            view = view,
                            nextX = nextX,
                            nextY = nextY
                        )
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    if (isDraggingFab) {
                        snapFabToNearestEdge(view)
                        isDraggingFab = false
                        true
                    } else {
                        view.performClick()
                        true
                    }
                }

                MotionEvent.ACTION_CANCEL -> {
                    isDraggingFab = false
                    true
                }

                else -> false
            }
        }

        binding.fragmentDailyNavigateLayoutEditLineOneBtnClose.setOnClickListener {
            viewModel.onAction(DailyNavigateAction.OnExitSelectionClick)
        }

        binding.fragmentDailyNavigateLayoutEditLineOneBtnDelete.setOnClickListener {
            viewModel.onAction(DailyNavigateAction.OnDeleteSelectionClick)
        }

        binding.fragmentDailyNavigateTutorialOverlay.setOnClickListener {
            hideAddTutorial()
        }

        binding.fragmentDailyNavigateTutorialClose.setOnClickListener {
            hideAddTutorial()
        }

        binding.fragmentDailyNavigateTutorialAction.setOnClickListener {
            hideAddTutorial()
            viewModel.onAction(DailyNavigateAction.OnAddTransactionClick)
        }
    }

    private fun render(state: DailyNavigateUiState) {
        binding.fragmentDailyNavigateMonthText.text = state.monthLabel
        binding.fragmentDailyNavigateIncomeCountAll.text = Helper.formatCurrency(requireContext() ,state.incomeSum)
        binding.fragmentDailyNavigateExpenseCountAll.text = Helper.formatCurrency(requireContext(), state.expenseSum)
        binding.fragmentDailyNavigateTotalCount.text = Helper.formatCurrency(requireContext(), state.totalSum)
        binding.fragmentDailyNavigateLayoutEdit.visibility =
            if (state.selectionMode) View.VISIBLE else View.GONE
        binding.fragmentDailyNavigateLayoutEditLineTwoSelectedCount.text =
            "${state.selectedCount} ${getString(R.string.selected)}"
        binding.fragmentDailyNavigateLayoutEditLineTwoSelectedTotal.text =
            "${getString(R.string.Total)} : ${Helper.formatCurrency(requireContext(), state.selectedTotal)}"
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

    private fun moveFabWithinParent(view: View, nextX: Float, nextY: Float) {
        val parent = binding.root
        val maxX = (parent.width - view.width).toFloat().coerceAtLeast(0f)
        val maxY = (parent.height - view.height).toFloat().coerceAtLeast(0f)

        view.x = nextX.coerceIn(0f, maxX)
        view.y = nextY.coerceIn(0f, maxY)

        if (isAddTutorialVisible) {
            positionAddTutorial()
        }
    }

    private fun snapFabToNearestEdge(view: View) {
        val parent = binding.root
        val maxX = (parent.width - view.width).toFloat().coerceAtLeast(0f)
        val targetX = if (view.x + view.width / 2f < parent.width / 2f) 0f else maxX

        view.animate()
            .x(targetX)
            .setDuration(180L)
            .start()

        if (isAddTutorialVisible) {
            view.postDelayed({ positionAddTutorial() }, 180L)
        }
    }

    private fun maybeShowAddTutorial() {
        if (isAddTutorialVisible) return
        if (PrefsManager.hasSeenAddTutorial(requireContext())) return

        binding.root.post {
            if (!isAdded || _binding == null) return@post
            isAddTutorialVisible = true
            binding.fragmentDailyNavigateTutorialOverlay.visibility = View.VISIBLE
            binding.fragmentDailyNavigateTutorialArrow.visibility = View.VISIBLE
            binding.fragmentDailyNavigateTutorialCard.visibility = View.VISIBLE
            binding.fragmentDailyNavigateTutorialOverlay.post {
                positionAddTutorial()
            }
        }
    }

    private fun hideAddTutorial(markSeen: Boolean = true) {
        if (!isAddTutorialVisible && binding.fragmentDailyNavigateTutorialOverlay.visibility == View.GONE) {
            return
        }

        if (markSeen) {
            PrefsManager.saveHasSeenAddTutorial(requireContext(), true)
        }

        isAddTutorialVisible = false
        binding.fragmentDailyNavigateTutorialOverlay.visibility = View.GONE
        binding.fragmentDailyNavigateTutorialArrow.visibility = View.GONE
        binding.fragmentDailyNavigateTutorialCard.visibility = View.GONE
    }

    private fun positionAddTutorial() {
        val fab = binding.fragmentDailyNavigateBtnAdd
        val overlay = binding.fragmentDailyNavigateTutorialOverlay
        val arrow = binding.fragmentDailyNavigateTutorialArrow
        val card = binding.fragmentDailyNavigateTutorialCard

        if (
            overlay.width == 0 ||
            overlay.height == 0 ||
            fab.width == 0 ||
            fab.height == 0 ||
            arrow.width == 0 ||
            arrow.height == 0 ||
            card.width == 0 ||
            card.height == 0
        ) {
            overlay.post { positionAddTutorial() }
            return
        }

        val spacing = 12f * resources.displayMetrics.density
        val fabCenterX = fab.x + fab.width / 2f

        val arrowX = (fabCenterX - arrow.width / 2f).coerceIn(
            0f,
            (overlay.width - arrow.width).toFloat().coerceAtLeast(0f)
        )
        val arrowY = (fab.y - arrow.height - spacing).coerceAtLeast(0f)

        val cardX = (fabCenterX - card.width / 2f).coerceIn(
            spacing,
            (overlay.width - card.width - spacing).coerceAtLeast(spacing)
        )
        val cardY = (arrowY - card.height - spacing).coerceAtLeast(spacing)

        arrow.x = arrowX
        arrow.y = arrowY
        card.x = cardX
        card.y = cardY
        arrow.bringToFront()
        card.bringToFront()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun navigateToDailyTabAndScrollToWeek(weekStart: LocalDate) {
        binding.fragmentDailyNavigateViewPager.setCurrentItem(0, true)
        binding.fragmentDailyNavigateViewPager.postDelayed({
            val dailyFragment =
                childFragmentManager.findFragmentByTag("f0") as? DailyFragment
            dailyFragment?.scrollToWeek(weekStart)
        }, 100)
    }
}
