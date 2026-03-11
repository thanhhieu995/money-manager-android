package com.henrystudio.moneymanager.domain.usecase.transaction

import com.henrystudio.moneymanager.data.model.TransactionGroup
import com.henrystudio.moneymanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetTransactionsGroupUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<List<TransactionGroup>> {
        return repository.getGroupedTransactions()
    }
}