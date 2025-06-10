package com.example.moneymanager.ui.bookmark

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.example.moneymanager.R

class BookmarkActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar

    private var isEditMode = false
    private var shouldAnimateExit = false
    private lateinit var bookmarkListFragment: BookmarkListFragment

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark)

        init()
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Nút đóng
        toolbar.setNavigationIcon(R.drawable.ic_baseline_close_24)
        toolbar.setNavigationOnClickListener {
            if (isEditMode) {
                toggleEditMode()
            } else {
                shouldAnimateExit = true
                finish()
            }
        }

        // Set tiêu đề giữa
        toolbar.post {
            val titleText = TextView(this).apply {
                text = "Bookmark"
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

            toolbar.addView(titleText, params)
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_bookmark, bookmarkListFragment)
            .commit()
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
        // Ẩn hiện icon tuỳ theo chế độ
        menu.findItem(R.id.menu_bookmark_edit)?.isVisible = !isEditMode
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_bookmark_edit -> {
                toggleEditMode()
                true
            }
            R.id.menu_bookmark_add -> {
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
        toolbar = findViewById(R.id.bookmark_toolbar)
    }
}