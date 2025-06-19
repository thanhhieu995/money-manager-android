package com.example.moneymanager.ui.addtransaction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanager.R

class ExpandableCategoryAdapter(
    private val originalItems: List<CategoryItem>,
    private val onItemClick: (CategoryItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val displayedItems = mutableListOf<CategoryItem>()

    init {
        refreshDisplayedItems()
    }

    private fun refreshDisplayedItems() {
        displayedItems.clear()
        for (item in originalItems) {
            displayedItems.add(item)
            if (item.isExpanded) {
                displayedItems.addAll(item.children)
            }
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (displayedItems[position].isParent) 0 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = if (viewType == 0)
            R.layout.item_category_parent
        else
            R.layout.item_category_child

        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return if (viewType == 0) ParentViewHolder(view) else ChildViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = displayedItems[position]
        if (holder is ParentViewHolder) {
            holder.bind(item)
        } else if (holder is ChildViewHolder) {
            holder.bind(item)
        }
    }

    override fun getItemCount(): Int = displayedItems.size

    inner class ParentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvEmoji: TextView = view.findViewById(R.id.item_category_parent_tvEmoji)
        private val tvName: TextView = view.findViewById(R.id.item_category_parent_tvName)
        private val ivArrow: View = view.findViewById(R.id.item_category_parent_ivArrow)

        fun bind(item: CategoryItem) {
            tvEmoji.text = item.emoji
            tvName.text = item.name
            ivArrow.visibility = if (item.children.isNotEmpty()) View.VISIBLE else View.INVISIBLE
            ivArrow.rotation = if (item.isExpanded) 180f else 0f

            itemView.setOnClickListener {
                if (item.children.isNotEmpty()) {
                    item.isExpanded = !item.isExpanded
                    refreshDisplayedItems()
                } else {
                    onItemClick(item)
                }
            }
        }
    }

    inner class ChildViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvEmoji: TextView = view.findViewById(R.id.item_category_child_tvEmoji)
        private val tvName: TextView = view.findViewById(R.id.item_category_child_tvName)

        fun bind(item: CategoryItem) {
            tvEmoji.text = item.emoji
            tvName.text = item.name

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}