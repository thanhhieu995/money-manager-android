package com.henrystudio.moneymanager.ui.yearly

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.core.util.Helper

class YearlyAdapter(private var yearlyList: List<YearlyData>,
private var onClickYear: (YearlyData) -> Unit) : RecyclerView.Adapter<YearlyAdapter.YearlyViewHolder>() {
    inner class YearlyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name : TextView = view.findViewById(R.id.yearly_name)
        private val arrange: TextView = view.findViewById(R.id.yearly_arrange)
        private val income: TextView = view.findViewById(R.id.yearly_income)
        private val expense: TextView = view.findViewById(R.id.yearly_expense)
        private val total: TextView = view.findViewById(R.id.yearly_total)
        fun bind(data: YearlyData) {
            name.text = data.name.toString()
            arrange.text = data.arrange
            income.text = Helper.formatCurrency(data.income)
            expense.text = Helper.formatCurrency(data.expense)
            total.text = Helper.formatCurrency(data.total)

            itemView.setOnClickListener {
                onClickYear.invoke(data)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YearlyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_year, parent, false)
        return YearlyViewHolder(view)
    }

    override fun onBindViewHolder(holder: YearlyViewHolder, position: Int) {
        holder.bind(yearlyList[position])
    }

    override fun getItemCount(): Int = yearlyList.size

    fun updateData(newList : List<YearlyData>) {
        yearlyList = newList
        notifyDataSetChanged()
    }
}