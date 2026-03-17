package com.henrystudio.moneymanager.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.data.model.Account
import com.henrystudio.moneymanager.data.model.Category

@Database(entities = [Transaction::class, Category::class, Account::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "transactions"
                )
                    .addCallback(DatabaseCallback(context))
                    .build().also { instance = it }
            }
        }
    }
}