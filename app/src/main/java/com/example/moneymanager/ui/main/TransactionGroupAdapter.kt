package com.example.moneymanager.ui.main

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanager.R
import com.example.moneymanager.helper.Currency
import com.example.moneymanager.model.Transaction
import com.example.moneymanager.model.TransactionGroup

class TransactionGroupAdapter : RecyclerView.Adapter<TransactionGroupAdapter.GroupViewHolder>() {

    private var groups: List<TransactionGroup> = listOf()
    private var currency: Currency = Currency()

    private val selectedIds = mutableSetOf<Int>()
    var selectionMode = false
    var onItemLongClickListener: ((Int) -> Unit)? = null
    var onSelectionChanged: ((List<Transaction>) -> Unit)? = null

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

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        val fullDate = group.date // "13/05/25 (Tue)"
        val dayPart = fullDate.substringBefore("/") // "13"
        val dayOfWeek = fullDate.substringAfterLast(" ") // "(Tue)"

        holder.date.text = "$dayPart $dayOfWeek"
        holder.income.text = currency.formatCurrency(group.income)
        holder.expense.text = currency.formatCurrency(group.expense)

        holder.container.removeAllViews()

        for (tx in group.transactions) {
            val row = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.item_transaction_row, holder.container, false)
            row.findViewById<TextView>(R.id.transaction_row_category).text = tx.category
            row.findViewById<TextView>(R.id.transaction_row_content).text = tx.note
            val amountTextView = row.findViewById<TextView>(R.id.transaction_row_amount)
            amountTextView.text = currency.formatCurrency(tx.amount)
            if (tx.isIncome) {
                amountTextView.setTextColor(ContextCompat.getColor(row.context, R.color.income))
            } else {
                amountTextView.setTextColor(Color.RED)
            }

            row.setOnLongClickListener {
                selectionMode = true
                toggleSelection(tx.id)
                onItemLongClickListener?.invoke(tx.id)
                true
            }

            // ✅ Thêm xử lý click vào đây
            row.setOnClickListener {
                if (selectionMode) {
                    toggleSelection(tx.id)
                    onSelectionChanged?.invoke(getSelectedTransactions())
                } else {
                    val context = row.context
                    val intent = android.content.Intent(context, com.example.moneymanager.ui.addtransaction.AddTransactionActivity::class.java)
                    intent.putExtra("transaction", tx) // Truyền transactionId
                    context.startActivity(intent)
                }
            }

            if (selectedIds.contains(tx.id)) {
                row.setBackgroundColor(ContextCompat.getColor(row.context, R.color.purple_200))
            } else {
                row.setBackgroundColor(Color.TRANSPARENT)
            }
            holder.container.addView(row)
        }
    }

    override fun getItemCount(): Int = groups.size

    fun getGroupAt(position: Int): TransactionGroup = groups[position]

    private fun toggleSelection(transactionId: Int) {
        if (selectedIds.contains(transactionId)) {
            selectedIds.remove(transactionId)
            if (selectedIds.isEmpty()) selectionMode = false
        } else {
            selectedIds.add(transactionId)
        }
        notifyDataSetChanged()
    }

    fun clearSelection() {
        selectedIds.clear()
        selectionMode = false
    }

    fun getSelectedTransactions(): List<Transaction> {
        return groups.flatMap { it.transactions }.filter { selectedIds.contains(it.id) }
    }
}
