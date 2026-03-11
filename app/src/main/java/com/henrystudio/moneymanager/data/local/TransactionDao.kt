package com.henrystudio.moneymanager.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.henrystudio.moneymanager.data.model.Transaction

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAll(): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE isBookmarked = 1 ORDER BY date DESC")
    fun getBookmarkedTransactions(): LiveData<List<Transaction>>

    @Delete
    suspend fun delete(transaction: Transaction)

    @Delete
    suspend fun deleteAll(transactions: List<Transaction>)

    @Update
    suspend fun update(transaction: Transaction)
}