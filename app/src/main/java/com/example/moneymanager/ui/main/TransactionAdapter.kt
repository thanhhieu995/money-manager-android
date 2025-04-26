package com.example.moneymanager.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanager.R
import com.example.moneymanager.model.Transaction

class TransactionAdapter : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(
    DiffCallback()
) {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryText: TextView = itemView.findViewById(R.id.transaction_category)
        private val contentText: TextView = itemView.findViewById(R.id.transaction_content)
        private val amountText: TextView = itemView.findViewById(R.id.transaction_amount)

        fun bind(transaction: Transaction) {
            categoryText.text = "${transaction.category}"
            contentText.text = "${transaction.content}"
            amountText.text = "${transaction.amount}đ"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}
