package com.example.moneymanager.ui.addtransaction

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.widget.Toolbar
import com.example.moneymanager.R
import com.example.moneymanager.model.*
import com.example.moneymanager.ui.bookmark.BookmarkActivity

class AddTransactionActivity : AppCompatActivity() {
    private var isIncome: Boolean = false
    private var transactions: List<Transaction> = listOf()
    private lateinit var titleTransaction: TextView
    private lateinit var iconBookmark: ImageView
    private var shouldAnimateExit = false
    private var isEditMode = false

    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var addTransactionFragment: AddTransactionFragment

    @SuppressLint("MissingInflatedId", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        init()

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Tạo tiêu đề tuỳ chỉnh và thêm vào toolbar
        titleTransaction = TextView(this).apply {
            text = "Expense" // Default
            textSize = 18f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }

        val params = Toolbar.LayoutParams(
            Toolbar.LayoutParams.WRAP_CONTENT,
            Toolbar.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        toolbar.addView(titleTransaction, params)

        val bookmarkIcon = ImageView(this).apply {
            setImageResource(R.drawable.ic_baseline_star_border_24) // icon sao
            setPadding(16, 0, 16, 0)
            layoutParams = Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.END
            }
        }

        toolbar.addView(bookmarkIcon)

        bookmarkIcon.setOnClickListener {
            val intent = Intent(this, BookmarkActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_bottom, R.anim.no_animation)
        }

        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24)
        toolbar.setNavigationOnClickListener {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            } else {
                finish()
            }
            overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right)
        }

        val transaction = intent.getSerializableExtra("transaction") as? Transaction

        if (transaction != null) {
            titleTransaction.text = if (transaction.isIncome) "Income" else "Expense"
        }

        if (savedInstanceState == null) {
            val fragment = AddTransactionFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("transaction", transaction)
                }
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_add_transaction, fragment)
                .commit()
        }
    }

    override fun onPause() {
        super.onPause()
        if (shouldAnimateExit) {
            overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right)
            shouldAnimateExit = false
        }
    }

    private fun init() {
        toolbar = findViewById(R.id.add_transaction_toolbar)
        addTransactionFragment = AddTransactionFragment()
    }

    fun updateTransactionTitle(title: String) {
        titleTransaction.text = title
    }
}