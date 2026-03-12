package com.henrystudio.moneymanager.di

import com.henrystudio.moneymanager.data.local.AccountDao
import com.henrystudio.moneymanager.data.local.CategoryDao
import com.henrystudio.moneymanager.data.local.TransactionDao
import com.henrystudio.moneymanager.data.repository.AccountRepositoryImpl
import com.henrystudio.moneymanager.data.repository.CategoryRepositoryImpl
import com.henrystudio.moneymanager.data.repository.TransactionRepositoryImpl
import com.henrystudio.moneymanager.domain.repository.AccountRepository
import com.henrystudio.moneymanager.domain.repository.CategoryRepository
import com.henrystudio.moneymanager.domain.repository.TransactionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    fun provideCategoryRepository(
        dao: CategoryDao
    ): CategoryRepository {
        return CategoryRepositoryImpl(dao)
    }

    @Provides
    fun provideTransactionRepository(dao: TransactionDao) : TransactionRepository {
        return TransactionRepositoryImpl(dao)
    }

    @Provides
    fun provideAccountRepository(dao: AccountDao): AccountRepository {
        return AccountRepositoryImpl(dao)
    }

}