package com.example.moneymanager.ui.calendar

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneymanager.R
import com.example.moneymanager.databinding.FragmentCalendarBinding
import com.example.moneymanager.databinding.FragmentDailyBinding
import com.example.moneymanager.model.AppDatabase
import com.example.moneymanager.ui.main.TransactionAdapter
import com.example.moneymanager.viewmodel.TransactionViewModel
import com.example.moneymanager.viewmodel.TransactionViewModelFactory

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TransactionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
////        val calendarView = binding.calendarView
//        val dao = AppDatabase.getInstance(requireContext().applicationContext).transactionDao()
//        val factory = TransactionViewModelFactory(dao)
//        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]
//
//        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
//            val grouped = transactions.groupBy { it.date }
//
//            val decorators = grouped.mapNotNull { (dateString, items) ->
//                val income = items.filter { it.isIncome }.sumOf { it.amount }
//                val expense = items.filter { !it.isIncome }.sumOf { it.amount }
//                val balance = income - expense
//
//                // Chuyển dateString (yyyy-MM-dd) sang CalendarDay
//                val parts = dateString.split("-").map { it.toIntOrNull() }
//                if (parts.size == 3 && parts.all { it != null }) {
//                    val (year, month, day) = parts
//                    val calendarDay = CalendarDay.from(year!!, month!! - 1, day!!) // month-1 vì CalendarDay tính từ 0
//
//                    object : DayViewDecorator {
//                        override fun shouldDecorate(day: CalendarDay): Boolean {
//                            return day == calendarDay
//                        }
//
//                        override fun decorate(view: DayViewFacade) {
//                            val text = "T:$income\nC:$expense\nB:$balance"
//                            val span = SpannableString(text)
//                            span.setSpan(ForegroundColorSpan(Color.GREEN), 0, 2, 0)   // T:
//                            span.setSpan(ForegroundColorSpan(Color.RED), 6, 8, 0)     // C:
//                            span.setSpan(ForegroundColorSpan(Color.BLUE), 12, 14, 0)  // B:
//                            view.addSpan(span)
//                        }
//                    }
//                } else null
//            }

            // Clear old decorators nếu cần
//            binding.calendarView.removeDecorators()
//
//            // Thêm các decorator mới
//            decorators.forEach {
//                binding.calendarView.addDecorator(it)
//            }
//        }
//        return binding.root
//    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
}}