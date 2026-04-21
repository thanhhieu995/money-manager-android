package com.henrystudio.moneymanager.presentation.daily

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.databinding.FragmentDailyBinding
import com.henrystudio.moneymanager.presentation.viewmodel.SharedTransactionViewModel
import java.time.LocalDate
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.henrystudio.moneymanager.presentation.addtransaction.components.viewholder.SharedTransactionHolder
import com.henrystudio.moneymanager.presentation.model.UiState
import com.henrystudio.moneymanager.presentation.daily.components.adapter.DailyAdapter
import com.henrystudio.moneymanager.presentation.daily.model.DailyAction
import com.henrystudio.moneymanager.presentation.daily.model.DailyEvent
import com.henrystudio.moneymanager.presentation.daily.model.DailyListItem
import com.henrystudio.moneymanager.presentation.model.KeyFilter
import com.henrystudio.moneymanager.presentation.model.TransactionType
import com.henrystudio.moneymanager.presentation.viewmodel.TransactionAction
import com.henrystudio.moneymanager.presentation.main.MainActivity
import com.henrystudio.moneymanager.presentation.views.main.StickyHeaderItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class DailyFragment : Fragment() {
    private lateinit var adapter: DailyAdapter
    private var _binding: FragmentDailyBinding? = null
    private val binding get() = _binding!!
    private var currentList: List<DailyListItem> = emptyList()
    private var hasHandledInitialScroll = false
    private var keyFilter: KeyFilter = KeyFilter.CategoryParent
    private var transactionType: TransactionType = TransactionType.EXPENSE
    private val sharedViewModel: SharedTransactionViewModel by activityViewModels()
    private val viewModel: DailyViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDailyBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoryName = arguments?.getString(ARG_CATEGORY_NAME)
        transactionType = arguments?.getSerializable(ARG_CATEGORY_TYPE) as? TransactionType
            ?: TransactionType.EXPENSE
        keyFilter = arguments?.getSerializable(ARG_CATEGORY_CHILD_CLICK) as? KeyFilter ?: KeyFilter.CategoryParent

        adapter = DailyAdapter(
            onClick = {transaction ->
                sharedViewModel.onAction(TransactionAction.OnTransactionClick(transaction))
            },
            onLongClick = {transaction ->
                sharedViewModel.onAction(TransactionAction.OnTransactionLongClick(transaction))
            }
        )
        binding.transactionList.layoutManager = LinearLayoutManager(requireContext())
        binding.transactionList.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            val decoration = StickyHeaderItemDecoration(
                isHeader = { position ->
                    adapter.getItemAt(position) is DailyListItem.Header
                           },
                createHeaderView = {
                    LayoutInflater.from(requireContext())
                        .inflate(R.layout.item_transaction_header, binding.transactionList, false)
                },
                bindHeader = { header, position ->
                    val item = adapter.getItemAt(position)
                    if (item is DailyListItem.Header) {
                        val headerText = header.findViewById<TextView>(R.id.item_transaction_header_date)
                        val headerIncome = header.findViewById<TextView>(R.id.item_transaction_header_income)
                        val headerExpense = header.findViewById<TextView>(R.id.item_transaction_header_expense)

                        val headerUi = viewModel.mapHeader(item)
                        headerText.text = headerUi.dateText
                        headerIncome.text = headerUi.incomeText
                        headerExpense.text = headerUi.expenseText
                    }
                }
            )
            binding.transactionList.addItemDecoration(decoration)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    sharedViewModel.categoriesState.collect { categories ->
                        adapter.setCategories(categories)
                    }
                }
                launch {
                    viewModel.uiState.collect { uiState ->
                        renderUi(uiState)
                    }
                }

                launch {
                    viewModel.event.collect { event ->
                        when(event) {
                            is DailyEvent.ScrollToPosition -> {
                                (binding.transactionList.layoutManager as LinearLayoutManager)
                                    .scrollToPositionWithOffset(event.position, 0)
                            }
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            sharedViewModel.openDetail.collect { transaction ->
                viewModel.onOpenTransactionDetail(requireContext(), transaction)
            }
        }

        binding.transactionList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val lm = recyclerView.layoutManager as LinearLayoutManager
                    val firstPos = lm.findFirstVisibleItemPosition()
                    if (firstPos != RecyclerView.NO_POSITION) {
                        val item = adapter.getItemAt(firstPos)
                        if (item is DailyListItem.TransactionItem) {
                            val date = viewModel.onParseEpochMillisToLocalDate(item.transaction.date)
                            viewModel.onAction(DailyAction.OnScrollStopped(date))
                        }
                    }
                }
            }
        })

        // 1. Set params
        viewModel.setParamsProcessData(
            categoryName = categoryName,
            transactionType = transactionType,
            keyFilter = keyFilter,
            isFromMainActivity = requireActivity() is MainActivity
        )

        // 2. Bind flow
        val combinedDataFlow = combine(
            sharedViewModel.combineGroupAndDate,
            sharedViewModel.filterOption
        ) { (transactions, selectedMonth), option ->
            Triple(transactions, selectedMonth, option)
        }

        val combineSelectionFlow = combine(
            sharedViewModel.selectionMode,
            sharedViewModel.selectedTransactions
        ) { mode, selected ->
            mode to selected
        }

        viewModel.bindProcessData(combinedDataFlow)
        viewModel.bindSelection(combineSelectionFlow)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        val dateShare = SharedTransactionHolder.currentFilterDate
        if (dateShare != null) {
            sharedViewModel.setCurrentFilterDate(dateShare)
            SharedTransactionHolder.currentFilterDate = null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun scrollToWeek(weekStart: LocalDate) {
        val matchedIndex = viewModel.findPositionForDate(currentList, weekStart)
        if (matchedIndex != -1) {
            binding.transactionList.scrollToPosition(matchedIndex)
            viewModel.onScrollStopped(weekStart)
        }
        SharedTransactionHolder.navigateFromMonthly = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun renderUi(uiState: DailyUiState) {
        currentList = uiState.dailyListItems

        when (uiState.dailyListItemState) {
            is UiState.Loading -> {
                renderLoading()
            }
            is UiState.Empty -> {
                renderEmpty()
            }
            is UiState.Success -> {
               renderSuccess()
                adapter.submitList(uiState.dailyListItems)
                if (!hasHandledInitialScroll) {
                    viewModel.handleInitialScroll(
                        isNavigateFromMonthly = SharedTransactionHolder.navigateFromMonthly
                    )
                    hasHandledInitialScroll = true
                }
            }
            else -> {}
        }

        if (SharedTransactionHolder.scrollToAddedTransaction) {
            val targetPosition = viewModel.findPositionForDate((uiState.dailyListItemState as? UiState.Success)?.data ?: emptyList(), uiState.selectedDate)
            if (targetPosition >= 0) {
                binding.transactionList.scrollToPosition(targetPosition)
            }
            SharedTransactionHolder.scrollToAddedTransaction = false
        }
    }

    private fun renderLoading() {
        binding.loadingView.visibility = View.VISIBLE
        binding.transactionList.visibility = View.GONE
        binding.noDataText.visibility = View.GONE
    }

    private fun renderEmpty() {
        binding.loadingView.visibility = View.GONE
        binding.transactionList.visibility = View.GONE
        binding.noDataText.visibility = View.VISIBLE
    }

    private fun renderSuccess() {
        binding.loadingView.visibility = View.GONE
        binding.transactionList.visibility = View.VISIBLE
        binding.noDataText.visibility = View.GONE
    }

    companion object {
        const val ARG_CATEGORY_NAME = "arg_category_name"
        const val ARG_CATEGORY_CHILD_CLICK = "item_click_statistic_keyWord"
        const val ARG_CATEGORY_TYPE = "item_click_statistic_category_type"

        fun newDailyInstance(categoryName: String?, keyFilter: KeyFilter, transactionType: TransactionType): DailyFragment {
            val fragment = DailyFragment()
            val args = Bundle()
            args.putString(ARG_CATEGORY_NAME, categoryName ?: "")
            args.putSerializable(ARG_CATEGORY_CHILD_CLICK, keyFilter)
            args.putSerializable(ARG_CATEGORY_TYPE, transactionType)
            fragment.arguments = args
            return fragment
        }
    }
}
