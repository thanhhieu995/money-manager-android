package com.henrystudio.moneymanager.presentation.bookmark.components.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.core.util.Helper.Companion.formatEpochMillisToDateKey
import com.henrystudio.moneymanager.data.model.Category
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.calendar.components.adapter.TransactionDiffCallback

class BookmarkAdapter(
    var items: List<Transaction>,
    private val onDeleteClick: (Transaction) -> Unit
) : RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder>() {

    var isEditMode = false
    var clickListener: OnBookmarkLickListener? = null
    private var categoriesById: Map<Int, Category> = emptyMap()

    interface OnBookmarkLickListener {
        fun onBookmarkClick(transaction: Transaction)
    }

    fun updateList(newList: List<Transaction>) {
        val diffCallback = TransactionDiffCallback(items, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        items = newList
        diffResult.dispatchUpdatesTo(this)
    }

    fun toggleEditMode() {
        isEditMode = !isEditMode
       notifyDataSetChanged()
    }

    inner class BookmarkViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val deleteIcon: ImageView = view.findViewById(R.id.item_bookmark_deleteIcon)
        private val date: TextView = view.findViewById(R.id.item_bookmark_date)
        private val category: TextView = view.findViewById(R.id.item_bookmark_category)
        private val content: TextView = view.findViewById(R.id.item_bookmark_content)
        private val account: TextView = view.findViewById(R.id.item_bookmark_account)
        private val amount: TextView = view.findViewById(R.id.item_bookmark_amount)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(item: Transaction) {
            date.text = formatEpochMillisToDateKey(item.date)
            val (parentLabel, _) = Helper.resolveTransactionCategoryLabels(item, categoriesById)
            category.text = parentLabel
            content.text = item.note
            account.text = item.account
            amount.text = Helper.formatCurrency(item.amount)
            amount.setTextColor(if (item.isIncome) ContextCompat.getColor(itemView.context, R.color.income)
            else ContextCompat.getColor(itemView.context, R.color.red))

            deleteIcon.visibility = if (isEditMode) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                clickListener?.onBookmarkClick(item)
            }

            deleteIcon.setOnClickListener {
                onDeleteClick.invoke(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bookmark, parent, false)
        return BookmarkViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun setCategories(categories: List<Category>) {
        categoriesById = categories.associateBy { it.id }
        notifyDataSetChanged()
    }
}