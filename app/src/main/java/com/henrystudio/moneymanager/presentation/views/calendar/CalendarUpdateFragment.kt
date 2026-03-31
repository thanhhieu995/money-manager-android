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
import com.henrystudio.moneymanager.data.model.TransactionGroup
import com.henrystudio.moneymanager.presentation.viewmodel.CalendarUpdateViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.SharedTransactionViewModel
import com.henrystudio.moneymanager.presentation.addtransaction.model.UiState
import com.henrystudio.moneymanager.presentation.views.main.TransactionGroupAdapter
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.yearMonth
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class CalendarUpdateFragment : Fragment() {
    private var eventsMap = mutableMapOf<LocalDate, TransactionGroup>()
    private lateinit var calendarView: com.kizitonwose.calendar.view.CalendarView

    private val sharedViewModel: SharedTransactionViewModel by activityViewModels()
    private val viewModel: CalendarUpdateViewModel by viewModels()

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
                MotionEvent.ACTION_MOVE, MotionEvent.ACTION_SCROLL -> true
                MotionEvent.ACTION_UP -> { v.performClick(); false }
                else -> false
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.combineGroupAndDate.collect { (state, currentDate) ->
                    viewModel.updateData(if (state is UiState.Success)
                        state.data else emptyList(),
                        currentDate)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    eventsMap.clear()
                    eventsMap.putAll(state.monthEvents)
                    val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY)
                    val currentMonth = state.currentDate.yearMonth
                    val startMonth = currentMonth.minusMonths(12)
                    val endMonth = currentMonth.plusMonths(12)
                    calendarView.setup(startMonth, endMonth, daysOfWeek.first())
                    calendarView.scrollToMonth(currentMonth)
                    calendarView.notifyCalendarChanged()
                }
            }
        }

        calendarView.monthHeaderBinder = object : com.kizitonwose.calendar.view.MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: com.kizitonwose.calendar.core.CalendarMonth) {}
        }

        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                container.dayText.text = day.date.dayOfMonth.toString()
                if (day.position == DayPosition.MonthDate) {
                    val textColorTheme = getAttrColor(requireContext(), com.google.android.material.R.attr.colorOnSurface)
                    if (day.date == LocalDate.now()) {
                        container.dayText.setTextColor(Color.RED)
                    } else {
                        container.dayText.background = null
                        container.dayText.setTextColor(textColorTheme)
                    }

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
                    container.dayText.setTextColor(Color.GRAY)
                    container.incomeText.text = ""
                    container.expenseText.text = ""
                    container.totalText.text = ""
                }
            }
        }
    }

    inner class MonthViewContainer(view: View) : ViewContainer(view)

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

        val dateString = date.format(DateTimeFormatter.ofPattern("dd/MM/yy", Locale.getDefault()))

        val dayListTransaction = dialogView.findViewById<RecyclerView>(R.id.item_day_calendar_list)
        dayListTransaction.layoutManager = LinearLayoutManager(requireContext())
        dayListTransaction.adapter = adapter

        val noDataText = dialogView.findViewById<TextView>(R.id.item_day_calendar_noData)

        val groupTransaction = viewModel.getGroupsForDate(dateString)

        adapter.submitList(groupTransaction)
        noDataText.visibility = if (groupTransaction.isEmpty()) View.VISIBLE else View.GONE
        adapter.onTransactionClick = { transaction ->
            Helper.openTransactionDetail(requireContext(), transaction)
            true
        }

        bottomSheet.show()
    }
}
