package com.henrystudio.moneymanager.domain.usecase.transaction

import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetTransactionsUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<List<Transaction>> {
        return repository.getAllTransactions()
    }
}