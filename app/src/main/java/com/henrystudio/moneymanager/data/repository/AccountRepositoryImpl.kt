package com.henrystudio.moneymanager.data.repository

import com.henrystudio.moneymanager.data.model.Account
import com.henrystudio.moneymanager.data.local.AccountDao
import com.henrystudio.moneymanager.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow

class AccountRepositoryImpl(private val dao: AccountDao): AccountRepository {

    override fun getAllAccount() : Flow<List<Account>> {
        return dao.getAll()
    }

    override suspend fun insert(account: Account) {
        return dao.insert(account)
    }

    override suspend fun delete(account: Account) {
        return dao.delete(account)
    }

    override suspend fun update(account: Account) {
        return dao.update(account)
    }
}