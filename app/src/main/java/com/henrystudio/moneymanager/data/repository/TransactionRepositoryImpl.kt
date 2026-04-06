package com.henrystudio.moneymanager.data.repository

import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.data.local.TransactionDao
import com.henrystudio.moneymanager.data.model.TransactionGroup
import com.henrystudio.moneymanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yy (EEE)")

        return dao.getAll().map { transactionList ->
            transactionList
                .groupBy { LocalDate.parse(it.date, formatter) }
                .map { (localDate, transactionsOnDate) ->

                    val income = transactionsOnDate
                        .filter { it.isIncome }
                        .sumOf { it.amount }

                    val expense = transactionsOnDate
                        .filter { !it.isIncome }
                        .sumOf { it.amount }

                    TransactionGroup(
                        id = localDate.toEpochDay().toInt(),
                        date = localDate.format(formatter),
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
