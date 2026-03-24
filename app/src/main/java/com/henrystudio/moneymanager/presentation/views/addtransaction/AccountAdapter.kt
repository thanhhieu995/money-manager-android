package com.henrystudio.moneymanager.presentation.views.addtransaction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.data.model.Account

class AccountAdapter(
    private var accounts: List<Account> = emptyList(),
    private val itemClick: (Account) -> Unit
) : RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    inner class AccountViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.item_account_dialog_name)
        fun bind(item: Account) {
            name.text = item.name
            itemView.setOnClickListener {
                itemClick.invoke(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_account_dialog, parent, false)
        return AccountViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(accounts[position])
    }

    override fun getItemCount(): Int = accounts.size

    fun updateData(newList: List<Account>) {
        this.accounts = newList
        notifyDataSetChanged()
    }
}