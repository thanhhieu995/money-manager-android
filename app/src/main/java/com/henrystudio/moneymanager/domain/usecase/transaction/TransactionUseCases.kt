package com.henrystudio.moneymanager.domain.usecase.transaction

data class TransactionUseCases(
    val addTransactionUseCase: AddTransactionUseCase,
    val deleteTransactionUseCase: DeleteTransactionUseCase,
    val deleteAllTransactionsUseCase: DeleteAllTransactionsUseCase,
    val getTransactionsGroupUseCase: GetTransactionsGroupUseCase,
    val getTransactionsUseCase: GetTransactionsUseCase,
    val getBookmarkedTransactionsUseCase: GetBookmarkedTransactionsUseCase,
    val updateTransactionsUseCase: UpdateTransactionsUseCase,
    val filterTransactionGroupsUseCase: FilterTransactionGroupsUseCase,
    val getStatisticTotalsUseCase: GetStatisticTotalsUseCase
)
