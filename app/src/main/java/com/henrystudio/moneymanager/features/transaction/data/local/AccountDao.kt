package com.henrystudio.moneymanager.features.transaction.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.henrystudio.moneymanager.model.Account

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