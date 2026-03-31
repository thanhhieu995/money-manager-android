package com.henrystudio.moneymanager.data.local

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.henrystudio.moneymanager.data.model.Account
import com.henrystudio.moneymanager.data.model.Category
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

        // 1. Insert parent trước
        val parents = listOf(
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
            Category(emoji = "📦", name = "Other", type = typeExpense),

            Category(emoji = "💸", name = "Allowance", type = typeIncome),
            Category(emoji = "💼", name = "Salary", type = typeIncome),
            Category(emoji = "🎁", name = "Bonus", type = typeIncome),
            Category(emoji = "📦", name = "Other", type = typeIncome),
        )

        db.categoryDao().insertAll(parents)

        // 2. Lấy lại toàn bộ để map ID
        val all = db.categoryDao().getAllOnce()

        fun idOf(name: String) = all.first { it.name == name }.id

        // 3. Insert child
        val children = listOf(

            // 🍔 Food
            Category(emoji = "🍳", name = "Breakfast", type = typeExpense, parentId = idOf("Food")),
            Category(emoji = "🍱", name = "Lunch", type = typeExpense, parentId = idOf("Food")),
            Category(emoji = "🍽️", name = "Dinner", type = typeExpense, parentId = idOf("Food")),
            Category(emoji = "☕", name = "Cafe", type = typeExpense, parentId = idOf("Food")),
            Category(emoji = "🥤", name = "Drinks", type = typeExpense, parentId = idOf("Food")),
            Category(emoji = "🍔", name = "Fast Food", type = typeExpense, parentId = idOf("Food")),

            // 🎉 Social Life
            Category(emoji = "🍻", name = "Party", type = typeExpense, parentId = idOf("Social Life")),
            Category(emoji = "🎬", name = "Cinema", type = typeExpense, parentId = idOf("Social Life")),
            Category(emoji = "🎤", name = "Karaoke", type = typeExpense, parentId = idOf("Social Life")),
            Category(emoji = "☕", name = "Hangout", type = typeExpense, parentId = idOf("Social Life")),

            // 🚗 Transport
            Category(emoji = "🚕", name = "Taxi", type = typeExpense, parentId = idOf("Transport")),
            Category(emoji = "🚌", name = "Bus", type = typeExpense, parentId = idOf("Transport")),
            Category(emoji = "🚇", name = "Metro", type = typeExpense, parentId = idOf("Transport")),
            Category(emoji = "⛽", name = "Fuel", type = typeExpense, parentId = idOf("Transport")),
            Category(emoji = "🛵", name = "Motorbike", type = typeExpense, parentId = idOf("Transport")),

            // 🏠 Household
            Category(emoji = "💡", name = "Electricity", type = typeExpense, parentId = idOf("Household")),
            Category(emoji = "🚿", name = "Water", type = typeExpense, parentId = idOf("Household")),
            Category(emoji = "📶", name = "Internet", type = typeExpense, parentId = idOf("Household")),
            Category(emoji = "🧹", name = "Cleaning", type = typeExpense, parentId = idOf("Household")),
            Category(emoji = "🛠️", name = "Repair", type = typeExpense, parentId = idOf("Household")),

            // 💄 Beauty
            Category(emoji = "💇‍♀️", name = "Haircut", type = typeExpense, parentId = idOf("Beauty")),
            Category(emoji = "💅", name = "Nails", type = typeExpense, parentId = idOf("Beauty")),
            Category(emoji = "🧴", name = "Skincare", type = typeExpense, parentId = idOf("Beauty")),
            Category(emoji = "💄", name = "Cosmetics", type = typeExpense, parentId = idOf("Beauty")),

            // 📚 Education
            Category(emoji = "📖", name = "Books", type = typeExpense, parentId = idOf("Education")),
            Category(emoji = "🎓", name = "Course", type = typeExpense, parentId = idOf("Education")),
            Category(emoji = "🧑‍🏫", name = "Tuition", type = typeExpense, parentId = idOf("Education")),

            // 🐶 Pets
            Category(emoji = "🐕", name = "Pet Food", type = typeExpense, parentId = idOf("Pets")),
            Category(emoji = "🦴", name = "Pet Care", type = typeExpense, parentId = idOf("Pets")),

            // 🎁 Gift
            Category(emoji = "🎂", name = "Birthday", type = typeExpense, parentId = idOf("Gift")),
            Category(emoji = "🎄", name = "Holiday", type = typeExpense, parentId = idOf("Gift")),

            // 🏋️ Sport
            Category(emoji = "🏋️‍♂️", name = "Gym", type = typeExpense, parentId = idOf("Sport")),
            Category(emoji = "⚽", name = "Football", type = typeExpense, parentId = idOf("Sport")),

            // 💻 Investment
            Category(emoji = "📈", name = "Stocks", type = typeExpense, parentId = idOf("Investment")),
            Category(emoji = "₿", name = "Crypto", type = typeExpense, parentId = idOf("Investment")),
            Category(emoji = "🏦", name = "Saving", type = typeExpense, parentId = idOf("Investment")),

            // 🚲 Bicycle
            Category(emoji = "🔧", name = "Maintenance", type = typeExpense, parentId = idOf("Bicycle")),
            Category(emoji = "🛞", name = "Parts", type = typeExpense, parentId = idOf("Bicycle"))
        )

        db.categoryDao().insertAll(children)

        // Accounts
        val accounts = listOf(
            Account(name = "Cash"),
            Account(name = "Bank Account"),
            Account(name = "Credit Card"),
            Account(name = "E-Wallet"),
            Account(name = "Crypto")
        )

        db.accountDao().insertAll(accounts)
    }
}