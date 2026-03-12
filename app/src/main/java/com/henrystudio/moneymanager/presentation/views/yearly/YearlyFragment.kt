package com.henrystudio.moneymanager.presentation.views.yearly

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.henrystudio.moneymanager.databinding.FragmentYearlyBinding
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.data.local.AppDatabase
import com.henrystudio.moneymanager.presentation.model.FilterOption
import com.henrystudio.moneymanager.presentation.model.FilterPeriodStatistic
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.data.repository.TransactionRepositoryImpl
import com.henrystudio.moneymanager.presentation.viewmodel.TransactionViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.TransactionViewModelFactory
import com.henrystudio.moneymanager.presentation.views.addtransaction.SharedTransactionHolder
import com.henrystudio.moneymanager.presentation.views.bottomNavigation.statistic.StatisticListActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@AndroidEntryPoint
class YearlyFragment : Fragment() {
    private var _binding: FragmentYearlyBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var noData: TextView
    private lateinit var adapter: YearlyAdapter
    private var allTransactions: List<Transaction> = emptyList()
    private val transactionViewModel : TransactionViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentYearlyBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        adapter = YearlyAdapter(emptyList(), onClickYear = { data ->
            SharedTransactionHolder.currentFilterDate = Helper.formatDateFromFilterOptionToDateDaily(data.date.toString())
            SharedTransactionHolder.filterOption = FilterOption(FilterPeriodStatistic.Yearly, data.date)
            (requireActivity() as StatisticListActivity).onBackAnimation()
        })
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                transactionViewModel.allTransactions.collect { transactions ->
                    allTransactions = transactions
                    adapter.updateData(mapTransactionsToYearlyData(transactions))
                    noData.visibility = if (transactions.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun init() {
        recyclerView = binding.fragmentYearlyRecyclerView
        noData = binding.fragmentYearlyNoDataText
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun mapTransactionsToYearlyData(transactions: List<Transaction>): List<YearlyData> {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yy (EEE)", Locale.ENGLISH)
        val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy") // dạng hiển thị

        return transactions
            .groupBy { LocalDate.parse(it.date, formatter).year } // group theo năm
            .map { (year, yearTransactions) ->
                val income = yearTransactions.filter { it.isIncome }.sumOf { it.amount }
                val expense = yearTransactions.filter { !it.isIncome }.sumOf { it.amount }

                val startDate = LocalDate.of(year, 1, 1).format(outputFormatter)
                val endDate = LocalDate.of(year, 12, 31).format(outputFormatter)

                YearlyData(
                    name = year,
                    date = LocalDate.of(year, 1, 1),
                    arrange = "$startDate ~ $endDate",  // ví dụ: 01/01/2025 - 31/12/2025
                    income = income,
                    expense = expense,
                    total = income - expense
                )
            }
            .sortedByDescending { it.name }
    }
}