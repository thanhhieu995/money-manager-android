package com.henrystudio.moneymanager.presentation.search

import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.data.model.Category
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.bookmark.model.TransactionUI
import java.text.Normalizer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

class TransactionAdapter(
    var transactions: List<Transaction>,
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {
    private var categoriesById: Map<Int, Category> = emptyMap()

    var isSelected: ((Transaction) -> Boolean)? = null

    private var filteredTransactions: List<Transaction> = transactions
    private var displayedItems: List<TransactionUI> = buildUiList(filteredTransactions)

    private class TransactionUiDiffCallback(
        private val oldList: List<TransactionUI>,
        private val newList: List<TransactionUI>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].transaction.id == newList[newItemPosition].transaction.id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

    interface OnTransactionLongClickListener {
        fun onTransactionLongClick(transaction: Transaction) : Boolean
    }

    interface OnTransactionClickListener {
        fun onTransactionClick(transaction: Transaction) : Boolean
    }

    var longClickListener: OnTransactionLongClickListener? = null
    var clickListener : OnTransactionClickListener? = null

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteText: TextView = itemView.findViewById(R.id.item_transaction_content)
        val amountText: TextView = itemView.findViewById(R.id.item_transaction_amount)
        val childCategory: TextView = itemView.findViewById(R.id.item_transaction_child_category)
        val account: TextView = itemView.findViewById(R.id.item_transaction_account)
        val category: TextView = itemView.findViewById(R.id.item_transaction_category)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val item = displayedItems[position]
        val tx = item.transaction

        holder.noteText.text = tx.note
        holder.amountText.text = Helper.formatCurrency(holder.itemView.context, tx.amount)
        holder.childCategory.text = ""
        holder.account.text = tx.account
        holder.category.text = item.categoryLabel

        holder.amountText.setTextColor(
            if (tx.isIncome)
                ContextCompat.getColor(holder.itemView.context, R.color.income)
            else
                Color.RED
        )

        holder.itemView.setOnClickListener {
            clickListener?.onTransactionClick(tx)
        }

        holder.itemView.setOnLongClickListener {
            longClickListener?.onTransactionLongClick(tx) ?: false
        }

        val selected = isSelected?.invoke(tx) == true
        holder.itemView.setBackgroundColor(
            if (selected)
                ContextCompat.getColor(holder.itemView.context, R.color.rose)
            else
                Color.TRANSPARENT
        )
    }

    override fun getItemCount(): Int = filteredTransactions.size

    fun updateList(newList: List<Transaction>) {
        this.transactions = newList
        updateFilteredList(newList)
    }

    fun setCategories(categories: List<Category>) {
        categoriesById = categories.associateBy { it.id }
        // categories changed => rebuild displayedItems so DiffUtil can trigger rebinds
        updateFilteredList(filteredTransactions)
    }

    private fun updateFilteredList(newFiltered: List<Transaction>) {
        val newDisplayed = buildUiList(newFiltered)
        val diffCallback = TransactionUiDiffCallback(this.displayedItems, newDisplayed)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.filteredTransactions = newFiltered
        this.displayedItems = newDisplayed
        diffResult.dispatchUpdatesTo(this)
    }

    private fun buildUiList(list: List<Transaction>): List<TransactionUI> {
        return list.map { tx ->
            val (parentLabel, _) = Helper.resolveTransactionCategoryLabels(tx, categoriesById)
            TransactionUI(tx, parentLabel)
        }
    }
}
