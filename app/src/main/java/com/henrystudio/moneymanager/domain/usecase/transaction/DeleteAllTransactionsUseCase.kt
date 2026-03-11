package com.henrystudio.moneymanager.domain.usecase.transaction

import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.domain.repository.TransactionRepository

class DeleteAllTransactionsUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(transactionList: List<Transaction>) {
        repository.deleteAll(transactionList)
    }
}