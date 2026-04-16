package com.henrystudio.moneymanager.presentation.views.search

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
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>(), Filterable {
    private var categoriesById: Map<Int, Category> = emptyMap()

    private var filterResultListener: OnFilterResultListener? = null
    var filterPeriod: FilterPeriodSearch = FilterPeriodSearch.All
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

    interface OnFilterResultListener {
        fun onFilterResult(filteredList: List<Transaction>)
    }

    fun setOnFilterResultListener(listener: OnFilterResultListener) {
        filterResultListener = listener
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
        holder.amountText.text = Helper.formatCurrency(tx.amount)
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

    override fun getFilter(): Filter {
        return object : Filter() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val queryRaw = constraint?.toString()?.lowercase()?.trim()
                if (queryRaw?.isEmpty() == true) {
                    // Nếu người dùng không nhập gì (hoặc chỉ nhập khoảng trắng), trả về toàn bộ hoặc rỗng tùy ý
                    val filtered = when (filterPeriod) {
                        FilterPeriodSearch.All -> transactions
                        else -> transactions.filter { tx ->
                            val formatter = DateTimeFormatter.ofPattern("dd/MM/yy")
                            val txDate = Helper.epochMillisToLocalDate(tx.date)
                            when (filterPeriod) {
                                FilterPeriodSearch.Weekly -> {
                                    val now = LocalDate.now()
                                    val startOfWeek = now.with(DayOfWeek.MONDAY)
                                    val endOfWeek = startOfWeek.plusDays(6)
                                    !txDate.isBefore(startOfWeek) && !txDate.isAfter(endOfWeek)
                                }
                                FilterPeriodSearch.Monthly -> {
                                    val now = LocalDate.now()
                                    txDate.monthValue == now.monthValue && txDate.year == now.year
                                }
                                FilterPeriodSearch.Yearly -> {
                                    val now = LocalDate.now()
                                    txDate.year == now.year
                                }
                                else -> true
                            }
                        }
                    }

                    return FilterResults().apply { values = filtered }
                }
                val query = queryRaw?.removeVietnameseDiacritics()
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yy")
                val currentDate = LocalDate.now()
                val startOfWeek = currentDate.with(DayOfWeek.MONDAY)
                val endOfWeek = startOfWeek.plusDays(6)
                val currentMonth = currentDate.monthValue
                val currentYear = currentDate.year

                val filtered = transactions.filter { tx ->
                    val txDate = Helper.epochMillisToLocalDate(tx.date)

                    val note = tx.note.lowercase().removeVietnameseDiacritics()
                    val date = Helper.formatEpochMillisToDisplayDate(tx.date).lowercase().removeVietnameseDiacritics()
                    val amount = Helper.formatCurrency(tx.amount).lowercase()

                    val matchQuery = query.isNullOrEmpty()
                            || note.contains(query)
                            || date.contains(query)
                            || amount.contains(query)

                    val matchPeriod = when (filterPeriod) {
                        FilterPeriodSearch.All -> true
                        FilterPeriodSearch.Weekly -> !txDate.isBefore(startOfWeek) && !txDate.isAfter(endOfWeek)
                        FilterPeriodSearch.Monthly -> txDate.monthValue == currentMonth && txDate.year == currentYear
                        FilterPeriodSearch.Yearly -> txDate.year == currentYear
                    }

                    matchQuery && matchPeriod
                }

                return FilterResults().apply {
                    values = filtered
                }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                val newFiltered = results?.values as? List<Transaction> ?: listOf()
                updateFilteredList(newFiltered)
                filterResultListener?.onFilterResult(newFiltered)
            }
        }
    }

    fun String.removeVietnameseDiacritics(): String {
        val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
        return Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(normalized).replaceAll("")
    }
}
