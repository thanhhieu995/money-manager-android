package com.henrystudio.moneymanager.ui.main

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.application.BaseActivity
import com.henrystudio.moneymanager.model.*
import com.henrystudio.moneymanager.ui.bottomNavigation.dailyNavigate.DailyNavigateFragment
import com.henrystudio.moneymanager.ui.bottomNavigation.statistic.StatisticViewPagerFragment
import com.henrystudio.moneymanager.ui.setting.LanguagePref
import com.henrystudio.moneymanager.ui.setting.SettingFragment
import com.henrystudio.moneymanager.viewmodel.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class MainActivity : BaseActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var viewModel: TransactionViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    @RequiresApi(Build.VERSION_CODES.O)
    private val formatterMonth = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

    val transactionDao by lazy {
        AppDatabase.getDatabase(application).transactionDao()
    }

    val categoryDao by lazy {
        AppDatabase.getDatabase(application).categoryDao()
    }

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val lastTab = prefs.getInt("last_selected_tab", R.id.nav_daily)

        // Lấy ngôn ngữ đã lưu
        val lang = LanguagePref.getLanguage(this)
        if (lang != null) {
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(lang)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }

        val dao = AppDatabase.getDatabase(application).transactionDao()
        val factory = TransactionViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]
        val categoryDao = AppDatabase.getDatabase(application).categoryDao()
        val categoryFactory = CategoryViewModelFactory(categoryDao)
        categoryViewModel = ViewModelProvider(this, categoryFactory)[CategoryViewModel::class.java]
        init()
        defaultCategory()
        defaultAccount()
        defaultTransactions()

        viewModel.currentFilterDate.observe(this) { month ->
            // Lấy locale hiện tại trong app (theo AppCompatDelegate)
            val currentLocales = AppCompatDelegate.getApplicationLocales()
            val currentLocale: Locale = if (!currentLocales.isEmpty) {
                currentLocales[0]!!
            } else {
                Locale.getDefault() // fallback
            }

            val formatterMonth = DateTimeFormatter.ofPattern("LLLL yyyy", currentLocale)
            bottomNav.menu.findItem(R.id.nav_daily).title = month.format(formatterMonth)
        }

        bottomNav.setOnItemSelectedListener { item ->
            // lưu lại tab mỗi khi chọn
            prefs.edit().putInt("last_selected_tab", item.itemId).apply()
            when(item.itemId) {
                R.id.nav_daily -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_fragment_container, DailyNavigateFragment())
                        .commit()
                    true
                }
                R.id.nav_stats -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_fragment_container, StatisticViewPagerFragment())
                        .commit()
                    true
                }
                R.id.nav_more -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_fragment_container, SettingFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
        // 🔑 Nếu app mới mở lại sau khi kill → vào daily
        //     Nếu resume bình thường → mở lại tab cuối
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_daily
        } else {
            bottomNav.selectedItemId = lastTab
        }
    }

    private fun init() {
        bottomNav = findViewById(R.id.main_bottomBar)
    }

    private fun defaultCategory() {
        val daoCategory = AppDatabase.getDatabase(application).categoryDao()
        val factoryCategory = CategoryViewModelFactory(daoCategory)
        val categoryViewModel = ViewModelProvider(this, factoryCategory)[CategoryViewModel::class.java]
        val typeIncome = CategoryType.INCOME
        val typeExpense = CategoryType.EXPENSE
        categoryViewModel.getAll().observe(this) { listCategory ->
            if (listCategory.isEmpty()) {
                val defaultCategories = listOf(
                    // expense
                    Category(emoji = " \uD83E\uDD57", name = "Food", type = typeExpense),
                    Category(emoji = "🚗", name = "Transport", type = typeExpense),
                    Category(emoji = "🏠", name = "Household", type = typeExpense),
                    Category(emoji = "🐶", name = "Pets", type = typeExpense),
                    Category(emoji = "🎁", name = "Gift", type = typeExpense),
                    Category(emoji = "📚", name = "Education", type = typeExpense),
                    Category(emoji = "🏋️‍♂️", name = "Sport", type = typeExpense),
                    Category(emoji = "💄", name = "Beauty", type = typeExpense),
                    Category(emoji = "🧘‍♂️", name = "Health", type = typeExpense),
                    Category(emoji = "💻", name = "Investment", type = typeExpense),
                    Category(emoji = "🎨", name = "Culture", type = typeExpense),
                    Category(emoji = "🚲", name = "Bicycle", type = typeExpense),
                    Category(emoji = "", name = "Breakfast", type = typeExpense, parentId = 1),
                    Category(emoji = "", name = "Lunch", type = typeExpense, parentId = 1),
                    Category(emoji = "", name = "Dinner", type = typeExpense, parentId = 1),
                    // income
                    Category(emoji = "💸", name = "Allowance", type = typeIncome),
                    Category(emoji = "💼", name = "Salary", type = typeIncome),
                    Category(emoji = "🎁", name = "Bonus", type = typeIncome),
                )
                defaultCategories.forEach { categoryViewModel.insert(it) }
            }
        }
    }

    private fun defaultAccount() {
        val daoAccount = AppDatabase.getDatabase(application).accountDao()
        val factoryAccount = AccountViewModelFactory(daoAccount)
        val accountViewModel = ViewModelProvider(this, factoryAccount)[AccountViewModel::class.java]
        accountViewModel.getAllAccount().observe(this) { accounts ->
            if (accounts.isEmpty()) {
                val defaultAccounts = listOf(
                    Account(name = "Cash"),
                    Account(name = "Bank Account"),
                    Account(name = "Credit Card"),
                    Account(name = "E-Wallet"),
                    Account(name = "Crypto")
                )
                defaultAccounts.forEach { accountViewModel.insert(it) }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun defaultTransactions() {
        val list = generateDefaultTransactions()
        viewModel.allTransactions.observe(this) {transactions ->
            if (transactions.isEmpty()) {
                list.forEach {
                    viewModel.insert(it)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateDefaultTransactions(
        monthsBack: Int = 6,
        locale: Locale = Locale.ENGLISH
    ): List<Transaction> {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yy (EEE)", locale)
        val today = LocalDate.now()
        val transactions = mutableListOf<Transaction>()

        val sampleExpenses = listOf(
            Triple("Food", "Breakfast", 40000.0),
            Triple("Food", "Lunch", 60000.0),
            Triple("Food", "Dinner", 70000.0),
            Triple("Transport", "Bus", 15000.0),
            Triple("Health", "Medicine", 50000.0)
        )

        val sampleIncomes = listOf(
            Triple("Salary", "", 10000000.0),
            Triple("Bonus", "", 2000000.0),
            Triple("Allowance", "", 500000.0)
        )

        repeat(monthsBack) { i ->
            val monthDate = today.minusMonths(i.toLong()).withDayOfMonth(1)
            val daysInMonth = monthDate.lengthOfMonth()

            for (day in 1..daysInMonth) {
                val currentDate = monthDate.withDayOfMonth(day)
                val dateStr = currentDate.format(formatter)

                // Add expenses
                sampleExpenses.forEach { (parent, sub, amount) ->
                    transactions.add(
                        Transaction(
                            title = "$parent $sub",
                            categoryParentName = parent,
                            categorySubName = sub,
                            note = "",
                            account = "Cash",
                            amount = amount,
                            isIncome = false,
                            date = dateStr
                        )
                    )
                }

                // Add incomes on first of the month only
                if (day == 1) {
                    sampleIncomes.forEach { (parent, _, amount) ->
                        transactions.add(
                            Transaction(
                                title = parent,
                                categoryParentName = parent,
                                categorySubName = "",
                                note = "",
                                account = "Bank",
                                amount = amount,
                                isIncome = true,
                                date = dateStr
                            )
                        )
                    }
                }
            }

        }
        return transactions
    }
}