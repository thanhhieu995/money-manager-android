package com.henrystudio.moneymanager.presentation.daily.components.adapter

import androidx.recyclerview.widget.DiffUtil
import com.henrystudio.moneymanager.presentation.daily.model.DailyTransactionUi

class DailyTransactionUiDiffCallback(
    private val oldList: List<DailyTransactionUi>,
    private val newList: List<DailyTransactionUi>
): DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].transaction.id == newList[newItemPosition].transaction.id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        return oldItem.transaction == newItem.transaction &&
                oldItem.isSelected == newItem.isSelected
    }

    override fun getChangePayload(oldPos: Int, newPos: Int): Any? {
        val oldItem = oldList[oldPos]
        val newItem = newList[newPos]

        return if (oldItem.isSelected != newItem.isSelected) {
            "SELECTION"
        } else null
    }
}