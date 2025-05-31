package com.example.moneymanager.ui.main

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanager.R
import com.example.moneymanager.helper.Currency
import com.example.moneymanager.model.Transaction
import com.example.moneymanager.model.TransactionGroup
import com.example.moneymanager.ui.search.TransactionAdapter

class TransactionGroupAdapter : RecyclerView.Adapter<TransactionGroupAdapter.GroupViewHolder>() {

    private var groups: List<TransactionGroup> = listOf()
    private var currency: Currency = Currency()

    var isTransactionSelected: ((Transaction) -> Boolean)? = null
    private val childAdapters = mutableMapOf<String, TransactionAdapter>() // key = group.date

    var onTransactionLongClick: ((Transaction) -> Boolean)? = null
    var onTransactionClick: ((Transaction) -> Boolean)? = null

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
        val container: RecyclerView = view.findViewById(R.id.transaction_container)
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

        // Setup RecyclerView con
        val childRecyclerView = holder.container
        childRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        childRecyclerView.setHasFixedSize(false)
        childRecyclerView.isNestedScrollingEnabled = false
        val adapter = childAdapters.getOrPut(group.date) {
            TransactionAdapter(group.transactions).apply {
                longClickListener = object : TransactionAdapter.OnTransactionLongClickListener {
                    override fun onTransactionLongClick(transaction: Transaction): Boolean {
                        return onTransactionLongClick?.invoke(transaction) ?: false
                    }
                }
                clickListener = object : TransactionAdapter.OnTransactionClickListener {
                    override fun onTransactionClick(transaction: Transaction): Boolean {
                        return onTransactionClick?.invoke(transaction) ?: false
                    }
                }
            }
        }
        adapter.isSelected = isTransactionSelected
        childRecyclerView.adapter = adapter
        adapter.updateList(group.transactions)
    }

    override fun getItemCount(): Int = groups.size

    fun getGroupAt(position: Int): TransactionGroup = groups[position]
}
