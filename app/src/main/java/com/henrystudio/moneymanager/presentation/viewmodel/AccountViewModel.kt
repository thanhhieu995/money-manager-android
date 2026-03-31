package com.henrystudio.moneymanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henrystudio.moneymanager.data.model.Account
import com.henrystudio.moneymanager.domain.usecase.account.AccountUseCases
import com.henrystudio.moneymanager.presentation.addtransaction.model.UiState
import com.henrystudio.moneymanager.presentation.addtransaction.model.toUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor (private val accountUseCases: AccountUseCases) : ViewModel() {
    val accountState: StateFlow<UiState<List<Account>>> =
        accountUseCases.getAccountsUseCase()
            .toUiState()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UiState.Loading
            )

    val allAccounts: StateFlow<List<Account>> = accountUseCases.getAccountsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
