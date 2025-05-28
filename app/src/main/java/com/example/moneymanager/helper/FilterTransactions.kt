package com.example.moneymanager.helper

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.moneymanager.model.TransactionGroup
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FilterTransactions {
    @RequiresApi(Build.VERSION_CODES.O)
    fun filterTransactionsByMonth(
        transactions: List<TransactionGroup>,
        selectedMonth: LocalDate,
    ): List<TransactionGroup> {
        val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yy")
        return transactions.filter { group ->
            val cleanedDate = group.date.substringBefore(" ")
            val date = LocalDate.parse(cleanedDate, inputFormatter)
            date.monthValue == selectedMonth.monthValue && date.year == selectedMonth.year
        }.sortedByDescending {
            val cleanedDate = it.date.substringBefore(" ")
            LocalDate.parse(cleanedDate, inputFormatter)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun filterTransactionsByYear(
        transactions: List<TransactionGroup>,
        selectedYear: LocalDate
    ): List<TransactionGroup> {
        val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yy")
        return transactions.filter { group ->
            val cleanedDate = group.date.substringBefore(" ")
            val date = LocalDate.parse(cleanedDate, inputFormatter)
            date.year == selectedYear.year
        }.sortedByDescending {
            val cleanedDate = it.date.substringBefore(" ")
            LocalDate.parse(cleanedDate, inputFormatter)
        }
    }
}