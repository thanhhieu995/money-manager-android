package com.example.moneymanager.ui.main

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanager.R
import com.example.moneymanager.helper.Currency
import com.example.moneymanager.model.TransactionGroup

class TransactionGroupAdapter : RecyclerView.Adapter<TransactionGroupAdapter.GroupViewHolder>() {

    private var groups: List<TransactionGroup> = listOf()
    private var currency: Currency = Currency()

    fun submitList(newList: List<TransactionGroup>) {
        val diffCallback = TransactionGroupDiffCallback(groups, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        groups = newList
        diffResult.dispatchUpdatesTo(this)
    }

    inner class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.transaction_date)
        val income: TextView = view.findViewById(R.id.transaction_income)
        val expense: TextView = view.findViewById(R.id.transaction_total_expense)
        val container: LinearLayout = view.findViewById(R.id.transaction_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction_grouped, parent, false)
        return GroupViewHolder(view)
    }

    @SuppressLint("MissingInflatedId")
    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.date.text = group.date
        holder.income.text = currency.formatCurrency(group.income)
        holder.expense.text = currency.formatCurrency(group.expense)

        holder.container.removeAllViews()
        for (tx in group.transactions) {
            val row = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.item_transaction_row, holder.container, false)
            row.findViewById<TextView>(R.id.transaction_row_category).text = tx.category
            row.findViewById<TextView>(R.id.transaction_row_content).text = tx.content
            row.findViewById<TextView>(R.id.transaction_row_amount).text = currency.formatCurrency(tx.amount)
            holder.container.addView(row)
        }
    }

    override fun getItemCount(): Int = groups.size
}
