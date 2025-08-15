package com.henrystudio.moneymanager.ui.weekly

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
import com.henrystudio.moneymanager.databinding.FragmentWeeklyBinding
import com.henrystudio.moneymanager.helper.FilterTransactions
import com.henrystudio.moneymanager.model.AppDatabase
import com.henrystudio.moneymanager.model.FilterOption
import com.henrystudio.moneymanager.model.TransactionGroup
import com.henrystudio.moneymanager.ui.monthly.WeeklyAdapter
import com.henrystudio.moneymanager.ui.monthly.WeeklyData
import com.henrystudio.moneymanager.viewmodel.TransactionViewModel
import com.henrystudio.moneymanager.viewmodel.TransactionViewModelFactory
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class WeeklyFragment : Fragment() {
    private var _binding : FragmentWeeklyBinding?= null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvNoData: TextView
    private lateinit var adapter: WeeklyAdapter
    private var listMonthTransactionGroup: List<TransactionGroup>? = null
    private var listWeekData: List<WeeklyData>? = null

    private val viewModel: TransactionViewModel by activityViewModels {
        TransactionViewModelFactory(
            AppDatabase.getDatabase(requireActivity().application).transactionDao()
        )
    }

    private var filterOptionTemp : FilterOption?= null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentWeeklyBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        filterOptionTemp = arguments?.getSerializable("filterOption") as? FilterOption
        adapter = WeeklyAdapter(listWeekData?: emptyList(),
            onWeekClick = {})
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        viewModel.groupedTransactions.observe(viewLifecycleOwner) { allTransactionGroups->
            listMonthTransactionGroup = FilterTransactions.filterTransactionGroupByMonth(allTransactionGroups ?: emptyList(),
                filterOptionTemp?.date ?: LocalDate.now())
            listWeekData = groupTransactionsByWeek(listMonthTransactionGroup!!)
            adapter.updateData(listWeekData!!)
        }
    }

    private fun init() {
        recyclerView = binding.fragmentWeeklyListSummary
        tvNoData = binding.fragmentWeeklyNoData
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun groupTransactionsByWeek(transactions: List<TransactionGroup>): List<WeeklyData> {
        val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yy")
        val outputFormatter = DateTimeFormatter.ofPattern("dd-MM")

        return transactions
            .groupBy {
                val rawDate = it.date
                val cleanedDate = rawDate.substringBefore(" ") // bỏ (Tue)
                val date = LocalDate.parse(cleanedDate, inputFormatter)
                date.with(DayOfWeek.MONDAY) // tuần bắt đầu từ thứ 2
            }
            .toSortedMap(compareByDescending { it }) // tuần mới trước
            .map { (weekStart, weekList) ->
                val weekIncome = weekList.sumOf { it.income }
                val weekExpense = weekList.sumOf { it.expense }
                val weekTotal = weekIncome - weekExpense

                WeeklyData(
                    weekStart = weekStart,
                    weekRange = "${weekStart.format(outputFormatter)} ~ ${weekStart.plusDays(6).format(outputFormatter)}",
                    income = weekIncome,
                    expense = weekExpense,
                    total = weekTotal
                )
            }
    }
}