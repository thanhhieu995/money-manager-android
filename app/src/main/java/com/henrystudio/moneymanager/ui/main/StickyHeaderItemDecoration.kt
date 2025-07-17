package com.henrystudio.moneymanager.ui.main

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class StickyHeaderItemDecoration(
    private val isHeader: (position: Int) -> Boolean,
    private val bindHeader: (header: View, position: Int) -> Unit,
    private val createHeaderView: () -> View
) : RecyclerView.ItemDecoration() {

    private var headerView: View? = null

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val topChild = parent.getChildAt(0) ?: return
        val topChildPosition = parent.getChildAdapterPosition(topChild)
        if (topChildPosition == RecyclerView.NO_POSITION) return

        // Nếu topChild chính là header và vẫn đang hiển thị: KHÔNG vẽ sticky header
        if (isHeader(topChildPosition) && topChild.top >= 0) {
            return
        }

        val header = getHeaderView(parent, topChildPosition)
        bindHeader(header, topChildPosition)
        fixLayoutSize(parent, header)

        val contactPoint = header.bottom
        val childInContact = getChildInContact(parent, contactPoint)
        val childAdapterPosition = parent.getChildAdapterPosition(childInContact ?: return)

        if (isHeader(childAdapterPosition) && childAdapterPosition != topChildPosition) {
            moveHeader(c, header, childInContact)
            return
        }

        drawHeader(c, header)
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == 0) {
            outRect.top = 0 // Ví dụ: 100
        }
    }

    private fun getHeaderView(parent: RecyclerView, position: Int): View {
        if (headerView == null) {
            headerView = createHeaderView()
        }
        return headerView!!
    }

    private fun drawHeader(c: Canvas, header: View) {
        c.save()
        c.translate(0f, 0f)
        header.draw(c)
        c.restore()
    }

    private fun moveHeader(c: Canvas, currentHeader: View, nextHeader: View) {
        c.save()
        c.translate(0f, (nextHeader.top - currentHeader.height).toFloat())
        currentHeader.draw(c)
        c.restore()
    }

    private fun getChildInContact(parent: RecyclerView, contactPoint: Int): View? {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            if (child.top <= contactPoint && child.bottom >= contactPoint) {
                return child
            }
        }
        return null
    }

    private fun fixLayoutSize(parent: ViewGroup, view: View) {
        val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(parent.height, View.MeasureSpec.UNSPECIFIED)

        val childWidth = ViewGroup.getChildMeasureSpec(
            widthSpec,
            0,
            view.layoutParams.width
        )
        val childHeight = ViewGroup.getChildMeasureSpec(
            heightSpec,
            0,
            view.layoutParams.height
        )

        view.measure(childWidth, childHeight)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }
}
