package com.henrystudio.moneymanager.data.local

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.henrystudio.moneymanager.data.model.Account
import com.henrystudio.moneymanager.data.model.Category
import com.henrystudio.moneymanager.data.model.CategoryType
import com.henrystudio.moneymanager.presentation.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DatabaseCallback(
    private val context: Context
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        CoroutineScope(Dispatchers.IO).launch {

            val database = AppDatabase.getDatabase(context)

            seed(database)
        }
    }

    private suspend fun seed(db: AppDatabase) {
        val typeIncome = TransactionType.INCOME
        val typeExpense = TransactionType.EXPENSE
        val defaultCategories = listOf(
            Category(emoji = "🥗", name = "Food", type = typeExpense),
            Category(emoji = "🎉", name = "Social Life", type = typeExpense),
            Category(emoji = "🚗", name = "Transport", type = typeExpense),
            Category(emoji = "🎨", name = "Culture", type = typeExpense),
            Category(emoji = "🏠", name = "Household", type = typeExpense),
            Category(emoji = "👕", name = "Apparel", type = typeExpense),
            Category(emoji = "💄", name = "Beauty", type = typeExpense),
            Category(emoji = "🧘‍♂️", name = "Health", type = typeExpense),
            Category(emoji = "📚", name = "Education", type = typeExpense),
            Category(emoji = "🐶", name = "Pets", type = typeExpense),
            Category(emoji = "🎁", name = "Gift", type = typeExpense),
            Category(emoji = "🏋️‍♂️", name = "Sport", type = typeExpense),
            Category(emoji = "💻", name = "Investment", type = typeExpense),
            Category(emoji = "🚲", name = "Bicycle", type = typeExpense),
            Category(emoji = "", name = "Other", type = typeExpense),

            Category(emoji = "💸", name = "Allowance", type = typeIncome),
            Category(emoji = "💼", name = "Salary", type = typeIncome),
            Category(emoji = "🎁", name = "Bonus", type = typeIncome),
            Category(emoji = "", name = "Other", type = typeIncome),
        )

        val defaultAccounts = listOf(
            Account(name = "Cash"),
            Account(name = "Bank Account"),
            Account(name = "Credit Card"),
            Account(name = "E-Wallet"),
            Account(name = "Crypto")
        )

        db.categoryDao().insertAll(defaultCategories)
        db.accountDao().insertAll(defaultAccounts)
    }
}