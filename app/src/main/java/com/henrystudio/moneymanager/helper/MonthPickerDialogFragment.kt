package com.henrystudio.moneymanager.helper

import android.app.Dialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.henrystudio.moneymanager.R
import java.time.LocalDate
import java.time.Month
import java.time.Year
import java.time.format.TextStyle
import java.util.*

class MonthPickerDialogFragment(
    private val onMonthSelected: (month: Int, year: Int) -> Unit
) : DialogFragment() {

    @RequiresApi(Build.VERSION_CODES.O)
    private var currentYear = Year.now().value

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_month_picker, null)

        val tvYear = view.findViewById<TextView>(R.id.tvYear)
        val btnPrevYear = view.findViewById<ImageButton>(R.id.btnPrevYear)
        val btnNextYear = view.findViewById<ImageButton>(R.id.btnNextYear)
        val btnThisMonth = view.findViewById<TextView>(R.id.btnThisMonth)
        val gridMonths = view.findViewById<GridLayout>(R.id.gridMonths)

        tvYear.text = currentYear.toString()

        fun populateMonths() {
            gridMonths.removeAllViews()
            val months = Month.values()
            val currentMonth = LocalDate.now().monthValue
            val currentYearNow = LocalDate.now().year

            gridMonths.columnCount = 4 // hiển thị 4 cột

            for ((index, month) in months.withIndex()) {
                val tv = TextView(requireContext()).apply {
                    text = month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                    textSize = 16f
                    gravity = Gravity.CENTER
                    setPadding(16, 16, 16, 16)

                    // Đặt kích thước ô để GridLayout chia đều
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 0
                        height = ViewGroup.LayoutParams.WRAP_CONTENT
                        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                        setMargins(8, 8, 8, 8)
                    }

                    // Highlight tháng hiện tại
                    if (index + 1 == currentMonth && currentYear == currentYearNow) {
                        setBackgroundResource(R.drawable.bg_month_selected)
                        setTextColor(Color.WHITE)
                    } else {
                        setBackgroundResource(R.drawable.bg_month_normal)
                        setTextColor(Color.BLACK)
                    }

                    setOnClickListener {
                        onMonthSelected(index + 1, currentYear)
                        dismiss()
                    }
                }
                gridMonths.addView(tv)
            }
        }

        btnPrevYear.setOnClickListener {
            currentYear--
            tvYear.text = currentYear.toString()
            populateMonths()
        }

        btnNextYear.setOnClickListener {
            currentYear++
            tvYear.text = currentYear.toString()
            populateMonths()
        }

        btnThisMonth.setOnClickListener {
            val now = LocalDate.now()
            onMonthSelected(now.monthValue, now.year)
            dismiss()
        }

        populateMonths()
        builder.setView(view)
        return builder.create()
    }
}