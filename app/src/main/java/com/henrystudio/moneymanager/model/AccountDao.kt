package com.henrystudio.moneymanager.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts")
    fun getAll(): LiveData<List<Account>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: Account)

    @Delete
    suspend fun delete(account: Account)

    @Update
    suspend fun update(account: Account)
}