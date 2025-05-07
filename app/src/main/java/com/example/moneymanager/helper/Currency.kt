package com.example.moneymanager.helper

import java.text.NumberFormat
import java.util.*

class Currency {
    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return format.format(amount) + "đ"
    }
}