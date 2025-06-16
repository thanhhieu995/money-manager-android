package com.example.moneymanager.viewmodel

import androidx.lifecycle.LiveData
import com.example.moneymanager.model.Transaction
import com.example.moneymanager.model.TransactionDao
import com.example.moneymanager.model.TransactionGroup
import androidx.lifecycle.map

class TransactionRepository(private val dao: TransactionDao) {
    val allTransactions: LiveData<List<Transaction>> = dao.getAll()

    suspend fun insert(transaction: Transaction) {
        dao.insert(transaction)
    }

    fun getGroupedTransactions(): LiveData<List<TransactionGroup>> {
        return allTransactions.map { transactionList ->
            transactionList
                .groupBy { it.date }
                .map { (date, transactionsOnDate) ->
                    val income = transactionsOnDate.filter { it.isIncome }.sumOf { it.amount }
                    val expense = transactionsOnDate.filter { !it.isIncome }.sumOf { it.amount }

                    TransactionGroup(
                        id = date.hashCode(),
                        date = date,
                        income = income,
                        expense = expense,
                        transactions = transactionsOnDate
                    )
                }
        }
    }
}
