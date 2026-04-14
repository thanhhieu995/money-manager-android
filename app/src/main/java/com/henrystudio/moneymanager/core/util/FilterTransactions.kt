package com.henrystudio.moneymanager.core.util

import android.os.Build
import androidx.annotation.RequiresApi
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.data.model.TransactionGroup
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields

class FilterTransactions {
    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        fun filterTransactionGroupByWeek(
            transactions: List<TransactionGroup>,
            localDate: LocalDate
        ): List<TransactionGroup> {
            val weekFields = WeekFields.of(DayOfWeek.MONDAY, 1)
            val startOfWeek = localDate.with(DayOfWeek.MONDAY)
            val selectedWeek = startOfWeek.get(weekFields.weekOfWeekBasedYear())
            val selectedYear = startOfWeek.get(weekFields.weekBasedYear())
            return transactions.filter { group ->
                val date = Instant.ofEpochMilli(group.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val startOfWeekDate = date.with(DayOfWeek.MONDAY)
                val week = startOfWeekDate.get(weekFields.weekOfWeekBasedYear())
                val year = startOfWeekDate.get(weekFields.weekBasedYear())
                week == selectedWeek && year == selectedYear
            }.sortedByDescending {
                Instant.ofEpochMilli(it.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun filterTransactionGroupByMonth(
            transactions: List<TransactionGroup>,
            selectedMonth: LocalDate,
        ): List<TransactionGroup> {
            return transactions.filter { group ->
                val date = Instant.ofEpochMilli(group.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                date.monthValue == selectedMonth.monthValue && date.year == selectedMonth.year
            }.sortedByDescending {
                Instant.ofEpochMilli(it.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun filterTransactionGroupByYear(
            transactions: List<TransactionGroup>,
            selectedYear: LocalDate
        ): List<TransactionGroup> {
            return transactions.filter { group ->
                val date = Instant.ofEpochMilli(group.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                date.year == selectedYear.year
            }.sortedByDescending {
                Instant.ofEpochMilli(it.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun filterTransactionsByMonth(
            transactions: List<Transaction>,
            selectedMonth: LocalDate
        ): List<Transaction> {
            return transactions.filter { tx ->
                val date = Instant.ofEpochMilli(tx.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                date.monthValue == selectedMonth.monthValue && date.year == selectedMonth.year
            }.sortedByDescending {
                it.date
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun filterTransactionsByWeek(
            transactions: List<Transaction>,
            selectedDate: LocalDate
        ): List<Transaction> {
            val weekFields = WeekFields.of(DayOfWeek.MONDAY, 1)
            val selectedWeek = selectedDate.get(weekFields.weekOfWeekBasedYear())
            val selectedYear = selectedDate.get(weekFields.weekBasedYear())

            return transactions.filter { tx ->
                val date = Instant.ofEpochMilli(tx.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val week = date.get(weekFields.weekOfWeekBasedYear())
                val year = date.get(weekFields.weekBasedYear())
                week == selectedWeek && year == selectedYear
            }.sortedByDescending {
                it.date
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun filterTransactionsByYear(
            transactions: List<Transaction>,
            selectedDate: LocalDate
        ): List<Transaction> {
            val selectedYear = selectedDate.year

            return transactions.filter { tx ->
                val date = Instant.ofEpochMilli(tx.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                date.year == selectedYear
            }.sortedByDescending {
                it.date
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun filterTransactionsByCategoryName(
            allTransactions: List<Transaction>,
            categoryName: String
        ) : List<Transaction> {
            // legacy filter by label; without categories map, cannot resolve IDs here
            return emptyList()
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
