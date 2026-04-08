package com.henrystudio.moneymanager.presentation.daily.components.adapter

import androidx.recyclerview.widget.DiffUtil
import com.henrystudio.moneymanager.presentation.daily.model.DailyTransactionGroupUi

class DailyTransactionGroupUiDiffCallback(
    private val oldList: List<DailyTransactionGroupUi>,
    private val newList: List<DailyTransactionGroupUi>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Group theo ngày, nên ngày là key chính
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return oldItem.date == newItem.date &&
                oldItem.income == newItem.income &&
                oldItem.expense == newItem.expense &&
                oldItem.transactions == newItem.transactions
    }

    override fun getChangePayload(oldPos: Int, newPos: Int): Any? {
        val oldItem = oldList[oldPos]
        val newItem = newList[newPos]

        return if (oldItem.transactions != newItem.transactions) {
            "TRANSACTIONS_CHANGED"
        } else null
    }
}
