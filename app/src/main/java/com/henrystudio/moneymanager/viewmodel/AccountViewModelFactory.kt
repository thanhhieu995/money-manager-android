package com.henrystudio.moneymanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.henrystudio.moneymanager.model.AccountDao

class AccountViewModelFactory(private val dao: AccountDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) : T {
        return AccountViewModel(dao) as T
    }
}