package com.henrystudio.moneymanager.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henrystudio.moneymanager.data.model.Account
import com.henrystudio.moneymanager.data.local.AccountDao
import com.henrystudio.moneymanager.data.repository.AccountRepositoryImpl
import kotlinx.coroutines.launch

class AccountViewModel(private val dao: AccountDao) : ViewModel() {
    private val repository = AccountRepositoryImpl(dao)

    fun getAllAccount(): LiveData<List<Account>> {
        return repository.getAllAccount()
    }

    fun insert(account: Account) = viewModelScope.launch {
        repository.insert(account)
    }

    fun delete(account: Account) = viewModelScope.launch {
        repository.delete(account)
    }

    fun update(account: Account) = viewModelScope.launch{
        repository.update(account)
    }
}