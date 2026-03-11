package com.henrystudio.moneymanager.domain.repository

import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.data.model.TransactionGroup
import kotlinx.coroutines.flow.Flow


interface TransactionRepository {

    fun  getAllTransactions(): Flow<List<Transaction>>

    suspend fun insert(transaction: Transaction)

    suspend fun delete(transaction: Transaction)

    suspend fun deleteAll(transactionList: List<Transaction>)

    suspend fun update(transaction: Transaction)

    fun getGroupedTransactions(): Flow<List<TransactionGroup>>

    fun getBookmarkedTransactions(): Flow<List<Transaction>>

}