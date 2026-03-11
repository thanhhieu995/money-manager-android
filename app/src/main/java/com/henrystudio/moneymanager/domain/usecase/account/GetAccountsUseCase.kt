package com.henrystudio.moneymanager.domain.usecase.account

import com.henrystudio.moneymanager.data.model.Account
import com.henrystudio.moneymanager.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow

class GetAccountsUseCase(
    private val repository: AccountRepository
) {
    operator fun invoke(): Flow<List<Account>> {
        return repository.getAllAccount()
    }
}