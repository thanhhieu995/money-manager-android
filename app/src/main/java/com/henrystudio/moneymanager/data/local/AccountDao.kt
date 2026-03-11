package com.henrystudio.moneymanager.data.local

import androidx.room.*
import com.henrystudio.moneymanager.data.model.Account
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts")
    fun getAll(): Flow<List<Account>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: Account)

    @Delete
    suspend fun delete(account: Account)

    @Update
    suspend fun update(account: Account)
}