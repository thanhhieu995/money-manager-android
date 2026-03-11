package com.henrystudio.moneymanager.domain.usecase.account

import com.henrystudio.moneymanager.data.model.Account
import com.henrystudio.moneymanager.domain.repository.AccountRepository

class DeleteAccountUseCase(
    private val repository: AccountRepository
) {
    suspend operator fun invoke(account: Account) {
        repository.delete(account)
    }
}