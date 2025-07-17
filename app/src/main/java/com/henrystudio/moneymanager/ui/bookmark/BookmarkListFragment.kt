package com.henrystudio.moneymanager.ui.bookmark

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.helper.Helper
import com.henrystudio.moneymanager.model.AppDatabase
import com.henrystudio.moneymanager.model.Transaction
import com.henrystudio.moneymanager.viewmodel.TransactionViewModel
import com.henrystudio.moneymanager.viewmodel.TransactionViewModelFactory

class BookmarkListFragment : Fragment() {
    private lateinit var viewModel: TransactionViewModel
    private lateinit var adapter: BookmarkAdapter
    private lateinit var tvNoData: TextView
    private var isEditMode = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bookmark_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvNoData = view.findViewById(R.id.bookmark_noData)
        val recyclerView = view.findViewById<RecyclerView>(R.id.bookmarkRecyclerView)

        val dao = AppDatabase.getDatabase(requireActivity().application).transactionDao()
        val factory = TransactionViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]

        adapter = BookmarkAdapter(
            items = emptyList(),
            onDeleteClick = { transaction ->
                viewModel.update(transaction.copy(isBookmarked = false))
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView)

        viewModel.getBookmarkedTransactions().observe(viewLifecycleOwner) { list ->
            adapter.updateList(list)
            tvNoData.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        adapter.clickListener = object : BookmarkAdapter.OnBookmarkLickListener {
            override fun onBookmarkClick(transaction: Transaction) {
                Helper.openTransactionDetail(requireActivity(), transaction)
            }
        }
    }

    fun toggleEditMode() {
        isEditMode = !isEditMode
        adapter.toggleEditMode()
    }

    private val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, tgt: RecyclerView.ViewHolder) = false

        override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {
            val item = adapter.items[vh.adapterPosition]
            viewModel.update(item.copy(isBookmarked = false))
        }

        override fun getSwipeEscapeVelocity(defaultValue: Float): Float = 0.5f

        override fun onChildDraw(c: Canvas, rv: RecyclerView, vh: RecyclerView.ViewHolder, dx: Float, dy: Float, state: Int, active: Boolean) {
            val itemView = vh.itemView
            if (dx < 0) {
                val paint = Paint().apply { color = Color.parseColor("#F44336") }
                c.drawRect(itemView.right + dx, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat(), paint)

                val textPaint = Paint().apply {
                    color = Color.WHITE
                    textSize = 48f
                    typeface = Typeface.DEFAULT_BOLD
                    textAlign = Paint.Align.RIGHT
                }

                val x = itemView.right - 40f
                val y = itemView.top + itemView.height / 2f + 20f
                c.drawText("Delete", x, y, textPaint)
            }

            vh.itemView.translationX = dx
        }

        override fun clearView(rv: RecyclerView, vh: RecyclerView.ViewHolder) {
            super.clearView(rv, vh)
            val fg = vh.itemView.findViewById<View>(R.id.item_bookmark_foreground)
            fg.animate().translationX(0f).start()
        }
    }
}