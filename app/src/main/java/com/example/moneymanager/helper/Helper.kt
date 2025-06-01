package com.example.moneymanager.helper

import android.content.Context
import android.content.Intent
import com.example.moneymanager.model.Transaction
import com.example.moneymanager.ui.addtransaction.AddTransactionActivity
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