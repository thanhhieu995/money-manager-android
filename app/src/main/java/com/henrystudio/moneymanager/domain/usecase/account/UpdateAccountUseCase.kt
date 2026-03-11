package com.henrystudio.moneymanager.domain.usecase.account

import com.henrystudio.moneymanager.data.model.Account
import com.henrystudio.moneymanager.domain.repository.AccountRepository

class UpdateAccountUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(account: Account) {
        repository.update(account)
    }
}