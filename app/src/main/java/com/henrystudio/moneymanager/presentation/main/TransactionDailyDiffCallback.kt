package com.henrystudio.moneymanager.presentation.main

import androidx.recyclerview.widget.DiffUtil
import com.henrystudio.moneymanager.data.model.Transaction

object TransactionDailyDiffCallback: DiffUtil.ItemCallback<Transaction>() {
    override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
        return oldItem.id == newItem.id // hoặc so sánh theo unique key của bạn
    }

    override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
        return oldItem == newItem
    }
}