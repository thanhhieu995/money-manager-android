package com.example.moneymanager.ui.search

import android.content.Intent
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
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanager.R
import com.example.moneymanager.model.Transaction
import com.example.moneymanager.ui.addtransaction.AddTransactionActivity
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class TransactionAdapter(
    private var transactions: List<Transaction>,
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>(), Filterable {

    interface OnFilterResultListener {
        fun onFilterResult(filteredList: List<Transaction>)
    }

    private var filterResultListener: OnFilterResultListener? = null
    var filterPeriod: FilterPeriod = FilterPeriod.All

    fun setOnFilterResultListener(listener: OnFilterResultListener) {
        filterResultListener = listener
    }

    private var filteredTransactions: List<Transaction> = transactions

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteText: TextView = itemView.findViewById(R.id.item_transaction_content)
        val amountText: TextView = itemView.findViewById(R.id.item_transaction_amount)
        val dateText: TextView = itemView.findViewById(R.id.item_transaction_date)
        val account: TextView = itemView.findViewById(R.id.item_transaction_account)
        val category : TextView = itemView.findViewById(R.id.item_transaction_category)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val tx = filteredTransactions[position]
        holder.noteText.text = tx.note
        holder.amountText.text = formatCurrency(tx.amount)
        holder.dateText.text = tx.date // Có thể định dạng nếu muốn
        holder.account.text = tx.account
        holder.category.text = tx.category

        if (tx.isIncome) {
            holder.amountText.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.teal_700))
        } else {
            holder.amountText.setTextColor(Color.RED)
        }
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, AddTransactionActivity::class.java)
            intent.putExtra("transaction", tx)
            context.startActivity(intent)
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
            @RequiresApi(Build.VERSION_CODES.O)
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim()

                val filtered = transactions.filter { transaction ->
                    val cleanedDate = transaction.date.substringBefore(" ") // "24/05/25"
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yy")
                    val date = LocalDate.parse(cleanedDate, formatter)
                    val currentDate = LocalDate.now()
                    val startOfWeek = currentDate.with(DayOfWeek.MONDAY)
                    val endOfWeek = startOfWeek.plusDays(6)
                    val currentMonth = currentDate.monthValue     // 1 đến 12
                    val currentYear = currentDate.year
                    val matchQuery = query.isNullOrEmpty() || transaction.note.lowercase().contains(query)
                            || transaction.date.contains(query)
                            || formatCurrency(transaction.amount).contains(query)

                    val matchPeriod = when (filterPeriod) {
                        FilterPeriod.All -> true
                        FilterPeriod.Weekly ->  !date.isBefore(startOfWeek) && !date.isAfter(endOfWeek)
                        FilterPeriod.Monthly -> date.month.value == currentMonth && date.year == currentYear
                        FilterPeriod.Yearly -> date.year == currentYear
                    }

                    matchQuery && matchPeriod
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
