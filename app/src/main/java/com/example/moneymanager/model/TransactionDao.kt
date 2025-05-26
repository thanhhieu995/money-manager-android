package com.example.moneymanager.model

import androidx.lifecycle.LiveData
import androidx.room.*

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

    @Update
    suspend fun update(transaction: Transaction)
}