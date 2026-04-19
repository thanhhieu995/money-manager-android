package com.henrystudio.moneymanager.presentation.bookmark.components.diffutil

import androidx.recyclerview.widget.DiffUtil
import com.henrystudio.moneymanager.presentation.bookmark.model.BookmarkItemUi

class BookmarkItemUiDiffCallback(
    private val oldList: List<BookmarkItemUi>,
    private val newList: List<BookmarkItemUi>
): DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].transaction.id == newList[newItemPosition].transaction.id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}