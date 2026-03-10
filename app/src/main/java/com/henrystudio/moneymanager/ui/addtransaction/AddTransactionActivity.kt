package com.henrystudio.moneymanager.ui.addtransaction

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.core.database.AppDatabase
import com.henrystudio.moneymanager.features.transaction.data.local.Transaction
import com.henrystudio.moneymanager.model.*
import com.henrystudio.moneymanager.repository.TransactionRepository
import com.henrystudio.moneymanager.ui.bookmark.BookmarkActivity
import com.henrystudio.moneymanager.viewmodel.TransactionViewModel
import com.henrystudio.moneymanager.viewmodel.TransactionViewModelFactory

class AddTransactionActivity : AppCompatActivity() {
    lateinit var titleCurrent: TextView
    lateinit var titleIncoming: TextView
    lateinit var bookmarkIcon: ImageView
    lateinit var addIcon: ImageView
    lateinit var iconBack: ImageView
    private var shouldAnimateExit = false

    private lateinit var toolbar: com.google.android.material.appbar.MaterialToolbar
    private var isIncome: Boolean = false

    var currentItemType: ItemType? = null
    var currentCategoryType: CategoryType? = null
    var selectedCategoryItemForAdd: CategoryItem? = null
    var selectedAccountItemForAdd: Account? = null

    private var currentFragment: Fragment?= null

    val titleStack = ArrayDeque<String>()

