package com.henrystudio.moneymanager.domain.usecase.transaction

import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetBookmarkedTransactionsUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke() : Flow<List<Transaction>> {
        return repository.getBookmarkedTransactions()
    }
}