package com.henrystudio.moneymanager.ui.daily

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.databinding.FragmentDailyBinding
import com.henrystudio.moneymanager.helper.Helper
import com.henrystudio.moneymanager.model.AppDatabase
import com.henrystudio.moneymanager.model.Transaction
import com.henrystudio.moneymanager.model.TransactionGroup
import com.henrystudio.moneymanager.ui.main.StickyHeaderItemDecoration
import com.henrystudio.moneymanager.ui.main.TransactionGroupAdapter
import com.henrystudio.moneymanager.viewmodel.TransactionViewModel
import com.henrystudio.moneymanager.viewmodel.TransactionViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DailyFragment : Fragment() {
    private lateinit var viewModel: TransactionViewModel
    private lateinit var adapter: TransactionGroupAdapter
    private var _binding: FragmentDailyBinding? = null
    private val binding get() = _binding!!
    private var transactionGroupListFilter: List<TransactionGroup> = emptyList()
    private var allTransactions: List<TransactionGroup> = emptyList()
    private var month: LocalDate? = null
    private var selectedList: List<Transaction> = emptyList()

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

        val dao = AppDatabase.getDatabase(requireActivity().application).transactionDao()
        val factory = TransactionViewModelFactory(dao)
        viewModel = ViewModelProvider(requireActivity(), factory)[TransactionViewModel::class.java]

        adapter = TransactionGroupAdapter()
        binding.transactionList.layoutManager = LinearLayoutManager(requireContext())
        binding.transactionList.adapter = adapter

        val decoration = StickyHeaderItemDecoration(
            isHeader = { position -> true }, // mọi item đều có header riêng
            createHeaderView = {
                LayoutInflater.from(requireContext()).inflate(R.layout.item_transaction_header, binding.transactionList, false)
            },
            bindHeader = { header, position ->
                val group = adapter.getGroupAt(position)
                val headerText = header.findViewById<TextView>(R.id.item_transaction_header_date)
                val headerIncome = header.findViewById<TextView>(R.id.item_transaction_header_income)
                val headerExpense = header.findViewById<TextView>(R.id.item_transaction_header_expense)
                val dayPart = group.date.substringBefore("/") // "13"
                val dayOfWeek = group.date.substringAfterLast(" ") // "(Tue)"
                headerText.text = "$dayPart $dayOfWeek"
                headerIncome.text = Helper.formatCurrency(group.income)
                headerExpense.text = Helper.formatCurrency(group.expense)
            }
        )

        binding.transactionList.addItemDecoration(decoration)

        viewModel.groupedTransactions.observe(viewLifecycleOwner) { transactions ->
            allTransactions = transactions
            // Sắp xếp ngày giảm dần
            val filteredList =
                month?.let { com.henrystudio.moneymanager.helper.FilterTransactions.filterTransactionsByMonth(transactions, it) } ?: emptyList()
            transactionGroupListFilter = filteredList
            adapter.submitList(filteredList)
            binding.noDataText.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.currentMonthYear.observe(viewLifecycleOwner) { selectedMonth ->
            month = selectedMonth
            val filtered = com.henrystudio.moneymanager.helper.FilterTransactions.filterTransactionsByMonth(allTransactions, selectedMonth)
            adapter.submitList(filtered)
            binding.noDataText.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
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

        // Adapter listen click update color item transaction
        adapter.isTransactionSelected = { transaction ->
            viewModel.selectionMode.value == true &&
                    viewModel.selectedTransactions.value?.contains(transaction) == true
        }

        // Click close edit layout change color item transaction
        viewModel.selectedTransactions.observe(viewLifecycleOwner) { selectedTransactions ->
            if (selectedTransactions.isEmpty()) {
                val allGroups = allTransactions
                for (tx in selectedList) {
                    for (group in allGroups) {
                        if (group.transactions.contains(tx)) {
                            val childAdapter = adapter.getChildAdapterForGroup(group.date) ?: continue
                            childAdapter.updateTransaction(tx)
                            break
                        }
                    }
                }
            }
            selectedList = selectedTransactions
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun scrollToWeek(weekStart: LocalDate) {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yy")
        val weekDates = (0..6).map { weekStart.plusDays(it.toLong()).format(formatter) }

        val matchedIndex = transactionGroupListFilter.indexOfFirst { group ->
            weekDates.contains(group.date.substringBefore(" "))
        }

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
}