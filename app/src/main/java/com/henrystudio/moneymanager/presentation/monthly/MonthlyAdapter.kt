package com.henrystudio.moneymanager.presentation.views.monthly

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.core.util.Helper
import java.time.format.TextStyle
import java.util.*

class MonthlyAdapter(
    private var monthlyList: List<MonthlyData>,
    private val onMonthClick: (MonthlyData) -> Unit,
    private val onWeekClick: (WeeklyData) -> Unit
) : RecyclerView.Adapter<MonthlyAdapter.MonthViewHolder>() {
    inner class MonthViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMonthName: TextView = itemView.findViewById(R.id.monthly_name)
        val tvDateRange: TextView = itemView.findViewById(R.id.monthly_arrange)
        val tvIncome: TextView = itemView.findViewById(R.id.monthly_income)
        val tvExpense: TextView = itemView.findViewById(R.id.monthly_expense)
        val tvTotal: TextView = itemView.findViewById(R.id.monthly_total)
        private val rvWeeks: RecyclerView = itemView.findViewById(R.id.monthly_list_week)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(data: MonthlyData) {
            tvMonthName.text = data.monthStart.month.getDisplayName(TextStyle.SHORT, Helper.getAppLocale())
            tvDateRange.text = data.dateRange
            tvIncome.text = Helper.formatCurrency(itemView.context, data.income)
            tvExpense.text = Helper.formatCurrency(itemView.context, data.expense)
            tvTotal.text = Helper.formatCurrency(itemView.context, data.total)

            tvTotal.setTextColor(
                when {
                    data.total > 0 -> itemView.context.getColor(R.color.income) // positive
                    data.total < 0 -> itemView.context.getColor(R.color.red)      // negative
                    else -> itemView.context.getColor(R.color.purple_200)               // zero
                }
            )

            rvWeeks.layoutManager = LinearLayoutManager(itemView.context)
            // Gán adapter tuần nếu đang mở rộng
            if (data.isExpanded) {
                rvWeeks.visibility = View.VISIBLE
                rvWeeks.adapter = WeeklyAdapter(data.weeks, onWeekClick)
            } else {
                rvWeeks.visibility = View.GONE
            }

            itemView.setOnClickListener {
                onMonthClick(data)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_month, parent, false)
        return MonthViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        holder.bind(monthlyList[position])
    }

    override fun getItemCount(): Int = monthlyList.size

    fun updateData(newList: List<MonthlyData>) {
        monthlyList = newList
    }
}
