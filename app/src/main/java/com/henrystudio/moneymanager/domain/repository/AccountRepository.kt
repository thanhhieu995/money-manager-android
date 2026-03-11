package com.henrystudio.moneymanager.domain.repository

import androidx.lifecycle.LiveData
import com.henrystudio.moneymanager.data.model.Account

interface AccountRepository {
    fun getAllAccount(): LiveData<List<Account>>

    suspend fun insert(account: Account)

    suspend fun delete(account: Account)

    suspend fun update(account: Account)
}