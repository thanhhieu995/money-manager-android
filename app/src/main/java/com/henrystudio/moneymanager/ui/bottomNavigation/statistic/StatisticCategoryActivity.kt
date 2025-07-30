package com.henrystudio.moneymanager.ui.bottomNavigation.statistic

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.model.CategoryType

class StatisticCategoryActivity : AppCompatActivity() {
    private lateinit var btnBack: ImageButton
    private lateinit var title: TextView
    private lateinit var container: FrameLayout
    private lateinit var statisticCategoryFragment: StatisticCategoryFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistic_category)
        init()

        btnBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right)
        }

        val name = intent.getStringExtra("item_click_statistic_category_name")
        val categoryType = intent.getSerializableExtra("item_click_statistic_category_type")
        val bundle =  Bundle().apply {
            putSerializable("item_click_statistic_category_name", name)
            putSerializable("item_click_statistic_category_type", categoryType)
        }
        statisticCategoryFragment.apply {
            arguments = bundle
        }
        supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(R.id.activity_statistic_category_container, statisticCategoryFragment)
            .commit()
    }

    private fun init() {
        btnBack = findViewById(R.id.activity_statistic_category_backButton)
        title = findViewById(R.id.activity_statistic_category_titleCurrent)
        container = findViewById(R.id.activity_statistic_category_container)
        statisticCategoryFragment = StatisticCategoryFragment()
    }
}