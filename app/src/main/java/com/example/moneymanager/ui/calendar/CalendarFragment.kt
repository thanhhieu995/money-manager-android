package com.example.moneymanager.ui.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.example.moneymanager.R
import com.example.moneymanager.databinding.FragmentCalendarBinding
import com.example.moneymanager.model.AppDatabase
import com.example.moneymanager.ui.main.TransactionGroupAdapter
import com.example.moneymanager.viewmodel.TransactionViewModel
import com.example.moneymanager.viewmodel.TransactionViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TransactionViewModel
    private var currency = com.example.moneymanager.helper.Currency()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val events = mutableListOf<EventDay>()
        val dao = AppDatabase.getDatabase(requireActivity().application).transactionDao()
        val factory = TransactionViewModelFactory(dao)
        viewModel = ViewModelProvider(requireActivity(), factory)[TransactionViewModel::class.java]
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

        viewModel.currentMonthYear.observe(viewLifecycleOwner) { date ->
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, date.year)
                set(Calendar.MONTH, date.monthValue - 1) // Vì Calendar.MONTH bắt đầu từ 0
                set(Calendar.DAY_OF_MONTH, 1)
            }
            binding.calendarView.setDate(calendar)
        }

        val header = binding.calendarView.findViewById<View>(com.applandeo.materialcalendarview.R.id.calendarHeader)
        header?.visibility = View.GONE

        binding.calendarView.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                showBottomSheetForDate(eventDay.calendar.time)
            }
        })
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

    @SuppressLint("MissingInflatedId")
    private fun showBottomSheetForDate(date: Date) {
        val adapter = TransactionGroupAdapter()

        val dialogView = layoutInflater.inflate(R.layout.item_calendar_day_detail, null)
        val bottomSheet = BottomSheetDialog(requireContext())
        bottomSheet.setContentView(dialogView)

        val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        val dateString = sdf.format(date)

        val dayListTransaction = dialogView.findViewById<RecyclerView>(R.id.item_day_calendar_list)
        dayListTransaction.layoutManager = LinearLayoutManager(requireContext())
        dayListTransaction.adapter = adapter

        val noDataText = dialogView.findViewById<TextView>(R.id.item_day_calendar_noData)

        val groupTransaction = viewModel.groupedTransactions.value?.filter {
            val transactionDate = sdf.parse(it.date)
            sdf.format(transactionDate) == dateString
        } ?: emptyList()

        adapter.submitList(groupTransaction)
        noDataText.visibility = if (groupTransaction.isEmpty()) View.VISIBLE else View.GONE

        bottomSheet.show()
    }
}

