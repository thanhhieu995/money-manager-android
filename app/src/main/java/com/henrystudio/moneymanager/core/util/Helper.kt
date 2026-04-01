package com.henrystudio.moneymanager.core.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.view.Gravity
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.shape.MaterialShapeDrawable
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.data.model.Category
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.addtransaction.AddTransactionActivity
import com.henrystudio.moneymanager.presentation.addtransaction.model.CategoryItem
import com.henrystudio.moneymanager.presentation.model.CategoryStat
import com.henrystudio.moneymanager.presentation.model.FilterOption
import com.henrystudio.moneymanager.presentation.model.FilterPeriodStatistic
import com.henrystudio.moneymanager.presentation.model.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
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

                val parentItem =
                    CategoryItem(
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

        fun CategoryItem.toCategory(type: TransactionType): Category {
            return Category(
                id = this.id,
                name = this.name,
                emoji = this.emoji,
                type = type,
                parentId = this.parentId
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
                   it.categorySubName.trim() == (category.emoji + category.name).trim()
                }
                val categoryAmount = categoryTransactions.sumOf { it.amount }
                if (categoryAmount > 0) {
                    CategoryStat(
                        name = category.name,
                        amount = categoryAmount.toFloat(),
                        percent = (categoryAmount / totalAmount).toFloat() * 100f,
                        color = colorList[index % colorList.size]
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

        @RequiresApi(Build.VERSION_CODES.O)
        fun getUpdateMonthText(filterOption: FilterOption) : String {
            val appLocale = getAppLocale()
            val formatterMonth: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", appLocale)
            // ✅ Cập nhật text phù hợp
             return when (filterOption.type) {
                FilterPeriodStatistic.Monthly -> filterOption.date.format(formatterMonth)
                FilterPeriodStatistic.Weekly -> {
                    val formatterFirst = DateTimeFormatter.ofPattern("dd/MM", appLocale)
                    val formatterLast = DateTimeFormatter.ofPattern("dd/MM/yyyy", appLocale)
                    // Ép tuần bắt đầu từ thứ Hai
                    val weekFields = WeekFields.of(DayOfWeek.MONDAY, 1)
                    val firstDayOfWeek = filterOption.date.with(weekFields.dayOfWeek(), 1) // Monday
                    val lastDayOfWeek = filterOption.date.with(weekFields.dayOfWeek(), 7) // Sunday
                    "${firstDayOfWeek.format(formatterFirst)} ~ ${lastDayOfWeek.format(formatterLast)}"
                }
                FilterPeriodStatistic.Yearly -> filterOption.date.year.toString()
                FilterPeriodStatistic.List -> "Not code now"
                FilterPeriodStatistic.Trend -> "Not code now"
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun formatDateFromFilterOptionToDateDaily(input: String): String {
            // Parse từ dạng gốc yyyy-MM-dd
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
            val date = LocalDate.parse(input, inputFormatter)

            // Format sang dạng dd/MM/yy (E)
            val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yy (EEE)", Locale.ENGLISH)
            return date.format(outputFormatter)
        }

        fun getAppLocale(): Locale {
            val appLocales = AppCompatDelegate.getApplicationLocales()
            return if (!appLocales.isEmpty) {
                appLocales[0]!!
            } else {
                Locale.getDefault()
            }
        }

        fun showToastWithIcon(context: Context, message: String) {
            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(32, 16, 32, 16)
                gravity = Gravity.CENTER_VERTICAL

                // Icon
                val icon = ImageView(context).apply {
                    setImageResource(R.mipmap.ic_launcher)
                    layoutParams = LinearLayout.LayoutParams(48, 48)
                }
                addView(icon)

                // Text
                val text = TextView(context).apply {
                    this.text = message
                    setTextColor(resolveThemeColor(context, com.google.android.material.R.attr.colorOnSurface))
                    textSize = 16f
                    setPadding(16, 0, 0, 0)
                }
                addView(text)

                // Nền toast follow dark/light
                background = MaterialShapeDrawable().apply {
                    fillColor = ColorStateList.valueOf(resolveThemeColor(context, com.google.android.material.R.attr.colorSurface))
                    setCornerSize(24f)
                    elevation = 6f
                }
            }

            Toast(context).apply {
                duration = Toast.LENGTH_SHORT
                view = layout
                setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
                show()
            }
        }

        // Helper: lấy màu theo theme
        private fun resolveThemeColor(context: Context, attr: Int): Int {
            val typedArray = context.obtainStyledAttributes(intArrayOf(attr))
            val color = typedArray.getColor(0, Color.WHITE)
            typedArray.recycle()
            return color
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun parseStringToLocalDate(date: String): LocalDate {
            val cleanedDate = date.substringBefore(" ")
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yy")
            return LocalDate.parse(cleanedDate, formatter)
        }

        fun dateKeyToCalendar(dateKey: String): Calendar {
            val sdf = java.text.SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            val cal = Calendar.getInstance()
            cal.time = sdf.parse(dateKey)!!
            return cal
        }

        fun getFormattedDateToday(): String {
            val currentDate = Calendar.getInstance().time
            val dateFormat = SimpleDateFormat("dd/MM/yy (EEE)", Helper.getAppLocale())
            return dateFormat.format(currentDate)
        }

        fun formatPickedDate(year: Int, month: Int, day: Int): String {
            val calendar = Calendar.getInstance()
            calendar.set(year, month, day)
            val dateFormat = SimpleDateFormat("dd/MM/yy (EEE)", Helper.getAppLocale())
            return dateFormat.format(calendar.time)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun parseDisplayDateToLocalDate(dateStr: String): LocalDate? {
            return try {
                val inputLocale = Helper.getAppLocale()
                val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yy (EEE)", inputLocale)
                LocalDate.parse(dateStr, inputFormatter)
            } catch (e: Exception) {
                null
            }
        }

        fun EditText.setTextIfDifferent(newText: String) {
            if (text.toString() != newText) {
                setText(newText)
                setSelection(newText.length)
            }
        }
     }
}