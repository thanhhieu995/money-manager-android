package com.henrystudio.moneymanager.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.model.Category
import com.henrystudio.moneymanager.model.CategoryStat
import com.henrystudio.moneymanager.model.CategoryType
import com.henrystudio.moneymanager.model.Transaction
import com.henrystudio.moneymanager.ui.addtransaction.AddTransactionActivity
import com.henrystudio.moneymanager.ui.addtransaction.CategoryItem
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
                            parentId = parent.id,
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

        fun CategoryItem.toCategory(type: CategoryType): Category {
            return Category(
                id = this.id,
                name = this.name,
                emoji = this.emoji,
                type = type,
                parentId = null // hoặc truyền thêm nếu cần
            )
        }

        fun convertToCategoryStats(
            categories: List<Category>,
            transactions: List<Transaction>,
            isIncome: Boolean,
            colorList: List<Int> // danh sách màu cho từng category
        ): List<CategoryStat> {
            // Lọc giao dịch theo loại (thu/chi)
            val filteredTransactions = transactions.filter { it.isIncome == isIncome }
            // Tính tổng toàn bộ
            val totalAmount = filteredTransactions.sumOf { it.amount }

            return categories.mapIndexedNotNull { index, category ->
                val categoryTransactions = filteredTransactions.filter {
                   it.categoryParentName.substringBefore("/").trim() == (category.emoji.trim() + " " + category.name.trim())
                }
                val categoryAmount = categoryTransactions.sumOf { it.amount }
                if (categoryAmount > 0) {
                    CategoryStat(
                        name = category.name,
                        amount = categoryAmount.toFloat(),
                        percent = (categoryAmount / totalAmount).toFloat() * 100f,
                        color = colorList.getOrNull(index) ?: Color.GRAY
                    )
                } else {
                    null // Không có giao dịch, bỏ qua
                }
            }
        }

        data class CategoryParts(val parent: String, val sub: String)

        fun splitCategoryName(fullCategory: String): CategoryParts {
            return if ("/" in fullCategory) {
                val parts = fullCategory.split("/")
                CategoryParts(
                    parent = parts[0].trim(),
                    sub = parts.getOrNull(1)?.trim() ?: ""
                )
            } else {
                CategoryParts(
                    parent = fullCategory.trim(),
                    sub = ""
                )
            }
        }
    }
}