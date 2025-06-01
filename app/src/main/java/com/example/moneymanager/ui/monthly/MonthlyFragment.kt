package com.example.moneymanager.ui.monthly

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneymanager.databinding.FragmentMonthlyBinding
import com.example.moneymanager.helper.FilterTransactions
import com.example.moneymanager.model.AppDatabase
import com.example.moneymanager.model.TransactionGroup
import com.example.moneymanager.viewmodel.TransactionViewModel
import com.example.moneymanager.viewmodel.TransactionViewModelFactory
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MonthlyFragment : Fragment() {
    private lateinit var viewModel: TransactionViewModel
    private var _binding: FragmentMonthlyBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MonthlyAdapter
    private var listMonthlyData: List<MonthlyData> = emptyList()

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
        val dao = AppDatabase.getDatabase(requireActivity().application).transactionDao()
        val factory = TransactionViewModelFactory(dao)
        viewModel = ViewModelProvider(requireActivity(), factory)[TransactionViewModel::class.java]
        // Gán layoutManager nếu chưa có
        binding.monthlyListSummary.layoutManager = LinearLayoutManager(requireContext())

        viewModel.currentMonthYear.observe(viewLifecycleOwner) {
            val listGroupTransaction = viewModel.groupedTransactions.value ?: emptyList()
            val currentYear = viewModel.currentMonthYear.value
            val filterTransactionYear = currentYear?.let { it1 ->
                FilterTransactions.filterTransactionsByYear(listGroupTransaction ,
                    it1
                )
            }
            listMonthlyData = filterTransactionYear?.let { it1 -> groupTransactionsByMonth(it1) } ?: emptyList()

            adapter = MonthlyAdapter(listMonthlyData,
                onMonthClick = { month ->
                    month.isExpanded = !month.isExpanded
                    val index = listMonthlyData.indexOf(month)
                    adapter.notifyItemChanged(index)
                },
                onWeekClick = { weekData ->
                    viewModel.navigateToWeekFromMonthly(weekData.weekStart)
                }
            )

            adapter.updateData(listMonthlyData)
            binding.monthlyListSummary.adapter = adapter
            binding.monthlyNoData.visibility = if (listMonthlyData.isEmpty()) View.VISIBLE else View.GONE
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
                LocalDate.of(localDate.year, localDate.month, 1) // Nhóm theo tháng
            }
            .map { (monthStart, monthList) ->
                val income = monthList.sumOf { it.income }
                val expense = monthList.sumOf { it.expense }
                val total = income - expense

                val dateRange = "${monthStart.format(outputFormatter)} ~ ${monthStart.withDayOfMonth(monthStart.lengthOfMonth()).format(outputFormatter)}"

                // ✅ Nhóm theo tuần trong từng tháng
                val weeklyGroups = monthList.groupBy {
                    val rawDate = it.date
                    val cleanedDate = rawDate.substringBefore(" ")
                    val date = LocalDate.parse(cleanedDate, inputFormatter)

                    // Lấy ngày đầu tuần (thứ Hai) làm khóa
                    date.with(DayOfWeek.MONDAY)
                }

                // ✅ Sort tuần theo weekStart giảm dần rồi map sang WeeklyData
                val weeks = weeklyGroups.toSortedMap(compareByDescending { it }).map { (weekStart, weekList) ->
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

                // ✅ Trả về MonthlyData đầy đủ
                MonthlyData(
                    monthName = monthStart.month.name.lowercase().replaceFirstChar { it.uppercase() },
                    dateRange = dateRange,
                    income = income,
                    expense = expense,
                    total = total,
                    weeks = weeks
                )
            }
    }
}