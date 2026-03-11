package com.henrystudio.moneymanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.henrystudio.moneymanager.data.local.AccountDao
import com.henrystudio.moneymanager.domain.usecase.account.AccountUseCases
import com.henrystudio.moneymanager.domain.usecase.transaction.TransactionUseCases

class AccountViewModelFactory(private val accountUseCases: AccountUseCases) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) : T {
        return AccountViewModel(accountUseCases) as T
    }
}