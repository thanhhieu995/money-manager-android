package com.example.moneymanager.ui.monthly

import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneymanager.R
import com.example.moneymanager.databinding.FragmentDailyBinding
import com.example.moneymanager.databinding.FragmentMonthlyBinding
import com.example.moneymanager.model.AppDatabase
import com.example.moneymanager.model.Transaction
import com.example.moneymanager.model.TransactionGroup
import com.example.moneymanager.viewmodel.TransactionViewModel
import com.example.moneymanager.viewmodel.TransactionViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MonthlyFragment : Fragment() {
    private lateinit var viewModel: TransactionViewModel
    private var _binding: FragmentMonthlyBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMonthlyBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val dao = AppDatabase.getDatabase(Application()).transactionDao()
        val factory = TransactionViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]
        // Gán layoutManager nếu chưa có
        binding.monthlyListSummary.layoutManager = LinearLayoutManager(requireContext())
        viewModel.groupedTransactions.observe(viewLifecycleOwner) {list ->
            val listMonthlyData = groupTransactionsByMonth(list)
            val adapter = MonthlyAdapter(listMonthlyData) { monthlyData ->
                // handle onMonthClick ở đây nếu cần
            }
            binding.monthlyListSummary.adapter = adapter
            binding.monthlyNoData.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun groupTransactionsByMonth(transactions: List<TransactionGroup>): List<MonthlyData> {
        val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yy")
        val outputFormatter = DateTimeFormatter.ofPattern("dd-MM")

        return transactions
            .groupBy {
                val rawDate = it.date
                val cleanedDate = rawDate.substringBefore(" ") // Bỏ phần (Tue)
                val localDate = LocalDate.parse(cleanedDate, inputFormatter)
                LocalDate.of(localDate.year, localDate.month, 1)
            }
            .map { (monthStart, list) ->
                val income = list.sumOf { it.income }
                val expense = list.sumOf { it.expense }
                val total = income - expense

                val dateRange = "${monthStart.format(outputFormatter)} ~ ${monthStart.withDayOfMonth(monthStart.lengthOfMonth()).format(outputFormatter)}"

                MonthlyData(
                    monthName = monthStart.month.name.lowercase().replaceFirstChar { it.uppercase() },
                    dateRange = dateRange,
                    income = income,
                    expense = expense,
                    total = total,
                    weeks = emptyList()
                )
            }
    }
}