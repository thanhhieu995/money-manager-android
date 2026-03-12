package com.henrystudio.moneymanager.di

import android.app.Application
import android.content.Context
import com.henrystudio.moneymanager.data.local.AccountDao
import com.henrystudio.moneymanager.data.local.AppDatabase
import com.henrystudio.moneymanager.data.local.CategoryDao
import com.henrystudio.moneymanager.data.local.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao {
        return db.categoryDao()
    }

    @Provides
    fun provideTransactionDao(db: AppDatabase): TransactionDao {
        return db.transactionDao()
    }

    @Provides
    fun provideAccountDao(db: AppDatabase) : AccountDao {
        return db.accountDao()
    }
}