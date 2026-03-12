package com.henrystudio.moneymanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henrystudio.moneymanager.data.model.Account
import com.henrystudio.moneymanager.domain.usecase.account.AccountUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor (private val accountUseCases: AccountUseCases) : ViewModel() {
    fun getAllAccount(): Flow<List<Account>> {
        return accountUseCases.getAccountsUseCase()
    }

    fun insert(account: Account) = viewModelScope.launch {
         accountUseCases.addAccountUseCase(account)
    }

    fun delete(account: Account) = viewModelScope.launch {
        accountUseCases.deleteAccountUseCase(account)
    }

    fun update(account: Account) = viewModelScope.launch{
        accountUseCases.updateAccountUseCase(account)
    }
}