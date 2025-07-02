package com.example.moneymanager.ui.addtransaction

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.appcompat.widget.Toolbar
import com.example.moneymanager.R
import com.example.moneymanager.model.*
import com.example.moneymanager.ui.bookmark.BookmarkActivity

class AddTransactionActivity : AppCompatActivity() {
    lateinit var titleTransaction: TextView
    lateinit var extraAddText: TextView
    lateinit var extraEditText: TextView
    lateinit var bookmarkIcon: ImageView
    lateinit var addIcon: ImageView
    private var shouldAnimateExit = false

    private lateinit var toolbar: Toolbar
    private var isIncome: Boolean = false

    var currentItemType: ItemType? = null
    var currentCategoryType: CategoryType? = null

    @SuppressLint("MissingInflatedId", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        init()
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        titleTextToolbar()
        extraTextAddToolbar()
        extraTextEditToolbar()
        bookmarkToolbar()
        addIconToolbar()
        bookmarkIcon.setOnClickListener {
            val intent = Intent(this, BookmarkActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_bottom, R.anim.no_animation)
        }

        addIcon.setOnClickListener {
            animateTitleToLeftOfIcon(titleTransaction)
            val fragment = AddItemFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("item_type", currentItemType)
                    putSerializable("category_type", currentCategoryType)
                    putSerializable("source", AddItemSource.FROM_EDIT_ITEM_DIALOG)
                }
            }
            when(currentItemType) {
                ItemType.CATEGORY -> {
                    updateTransactionTitle("Category")
                }
                ItemType.ACCOUNT -> {
                    updateTransactionTitle("Account")
                }
                else -> {}
            }
            addIcon.visibility = View.GONE
            animateExtraTextToCenter(extraAddText)

            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,  // enter
                    R.anim.no_animation,    // exit
                    R.anim.no_animation,    // popEnter (khi quay lại)
                    R.anim.slide_out_right  // popExit (khi quay lại)
                ).replace(R.id.fragment_container_add_transaction, fragment)
                .addToBackStack(null)
                .commit()
        }

        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24)
        toolbar.setNavigationOnClickListener {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
                val currentFragment =
                    supportFragmentManager.findFragmentById(R.id.fragment_container_add_transaction)
                if (currentFragment is EditItemDialogFragment) {
                    animateTitleToCenter(titleTransaction)
                    switchToBookmarkIconWithFade()
                } else {
                    getIsIncome()
                    val title = if (isIncome) "Income" else "Expense"
                    updateTransactionTitle(title)
                    when (currentFragment?.arguments?.getSerializable("source") as? AddItemSource ?: AddItemSource.FROM_ADD_TRANSACTION) {
                        AddItemSource.FROM_ADD_TRANSACTION -> {
                            switchToBookmarkIconWithFade()
                            animateTitleToCenter(titleTransaction)
                        }
                        AddItemSource.FROM_EDIT_ITEM_DIALOG -> {
                            switchToAddIconWithFade()
                        }
                    }
                    animateExtraTextToRight(extraAddText)
                }
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

    private fun getIsIncome() {
        supportFragmentManager.setFragmentResultListener("update_title", this) { _, bundle ->
            isIncome = bundle.getBoolean("is_income", false)
        }
    }

    private fun init() {
        toolbar = findViewById(R.id.add_transaction_toolbar)
    }

    fun updateTransactionTitle(title: String) {
        titleTransaction.text = title
    }

    fun animateTitleToLeftOfIcon(titleTransaction: TextView) {
        // Kích thước icon back
        val iconWidth = toolbar.navigationIcon?.intrinsicWidth ?: 0
        // Padding mặc định icon bên trái
        val iconMargin = (toolbar.contentInsetStartWithNavigation)
        // Tổng khoảng trống cần dịch tiêu đề sang trái
        val targetTranslationX =
            -(toolbar.width / 2f) + iconWidth + iconMargin // 16dp padding tùy chỉnh
        titleTransaction.animate()
            .translationX(targetTranslationX)
            .setDuration(300L)
            .start()
    }

    fun animateTitleToCenter(titleTransaction: TextView) {
        val animator = ObjectAnimator.ofFloat(
            titleTransaction,
            "translationX",
            titleTransaction.translationX,
            0f
        )
        animator.duration = 300
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }

    private fun titleTextToolbar() {
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
    }

    private fun extraTextAddToolbar() {
        extraAddText = TextView(this).apply {
            text = "Add"
            textSize = 18f
            visibility = View.GONE
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }
        val params = Toolbar.LayoutParams(
            Toolbar.LayoutParams.WRAP_CONTENT,
            Toolbar.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.END
        }
        toolbar.addView(extraAddText, params)
    }

    private fun extraTextEditToolbar() {
        extraEditText = TextView(this).apply {
            text = "Edit"
            textSize = 18f
            visibility = View.GONE
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }
        val params = Toolbar.LayoutParams(
            Toolbar.LayoutParams.WRAP_CONTENT,
            Toolbar.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        toolbar.addView(extraEditText, params)
    }

    private fun bookmarkToolbar() {
        // icon bookmark
        bookmarkIcon = ImageView(this).apply {
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
    }

    private fun addIconToolbar() {
        addIcon = ImageView(this).apply {
            setImageResource(R.drawable.ic_baseline_add_24)
            visibility = View.GONE // ẩn ban đầu
            layoutParams = Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.END
                marginEnd = 16
            }
        }
        toolbar.addView(addIcon)
    }

    fun switchToAddIconWithFade() {
        bookmarkIcon.animate().alpha(0f).setDuration(400).withEndAction {
            bookmarkIcon.visibility = View.GONE
            addIcon.alpha = 0f
            addIcon.visibility = View.VISIBLE
            addIcon.animate().alpha(1f).setDuration(400).start()
        }.start()
    }

    fun switchToBookmarkIconWithFade() {
        addIcon.animate().alpha(0f).setDuration(400).withEndAction {
            addIcon.visibility = View.GONE
            bookmarkIcon.alpha = 0f
            bookmarkIcon.visibility = View.VISIBLE
            bookmarkIcon.animate().alpha(1f).setDuration(400).start()
        }.start()
    }

    fun animateExtraTextToCenter(textView: TextView) {
        textView.visibility = View.VISIBLE
        textView.post {
            val toolbarCenterX = toolbar.width / 2f
            val textCenterX = textView.left + textView.width / 2f
            val targetTranslationX = toolbarCenterX - textCenterX

            textView.animate()
                .translationX(targetTranslationX)
                .setDuration(300)
                .start()
        }
    }

    fun animateExtraTextToRight(textView: TextView) {
        textView.visibility = View.VISIBLE
        textView.animate()
            .translationX(0f) // Di chuyển về vị trí ban đầu
            .alpha(0f)        // Làm mờ dần
            .setDuration(300)
            .withEndAction {
                textView.visibility = View.GONE
                textView.alpha = 1f // reset alpha để dùng lại sau
            }
            .start()
    }
}