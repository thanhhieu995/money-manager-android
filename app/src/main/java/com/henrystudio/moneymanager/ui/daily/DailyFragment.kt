package com.henrystudio.moneymanager.ui.daily

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
import com.henrystudio.moneymanager.helper.Helper
import com.henrystudio.moneymanager.ui.main.StickyHeaderItemDecoration
import com.henrystudio.moneymanager.ui.main.TransactionGroupAdapter
import com.henrystudio.moneymanager.viewmodel.TransactionViewModel
import com.henrystudio.moneymanager.viewmodel.TransactionViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.henrystudio.moneymanager.helper.FilterTransactions
import com.henrystudio.moneymanager.model.*
import com.henrystudio.moneymanager.ui.addtransaction.SharedTransactionHolder
import com.henrystudio.moneymanager.ui.bottomNavigation.dailyNavigate.PrefsManager.loadLastDate
import com.henrystudio.moneymanager.ui.bottomNavigation.dailyNavigate.PrefsManager.saveLastDate
import com.henrystudio.moneymanager.ui.main.MainActivity

class DailyFragment : Fragment() {
    private lateinit var adapter: TransactionGroupAdapter
    private var _binding: FragmentDailyBinding? = null
    private val binding get() = _binding!!
    private var transactionGroupListFilter: List<TransactionGroup> = emptyList()
    private var allTransactions: List<TransactionGroup> = emptyList()
    private var month: LocalDate? = null
    private var selectedList: List<Transaction> = emptyList()
    private var keyFilter: KeyFilter = KeyFilter.CategoryParent
    private var categoryType : CategoryType = CategoryType.EXPENSE

    @RequiresApi(Build.VERSION_CODES.O)
    private var filterOption: FilterOption =
        FilterOption(FilterPeriodStatistic.Monthly, LocalDate.now())

    private val viewModel: TransactionViewModel by activityViewModels {
        TransactionViewModelFactory(
            AppDatabase.getDatabase(requireActivity().application).transactionDao()
        )
    }

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
        categoryType = arguments?.getSerializable(ARG_CATEGORY_TYPE) as? CategoryType ?: CategoryType.EXPENSE
        keyFilter = arguments?.getSerializable(ARG_CATEGORY_CHILD_CLICK) as? KeyFilter ?: KeyFilter.CategoryParent

        adapter = TransactionGroupAdapter()
        binding.transactionList.layoutManager = LinearLayoutManager(requireContext())
        binding.transactionList.adapter = adapter

