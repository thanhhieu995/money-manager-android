package com.henrystudio.moneymanager.presentation.addtransaction

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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.appbar.MaterialToolbar
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.core.util.IntentKeys
import com.henrystudio.moneymanager.data.model.Account
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.addtransaction.model.AddTransactionEvent
import com.henrystudio.moneymanager.presentation.addtransaction.model.CategoryItem
import com.henrystudio.moneymanager.presentation.addtransaction.model.ToolbarTitle
import com.henrystudio.moneymanager.presentation.addtransaction.ui.addItemFragment.AddItemFragment
import com.henrystudio.moneymanager.presentation.addtransaction.ui.addTransactionFragment.AddTransactionFragment
import com.henrystudio.moneymanager.presentation.addtransaction.ui.categoryDetailFragment.CategoryDetailFragment
import com.henrystudio.moneymanager.presentation.addtransaction.ui.editItemFragment.EditItemDialogFragment
import com.henrystudio.moneymanager.presentation.bookmark.BookmarkActivity
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
    var selectedCategoryItemForAdd: CategoryItem? = null
    var selectedAccountItemForAdd: Account? = null
    private val addTransactionActivityViewModel: AddTransactionActivityViewModel by viewModels()
    private var lastTitle: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)
        init()
        onBackPressedDispatcher.addCallback(this) {
            if (supportFragmentManager.backStackEntryCount > 0) {
                addTransactionActivityViewModel.onBackClicked()
                overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right)
            } else {
                finish()
            }
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
                addTransactionActivityViewModel.onBackClicked()
            } else {
                finish()
            }
            overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right)
        }

        val transaction = intent.getParcelableExtra(IntentKeys.TRANSACTION) as? Transaction
        addTransactionActivityViewModel.init(transaction)

        if (savedInstanceState == null) {
            val fragment = AddTransactionFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(IntentKeys.TRANSACTION, transaction)
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
                addTransactionActivityViewModel.event.collect { event ->
                    handleNavigation(event)
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

    private fun renderToolbar(state: AddTransactionToolbarState) {
        val current = mapTitle(state.title)

        if (current == lastTitle && state.animation == TitleAnimation.None) return

        when(state.animation) {
            TitleAnimation.SlideFromRight -> { animateForward( current)}
            TitleAnimation.SlideFromLeft -> { animateBack(current)}
            TitleAnimation.None -> {
                titleCurrent.text = current
                titleCurrent.translationX = 0f
            }
        }

        lastTitle = current

        addIcon.visibility = if (state.config.showAdd) View.VISIBLE else View.GONE
        bookmarkIcon.visibility = if (state.config.showBookmark) View.VISIBLE else View.GONE
    }

    private fun handleNavigation(event: AddTransactionEvent) {
        when (event) {

            is AddTransactionEvent.NavigateToAddItem -> {
                navigateTo(
                    AddItemFragment.newInstance(
                        itemType = event.itemType,
                        action = event.action,
                        itemEdit = event.editItem
                    )
                )
            }

            is AddTransactionEvent.NavigateToEditItem -> {
                navigateTo(
                    EditItemDialogFragment.newInstance(
                        itemType = event.itemType,
                        transactionType = event.transactionType,
                        action = event.action
                    )
                )
            }

            AddTransactionEvent.PopBack -> {
                supportFragmentManager.popBackStack()
            }

            AddTransactionEvent.NavigateBackToDaily -> {
                finish()
            }

            is AddTransactionEvent.NavigateToCategoryDetailWithTitle -> {
                navigateTo(
                    CategoryDetailFragment.newInstance(event.item, event.action, event.title)
                )
            }

            else -> {}
        }
    }

    private fun navigateTo(fragment: Fragment) {
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

    private fun mapTitle(title: ToolbarTitle): String {
        return when (title) {
            ToolbarTitle.INCOME -> getString(R.string.Income)
            ToolbarTitle.EXPENSE -> getString(R.string.Expense)
            ToolbarTitle.ADD -> getString(R.string.add)
            ToolbarTitle.ACCOUNT -> getString(R.string.account)
            ToolbarTitle.CATEGORY -> getString(R.string.category)
            is ToolbarTitle.Custom -> title.value
        }
    }

    private fun animateForward(newText: String) {
        val width = toolbar.width.toFloat()

        // reset trạng thái
        titleCurrent.animate().cancel()
        titleIncoming.animate().cancel()

        titleIncoming.text = newText
        titleIncoming.translationX = width
        titleIncoming.visibility = View.VISIBLE

        titleCurrent.translationX = 0f

        titleCurrent.animate()
            .translationX(-width)
            .setDuration(250)
            .start()

        titleIncoming.animate()
            .translationX(0f)
            .setDuration(250)
            .withEndAction {
                titleCurrent.text = newText
                titleCurrent.translationX = 0f
                titleIncoming.visibility = View.GONE
            }
            .start()
    }

    private fun animateBack(newText: String) {
        val width = toolbar.width.toFloat()

        titleCurrent.animate().cancel()
        titleIncoming.animate().cancel()

        titleIncoming.text = newText
        titleIncoming.translationX = -width
        titleIncoming.visibility = View.VISIBLE

        titleCurrent.translationX = 0f

        titleCurrent.animate()
            .translationX(width)
            .setDuration(250)
            .start()

        titleIncoming.animate()
            .translationX(0f)
            .setDuration(250)
            .withEndAction {
                titleCurrent.text = newText
                titleCurrent.translationX = 0f
                titleIncoming.visibility = View.GONE
            }
            .start()
    }
}