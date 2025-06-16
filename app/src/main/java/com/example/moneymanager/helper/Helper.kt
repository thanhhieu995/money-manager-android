package com.example.moneymanager.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.example.moneymanager.R
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
            if (context is Activity) {
                context.overridePendingTransition(R.anim.slide_in_right, R.anim.no_animation)
            }
        }

        fun formatCurrency(amount: Double): String {
            val format = NumberFormat.getNumberInstance(Locale("vi", "VN"))
            return format.format(amount) + "đ"
        }
    }
}