package com.henrystudio.moneymanager.presentation.views.daily

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
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.presentation.viewmodel.DailyViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.SharedTransactionViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.data.model.TransactionGroup
import com.henrystudio.moneymanager.presentation.addtransaction.components.viewholder.SharedTransactionHolder
import com.henrystudio.moneymanager.presentation.model.KeyFilter
import com.henrystudio.moneymanager.presentation.model.TransactionType
import com.henrystudio.moneymanager.presentation.views.bottomNavigation.dailyNavigate.PrefsManager.loadLastDate
import com.henrystudio.moneymanager.presentation.views.bottomNavigation.dailyNavigate.PrefsManager.saveLastDate
import com.henrystudio.moneymanager.presentation.views.main.MainActivity
import com.henrystudio.moneymanager.presentation.views.main.StickyHeaderItemDecoration
import com.henrystudio.moneymanager.presentation.views.main.TransactionGroupAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class DailyFragment : Fragment() {
    private lateinit var adapter: TransactionGroupAdapter
    private var _binding: FragmentDailyBinding? = null
    private val binding get() = _binding!!
    private var currentList: List<TransactionGroup> = emptyList()
    private var selectedList: List<Transaction> = emptyList()
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

        adapter = TransactionGroupAdapter()
        binding.transactionList.layoutManager = LinearLayoutManager(requireContext())
        binding.transactionList.adapter = adapter

        val decoration = StickyHeaderItemDecoration(
            isHeader = { position -> true },
            createHeaderView = {
                LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_transaction_header, binding.transactionList, false)
            },
            bindHeader = { header, position ->
                val group = adapter.getGroupAt(position)
                val headerText = header.findViewById<TextView>(R.id.item_transaction_header_date)
                val headerIncome = header.findViewById<TextView>(R.id.item_transaction_header_income)
                val headerExpense = header.findViewById<TextView>(R.id.item_transaction_header_expense)

                val cleanedDate = group.date.substringBefore(" ")
                val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yy", Locale.getDefault())
                val localDate = LocalDate.parse(cleanedDate, inputFormatter)
                val currentLocale = requireContext().resources.configuration.locales[0]

                val dayPart = localDate.format(DateTimeFormatter.ofPattern("dd", currentLocale))
                val dayOfWeek = localDate.format(DateTimeFormatter.ofPattern("EEE", currentLocale))
                headerText.text = "$dayPart $dayOfWeek"
                headerIncome.text = Helper.formatCurrency(group.income)
                headerExpense.text = Helper.formatCurrency(group.expense)
            }
        )
        binding.transactionList.addItemDecoration(decoration)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    combine(
                        sharedViewModel.combineGroupAndDate,
                        sharedViewModel.filterOption
                    ) { (transactions, selectedMonth), option ->
                        Triple(transactions, selectedMonth, option)
                    }.collect { (transactions, selectedMonth, option) ->
                        viewModel.updateData(
                            transactions = transactions,
                            filterOption = option,
                            selectedMonth = selectedMonth,
                            categoryName = categoryName,
                            transactionType = transactionType,
                            keyFilter = keyFilter,
                            isFromMainActivity = requireActivity() is MainActivity
                        )
                    }
                }

                launch {
                    viewModel.uiState.collect { uiState ->
                        currentList = uiState.transactions
                        adapter.setFilterYear(uiState.isYearly)
                        adapter.submitList(uiState.transactions)
                        binding.noDataText.visibility = if (uiState.isEmpty) View.VISIBLE else View.GONE

                        if (SharedTransactionHolder.scrollToAddedTransaction) {
                            val targetPosition = findPositionForDate(uiState.transactions, uiState.selectedDate)
                            if (targetPosition >= 0) {
                                binding.transactionList.scrollToPosition(targetPosition)
                            }
                            SharedTransactionHolder.scrollToAddedTransaction = false
                        }
                    }
                }

                launch {
                    sharedViewModel.selectedTransactions.collect { selectedTransactions ->
                        if (selectedTransactions.isEmpty()) {
                            for (tx in selectedList) {
                                val childAdapter = adapter.getChildAdapterForGroup(tx.date) ?: continue
                                childAdapter.updateTransaction(tx)
                            }
                        } else {
                            val added = selectedTransactions.filterNot { selectedList.contains(it) }
                            val removed = selectedList.filterNot { selectedTransactions.contains(it) }

                            for (tx in added + removed) {
                                val childAdapter = adapter.getChildAdapterForGroup(tx.date) ?: continue
                                childAdapter.updateTransaction(tx)
                            }
                        }

                        selectedList = selectedTransactions
                        viewModel.updateSelection(
                            selectionMode = sharedViewModel.selectionMode.value,
                            selectedTransactions = selectedTransactions
                        )
                    }
                }
            }
        }

        adapter.onTransactionLongClick = { transaction ->
            sharedViewModel.enterSelectionMode()
            sharedViewModel.toggleTransactionSelection(transaction)
            updateTransactionItem(transaction)
            true
        }

        adapter.onTransactionClick = { transaction ->
            if (sharedViewModel.selectionMode.value) {
                sharedViewModel.toggleTransactionSelection(transaction)
                updateTransactionItem(transaction)
            } else {
                Helper.openTransactionDetail(requireContext(), transaction)
            }
            true
        }

        adapter.isTransactionSelected = { transaction ->
            val mode = sharedViewModel.selectionMode.value
            val selectedList = sharedViewModel.selectedTransactions.value
            val selected = selectedList.any { it.id == transaction.id }
            mode && selected
        }

        val lastDate = loadLastDate(requireContext())
        if (lastDate != null && !SharedTransactionHolder.navigateFromMonthly) {
            binding.transactionList.post {
                val position = findPositionForDate(currentList, lastDate)
                if (position != -1) {
                    (binding.transactionList.layoutManager as LinearLayoutManager)
                        .scrollToPositionWithOffset(position, 0)
                }
            }
        }

        binding.transactionList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val lm = recyclerView.layoutManager as LinearLayoutManager
                    val firstPos = lm.findFirstVisibleItemPosition()
                    if (firstPos != RecyclerView.NO_POSITION) {
                        val txGroup = adapter.getGroupAt(firstPos)
                        val cleanedDate = txGroup.date.substringBefore(" ")
                        val formatter = DateTimeFormatter.ofPattern("dd/MM/yy")
                        val date = LocalDate.parse(cleanedDate, formatter)
                        saveLastDate(requireContext(), date)
                    }
                }
            }
        })
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
        val matchedIndex = findPositionForDate(currentList, weekStart)
        if (matchedIndex != -1) {
            binding.transactionList.scrollToPosition(matchedIndex)
            saveLastDate(requireContext(), weekStart)
        }
        SharedTransactionHolder.navigateFromMonthly = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateTransactionItem(transaction: Transaction) {
        val groupIndex = currentList.indexOfFirst { group ->
            group.transactions.contains(transaction)
        }
        if (groupIndex != -1) {
            val group = currentList[groupIndex]
            val transactionIndex = group.transactions.indexOf(transaction)
            val childAdapter = adapter.getChildAdapterForGroup(group.date)
            if (childAdapter != null && transactionIndex != -1) {
                childAdapter.notifyItemChanged(transactionIndex)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun findPositionForDate(transactions: List<TransactionGroup>, date: LocalDate): Int {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yy")
        return transactions.indexOfFirst { tx ->
            val cleanedDate = tx.date.substringBefore(" ")
            val txDate = LocalDate.parse(cleanedDate, formatter)
            txDate == date
        }
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
