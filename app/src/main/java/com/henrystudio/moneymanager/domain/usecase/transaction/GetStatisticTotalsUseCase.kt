package com.henrystudio.moneymanager.domain.usecase.transaction

import android.os.Build
import androidx.annotation.RequiresApi
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.model.FilterOption
import com.henrystudio.moneymanager.presentation.model.FilterPeriodStatistic
import java.time.DayOfWeek
import java.time.temporal.WeekFields

data class StatisticTotals(
    val income: Long = 0L,
    val expense: Long = 0L
)

class GetStatisticTotalsUseCase {
    @RequiresApi(Build.VERSION_CODES.O)
    operator fun invoke(
        transactions: List<Transaction>,
        filterOption: FilterOption
    ): StatisticTotals {
        val filteredTransactions = transactions.filter { transaction ->
            val transactionDate = Helper.epochMillisToLocalDate(transaction.date)

            when (filterOption.type) {
                FilterPeriodStatistic.Monthly -> {
                    transactionDate.month == filterOption.date.month &&
                        transactionDate.year == filterOption.date.year
                }

                FilterPeriodStatistic.Weekly -> {
                    val weekFields = WeekFields.of(DayOfWeek.MONDAY, 1)
                    val start = filterOption.date.with(weekFields.dayOfWeek(), 1)
                    val end = filterOption.date.with(weekFields.dayOfWeek(), 7)
                    !transactionDate.isBefore(start) && !transactionDate.isAfter(end)
                }

                FilterPeriodStatistic.Yearly -> {
                    transactionDate.year == filterOption.date.year
                }

                else -> true
            }
        }

        return StatisticTotals(
            income = filteredTransactions.filter { it.isIncome }.sumOf { it.amount },
            expense = filteredTransactions.filterNot { it.isIncome }.sumOf { it.amount }
        )
    }
}
