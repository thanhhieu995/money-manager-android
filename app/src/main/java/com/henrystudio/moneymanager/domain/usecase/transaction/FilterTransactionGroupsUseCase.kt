package com.henrystudio.moneymanager.domain.usecase.transaction

import android.os.Build
import androidx.annotation.RequiresApi
import com.henrystudio.moneymanager.core.util.FilterTransactions
import com.henrystudio.moneymanager.data.model.TransactionGroup
import com.henrystudio.moneymanager.presentation.model.FilterOption
import com.henrystudio.moneymanager.presentation.model.FilterPeriodStatistic
import com.henrystudio.moneymanager.presentation.model.KeyFilter
import com.henrystudio.moneymanager.presentation.model.TransactionType
import java.time.LocalDate
import javax.inject.Inject

class FilterTransactionGroupsUseCase @Inject constructor() {
    @RequiresApi(Build.VERSION_CODES.O)
    operator fun invoke(
        transactions: List<TransactionGroup>,
        filterOption: FilterOption,
        selectedMonth: LocalDate,
        categoryName: String?,
        transactionType: TransactionType,
        keyFilter: KeyFilter,
        isFromMainActivity: Boolean
    ): List<TransactionGroup> {

        val filteredByCategory = if (categoryName != null) {
            filterByCategory(transactions, categoryName, transactionType, keyFilter)
        } else transactions

        return when {
            isFromMainActivity -> {
                FilterTransactions.filterTransactionGroupByMonth(filteredByCategory, selectedMonth)
            }
            else -> {
                when (filterOption.type) {
                    FilterPeriodStatistic.Weekly ->
                        FilterTransactions.filterTransactionGroupByWeek(filteredByCategory, selectedMonth)
                    FilterPeriodStatistic.Monthly ->
                        FilterTransactions.filterTransactionGroupByMonth(filteredByCategory, selectedMonth)
                    FilterPeriodStatistic.Yearly ->
                        FilterTransactions.filterTransactionGroupByYear(filteredByCategory, selectedMonth)
                    else ->
                        FilterTransactions.filterTransactionGroupByMonth(filteredByCategory, selectedMonth)
                }
            }
        }
    }

    private fun filterByCategory(
        transactions: List<TransactionGroup>,
        categoryName: String,
        transactionType: TransactionType,
        keyFilter: KeyFilter
    ): List<TransactionGroup> {
        val isInCome = transactionType == TransactionType.INCOME
        return transactions.mapNotNull { group ->
            val filteredTransactions = when (keyFilter) {
                KeyFilter.CategoryParent -> {
                    group.transactions.filter {
                        it.categoryParentName.equals(categoryName, ignoreCase = true)
                    }
                }
                KeyFilter.CategorySub -> {
                    group.transactions.filter {
                        it.categorySubName.trim().equals(categoryName.trim(), ignoreCase = true)
                    }
                }
                KeyFilter.Note -> {
                    group.transactions.filter {
                        it.note.trim().equals(categoryName.trim(), ignoreCase = true) && it.isIncome == isInCome
                    }
                }
                KeyFilter.Account -> {
                    group.transactions.filter {
                        it.account.trim().equals(categoryName.trim(), ignoreCase = true)
                    }
                }
                else -> group.transactions.filter { it.isIncome == isInCome }
            }

            if (filteredTransactions.isNotEmpty()) {
                group.copy(
                    income = filteredTransactions.filter { it.isIncome }.sumOf { it.amount },
                    expense = filteredTransactions.filter { !it.isIncome }.sumOf { it.amount },
                    transactions = filteredTransactions
                )
            } else null
        }
    }
}