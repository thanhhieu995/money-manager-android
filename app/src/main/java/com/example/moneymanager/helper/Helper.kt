package com.example.moneymanager.helper

import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.DiffUtil
import com.example.moneymanager.model.Transaction
import com.example.moneymanager.model.TransactionGroup
import com.example.moneymanager.ui.addtransaction.AddTransactionActivity
import com.example.moneymanager.ui.main.TransactionDiffCallback
import java.text.NumberFormat
import java.util.*

class Helper {
    companion object {
        fun openTransactionDetail(context: Context, transaction: Transaction) {
            val intent = Intent(context, AddTransactionActivity::class.java).apply {
                putExtra("transaction", transaction)
            }
            context.startActivity(intent)
        }

        fun formatCurrency(amount: Double): String {
            val format = NumberFormat.getNumberInstance(Locale("vi", "VN"))
            return format.format(amount) + "đ"
        }
    }
}