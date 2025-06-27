package com.example.moneymanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneymanager.model.Account
import com.example.moneymanager.model.AccountDao
import com.example.moneymanager.repository.AccountRepository
import kotlinx.coroutines.launch

class AccountViewModel(private val dao: AccountDao) : ViewModel() {
    private val repository = AccountRepository(dao)

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