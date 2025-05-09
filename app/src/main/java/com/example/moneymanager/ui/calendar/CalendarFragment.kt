package com.example.moneymanager.ui.calendar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.applandeo.materialcalendarview.EventDay
import com.example.moneymanager.R
import com.example.moneymanager.databinding.FragmentCalendarBinding
import com.example.moneymanager.model.AppDatabase
import com.example.moneymanager.viewmodel.TransactionViewModel
import com.example.moneymanager.viewmodel.TransactionViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TransactionViewModel
    private var currency = com.example.moneymanager.helper.Currency()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        val events = mutableListOf<EventDay>()
        val dao = AppDatabase.getDatabase(requireActivity().application).transactionDao()
        val factory = TransactionViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]
        viewModel.groupedTransactions.observe(requireActivity()) { list ->
            for (group in list) {
                val calendar = Calendar.getInstance()
                calendar.time =
                    SimpleDateFormat("dd/MM/yy", Locale.getDefault()).parse(group.date)!!

                val drawable = createEventDrawable(
                    requireContext(),
                    group.income,
                    group.expense,
                    group.income - group.expense
                )
                events.add(EventDay(calendar, drawable))
            }
            binding.calendarView.setEvents(events)
        }
        return binding.root
    }

    private fun createEventDrawable(
        context: Context,
        income: Double,
        expense: Double,
        total: Double
    ): Drawable {
        val view = LayoutInflater.from(context).inflate(R.layout.calendar_event_layout, null)
        view.findViewById<TextView>(R.id.income).text = currency.formatCurrency(income)
        view.findViewById<TextView>(R.id.expense).text = currency.formatCurrency(expense)
        view.findViewById<TextView>(R.id.total).text = currency.formatCurrency(total)

        view.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bitmap =
            Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return BitmapDrawable(context.resources, bitmap)
    }
}

