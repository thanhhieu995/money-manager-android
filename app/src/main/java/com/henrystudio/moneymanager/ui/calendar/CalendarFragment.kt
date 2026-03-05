package com.henrystudio.moneymanager.ui.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.databinding.FragmentCalendarBinding
import com.henrystudio.moneymanager.helper.Helper
import com.henrystudio.moneymanager.model.AppDatabase
import com.henrystudio.moneymanager.repository.TransactionRepository
import com.henrystudio.moneymanager.ui.main.TransactionGroupAdapter
import com.henrystudio.moneymanager.viewmodel.TransactionViewModel
import com.henrystudio.moneymanager.viewmodel.TransactionViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private lateinit var calendarResume: Calendar
    private val viewModel: TransactionViewModel by activityViewModels {
        val database = AppDatabase.getDatabase(requireActivity().application)
        val repository = TransactionRepository(database.transactionDao())
        TransactionViewModelFactory(
            repository
        )
    }

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
        val bgColor = getAttrColor(android.R.attr.colorBackground)
        binding.calendarView.setBackgroundColor(bgColor)

        // Đổi màu chữ ngày, tiêu đề theo theme
        binding.calendarView.setHeaderColor(bgColor)
        binding.calendarView.setHeaderLabelColor(getAttrColor(android.R.attr.textColorPrimary))
        binding.calendarView.setBackgroundColor(getAttrColor(android.R.attr.textColorSecondary))
        val events = mutableListOf<EventDay>()
        viewModel.groupedTransactions.observe(viewLifecycleOwner) { list ->
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

        viewModel.currentFilterDate.observe(viewLifecycleOwner) { date ->
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, date.year)
                set(Calendar.MONTH, date.monthValue - 1) // Vì Calendar.MONTH bắt đầu từ 0
                set(Calendar.DAY_OF_MONTH, 1)
            }
            calendarResume = calendar
            binding.calendarView.setDate(calendar)
        }

        setupDayClickListener()

        val header = binding.calendarView.findViewById<View>(com.applandeo.materialcalendarview.R.id.calendarHeader)
        header?.visibility = View.GONE
    }

    private fun setupDayClickListener() {
        binding.calendarView.setOnDayClickListener(object : OnDayClickListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDayClick(eventDay: EventDay) {
                showBottomSheetForDate(eventDay.calendar.time)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        binding.calendarView.setDate(calendarResume)
    }

    private fun createEventDrawable(
        context: Context,
        income: Double,
        expense: Double,
        total: Double
    ): Drawable {
        val view = LayoutInflater.from(context).inflate(R.layout.calendar_event_layout, null)
        view.findViewById<TextView>(R.id.income).text = Helper.formatCurrency(income)
        view.findViewById<TextView>(R.id.expense).text = Helper.formatCurrency(expense)
        view.findViewById<TextView>(R.id.total).text = Helper.formatCurrency(total)

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

    @RequiresApi(Build.VERSION_CODES.O)
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

    private fun getAttrColor(attr: Int): Int {
        val typedValue = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

}

