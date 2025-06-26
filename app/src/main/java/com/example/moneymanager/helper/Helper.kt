package com.example.moneymanager.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.example.moneymanager.R
import com.example.moneymanager.model.Category
import com.example.moneymanager.model.Transaction
import com.example.moneymanager.ui.addtransaction.AddTransactionActivity
import com.example.moneymanager.ui.addtransaction.CategoryItem
import java.text.NumberFormat
import java.util.*

class Helper {
    companion object {
        fun openTransactionDetail(context: Context, transaction: Transaction) {
            val intent = Intent(context, AddTransactionActivity::class.java).apply {
                putExtra("transaction", transaction)
            }
            context.startActivity(intent)
            if (context is Activity) {
                context.overridePendingTransition(R.anim.slide_in_right, R.anim.no_animation)
            }
        }

        fun formatCurrency(amount: Double): String {
            val format = NumberFormat.getNumberInstance(Locale("vi", "VN"))
            return format.format(amount) + "đ"
        }

        fun buildCategoryTree(categories: List<Category>): List<CategoryItem> {
            val parentItems = mutableListOf<CategoryItem>()

            categories.filter { it.parentId == null }.forEach { parent ->
                val children = categories.filter { it.parentId == parent.id }
                    .map { child ->
                        CategoryItem(
                            id = child.id,
                            emoji = child.emoji,
                            name = child.name,
                            isParent = false,
                            parentName = parent.name,
                            parentEmoji = parent.emoji
                        )
                    }

                val parentItem = CategoryItem(
                    id = parent.id,
                    emoji = parent.emoji,
                    name = parent.name,
                    isParent = true,
                    children = children
                )

                parentItems.add(parentItem)
            }

            return parentItems
        }
    }
}