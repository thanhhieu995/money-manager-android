package com.henrystudio.moneymanager.presentation.daily

import androidx.recyclerview.widget.DiffUtil
import com.henrystudio.moneymanager.data.model.TransactionGroup
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
        return oldList[oldItemPosition].date == newList[newItemPosition].date &&
                oldList[oldItemPosition].income == newList[newItemPosition].income &&
                oldList[oldItemPosition].expense == newList[newItemPosition].expense &&
                oldList[oldItemPosition].transactions == newList[newItemPosition].transactions
    }
}
