package com.henrystudio.moneymanager.ui.main

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.core.database.AppDatabase
import com.henrystudio.moneymanager.features.transaction.data.local.Transaction
import com.henrystudio.moneymanager.model.*
import com.henrystudio.moneymanager.repository.TransactionRepository
import com.henrystudio.moneymanager.ui.bottomNavigation.dailyNavigate.DailyNavigateFragment
import com.henrystudio.moneymanager.ui.bottomNavigation.statistic.StatisticViewPagerFragment
import com.henrystudio.moneymanager.ui.setting.SettingFragment
import com.henrystudio.moneymanager.viewmodel.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {

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

    private lateinit var adView: AdView

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Khởi tạo SDK AdMob (nên gọi 1 lần trong App)
        MobileAds.initialize(this) {}

        adView = findViewById(R.id.adView)
//        val adUnitId = if (BuildConfig.DEBUG) {
//            // Dùng ID test khi chạy debug
//            "ca-app-pub-3940256099942544/6300978111"
//        } else {
//            // Dùng ID thật khi release
//            "ca-app-pub-8536795401427760/2052264213"
//        }
//        adView.adUnitId = adUnitId
//        adView.setAdSize(AdSize.BANNER) // 👈 bắt buộc khi set qua code
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val lastTab = prefs.getInt("last_selected_tab", R.id.nav_daily)

        val database = AppDatabase.getDatabase(application)
        val transactionRepository = TransactionRepository(database.transactionDao())
        val transactionFactory = TransactionViewModelFactory(transactionRepository)
        viewModel = ViewModelProvider(this, transactionFactory)[TransactionViewModel :: class.java]

        val categoryDao = AppDatabase.getDatabase(application).categoryDao()
        val categoryFactory = CategoryViewModelFactory(categoryDao)
        categoryViewModel = ViewModelProvider(this, categoryFactory)[CategoryViewModel::class.java]
        init()
        defaultCategory()
        defaultAccount()
//        defaultTransactions()

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
                    Category(emoji = "", name = "Breakfast", type = typeExpense, parentId = 1),
                    Category(emoji = "", name = "Lunch", type = typeExpense, parentId = 1),
                    Category(emoji = "", name = "Dinner", type = typeExpense, parentId = 1),
                    Category(emoji = "", name = "Friend", type = typeExpense, parentId = 2),
                    Category(emoji = "", name = "Fellowship", type = typeExpense, parentId = 2),
                    Category(emoji = "", name = "Alumni", type = typeExpense, parentId = 2),
                    Category(emoji = "", name = "Dues", type = typeExpense, parentId = 2),
                    Category(emoji = "", name = "Beer", type = typeExpense, parentId = 2),
                    Category(emoji = "", name = "Bus", type = typeExpense, parentId = 3),
                    Category(emoji = "", name = "Subway", type = typeExpense, parentId = 3),
                    Category(emoji = "", name = "Taxi", type = typeExpense, parentId = 3),
                    Category(emoji = "", name = "Car", type = typeExpense, parentId = 3),
                    Category(emoji = "", name = "Books", type = typeExpense, parentId = 4),
                    Category(emoji = "", name = "Movie", type = typeExpense, parentId = 4),
                    Category(emoji = "", name = "Music", type = typeExpense, parentId = 4),
                    Category(emoji = "", name = "Apps", type = typeExpense, parentId = 4),
                    Category(emoji = "", name = "Appliances", type = typeExpense, parentId = 5),
                    Category(emoji = "", name = "Furniture", type = typeExpense, parentId = 5),
                    Category(emoji = "", name = "Kitchen", type = typeExpense, parentId = 5),
                    Category(emoji = "", name = "Toiletries", type = typeExpense, parentId = 5),
                    Category(emoji = "", name = "Chandlery", type = typeExpense, parentId = 5),
                    Category(emoji = "", name = "Clothing", type = typeExpense, parentId = 6),
                    Category(emoji = "", name = "Fashion", type = typeExpense, parentId = 6),
                    Category(emoji = "", name = "Shoes", type = typeExpense, parentId = 6),
                    Category(emoji = "", name = "Laundry", type = typeExpense, parentId = 6),
                    Category(emoji = "", name = "Underwear", type = typeExpense, parentId = 6),
                    Category(emoji = "", name = "Cosmetics", type = typeExpense, parentId = 7),
                    Category(emoji = "", name = "Makeup", type = typeExpense, parentId = 7),
                    Category(emoji = "", name = "Accessories", type = typeExpense, parentId = 7),
                    Category(emoji = "", name = "Beauty", type = typeExpense, parentId = 7),
                    Category(emoji = "", name = "Health", type = typeExpense, parentId = 8),
                    Category(emoji = "", name = "Yoga", type = typeExpense, parentId = 8),
                    Category(emoji = "", name = "Hospital", type = typeExpense, parentId = 8),
                    Category(emoji = "", name = "Medicine", type = typeExpense, parentId = 8),
                    Category(emoji = "", name = "Schooling", type = typeExpense, parentId = 9),
                    Category(emoji = "", name = "Textbooks", type = typeExpense, parentId = 9),
                    Category(emoji = "", name = "School supplies", type = typeExpense, parentId = 9),
                    Category(emoji = "", name = "Academy", type = typeExpense, parentId = 9),
                    // income
                    Category(emoji = "💸", name = "Allowance", type = typeIncome),
                    Category(emoji = "💼", name = "Salary", type = typeIncome),
                    Category(emoji = "🎁", name = "Bonus", type = typeIncome),
                    Category(emoji = "", name = "Other", type = typeIncome),
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