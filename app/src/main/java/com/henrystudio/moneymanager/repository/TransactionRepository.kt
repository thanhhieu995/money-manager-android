package com.henrystudio.moneymanager.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.henrystudio.moneymanager.features.transaction.data.local.Transaction
import com.henrystudio.moneymanager.features.transaction.data.local.TransactionDao
import com.henrystudio.moneymanager.features.transaction.data.local.TransactionGroup

class TransactionRepository(private val dao: TransactionDao) {
    val allTransactions: LiveData<List<Transaction>> = dao.getAll()

    suspend fun insert(transaction: Transaction) {
        dao.insert(transaction)
    }

    suspend fun delete(transaction: Transaction) {
        dao.delete(transaction)
    }

    suspend fun deleteAll(transactionList: List<Transaction>) {
        dao.deleteAll(transactionList)
    }

    suspend fun update(transaction: Transaction) {
        dao.update(transaction)
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

    fun getBookmarkedTransactions() : LiveData<List<Transaction>> {
        return dao.getBookmarkedTransactions()
    }
}
