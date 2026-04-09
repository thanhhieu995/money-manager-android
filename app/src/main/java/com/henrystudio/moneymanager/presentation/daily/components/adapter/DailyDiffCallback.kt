package com.henrystudio.moneymanager.presentation.daily.components.adapter

import androidx.recyclerview.widget.DiffUtil
import com.henrystudio.moneymanager.presentation.daily.model.DailyListItem

class DailyDiffCallback(
    private val oldList: List<DailyListItem>,
    private val newList: List<DailyListItem>
): DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]

        return when {
            old is DailyListItem.Header && new is DailyListItem.Header ->
                old.id == new.id

            old is DailyListItem.TransactionItem && new is DailyListItem.TransactionItem ->
                old.transaction.id == new.transaction.id

            else -> false
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]

        if (old is DailyListItem.TransactionItem &&
            new is DailyListItem.TransactionItem &&
            old.isSelected != new.isSelected
            ) {
            return "SELECTION"
        }

        return null
    }
}