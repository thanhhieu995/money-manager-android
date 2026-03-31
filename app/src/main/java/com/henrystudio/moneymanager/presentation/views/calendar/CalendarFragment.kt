package com.henrystudio.moneymanager.presentation.views.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.databinding.FragmentCalendarBinding
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.presentation.viewmodel.CalendarViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.SharedTransactionViewModel
import com.henrystudio.moneymanager.presentation.views.main.TransactionGroupAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import com.henrystudio.moneymanager.presentation.addtransaction.model.UiState

@AndroidEntryPoint
class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private lateinit var calendarResume: Calendar

    private val sharedViewModel: SharedTransactionViewModel by activityViewModels()
    private val viewModel: CalendarViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bgColor = getAttrColor(android.R.attr.colorBackground)
        binding.calendarView.setBackgroundColor(bgColor)

        binding.calendarView.setHeaderColor(bgColor)
        binding.calendarView.setHeaderLabelColor(getAttrColor(android.R.attr.textColorPrimary))
        binding.calendarView.setBackgroundColor(getAttrColor(android.R.attr.textColorSecondary))
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    sharedViewModel.groupedTransactionsState.collect { state ->
                        viewModel.updateGroupedTransactions(
                            if (state is UiState.Success)
                        state.data else emptyList()
                        )
                    }
                }
                launch {
                    sharedViewModel.currentFilterDate.collect { date ->
                        viewModel.updateCurrentFilterDate(date)
                    }
                }
                launch {
                    viewModel.uiState.collect { state ->
                        val events = state.eventItems.map { item ->
                            val calendar = Helper.dateKeyToCalendar(item.dateKey)
                            val drawable = createEventDrawable(
                                requireContext(),
                                item.income,
                                item.expense,
                                item.total
                            )
                            EventDay(calendar, drawable)
                        }
                        binding.calendarView.setEvents(events)
                        val cal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, state.currentFilterDate.year)
                            set(Calendar.MONTH, state.currentFilterDate.monthValue - 1)
                            set(Calendar.DAY_OF_MONTH, 1)
                        }
                        calendarResume = cal
                        binding.calendarView.setDate(cal)
                    }
                }
            }
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
        if (::calendarResume.isInitialized) {
            binding.calendarView.setDate(calendarResume)
        }
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
        val bitmap = createBitmap(view.measuredWidth, view.measuredHeight)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap.toDrawable(context.resources)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    private fun showBottomSheetForDate(date: Date) {
        val adapter = TransactionGroupAdapter()

        val dialogView = layoutInflater.inflate(R.layout.item_calendar_day_detail, null)
        val bottomSheet = BottomSheetDialog(requireContext())
        bottomSheet.setContentView(dialogView)

        val dateString = java.text.SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(date)

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

    private fun getAttrColor(attr: Int): Int {
        val typedValue = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
