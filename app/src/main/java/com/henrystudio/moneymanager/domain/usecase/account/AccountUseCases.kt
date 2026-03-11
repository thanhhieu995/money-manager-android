package com.henrystudio.moneymanager.domain.usecase.account
data class AccountUseCases(
    val addAccountUseCase: AddAccountUseCase,
    val deleteAccountUseCase: DeleteAccountUseCase,
    val getAccountsUseCase: GetAccountsUseCase,
    val updateAccountUseCase: UpdateAccountUseCase,
    )
