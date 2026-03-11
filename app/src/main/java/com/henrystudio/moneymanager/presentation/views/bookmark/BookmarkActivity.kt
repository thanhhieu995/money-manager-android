package com.henrystudio.moneymanager.presentation.views.bookmark

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.presentation.bookmark.AddBookmarkFragment

class BookmarkActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar

    private var isEditMode = false
    private var shouldAnimateExit = false
    private lateinit var bookmarkListFragment: BookmarkListFragment
    private lateinit var addBookmarkFragment: AddBookmarkFragment
    private lateinit var bookmarkTitleView: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark)

        init()
        onBackPressedDispatcher.addCallback(this) {
            finish()
            overridePendingTransition(R.anim.no_animation, R.anim.slide_out_bottom)
        }
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Set tiêu đề giữa
        toolbar.post {
            bookmarkTitleView = TextView(this).apply {
                text = getString(R.string.bookmark)
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

            toolbar.addView(bookmarkTitleView, params)
            setUpToolBarBookmarkListFragment()
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_bookmark, bookmarkListFragment)
            .commit()

        supportFragmentManager.addOnBackStackChangedListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_bookmark)
            if (currentFragment is AddBookmarkFragment) {
                toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24)
                toolbar.setNavigationOnClickListener {
                    supportFragmentManager.popBackStack()
                }
                toolbar.post {
                    animateTitleToLeftOfIcon()
                }
            } else {
               setUpToolBarBookmarkListFragment()
            }
            invalidateOptionsMenu()
        }
    }

    override fun onPause() {
        super.onPause()
        if (shouldAnimateExit) {
            overridePendingTransition(R.anim.no_animation, R.anim.slide_out_bottom)
            shouldAnimateExit = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bookmark, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_bookmark)
        val isAddBookmark = currentFragment is AddBookmarkFragment

        // Ẩn hiện menu tùy theo fragment
        menu.findItem(R.id.menu_bookmark_edit)?.isVisible = !isAddBookmark && !isEditMode
        menu.findItem(R.id.menu_bookmark_add)?.isVisible = !isAddBookmark
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_bookmark_edit -> {
                toggleEditMode()
                true
            }
            R.id.menu_bookmark_add -> {
                supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right, // vào
                        R.anim.slide_out_left, // ra khi bị replace
                        R.anim.slide_in_left,  // vào lại khi popBackStack
                        R.anim.slide_out_right // ra khi popBackStack
                    )
                    .replace(R.id.fragment_container_bookmark, addBookmarkFragment)
                    .addToBackStack(null)
                    .commit()
                toolbar.post {
                    invalidateOptionsMenu()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode
        bookmarkListFragment.toggleEditMode()
        invalidateOptionsMenu()

        // Cập nhật icon và tiêu đề toolbar
        if (isEditMode) {
            toolbar.setNavigationIcon(R.drawable.ic_baseline_close_black)
        } else {
            toolbar.setNavigationIcon(R.drawable.ic_baseline_close_24)
        }
    }

    private fun init() {
        bookmarkListFragment = BookmarkListFragment()
        addBookmarkFragment = AddBookmarkFragment()
        toolbar = findViewById(R.id.bookmark_toolbar)
    }

    private fun animateTitleToLeftOfIcon() {
        // Kích thước icon back
        val iconWidth = toolbar.navigationIcon?.intrinsicWidth ?: 0

        // Padding mặc định icon bên trái
        val iconMargin = (toolbar.contentInsetStartWithNavigation)

        // Tổng khoảng trống cần dịch tiêu đề sang trái
        val targetTranslationX = -(toolbar.width / 2f) + iconWidth + iconMargin + 8f // 16dp padding tùy chỉnh

        bookmarkTitleView.animate()
            .translationX(targetTranslationX)
            .setDuration(300L)
            .start()
    }

    private fun animateTitleToCenter() {
        val animator = ObjectAnimator.ofFloat(bookmarkTitleView, "translationX", bookmarkTitleView.translationX, 0f)
        animator.duration = 300
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }

    private fun setUpToolBarBookmarkListFragment() {
        toolbar.setNavigationIcon(R.drawable.ic_baseline_close_24)
        toolbar.setNavigationOnClickListener {
            if (isEditMode) {
                toggleEditMode()
            } else {
                shouldAnimateExit = true
                finish()
            }
        }
        isEditMode = false
        animateTitleToCenter()
    }
}