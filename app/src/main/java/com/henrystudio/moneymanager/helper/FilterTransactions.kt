package com.henrystudio.moneymanager.helper

import android.os.Build
import androidx.annotation.RequiresApi
import com.henrystudio.moneymanager.model.Transaction
import com.henrystudio.moneymanager.model.TransactionGroup
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FilterTransactions {
    companion object{
        @RequiresApi(Build.VERSION_CODES.O)
        fun filterTransactionGroupByMonth(
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
        fun filterTransactionGroupByYear(
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

        @RequiresApi(Build.VERSION_CODES.O)
        fun filterTransactionsByMonth(
            transactions: List<Transaction>,
            selectedMonth: LocalDate
        ): List<Transaction> {
            val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yy")
            return transactions.filter { tx ->
                val cleanedDate = tx.date.substringBefore(" ")
                val date = LocalDate.parse(cleanedDate, inputFormatter)
                date.monthValue == selectedMonth.monthValue && date.year == selectedMonth.year
            }.sortedByDescending {
                val cleanedDate = it.date.substringBefore(" ")
                LocalDate.parse(cleanedDate, inputFormatter)
            }
        }
    }
}