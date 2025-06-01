package com.example.moneymanager.ui.monthly

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanager.R
import com.example.moneymanager.helper.Helper

class WeeklyAdapter(
    private val weeklyList: List<WeeklyData>,
    private val onWeekClick: (WeeklyData) -> Unit
) : RecyclerView.Adapter<WeeklyAdapter.WeekViewHolder>() {

    inner class WeekViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvWeekRange: TextView = view.findViewById(R.id.monthly_detail_arrange)
        private val tvIncome: TextView = view.findViewById(R.id.monthly_detail_income)
        private val tvExpense: TextView = view.findViewById(R.id.monthly_detail_expense)
        private val tvTotal: TextView = view.findViewById(R.id.monthly_detail_total)

        @RequiresApi(Build.VERSION_CODES.M)
        fun bind(data: WeeklyData) {
            tvWeekRange.text = data.weekRange
            tvIncome.text = Helper.formatCurrency(data.income)
            tvExpense.text = Helper.formatCurrency(data.expense)
            tvTotal.text = Helper.formatCurrency(data.total)

            tvTotal.setTextColor(
                when {
                    data.total > 0 -> itemView.context.getColor(R.color.teal_700)
                    data.total < 0 -> itemView.context.getColor(R.color.red)
                    else -> itemView.context.getColor(R.color.purple_200)
                }
            )
            itemView.setOnClickListener {
                onWeekClick(data)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_week, parent, false)
        return WeekViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: WeekViewHolder, position: Int) {
        holder.bind(weeklyList[position])
    }

    override fun getItemCount(): Int = weeklyList.size
}
