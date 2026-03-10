package com.henrystudio.moneymanager.ui.daily

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.features.transaction.data.local.Transaction

class TransactionDailyAdapter(
    private val isSelected: ((Transaction) -> Boolean)? = null,
    private val clickListener: ((Transaction) -> Boolean)? = null,
    private val longClickListener: ((Transaction) -> Boolean)? = null
) : ListAdapter<Transaction, TransactionDailyAdapter.TransactionViewHolder>(TransactionDailyDiffCallback) {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteText: TextView = itemView.findViewById(R.id.item_transaction_content)
        val amountText: TextView = itemView.findViewById(R.id.item_transaction_amount)
        val childCategory: TextView = itemView.findViewById(R.id.item_transaction_child_category)
        val account: TextView = itemView.findViewById(R.id.item_transaction_account)
        val category: TextView = itemView.findViewById(R.id.item_transaction_category)
    }

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val tx = getItem(position)

        holder.noteText.text = tx.note
        holder.amountText.text = Helper.formatCurrency(tx.amount)
        holder.category.text = tx.categoryParentName
        holder.childCategory.text = tx.categorySubName
        holder.account.text = tx.account.trim()

        holder.amountText.setTextColor(
            if (tx.isIncome)
                ContextCompat.getColor(holder.itemView.context, R.color.income)
            else
                Color.RED
        )

        holder.itemView.setOnClickListener {
            clickListener?.invoke(tx)
        }

        holder.itemView.setOnLongClickListener {
            longClickListener?.invoke(tx) ?: false
        }

        val selected = isSelected?.invoke(tx) == true
        holder.itemView.setBackgroundColor(
            if (selected)
                ContextCompat.getColor(holder.itemView.context, R.color.rose)
            else
                Color.TRANSPARENT
        )
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id.toLong()
    }

    fun updateTransaction(transaction: Transaction) {
        val index = currentList.indexOf(transaction)
        if (index != -1) {
            notifyItemChanged(index)
        }
    }
}