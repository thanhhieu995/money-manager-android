package com.henrystudio.moneymanager.data.repository

import androidx.lifecycle.LiveData
import com.henrystudio.moneymanager.data.model.Account
import com.henrystudio.moneymanager.data.local.AccountDao
import com.henrystudio.moneymanager.domain.repository.AccountRepository

class AccountRepositoryImpl(private val dao: AccountDao): AccountRepository {

    override fun getAllAccount() : LiveData<List<Account>> {
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