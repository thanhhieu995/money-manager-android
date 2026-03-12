package com.henrystudio.moneymanager.di

import com.henrystudio.moneymanager.domain.repository.AccountRepository
import com.henrystudio.moneymanager.domain.repository.CategoryRepository
import com.henrystudio.moneymanager.domain.repository.TransactionRepository
import com.henrystudio.moneymanager.domain.usecase.account.AccountUseCases
import com.henrystudio.moneymanager.domain.usecase.account.AddAccountUseCase
import com.henrystudio.moneymanager.domain.usecase.account.DeleteAccountUseCase
import com.henrystudio.moneymanager.domain.usecase.account.GetAccountsUseCase
import com.henrystudio.moneymanager.domain.usecase.account.UpdateAccountUseCase
import com.henrystudio.moneymanager.domain.usecase.category.*
import com.henrystudio.moneymanager.domain.usecase.transaction.AddTransactionUseCase
import com.henrystudio.moneymanager.domain.usecase.transaction.DeleteAllTransactionsUseCase
import com.henrystudio.moneymanager.domain.usecase.transaction.DeleteTransactionUseCase
import com.henrystudio.moneymanager.domain.usecase.transaction.GetBookmarkedTransactionsUseCase
import com.henrystudio.moneymanager.domain.usecase.transaction.GetTransactionsGroupUseCase
import com.henrystudio.moneymanager.domain.usecase.transaction.GetTransactionsUseCase
import com.henrystudio.moneymanager.domain.usecase.transaction.TransactionUseCases
import com.henrystudio.moneymanager.domain.usecase.transaction.UpdateTransactionsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    fun provideCategoryUseCases(
        repository: CategoryRepository
    ): CategoryUseCases {
        return CategoryUseCases(
            insertCategory = InsertCategoryUseCase(repository),
            updateCategory = UpdateCategoryUseCase(repository),
            deleteCategory = DeleteCategoryUseCase(repository),
            deleteCategoryById = DeleteCategoryByIdUseCase(repository),
            getParentCategories = GetParentCategoriesUseCase(repository),
            getChildCategories = GetChildCategoriesUseCase(repository),
            getAllCategories = GetAllCategoriesUseCase(repository),
            getCategoriesByType = GetCategoriesByTypeUseCase(repository)
        )
    }

    @Provides
    fun provideTransactionUseCases(
        repository: TransactionRepository
    ): TransactionUseCases {
        return TransactionUseCases(
            addTransactionUseCase = AddTransactionUseCase(repository),
            deleteTransactionUseCase = DeleteTransactionUseCase(repository),
            updateTransactionsUseCase = UpdateTransactionsUseCase(repository),
            getTransactionsUseCase = GetTransactionsUseCase(repository),
            getTransactionsGroupUseCase = GetTransactionsGroupUseCase(repository),
            deleteAllTransactionsUseCase = DeleteAllTransactionsUseCase(repository),
            getBookmarkedTransactionsUseCase = GetBookmarkedTransactionsUseCase(repository)
        )
    }

    @Provides
    fun provideAccountUseCases(
        repository: AccountRepository
    ): AccountUseCases {
        return AccountUseCases(
            addAccountUseCase = AddAccountUseCase(repository),
            deleteAccountUseCase = DeleteAccountUseCase(repository),
            updateAccountUseCase = UpdateAccountUseCase(repository),
            getAccountsUseCase = GetAccountsUseCase(repository)
        )
    }
}