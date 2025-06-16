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
import com.example.moneymanager.helper.Helper
import com.example.moneymanager.model.Transaction
import com.example.moneymanager.model.TransactionGroup
import com.example.moneymanager.ui.daily.TransactionDailyAdapter

class TransactionGroupAdapter : RecyclerView.Adapter<TransactionGroupAdapter.GroupViewHolder>() {

    private var groups: List<TransactionGroup> = listOf()

    var isTransactionSelected: ((Transaction) -> Boolean)? = null
    private val childAdapters = mutableMapOf<String, TransactionDailyAdapter>() // key = group.date

    var onTransactionLongClick: ((Transaction) -> Boolean)? = null
    var onTransactionClick: ((Transaction) -> Boolean)? = null

    fun submitList(newList: List<TransactionGroup>) {
        // Xoá adapter cũ chỉ nếu group bị xoá khỏi danh sách
        val newKeys = newList.map { it.date }.toSet()
        childAdapters.keys.retainAll(newKeys) // giữ lại các key vẫn còn trong danh sách mới

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

    init {
        setHasStableIds(true)
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
        holder.income.text = Helper.formatCurrency(group.income)
        holder.expense.text = Helper.formatCurrency(group.expense)

        // Setup RecyclerView con
        val childRecyclerView = holder.container
        if (childRecyclerView.layoutManager == null) {
            childRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        }
        childRecyclerView.setHasFixedSize(false)
        childRecyclerView.isNestedScrollingEnabled = false
        val adapter = childAdapters.getOrPut(group.date) {
            TransactionDailyAdapter(
                isSelected = isTransactionSelected,
                clickListener = onTransactionClick,
                longClickListener = onTransactionLongClick
            )
        }
        childRecyclerView.adapter = adapter
        if (adapter.currentList != group.transactions) {
            adapter.submitList(group.transactions.toMutableList())
        }
    }

    override fun getItemCount(): Int = groups.size

    fun getGroupAt(position: Int): TransactionGroup = groups[position]

    fun getChildAdapterForGroup(groupDate: String): TransactionDailyAdapter? {
        return childAdapters[groupDate]
    }

    override fun getItemId(position: Int): Long {
        return groups[position].id.toLong()
    }
}
