package com.henrystudio.moneymanager.presentation.daily.components.adapter

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.daily.model.DailyTransactionGroupUi

class DailyTransactionGroupUiAdapter : RecyclerView.Adapter<DailyTransactionGroupUiAdapter.GroupViewHolder>() {

    private var groups: List<DailyTransactionGroupUi> = emptyList()
    private val childAdapters = mutableMapOf<Int, DailyTransactionUiAdapter>()

    var onTransactionClick: ((Transaction) -> Unit)? = null
    var onTransactionLongClick: ((Transaction) -> Unit)? = null

    fun submitList(newList: List<DailyTransactionGroupUi>) {
        val diff = DiffUtil.calculateDiff(DailyTransactionGroupUiDiffCallback(groups, newList))

        groups = newList
        val newKeys = newList.map { it.id }.toSet()
        childAdapters.keys.retainAll(newKeys)

        diff.dispatchUpdatesTo(this)
    }

    inner class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.transaction_date)
        val income: TextView = view.findViewById(R.id.transaction_income)
        val expense: TextView = view.findViewById(R.id.transaction_total_expense)
        val recycler: RecyclerView = view.findViewById(R.id.transaction_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction_grouped, parent, false)
        return GroupViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]

        holder.date.text = group.date
        holder.income.text = Helper.formatCurrency(group.income)
        holder.expense.text = Helper.formatCurrency(group.expense)

        val adapter = childAdapters.getOrPut(group.id) {
            DailyTransactionUiAdapter(
                onClick = { ui ->
                    onTransactionClick?.invoke(ui.transaction)
                },
                onLongClick = { ui ->
                    onTransactionLongClick?.invoke(ui.transaction)
                }
            )
        }

        if (holder.recycler.layoutManager == null) {
            holder.recycler.layoutManager = LinearLayoutManager(holder.itemView.context)
        }

        holder.recycler.adapter = adapter
        holder.recycler.isNestedScrollingEnabled = false

        adapter.submitList(group.transactions)
    }

    override fun onBindViewHolder(
        holder: GroupViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        val group = groups[position]

        val adapter = childAdapters.getOrPut(group.id) {
            DailyTransactionUiAdapter(
                onClick = { onTransactionClick?.invoke(it.transaction) },
                onLongClick = { onTransactionLongClick?.invoke(it.transaction) }
            )
        }

        if (payloads.isNotEmpty()) {
            // 🔥 CHỈ update child list
            adapter.submitList(group.transactions)
        } else {
            // bind full
            holder.date.text = group.date
            holder.income.text = Helper.formatCurrency(group.income)
            holder.expense.text = Helper.formatCurrency(group.expense)

            if (holder.recycler.layoutManager == null) {
                holder.recycler.layoutManager = LinearLayoutManager(holder.itemView.context)
            }

            holder.recycler.adapter = adapter
            holder.recycler.isNestedScrollingEnabled = false

            adapter.submitList(group.transactions)
        }
    }

    override fun getItemCount() = groups.size

    fun getGroupAt(position: Int): DailyTransactionGroupUi = groups[position]
}