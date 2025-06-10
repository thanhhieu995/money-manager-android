package com.example.moneymanager.ui.main

import androidx.recyclerview.widget.DiffUtil
import com.example.moneymanager.model.TransactionGroup

class TransactionGroupDiffCallback(
    private val oldList: List<TransactionGroup>,
    private val newList: List<TransactionGroup>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Group theo ngày, nên ngày là key chính
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
