package com.henrystudio.moneymanager.domain.repository

import com.henrystudio.moneymanager.data.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getAllAccount(): Flow<List<Account>>

    suspend fun insert(account: Account)

    suspend fun delete(account: Account)

    suspend fun update(account: Account)
}
