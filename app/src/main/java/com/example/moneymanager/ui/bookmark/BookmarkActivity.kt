package com.example.moneymanager.ui.bookmark

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanager.R
import com.example.moneymanager.helper.Helper
import com.example.moneymanager.model.AppDatabase
import com.example.moneymanager.model.Transaction
import com.example.moneymanager.viewmodel.TransactionViewModel
import com.example.moneymanager.viewmodel.TransactionViewModelFactory

class BookmarkActivity : AppCompatActivity() {
    private lateinit var viewModel: TransactionViewModel
    private lateinit var bookmarkAdapter: BookmarkAdapter
    private lateinit var tvNoData: TextView
    private lateinit var toolbar: Toolbar

    private var isEditMode = false

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark)

        tvNoData = findViewById(R.id.bookmark_noData)
        toolbar = findViewById(R.id.bookmark_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Nút đóng
        toolbar.setNavigationIcon(R.drawable.ic_baseline_close_24)
        toolbar.setNavigationOnClickListener {
            if (isEditMode) {
                toggleEditMode()
            } else {
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

        // Khởi tạo ViewModel & Adapter
        val dao = AppDatabase.getDatabase(application).transactionDao()
        val factory = TransactionViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]

        bookmarkAdapter = BookmarkAdapter(
            items = emptyList(),
            onDeleteClick = { transaction ->
                viewModel.delete(transaction)
            }
        )

        val recyclerView = findViewById<RecyclerView>(R.id.bookmarkRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = bookmarkAdapter

        ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView)

        viewModel.getBookmarkedTransactions().observe(this) { list ->
            bookmarkAdapter.updateList(list)
            tvNoData.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        // Click giao dịch
        bookmarkAdapter.clickListener = object : BookmarkAdapter.OnBookmarkLickListener {
            override fun onBookmarkLick(transaction: Transaction) {
                Helper.openTransactionDetail(this@BookmarkActivity, transaction)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bookmark, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // Ẩn hiện icon tuỳ theo chế độ
        menu.findItem(R.id.action_edit)?.isVisible = !isEditMode
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                toggleEditMode()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode
        bookmarkAdapter.toggleEditMode()
        invalidateOptionsMenu()

        // Cập nhật icon và tiêu đề toolbar
        if (isEditMode) {
            toolbar.setNavigationIcon(R.drawable.ic_baseline_close_black)
        } else {
            toolbar.setNavigationIcon(R.drawable.ic_baseline_close_24)
        }
    }

    private val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val item = bookmarkAdapter.items[position]
            viewModel.delete(item)
        }

        override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
            return 0.5f
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float, dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

            val itemView = viewHolder.itemView

            // Vẽ nền đỏ khi vuốt trái
            val paint = Paint().apply { color = Color.parseColor("#F44336") }
            c.drawRect(
                itemView.right.toFloat() + dX, // trái bắt đầu từ dX
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat(),
                paint
            )

            // Vẽ chữ "Delete"
            val text = "Delete"
            val textPaint = Paint().apply {
                color = Color.WHITE
                textSize = 48f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.RIGHT
            }

            val textMargin = 40f
            val textX = itemView.right.toFloat() - textMargin
            val textY = itemView.top + itemView.height / 2f + 20f
            c.drawText(text, textX, textY, textPaint)

            // Kéo item đi theo dX
            viewHolder.itemView.translationX = dX
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            val foreground = viewHolder.itemView.findViewById<View>(R.id.item_bookmark_foreground)
            foreground.animate().translationX(0f).start()
        }
    }
}