        val decoration = StickyHeaderItemDecoration(
            isHeader = { position -> true }, // mọi item đều có header riêng
            createHeaderView = {
                LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_transaction_header, binding.transactionList, false)
            },
            bindHeader = { header, position ->
                val group = adapter.getGroupAt(position)
                val headerText = header.findViewById<TextView>(R.id.item_transaction_header_date)
                val headerIncome =
                    header.findViewById<TextView>(R.id.item_transaction_header_income)
                val headerExpense =
                    header.findViewById<TextView>(R.id.item_transaction_header_expense)
                val dayPart = group.date.substringBefore("/") // "13"
                val dayOfWeek = group.date.substringAfterLast(" ") // "(Tue)"
                headerText.text = "$dayPart $dayOfWeek"
                headerIncome.text = Helper.formatCurrency(group.income)
                headerExpense.text = Helper.formatCurrency(group.expense)
            }
        )

        binding.transactionList.addItemDecoration(decoration)

        viewModel.filterOption.observe(viewLifecycleOwner) { option ->
            filterOption = option
            viewModel.combineGroupAndDate.observe(viewLifecycleOwner) {(transactions, selectedMonth) ->
                updateAllTransactions(transactions, categoryName, categoryType)
                filterAndDisplay(selectedMonth)
            }
        }

        viewModel.combineGroupAndDate.observe(viewLifecycleOwner) {(transactions, selectedMonth) ->
            updateAllTransactions(transactions, categoryName, categoryType)
            filterAndDisplay(selectedMonth)
        }

        adapter.onTransactionLongClick = { transaction ->
            viewModel.enterSelectionMode()
            viewModel.toggleTransactionSelection(transaction)
            updateTransactionItem(transaction)
            true
        }

        adapter.onTransactionClick = { transaction ->
            if (viewModel.selectionMode.value == true) {
                viewModel.toggleTransactionSelection(transaction)
                updateTransactionItem(transaction)
            } else {
                // Mở màn hình sửa
                Helper.openTransactionDetail(requireContext(), transaction)
            }
            true
        }

        // click to change color choose transaction
        adapter.isTransactionSelected = { transaction ->
            val mode = viewModel.selectionMode.value == true
            val selectedList = viewModel.selectedTransactions.value
            val selected = selectedList?.any { it.id == transaction.id } == true
            mode && selected
        }

        // Click close edit layout change color item transaction
        viewModel.selectedTransactions.observe(viewLifecycleOwner) { selectedTransactions ->
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
        }

        val lastDate = loadLastDate(requireContext()) // hàm tự viết lấy từ SharedPreferences
        if (lastDate != null) {
            binding.transactionList.post {
                val position = findPositionForDate(getFilterListGroupTransaction(lastDate), lastDate)
                if (position != -1) {
                    (binding.transactionList.layoutManager as LinearLayoutManager)
                        .scrollToPositionWithOffset(position, 0)
                }
            }
        }

        binding.transactionList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) { // chỉ khi user dừng scroll
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
            viewModel.setCurrentFilterDate(dateShare)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun scrollToWeek(weekStart: LocalDate) {
        val matchedIndex = findPositionForDate(getFilterListGroupTransaction(weekStart), weekStart)
        if (matchedIndex != -1) {
            binding.transactionList.scrollToPosition(matchedIndex)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateTransactionItem(transaction: Transaction) {
        // Tìm group chứa transaction
        val groupIndex = transactionGroupListFilter.indexOfFirst { group ->
            group.transactions.contains(transaction)
        }
        if (groupIndex != -1) {
            val group = transactionGroupListFilter[groupIndex]
            val transactionIndex = group.transactions.indexOf(transaction)

            val childAdapter = adapter.getChildAdapterForGroup(group.date) // custom method
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

        fun newDailyInstance(categoryName: String?, keyFilter: KeyFilter, categoryType: CategoryType): DailyFragment {
            val fragment = DailyFragment()
            val args = Bundle()
            args.putString(ARG_CATEGORY_NAME, categoryName ?: "")
            args.putSerializable(ARG_CATEGORY_CHILD_CLICK, keyFilter)
            args.putSerializable(ARG_CATEGORY_TYPE, categoryType)
            fragment.arguments = args
            return fragment
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateAllTransactions(transactions: List<TransactionGroup>, categoryName: String?, categoryType: CategoryType) {
        allTransactions = if (categoryName != null) {
            val isInCome = categoryType == CategoryType.INCOME
            transactions.mapNotNull { group ->
                val filteredTransactions = when (keyFilter) {
                    KeyFilter.CategoryParent -> {
                        group.transactions.filter {
                            it.categoryParentName.equals(categoryName, ignoreCase = true)
                        }
                    }
                    KeyFilter.CategorySub -> {
                        group.transactions.filter {
                            it.categorySubName.trim().equals(categoryName.trim(), ignoreCase = true)
                        }
                    }
                    KeyFilter.Note -> {
                        group.transactions.filter {
                            it.note.trim().equals(categoryName.trim(), ignoreCase = true) && it.isIncome == isInCome
                        }
                    }
                    KeyFilter.Account -> {
                        group.transactions.filter {
                            it.account.trim().equals(categoryName.trim(), ignoreCase = true)
                        }
                    }
                    else -> group.transactions.filter { it.isIncome == isInCome }
                }

                if (filteredTransactions.isNotEmpty()) {
                    group.copy(
                        income = filteredTransactions.filter { it.isIncome }.sumOf { it.amount },
                        expense = filteredTransactions.filter { !it.isIncome }.sumOf { it.amount },
                        transactions = filteredTransactions
                    )
                } else null
            }
        } else {
            transactions
        }
        val filteredList =
            month?.let { FilterTransactions.filterTransactionGroupByMonth(allTransactions, it) }
                ?: emptyList()
        transactionGroupListFilter = filteredList
        adapter.submitList(filteredList)
        binding.noDataText.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun filterAndDisplay(selectedMonth: LocalDate) {
        val firstDayOfMonth = LocalDate.of(selectedMonth.year, selectedMonth.month, 1)
        val lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth())
        month = lastDayOfMonth
        val filtered = getFilterListGroupTransaction(selectedMonth)
        adapter.submitList(filtered)
        binding.noDataText.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE

        if (SharedTransactionHolder.scrollToAddedTransaction) {
            // 🟡 Scroll đến ngày vừa thêm
            val targetPosition = findPositionForDate(filtered, selectedMonth)
            if (targetPosition >= 0) {
                binding.transactionList.scrollToPosition(targetPosition)
            }
            SharedTransactionHolder.scrollToAddedTransaction = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getFilterListGroupTransaction(selectedMonth: LocalDate): List<TransactionGroup> {
        val firstDayOfMonth = LocalDate.of(selectedMonth.year, selectedMonth.month, 1)
        val lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth())
        return if (requireActivity() is MainActivity) {
            adapter.setFilterYear(false)
            FilterTransactions.filterTransactionGroupByMonth(allTransactions, lastDayOfMonth)
        } else {
            when (filterOption.type) {
                FilterPeriodStatistic.Weekly -> {
                    adapter.setFilterYear(false)
                    FilterTransactions.filterTransactionGroupByWeek(allTransactions, selectedMonth)
                }
                FilterPeriodStatistic.Monthly -> {
                    adapter.setFilterYear(false)
                    FilterTransactions.filterTransactionGroupByMonth(allTransactions, lastDayOfMonth)
                }
                FilterPeriodStatistic.Yearly -> {
                    adapter.setFilterYear(true)
                    FilterTransactions.filterTransactionGroupByYear(allTransactions, lastDayOfMonth)
                }
                else -> {
                    adapter.setFilterYear(false)
                    FilterTransactions.filterTransactionGroupByMonth(allTransactions, lastDayOfMonth)
                }
            }
        }
    }
}