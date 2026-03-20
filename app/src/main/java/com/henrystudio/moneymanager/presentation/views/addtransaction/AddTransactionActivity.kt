package com.henrystudio.moneymanager.presentation.views.addtransaction

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.appbar.MaterialToolbar
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.data.model.Account
import com.henrystudio.moneymanager.data.model.CategoryType
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.model.ItemType
import com.henrystudio.moneymanager.presentation.views.bookmark.BookmarkActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddTransactionActivity : AppCompatActivity() {
    lateinit var titleCurrent: TextView
    lateinit var titleIncoming: TextView
    lateinit var bookmarkIcon: ImageView
    lateinit var addIcon: ImageView
    lateinit var iconBack: ImageView
    private var shouldAnimateExit = false
    private lateinit var toolbar: MaterialToolbar
    private var isIncome: Boolean = false
    var selectedCategoryItemForAdd: CategoryItem? = null
    var selectedAccountItemForAdd: Account? = null
    private val addTransactionActivityViewModel: AddTransactionActivityViewModel by viewModels()
    private val titleStack = ArrayDeque<String>()

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

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        bookmarkIcon.setOnClickListener {
            val intent = Intent(this, BookmarkActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_bottom, R.anim.no_animation)
        }

        addIcon.setOnClickListener {
            addTransactionActivityViewModel.onAddIconClicked()
        }

        iconBack.setOnClickListener {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()

                addTransactionActivityViewModel.onBackToRoot(isIncome)
            } else {
                finish()
            }
            overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right)
        }

        val transaction = intent.getSerializableExtra("transaction") as? Transaction

        if (transaction != null) {
            titleCurrent.text =
                if (transaction.isIncome) getString(R.string.Income) else getString(R.string.Expense)
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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                addTransactionActivityViewModel.toolbarState.collect { state ->
                    renderToolbar(state)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                addTransactionActivityViewModel.event.collect { action ->
                    handleNavigation(action)
                }
            }
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

    fun onTransactionSaved() {
        finish()
        overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right)
    }

    private fun renderToolbar(state: AddTransactionToolbarState) {
        when(state.animation) {
            TitleAnimation.SlideFromRight -> animateIncomingTitleToCenter(titleIncoming, state.title)
            TitleAnimation.SlideFromLeft -> animateBackTitleTransition(state.title)
            TitleAnimation.None -> titleCurrent.text = state.title
            else -> {}
        }

        addIcon.visibility = if (state.showAddIcon) View.VISIBLE else View.GONE
        bookmarkIcon.visibility = if (state.showBookmarkIcon) View.VISIBLE else View.GONE
    }

    private fun handleNavigation(action: AddItemAction) {
        val fragment = when (action) {

            is AddItemAction.FromAddTransaction -> {
                AddItemFragment.newInstance(action.itemType)
            }

            is AddItemAction.FromEditCategory -> {
                AddItemFragment.newInstance(ItemType.CATEGORY)
            }

            is AddItemAction.FromEditAccount -> {
                AddItemFragment.newInstance(ItemType.ACCOUNT)
            }

            is AddItemAction.FromCategoryDetail -> {
                AddItemFragment.newInstance(ItemType.CATEGORY)
            }
        }

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.no_animation,
                R.anim.no_animation,
                R.anim.slide_out_right
            )
            .replace(R.id.fragment_container_add_transaction, fragment)
            .addToBackStack(null)
            .commit()
    }
}