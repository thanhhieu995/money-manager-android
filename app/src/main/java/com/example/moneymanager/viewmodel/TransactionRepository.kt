package com.example.moneymanager.viewmodel

import androidx.lifecycle.LiveData
import com.example.moneymanager.model.Transaction
import com.example.moneymanager.model.TransactionDao

class TransactionRepository(private val dao: TransactionDao) {
    val allTransactions: LiveData<List<Transaction>> = dao.getAll()

    suspend fun insert(transaction: Transaction) {
        dao.insert(transaction)
    }
}
