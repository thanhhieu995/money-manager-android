package com.example.moneymanager.ui.bookmark

import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.example.moneymanager.R

class BookmarkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark)

        val toolbar = findViewById<Toolbar>(R.id.bookmark_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Set nút đóng (X)
        toolbar.setNavigationIcon(R.drawable.ic_baseline_close_24)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Set tiêu đề ở giữa nếu muốn
        toolbar.post {
            val titleText = TextView(this).apply {
                text = "Bookmark"
                textSize = 18f
                setTextColor(Color.BLACK)
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
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bookmark, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                // TODO: xử lý edit
                true
            }
            R.id.action_add -> {
                // TODO: xử lý thêm mới
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}