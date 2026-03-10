package com.henrystudio.moneymanager.core.database

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.henrystudio.moneymanager.features.transaction.data.local.AccountDao
import com.henrystudio.moneymanager.features.transaction.data.local.CategoryDao
import com.henrystudio.moneymanager.features.transaction.data.local.Transaction
import com.henrystudio.moneymanager.features.transaction.data.local.TransactionDao
import com.henrystudio.moneymanager.model.*

@Database(entities = [Transaction::class, Category::class, Account::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getDatabase(context: Application): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "transactions"
                ).build().also { instance = it }
            }
        }
    }
}

