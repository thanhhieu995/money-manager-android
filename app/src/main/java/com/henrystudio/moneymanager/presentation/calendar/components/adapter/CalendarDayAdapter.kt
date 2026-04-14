package com.henrystudio.moneymanager.presentation.calendar.components.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.data.model.Category
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.calendar.model.CalendarDayItem

class CalendarDayAdapter(
    private val onClick: (Transaction) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<CalendarDayItem> = emptyList()
    private var categoriesById: Map<Int, Category> = emptyMap()

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_TRANSACTION = 1
    }

    fun submitList(newList: List<CalendarDayItem>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when(items[position]) {
            is CalendarDayItem.Header -> TYPE_HEADER
            is CalendarDayItem.TransactionItem -> TYPE_TRANSACTION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_transaction_grouped, parent, false)
            return HeaderVH(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_transaction, parent, false)
            return TransactionVH(view)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(val item = items[position]) {
            is CalendarDayItem.Header -> {
                val h = holder as HeaderVH
                h.bind(item)
            }
            is CalendarDayItem.TransactionItem -> {
                val h = holder as TransactionVH
                h.bind(item)
            }
        }
    }

    inner class HeaderVH(view: View) : RecyclerView.ViewHolder(view) {
        private val date = view.findViewById<TextView>(R.id.transaction_date)
        private val income = view.findViewById<TextView>(R.id.transaction_income)
        private val expense = view.findViewById<TextView>(R.id.transaction_total_expense)

        fun bind(item: CalendarDayItem.Header) {
            date.text = item.date
            income.text = Helper.formatCurrency(item.income)
            expense.text = Helper.formatCurrency(item.expense)
        }
    }

    inner class TransactionVH(view: View) : RecyclerView.ViewHolder(view) {
        private val noteText = view.findViewById<TextView>(R.id.item_transaction_content)
        private val amountText = view.findViewById<TextView>(R.id.item_transaction_amount)
        private val childCategory = view.findViewById<TextView>(R.id.item_transaction_child_category)
        private val account = view.findViewById<TextView>(R.id.item_transaction_account)
        private val category = view.findViewById<TextView>(R.id.item_transaction_category)

        fun bind(item: CalendarDayItem.TransactionItem) {
            val tx = item.transaction
            noteText.text = item.transaction.note
            amountText.text = Helper.formatCurrency(item.transaction.amount)
            val (parentLabel, childLabel) =
                Helper.resolveTransactionCategoryLabels(item.transaction, categoriesById)
            childCategory.text = childLabel
            account.text = item.transaction.account
            category.text = parentLabel

            itemView.setOnClickListener {
                onClick(tx)
            }

            amountText.setTextColor(
                if (item.transaction.isIncome)
                    ContextCompat.getColor(itemView.context, R.color.income)
                else
                    ContextCompat.getColor(itemView.context, R.color.red)
            )
        }
    }

    fun setCategories(categories: List<Category>) {
        categoriesById = categories.associateBy { it.id }
        notifyDataSetChanged()
    }
}