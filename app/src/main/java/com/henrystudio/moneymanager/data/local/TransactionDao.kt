package com.henrystudio.moneymanager.data.local

import androidx.room.*
import com.henrystudio.moneymanager.data.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAll(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE isBookmarked = 1 ORDER BY date DESC")
    fun getBookmarkedTransactions(): Flow<List<Transaction>>

    @Delete
    suspend fun delete(transaction: Transaction)

    @Delete
    suspend fun deleteAll(transactions: List<Transaction>)

    @Update
    suspend fun update(transaction: Transaction)
}