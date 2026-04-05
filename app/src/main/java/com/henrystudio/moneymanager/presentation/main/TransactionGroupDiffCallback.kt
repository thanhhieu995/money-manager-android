package com.henrystudio.moneymanager.presentation.views.main

import androidx.recyclerview.widget.DiffUtil
import com.henrystudio.moneymanager.data.model.TransactionGroup

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
        return oldList[oldItemPosition].date == newList[newItemPosition].date &&
                oldList[oldItemPosition].income == newList[newItemPosition].income &&
                oldList[oldItemPosition].expense == newList[newItemPosition].expense &&
                oldList[oldItemPosition].transactions == newList[newItemPosition].transactions
    }
}