    private lateinit var viewModel: TransactionViewModel
    val transactionDao by lazy {
        AppDatabase.getDatabase(application).transactionDao()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)
        init()
        onBackPressedDispatcher.addCallback(this) {
            finish()
            overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right)
        }
        val database = AppDatabase.getDatabase(application)

        val transactionRepository = TransactionRepository(database.transactionDao())
        val transactionFactory = TransactionViewModelFactory(transactionRepository)

        viewModel = ViewModelProvider(this, transactionFactory)[TransactionViewModel::class.java]

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        bookmarkIcon.setOnClickListener {
            val intent = Intent(this, BookmarkActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_bottom, R.anim.no_animation)
        }

        addIcon.setOnClickListener {
            titleStack.addLast(titleCurrent.text.toString()) // Thêm vào đầu stack

            animateTitleToLeftOfIcon(titleCurrent)
            currentFragment =
                supportFragmentManager.findFragmentById(R.id.fragment_container_add_transaction)
            // source truoc khi chuyen
            val source = currentFragment?.arguments?.getSerializable("source") as? AddItemSource
            val fragment = AddItemFragment()
            when (currentFragment) {
                is EditItemDialogFragment -> {
                    fragment.apply {
                        arguments = Bundle().apply {
                            putSerializable("item_type", currentItemType)
                            putSerializable("category_type", currentCategoryType)
                            putSerializable("source", AddItemSource.FROM_EDIT_ITEM_CATEGORY_DIALOG)
                        }
                    }
                }
                is CategoryDetailFragment -> {
                    fragment.apply {
                        arguments = Bundle().apply {
                            putSerializable("item_type", currentItemType)
                            putSerializable("category_type", currentCategoryType)
                            putSerializable("source", AddItemSource.FROM_DETAIL_CATEGORY)
                        }
                    }
                }
            }

            when(currentItemType) {
                ItemType.CATEGORY -> {
                    when(source) {
                        AddItemSource.FROM_EDIT_ITEM_CATEGORY_DIALOG -> {
                            updateTransactionTitle(selectedCategoryItemForAdd?.name ?: getString(R.string.category))
                        }
                        else -> {}
                    }
                }
                ItemType.ACCOUNT -> {
                    updateTransactionTitle(selectedAccountItemForAdd?.name ?: getString(R.string.account))
                }
                else -> {}
            }
            addIcon.visibility = View.GONE
            animateIncomingTitleToCenter(titleIncoming, getString(R.string.add))

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

        iconBack.setOnClickListener {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()

                currentFragment =
                    supportFragmentManager.findFragmentById(R.id.fragment_container_add_transaction)

                // Lấy title từ titleStack (có thể là title trước đó, hoặc mặc định là "Transaction")
                val previousTitle = if (titleStack.isNotEmpty()) titleStack.removeLast() else "Transaction"

                // Áp dụng animation cho title khi quay lại
                animateBackTitleTransition(previousTitle)

                // Xử lý các thay đổi UI khác khi quay lại
                when (currentFragment?.arguments?.getSerializable("source") as? AddItemSource) {
                    AddItemSource.FROM_ADD_TRANSACTION -> {
                        switchToBookmarkIconWithFade()
                    }
                    AddItemSource.FROM_EDIT_ITEM_CATEGORY_DIALOG -> {
                        switchToAddIconWithFade()
                    }
                    AddItemSource.FROM_DETAIL_CATEGORY -> {
                        // Optional logic if necessary
                        switchToAddIconWithFade()
                    }
                    AddItemSource.FROM_EDIT_ITEM_ACCOUNT_DIALOG -> {
                        addIcon.visibility = View.VISIBLE
                    }
                    else -> {}
                }
            } else {
                finish()
            }
            overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right)
        }

        val transaction = intent.getSerializableExtra("transaction") as? Transaction

        if (transaction != null) {
            titleCurrent.text = if (transaction.isIncome) getString(R.string.Income) else getString(R.string.Expense)
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
        currentFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container_add_transaction)
        bookmarkIcon = findViewById(R.id.add_transaction_starButton)
        addIcon = findViewById(R.id.add_transaction_addButton)
        iconBack = findViewById(R.id.add_transaction_backButton)
        titleCurrent = findViewById(R.id.add_transaction_titleCurrent)
        titleIncoming = findViewById(R.id.add_transaction_titleIncoming)
    }

    fun updateTransactionTitle(title: String) {
        titleCurrent.text = title
    }

    fun updateTitleIncoming(text: String) {
        titleIncoming.text = text
    }

    fun animateTitleToLeftOfIcon(titleView: TextView) {
        titleView.post {
            val iconStart = iconBack.left
            val titleCenterX = titleView.left + titleView.width / 2f
            val iconCenterX = iconStart + iconBack.width / 2f
            val targetTranslationX = iconCenterX - titleCenterX

            titleView.animate()
                .translationX(targetTranslationX)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }
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

    fun switchToAddIconWithFade() {
        bookmarkIcon.animate().alpha(0f).setDuration(300).withEndAction {
            bookmarkIcon.visibility = View.GONE
            addIcon.alpha = 0f
            addIcon.visibility = View.VISIBLE
            addIcon.animate().alpha(1f).setDuration(300).start()
        }.start()
    }

    fun switchToBookmarkIconWithFade() {
        addIcon.animate().alpha(0f).setDuration(300).withEndAction {
            addIcon.visibility = View.GONE
            bookmarkIcon.alpha = 0f
            bookmarkIcon.visibility = View.VISIBLE
            bookmarkIcon.animate().alpha(1f).setDuration(300).start()
        }.start()
    }

    fun animateIncomingTitleToCenter(titleView: TextView, newText: String) {
        titleView.text = newText
        titleView.visibility = View.VISIBLE
        titleView.alpha = 1f

        titleView.post {
            val screenWidth = toolbar.width
            val centerX = screenWidth / 2f
            val textCenterX = titleView.left + titleView.width / 2f
            val offsetToCenter = centerX - textCenterX

            // Bắt đầu từ bên phải ngoài màn hình
            titleView.translationX = screenWidth.toFloat()

            // Animate vào giữa
            titleView.animate()
                .translationX(offsetToCenter)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction {
                    // Sau khi vào giữa, cập nhật lại titleCurrent
                    titleCurrent.text = newText
                    titleCurrent.translationX = 0f

                    // Ẩn và reset titleIncoming
                    titleView.visibility = View.GONE
                    titleView.translationX = 0f
                }
                .start()
        }
    }

    fun animateBackTitleTransition(previousTitle: String) {
        val toolbarWidth = toolbar.width.toFloat()

        // 1. Animate titleCurrent trượt sang phải và ẩn
        titleCurrent.animate()
            .translationX(toolbarWidth) // trượt ra bên phải
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                titleCurrent.visibility = View.GONE
                titleCurrent.translationX = 0f // reset để dùng lại
            }
            .start()

        // 2. titleIncoming xuất hiện từ bên trái
        titleIncoming.text = previousTitle
        titleIncoming.visibility = View.VISIBLE
        titleIncoming.translationX = -toolbarWidth // bắt đầu từ ngoài trái

        // 3. Animate titleIncoming vào giữa
        titleIncoming.animate()
            .translationX(0f)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                // Khi animation kết thúc: cập nhật titleCurrent
                titleCurrent.text = previousTitle
                titleCurrent.translationX = 0f
                titleCurrent.visibility = View.VISIBLE

                // Reset titleIncoming
                titleIncoming.visibility = View.GONE
                titleIncoming.translationX = 0f
            }
            .start()
    }

    fun popTitleStackAndAnimateBack() {
        val previousTitle = if (titleStack.isNotEmpty()) titleStack.removeLast() else "Transaction"
        animateBackTitleTransition(previousTitle)
    }

    fun onTransactionSaved() {
        finish()
        overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right)
    }
}