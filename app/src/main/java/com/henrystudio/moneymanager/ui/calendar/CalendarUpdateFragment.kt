package com.henrystudio.moneymanager.ui.calendar

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.henrystudio.moneymanager.R
//import com.kizitonwose.calendar.core.CalendarDay
//import com.kizitonwose.calendar.core.DayPosition
//import com.kizitonwose.calendar.core.daysOfWeek
//import com.kizitonwose.calendar.view.MonthDayBinder
//import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth

class CalendarUpdateFragment : Fragment() {
//    private lateinit var calendarView: com.kizitonwose.calendar.view.CalendarView
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        val view = inflater.inflate(R.layout.fragment_calendar_kizitonwose, container, false)
//        calendarView = view.findViewById(R.id.calendarView_update)
//        return view
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        val daysOfWeek = daysOfWeek()
//        val currentMonth = YearMonth.now()
//        val startMonth = currentMonth.minusMonths(12)
//        val endMonth = currentMonth.plusMonths(12)
//
//        calendarView.setup(startMonth, endMonth, daysOfWeek.first())
//        calendarView.scrollToMonth(currentMonth)
//
//        // Render từng ngày
//        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
//            override fun create(view: View) = DayViewContainer(view)
//            override fun bind(container: DayViewContainer, day: CalendarDay) {
//                container.day = day
//                val textView = container.textView
//                val dotView = container.dotView
//
//                textView.text = day.date.dayOfMonth.toString()
//
//                if (day.position == DayPosition.MonthDate) {
//                    textView.setTextColor(getAttrColor(requireContext(), android.R.attr.textColorPrimary))
//
//                    // Gắn event (ví dụ có giao dịch)
//                    if (hasEvent(day.date)) {
//                        dotView.visibility = View.VISIBLE
//                    } else {
//                        dotView.visibility = View.GONE
//                    }
//
//                    container.view.setOnClickListener {
//                        showTransactionBottomSheet(day.date)
//                    }
//
//                } else {
//                    textView.setTextColor(Color.GRAY)
//                    dotView.visibility = View.GONE
//                }
//            }
//        }
//    }
//
//    private fun hasEvent(date: LocalDate): Boolean {
//        // TODO: check trong DB có giao dịch không
//        return false
//    }
//
//    private fun showTransactionBottomSheet(date: LocalDate) {
//        // TODO: hiển thị bottomsheet chi tiết giao dịch
//    }
//
//    private fun getAttrColor(context: Context, attr: Int): Int {
//        val typedValue = TypedValue()
//        context.theme.resolveAttribute(attr, typedValue, true)
//        return typedValue.data
//    }
//
//    inner class DayViewContainer(view: View) : ViewContainer(view) {
//        lateinit var day: CalendarDay
//        val textView: TextView = view.findViewById(R.id.dayText)
//        val dotView: View = view.findViewById(R.id.eventDot)
//    }
}