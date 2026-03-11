package com.henrystudio.moneymanager.core.util

import android.os.Build
import androidx.annotation.RequiresApi
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.data.model.TransactionGroup
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields

class FilterTransactions {
    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        fun filterTransactionGroupByWeek(
            transactions: List<TransactionGroup>,
            localDate: LocalDate
        ): List<TransactionGroup> {
            val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yy")
            val weekFields = WeekFields.of(DayOfWeek.MONDAY, 1)
            val startOfWeek = localDate.with(DayOfWeek.MONDAY)
            val selectedWeek = startOfWeek.get(weekFields.weekOfWeekBasedYear())
            val selectedYear = startOfWeek.get(weekFields.weekBasedYear())
            return transactions.filter { group ->
                val cleanedDate = group.date.substringBefore(" ")
                val date = LocalDate.parse(cleanedDate, inputFormatter)
                val startOfWeekDate = date.with(DayOfWeek.MONDAY)
                val week = startOfWeekDate.get(weekFields.weekOfWeekBasedYear())
                val year = startOfWeekDate.get(weekFields.weekBasedYear())
                week == selectedWeek && year == selectedYear
            }.sortedByDescending {
                val cleanedDate = it.date.substringBefore(" ")
                LocalDate.parse(cleanedDate, inputFormatter)
            }
        }

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

        @RequiresApi(Build.VERSION_CODES.O)
        fun filterTransactionsByWeek(
            transactions: List<Transaction>,
            selectedDate: LocalDate
        ): List<Transaction> {
            val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yy")
            val weekFields = WeekFields.of(DayOfWeek.MONDAY, 1)
            val selectedWeek = selectedDate.get(weekFields.weekOfWeekBasedYear())
            val selectedYear = selectedDate.get(weekFields.weekBasedYear())

            return transactions.filter { tx ->
                val cleanedDate = tx.date.substringBefore(" ")
                val date = LocalDate.parse(cleanedDate, inputFormatter)
                val week = date.get(weekFields.weekOfWeekBasedYear())
                val year = date.get(weekFields.weekBasedYear())
                week == selectedWeek && year == selectedYear
            }.sortedByDescending {
                val cleanedDate = it.date.substringBefore(" ")
                LocalDate.parse(cleanedDate, inputFormatter)
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun filterTransactionsByYear(
            transactions: List<Transaction>,
            selectedDate: LocalDate
        ): List<Transaction> {
            val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yy")
            val selectedYear = selectedDate.year

            return transactions.filter { tx ->
                val cleanedDate = tx.date.substringBefore(" ")
                val date = LocalDate.parse(cleanedDate, inputFormatter)
                date.year == selectedYear
            }.sortedByDescending {
                val cleanedDate = it.date.substringBefore(" ")
                LocalDate.parse(cleanedDate, inputFormatter)
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun filterTransactionsByCategoryName(
            allTransactions: List<Transaction>,
            categoryName: String
        ) : List<Transaction> {
            return allTransactions.filter { tx ->
                tx.categoryParentName.equals(categoryName, ignoreCase = true)
            }
        }

        fun filterTransactionsByNoteName(
            allTransactions: List<Transaction>,
            note: String
        ) : List<Transaction> {
            return allTransactions.filter { tx ->
                tx.note.equals(note, ignoreCase = true)
            }
        }
    }
}