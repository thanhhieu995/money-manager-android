package com.example.moneymanager.ui.main

import androidx.recyclerview.widget.DiffUtil
import com.example.moneymanager.model.Transaction

class TransactionDiffCallback(private val oldList: List<Transaction>,
                              private val newList: List<Transaction>): DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}