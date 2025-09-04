package com.henrystudio.moneymanager.ui.bottomNavigation.statistic

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.databinding.FragmentStatisticNoteBinding
import com.henrystudio.moneymanager.helper.FilterTransactions
import com.henrystudio.moneymanager.model.*
import com.henrystudio.moneymanager.viewmodel.TransactionViewModel
import com.henrystudio.moneymanager.viewmodel.TransactionViewModelFactory
import java.time.LocalDate

class StatisticNoteFragment : Fragment() {
    private var _binding: FragmentStatisticNoteBinding? = null
    private val binding get() = _binding!!
    private lateinit var tvNote: TextView
    private lateinit var tvCount: TextView
    private lateinit var tvAmount: TextView
    private lateinit var tvNoData: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NoteAdapter
    private var listNotes: List<Note> = emptyList()
    private var currentSortField = SortField.AMOUNT
    private var currentSortOrder = SortOrder.DESC
    private var categoryType: CategoryType = CategoryType.EXPENSE
    @RequiresApi(Build.VERSION_CODES.O)
    private var filterOptionTemp : FilterOption = FilterOption(FilterPeriodStatistic.Monthly, LocalDate.now())

    private val viewModel: TransactionViewModel by activityViewModels {
        TransactionViewModelFactory(
            AppDatabase.getDatabase(requireActivity().application).transactionDao()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentStatisticNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        adapter = NoteAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        viewModel.combinedFilter.observe(viewLifecycleOwner) { (type, filterList) ->
            categoryType = type
            val transactionsType = when (type) {
                CategoryType.INCOME -> filterList.filter { it.isIncome }
                CategoryType.EXPENSE -> filterList.filter { !it.isIncome }
            }
            listNotes = getListNoteFilter(transactionsType)
            sortAndUpdate()
            updateSortIndicators()
        }

        viewModel.filterOption.observe(viewLifecycleOwner) {filterOption ->
            filterOptionTemp = filterOption
            viewModel.allTransactions.observe(viewLifecycleOwner) {allTransactions->
                val list = when (filterOption.type) {
                    FilterPeriodStatistic.Monthly -> FilterTransactions.filterTransactionsByMonth(allTransactions, filterOption.date)
                    FilterPeriodStatistic.Weekly -> FilterTransactions.filterTransactionsByWeek(allTransactions, filterOption.date)
                    FilterPeriodStatistic.Yearly -> FilterTransactions.filterTransactionsByYear(allTransactions, filterOption.date)
                    FilterPeriodStatistic.List -> emptyList()
                    FilterPeriodStatistic.Trend -> emptyList()
                }
                viewModel.setStatisticTransactionFilter(list)
            }
        }

        tvNote.setOnClickListener {
            if (currentSortField == SortField.NOTE) {
                toggleSortOrder()
            } else {
                currentSortField = SortField.NOTE
                currentSortOrder = SortOrder.ASC
            }
            sortAndUpdate()
            updateSortIndicators()
        }
        tvCount.setOnClickListener {
            if (currentSortField == SortField.COUNT) {
                toggleSortOrder()
            } else {
                currentSortField = SortField.COUNT
                currentSortOrder = SortOrder.ASC
            }
            sortAndUpdate()
            updateSortIndicators()
        }
        tvAmount.setOnClickListener {
            if (currentSortField == SortField.AMOUNT) {
                toggleSortOrder()
            } else {
                currentSortField = SortField.AMOUNT
                currentSortOrder = SortOrder.ASC
            }
            sortAndUpdate()
            updateSortIndicators()
        }

        adapter.onClickListener = { note ->
            val intent = Intent(requireContext(), StatisticCategoryActivity::class.java)
            intent.putExtra("item_click_statistic_category_name", note.note)
            intent.putExtra("item_click_statistic_category_type", categoryType)
            intent.putExtra("item_click_statistic_filterOption", filterOptionTemp)
            intent.putExtra("item_click_statistic_keyWord", KeyFilter.Note)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.no_animation)
            true
        }
    }

    private fun init() {
        tvNote = binding.fragmentStatisticNoteHeaderNote
        tvCount = binding.fragmentStatisticNoteHeaderCount
        tvAmount = binding.fragmentStatisticNoteHeaderAmount
        tvNoData = binding.fragmentStatisticNoteNoDataText
        recyclerView = binding.fragmentStatisticNoteRecyclerView
    }

    private fun sortAndUpdate() {
        val sortedList = when (currentSortField) {
            SortField.NOTE -> {
                if (currentSortOrder == SortOrder.ASC) listNotes.sortedBy { it.note }
                else listNotes.sortedByDescending { it.note }
            }
            SortField.COUNT -> {
                if (currentSortOrder == SortOrder.ASC) listNotes.sortedBy { it.count }
                else listNotes.sortedByDescending { it.count }
            }
            SortField.AMOUNT -> {
                if (currentSortOrder == SortOrder.ASC) listNotes.sortedBy { it.amount }
                else listNotes.sortedByDescending { it.amount }
            }
        }
        tvNoData.visibility = if (sortedList.isEmpty()) View.VISIBLE else View.GONE
        adapter.submitList(sortedList)
    }

    private fun toggleSortOrder() {
        currentSortOrder = if (currentSortOrder == SortOrder.ASC) SortOrder.DESC else SortOrder.ASC
    }

    private fun updateSortIndicators() {
        val upIcon = R.drawable.ic_baseline_arrow_upward_24
        val downIcon = R.drawable.ic_baseline_arrow_downward_24
        val noneIcon = 0

        fun setDrawable(textView: TextView, isActive: Boolean) {
            val icon = if (!isActive) noneIcon
            else if (currentSortOrder == SortOrder.ASC) upIcon else downIcon
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0)
        }

        setDrawable(tvNote, currentSortField == SortField.NOTE)
        setDrawable(tvCount, currentSortField == SortField.COUNT)
        setDrawable(tvAmount, currentSortField == SortField.AMOUNT)
    }

    private fun getListNoteFilter(listTransactionFilter: List<Transaction>) : List<Note> {
        return listTransactionFilter
            .groupBy { it.note }
            .map { (note, transactions) ->
                val count = transactions.size
                val totalAmount = transactions.sumOf { it.amount }
                Note(note = note, count = count, amount = totalAmount)
            }.sortedByDescending { it.amount }
    }
}