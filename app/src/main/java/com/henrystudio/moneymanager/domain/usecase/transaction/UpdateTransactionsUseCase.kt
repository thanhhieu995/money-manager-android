package com.henrystudio.moneymanager.domain.usecase.transaction

import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.domain.repository.TransactionRepository

class UpdateTransactionsUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction) {
        repository.update(transaction)
    }
}