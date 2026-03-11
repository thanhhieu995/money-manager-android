package com.henrystudio.moneymanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.henrystudio.moneymanager.data.local.AccountDao

class AccountViewModelFactory(private val dao: AccountDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) : T {
        return AccountViewModel(dao) as T
    }
}