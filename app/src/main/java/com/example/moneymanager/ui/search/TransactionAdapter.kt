package com.example.moneymanager.ui.search

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanager.R
import com.example.moneymanager.model.Transaction
import java.text.NumberFormat
import java.util.*

class TransactionAdapter(
    private var transactions: List<Transaction>,
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>(), Filterable {

    interface OnFilterResultListener {
        fun onFilterResult(filteredList: List<Transaction>)
    }

    private var filterResultListener: OnFilterResultListener? = null

    fun setOnFilterResultListener(listener: OnFilterResultListener) {
        filterResultListener = listener
    }

    private var filteredTransactions: List<Transaction> = transactions

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteText: TextView = itemView.findViewById(R.id.item_transaction_content)
        val amountText: TextView = itemView.findViewById(R.id.item_transaction_amount)
        val dateText: TextView = itemView.findViewById(R.id.item_transaction_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val tx = filteredTransactions[position]
        holder.noteText.text = tx.content
        holder.amountText.text = formatCurrency(tx.amount)
        holder.dateText.text = tx.date // Có thể định dạng nếu muốn

        if (tx.isIncome) {
            holder.amountText.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.teal_700))
        } else {
            holder.amountText.setTextColor(Color.RED)
        }
    }

    override fun getItemCount(): Int = filteredTransactions.size

    fun updateList(newList: List<Transaction>) {
        this.transactions = newList
        this.filteredTransactions = newList
        notifyDataSetChanged()
    }

    // Filter cho SearchView
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim()
                val filtered = if (query.isNullOrEmpty()) {
                    transactions
                } else {
                    transactions.filter {
                        it.content.lowercase().contains(query)
                                || it.date.contains(query)
                                || formatCurrency(it.amount).contains(query)
                    }
                }

                val results = FilterResults()
                results.values = filtered
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredTransactions = results?.values as List<Transaction>
                notifyDataSetChanged()
                filterResultListener?.onFilterResult(filteredTransactions)
            }
        }
    }

    fun formatCurrency(amount: Double): String {
        return NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(amount)
    }
}
