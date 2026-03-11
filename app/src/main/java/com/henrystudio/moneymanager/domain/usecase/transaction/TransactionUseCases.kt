package com.henrystudio.moneymanager.domain.usecase.transaction

data class TransactionUseCases(
    val addTransactionUseCase: AddTransactionUseCase,
    val deleteTransactionUseCase: DeleteTransactionUseCase,
    val getBookmarkedTransactionsUseCase: GetBookmarkedTransactionsUseCase,
    val getTransactionsByDateUseCase: GetTransactionsByDateUseCase,
    val getTransactionsUseCase: GetTransactionsUseCase,
    val updateTransactionsUseCase: UpdateTransactionsUseCase
)
