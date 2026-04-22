package com.henrystudio.moneymanager.presentation.daily.components.adapter

import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.data.model.Category
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.daily.model.DailyListItem
import java.time.format.DateTimeFormatter

class DailyAdapter(
    private val onClick: (Transaction) -> Unit,
    private val onLongClick: (Transaction) -> Unit
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<DailyListItem> = emptyList()
    private var categoriesById: Map<Int, Category> = emptyMap()
    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_TRANSACTION = 1
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_transaction_grouped, parent, false)
                HeaderViewHolder(view)
            }

            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_transaction, parent, false)
                TransactionViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(val item = items[position]) {
            is DailyListItem.Header -> {
                val h = holder as HeaderViewHolder
                h.bind(item)
            }

            is DailyListItem.TransactionItem -> {
                val h = holder as TransactionViewHolder
                h.bind(item)
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            val item = items[position]
            if (item is DailyListItem.TransactionItem) {
                val h = holder as TransactionViewHolder
                h.updateSelection(item) // 👈 QUAN TRỌNG
            }
            return
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    inner class HeaderViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val date : TextView = view.findViewById(R.id.transaction_date)
        private val income: TextView = view.findViewById(R.id.transaction_income)
        private val expense : TextView = view.findViewById(R.id.transaction_total_expense)
        private val headerFormatter = DateTimeFormatter.ofPattern("dd EEE")

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(item: DailyListItem.Header) {
            date.text = Helper.epochMillisToLocalDate(item.date).format(headerFormatter)
            income.text = Helper.formatCurrency(itemView.context , item.income)
            expense.text = Helper.formatCurrency(itemView.context, item.expense)
        }
    }

    inner class TransactionViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val noteText = view.findViewById<TextView>(R.id.item_transaction_content)
        private val amountText = view.findViewById<TextView>(R.id.item_transaction_amount)
        private val childCategory = view.findViewById<TextView>(R.id.item_transaction_child_category)
        private val account = view.findViewById<TextView>(R.id.item_transaction_account)
        private val category = view.findViewById<TextView>(R.id.item_transaction_category)

        fun bind(item: DailyListItem.TransactionItem) {
            noteText.text = item.transaction.note
            amountText.text = Helper.formatCurrency(itemView.context, item.transaction.amount)
            val (parentLabel, childLabel) =
                Helper.resolveTransactionCategoryLabels(item.transaction, categoriesById)
            childCategory.text = childLabel
            account.text = item.transaction.account
            category.text = parentLabel
            itemView.isSelected = item.isSelected

            itemView.setOnClickListener {
                onClick.invoke(item.transaction)
            }

            itemView.setOnLongClickListener {
                onLongClick.invoke(item.transaction)
                true
            }

            amountText.setTextColor(
                if (item.transaction.isIncome)
                    ContextCompat.getColor(itemView.context, R.color.income)
                else
                    ContextCompat.getColor(itemView.context, R.color.red)
            )

            updateSelection(item)
        }

        fun updateSelection(item: DailyListItem.TransactionItem) {
            itemView.setBackgroundColor(
                if (item.isSelected)
                    ContextCompat.getColor(itemView.context, R.color.rose)
                else
                    Color.TRANSPARENT
            )
        }
    }

    fun submitList(newList: List<DailyListItem>) {
        val diff = DiffUtil.calculateDiff(DailyDiffCallback(items, newList))
        items = newList
        diff.dispatchUpdatesTo(this)
    }

    fun setCategories(categories: List<Category>) {
        categoriesById = categories.associateBy { it.id }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is DailyListItem.Header -> TYPE_HEADER
            is DailyListItem.TransactionItem -> TYPE_TRANSACTION
        }
    }

    fun getItemAt(position: Int): DailyListItem = items[position]
}
