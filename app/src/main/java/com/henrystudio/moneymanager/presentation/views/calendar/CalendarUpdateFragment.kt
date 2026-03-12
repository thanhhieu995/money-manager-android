package com.henrystudio.moneymanager.presentation.views.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.data.local.AppDatabase
import com.henrystudio.moneymanager.data.model.TransactionGroup
import com.henrystudio.moneymanager.data.repository.TransactionRepositoryImpl
import com.henrystudio.moneymanager.presentation.viewmodel.TransactionViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.TransactionViewModelFactory
import com.henrystudio.moneymanager.presentation.views.main.TransactionGroupAdapter
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.yearMonth
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@AndroidEntryPoint
class CalendarUpdateFragment : Fragment() {
    private var eventsMap = mutableMapOf<LocalDate, TransactionGroup>()
    private lateinit var calendarView: com.kizitonwose.calendar.view.CalendarView
    private val transactionViewModel: TransactionViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_calendar_kizitonwose, container, false)
        calendarView = view.findViewById(R.id.calendarView_update)
        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        calendarView.setOnTouchListener { v, e ->
            when (e.actionMasked) {
                // Chặn MOVE để không thể scroll
                MotionEvent.ACTION_MOVE, MotionEvent.ACTION_SCROLL -> true

                // Trả click về hệ thống + hỗ trợ accessibility
                MotionEvent.ACTION_UP -> { v.performClick(); false }

                else -> false
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                transactionViewModel.combineGroupAndDate.collect { (groups, currentDate) ->
                    eventsMap.clear()
                    val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                    // Lấy ra tháng đang được filter
                    val month = currentDate.month
                    val year = currentDate.year

                    // Tạo map date -> TransactionGroup (dùng LocalDate làm key)
                    val monthGroups = groups.mapNotNull { group ->
                        val parsedDate = sdf.parse(group.date)
                        val localDate = parsedDate?.toInstant()
                            ?.atZone(ZoneId.systemDefault())
                            ?.toLocalDate()
                        if (localDate != null && localDate.month == month && localDate.year == year) {
                            localDate to group
                        } else null
                    }.toMap()

                    eventsMap.putAll(monthGroups)

                    val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY)
                    val currentMonth = currentDate.yearMonth
                    val startMonth = currentMonth.minusMonths(12)
                    val endMonth = currentMonth.plusMonths(12)

                    calendarView.setup(startMonth, endMonth, daysOfWeek.first())
                    calendarView.scrollToMonth(currentMonth)
                    // Gọi hàm update CalendarView
                    calendarView.notifyCalendarChanged()  // hoặc notifyDateChanged(date) tùy thư viện
                }
            }
        }

        // Header hiển thị tháng
        calendarView.monthHeaderBinder = object : com.kizitonwose.calendar.view.MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: com.kizitonwose.calendar.core.CalendarMonth) {
                val title = "${month.yearMonth.month} ${month.yearMonth.year}"
//                container.textView.text = title
            }
        }

        // Render từng ngày
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                container.dayText.text = day.date.dayOfMonth.toString()
                if (day.position == DayPosition.MonthDate) {
                    val textColorTheme = getAttrColor(requireContext(), com.google.android.material.R.attr.colorOnSurface)
                    // tô màu ngày hiện tại
                    if (day.date == LocalDate.now()) {
                        container.dayText.setTextColor(Color.RED)
                    } else {
                        container.dayText.background = null
                        container.dayText.setTextColor(textColorTheme)
                    }

                    // gắn income/expense/total
                    val event = eventsMap[day.date]
                    if (event != null) {
                        container.incomeText.text = "↑${Helper.formatCurrency(event.income)}"
                        container.expenseText.text = "↓${Helper.formatCurrency(event.expense)}"
                        container.totalText.text = Helper.formatCurrency(event.income - event.expense)
                    } else {
                        container.incomeText.text = ""
                        container.expenseText.text = ""
                        container.totalText.text = ""
                    }

                    container.view.setOnClickListener {
                        showBottomSheetForDate(day.date)
                    }
                } else {
                    // ngày ngoài tháng -> xám
                    container.dayText.setTextColor(Color.GRAY)
                    container.incomeText.text = ""
                    container.expenseText.text = ""
                    container.totalText.text = ""
                }
            }
        }
    }

    inner class MonthViewContainer(view: View) : ViewContainer(view) {
//        val textView: TextView = view.findViewById(R.id.monthTitle)
    }

    private fun getAttrColor(context: Context, attr: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val dayText: TextView = view.findViewById(R.id.item_calendar_layout_dayText)
        val incomeText: TextView = view.findViewById(R.id.item_calendar_layout_income)
        val expenseText: TextView = view.findViewById(R.id.item_calendar_layout_expense)
        val totalText: TextView = view.findViewById(R.id.item_calendar_layout_total)
        lateinit var day: CalendarDay
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showBottomSheetForDate(date: LocalDate) {
        val adapter = TransactionGroupAdapter()

        val dialogView = layoutInflater.inflate(R.layout.item_calendar_day_detail, null)
        val bottomSheet = BottomSheetDialog(requireContext())
        bottomSheet.setContentView(dialogView)

        val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        val dateString = sdf.format(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()))

        val dayListTransaction = dialogView.findViewById<RecyclerView>(R.id.item_day_calendar_list)
        dayListTransaction.layoutManager = LinearLayoutManager(requireContext())
        dayListTransaction.adapter = adapter

        val noDataText = dialogView.findViewById<TextView>(R.id.item_day_calendar_noData)

        val groupTransaction = transactionViewModel.groupedTransactions.value?.filter {
            val transactionDate = sdf.parse(it.date)
            transactionDate?.let { it1 -> sdf.format(it1) } == dateString
        } ?: emptyList()

        adapter.submitList(groupTransaction)
        noDataText.visibility = if (groupTransaction.isEmpty()) View.VISIBLE else View.GONE
        adapter.onTransactionClick = {transaction ->
            Helper.openTransactionDetail(requireContext(), transaction)
            true
        }

        bottomSheet.show()
    }
}