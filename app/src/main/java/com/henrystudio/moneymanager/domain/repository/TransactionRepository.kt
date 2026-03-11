package com.henrystudio.moneymanager.domain.repository

import androidx.lifecycle.LiveData
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.data.model.TransactionGroup


interface TransactionRepository {

    val allTransactions: LiveData<List<Transaction>>

    suspend fun insert(transaction: Transaction)

    suspend fun delete(transaction: Transaction)

    suspend fun deleteAll(transactionList: List<Transaction>)

    suspend fun update(transaction: Transaction)

    fun getGroupedTransactions(): LiveData<List<TransactionGroup>>

    fun getBookmarkedTransactions(): LiveData<List<Transaction>>

}