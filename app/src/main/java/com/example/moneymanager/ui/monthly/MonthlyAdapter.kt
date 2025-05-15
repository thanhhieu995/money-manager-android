package com.example.moneymanager.ui.monthly

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanager.R
import com.example.moneymanager.helper.Currency

class MonthlyAdapter(
    private var monthlyList: List<MonthlyData>,
    private val onMonthClick: (MonthlyData) -> Unit
) : RecyclerView.Adapter<MonthlyAdapter.MonthViewHolder>() {

    private val currency = Currency() // Your formatter

    inner class MonthViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMonthName: TextView = itemView.findViewById(R.id.monthly_name)
        val tvDateRange: TextView = itemView.findViewById(R.id.monthly_arrange)
        val tvIncome: TextView = itemView.findViewById(R.id.monthly_income)
        val tvExpense: TextView = itemView.findViewById(R.id.monthly_expense)
        val tvTotal: TextView = itemView.findViewById(R.id.monthly_total)
        private val rvWeeks: RecyclerView = itemView.findViewById(R.id.monthly_list_week)

        @RequiresApi(Build.VERSION_CODES.M)
        fun bind(data: MonthlyData) {
            tvMonthName.text = data.monthName
            tvDateRange.text = data.dateRange
            tvIncome.text = currency.formatCurrency(data.income)
            tvExpense.text = currency.formatCurrency(data.expense)
            tvTotal.text = currency.formatCurrency(data.total)

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
                rvWeeks.adapter = WeeklyAdapter(data.weeks)
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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        holder.bind(monthlyList[position])
    }

    override fun getItemCount(): Int = monthlyList.size

    fun updateData(newList: List<MonthlyData>) {
        monthlyList = newList
    }
}
