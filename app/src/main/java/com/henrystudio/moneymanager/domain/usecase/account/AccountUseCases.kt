package com.henrystudio.moneymanager.domain.usecase.account

import com.henrystudio.moneymanager.domain.usecase.transaction.AddTransactionUseCase

data class AccountUseCases(
    val addTransactionUseCase: AddTransactionUseCase,
    val deleteAccountUseCase: DeleteAccountUseCase,
    val getAccountsUseCase: GetAccountsUseCase,
    val updateAccountUseCase: UpdateAccountUseCase,
    )
