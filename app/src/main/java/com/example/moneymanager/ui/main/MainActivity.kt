package com.example.moneymanager.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.moneymanager.R
import com.example.moneymanager.model.AppDatabase
import com.example.moneymanager.ui.addtransaction.AddTransactionActivity
import com.example.moneymanager.viewmodel.TransactionViewModel
import com.example.moneymanager.viewmodel.TransactionViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: TransactionViewModel
//    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var transactionGroupAdapter: TransactionGroupAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dao = AppDatabase.getDatabase(application).transactionDao()
        val factory = TransactionViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]
//        transactionAdapter = TransactionAdapter()
        transactionGroupAdapter = TransactionGroupAdapter()

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Daily"
                1 -> tab.text = "Calendar"
                2 -> tab.text = "Monthly"
            }
        }.attach()

//        viewModel.allTransactions.observe(this) {list ->
//            transactionAdapter.submitList(list)
//        }

        viewModel.groupedTransactions.observe(this) {list ->
            transactionGroupAdapter.submitList(list)
        }

        val btnAdd = findViewById<FloatingActionButton>(R.id.btn_add)
        btnAdd.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }
    }
}