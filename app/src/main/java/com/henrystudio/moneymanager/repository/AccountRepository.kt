package com.henrystudio.moneymanager.repository

import androidx.lifecycle.LiveData
import com.henrystudio.moneymanager.model.Account
import com.henrystudio.moneymanager.model.AccountDao

class AccountRepository(private val dao: AccountDao) {

    fun getAllAccount() : LiveData<List<Account>> {
        return dao.getAll()
    }

    suspend fun insert(account: Account) {
        return dao.insert(account)
    }

    suspend fun delete(account: Account) {
        return dao.delete(account)
    }

    suspend fun update(account: Account) {
        return dao.update(account)
    }
}