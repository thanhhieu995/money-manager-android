package com.henrystudio.moneymanager.data.repository

import androidx.lifecycle.LiveData
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.data.local.TransactionDao
import com.henrystudio.moneymanager.data.model.TransactionGroup
import com.henrystudio.moneymanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl(private val dao: TransactionDao) : TransactionRepository {
    override fun getAllTransactions(): Flow<List<Transaction>> {
        return dao.getAll()
    }

    override suspend fun insert(transaction: Transaction) {
        dao.insert(transaction)
    }

    override suspend fun delete(transaction: Transaction) {
        dao.delete(transaction)
    }

    override suspend fun deleteAll(transactionList: List<Transaction>) {
        dao.deleteAll(transactionList)
    }

    override suspend fun update(transaction: Transaction) {
        dao.update(transaction)
    }

    override fun getGroupedTransactions(): Flow<List<TransactionGroup>> {
        return dao.getAll().map { transactionList ->
            transactionList
                .groupBy { it.date }
                .map { (date, transactionsOnDate) ->

                    val income = transactionsOnDate
                        .filter { it.isIncome }
                        .sumOf { it.amount }

                    val expense = transactionsOnDate
                        .filter { !it.isIncome }
                        .sumOf { it.amount }

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

    override fun getBookmarkedTransactions() : Flow<List<Transaction>> {
        return dao.getBookmarkedTransactions()
    }
}
