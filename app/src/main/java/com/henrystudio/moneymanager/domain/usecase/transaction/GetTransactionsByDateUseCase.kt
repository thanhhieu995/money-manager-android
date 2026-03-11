package com.henrystudio.moneymanager.domain.usecase.transaction

import com.henrystudio.moneymanager.data.model.TransactionGroup
import com.henrystudio.moneymanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetTransactionsByDateUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(date: String): Flow<List<TransactionGroup>> {
        return repository.getGroupedTransactions()
    }
}