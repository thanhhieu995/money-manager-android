package com.henrystudio.moneymanager.presentation.daily.components.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.presentation.daily.model.DailyTransactionUi

class DailyTransactionUiAdapter(
    private val onClick: ((DailyTransactionUi) -> Unit)? = null,
    private val onLongClick: ((DailyTransactionUi) -> Unit)? = null
) : RecyclerView.Adapter<DailyTransactionUiAdapter.ViewHolder>() {

    private var items: List<DailyTransactionUi> = emptyList()

    fun submitList(newList: List<DailyTransactionUi>) {
        val diff = DiffUtil.calculateDiff(
            DailyTransactionUiDiffCallback(items, newList)
        )

        items = newList.toList()
        diff.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val noteText = view.findViewById<TextView>(R.id.item_transaction_content)
        private val amountText = view.findViewById<TextView>(R.id.item_transaction_amount)
        private val childCategory = view.findViewById<TextView>(R.id.item_transaction_child_category)
        private val account = view.findViewById<TextView>(R.id.item_transaction_account)
        private val category = view.findViewById<TextView>(R.id.item_transaction_category)
        fun bind(item: DailyTransactionUi) {
            // TODO bind data
            itemView.isSelected = item.isSelected
            noteText.text = item.transaction.note
            amountText.text = Helper.formatCurrency(item.transaction.amount)
            childCategory.text = item.transaction.categorySubName
            account.text = item.transaction.account
            category.text = item.transaction.categoryParentName

            itemView.setOnClickListener {
                onClick?.invoke(item)
            }

            itemView.setOnLongClickListener {
                onLongClick?.invoke(item)
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

        fun updateSelection(item: DailyTransactionUi) {
            itemView.setBackgroundColor(
                if (item.isSelected)
                    ContextCompat.getColor(itemView.context, R.color.rose)
                else
                    Color.TRANSPARENT
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            // chỉ update phần cần thiết
            holder.updateSelection(items[position])
        } else {
            // bind full
            holder.bind(items[position])
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}