package com.henrystudio.moneymanager.ui.monthly

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.henrystudio.moneymanager.databinding.FragmentMonthlyBinding
import com.henrystudio.moneymanager.helper.FilterTransactions
import com.henrystudio.moneymanager.helper.Helper
import com.henrystudio.moneymanager.model.AppDatabase
import com.henrystudio.moneymanager.model.FilterOption
import com.henrystudio.moneymanager.model.FilterPeriodStatistic
import com.henrystudio.moneymanager.model.TransactionGroup
import com.henrystudio.moneymanager.ui.addtransaction.SharedTransactionHolder
import com.henrystudio.moneymanager.ui.bottomNavigation.statistic.StatisticListActivity
import com.henrystudio.moneymanager.ui.main.MainActivity
import com.henrystudio.moneymanager.viewmodel.TransactionViewModel
import com.henrystudio.moneymanager.viewmodel.TransactionViewModelFactory
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MonthlyFragment : Fragment() {
    private var _binding: FragmentMonthlyBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MonthlyAdapter
    private var listMonthlyData: List<MonthlyData> = emptyList()
    private val viewModel: TransactionViewModel by activityViewModels {
        TransactionViewModelFactory(
            AppDatabase.getDatabase(requireActivity().application).transactionDao()
        )
    }
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
        // Gán layoutManager nếu chưa có
        binding.monthlyListSummary.layoutManager = LinearLayoutManager(requireContext())
        viewModel.combineGroupAndDate.observe(viewLifecycleOwner) {(groups, date) ->
            val filterTransactionYear = FilterTransactions.filterTransactionGroupByYear(
                groups, date
            )
            listMonthlyData = groupTransactionsByMonth(filterTransactionYear)

            adapter = MonthlyAdapter(listMonthlyData,
            onMonthClick = { month ->
                val activity = requireActivity()
                if (activity is MainActivity) {
                    month.isExpanded = !month.isExpanded
                    val index = listMonthlyData.indexOf(month)
                    adapter.notifyItemChanged(index)
                } else if (activity is StatisticListActivity) {
                    SharedTransactionHolder.currentFilterDate = Helper.formatDateFromFilterOptionToDateDaily(month.monthStart.toString())
                    SharedTransactionHolder.filterOption = FilterOption(FilterPeriodStatistic.Monthly, month.monthStart)
                    activity.onBackAnimation()
                }
            },
            onWeekClick = { weeklyData ->
                viewModel.navigateToWeekFromMonthly(weeklyData.weekStart)
            })
            adapter.updateData(listMonthlyData)
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
                    monthStart = monthStart,
                    dateRange = dateRange,
                    income = income,
                    expense = expense,
                    total = total,
                    weeks = weeks
                )
            }
    }
}