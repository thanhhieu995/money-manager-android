package com.henrystudio.moneymanager.ui.addtransaction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.henrystudio.moneymanager.R

class ExpandableCategoryAdapter(
    private val originalItems: List<CategoryItem>,
    private val onItemClick: (CategoryItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val displayedItems = mutableListOf<DisplayedItem>()

    init {
        refreshDisplayedItems()
    }

    private fun refreshDisplayedItems() {
        displayedItems.clear()
        for (item in originalItems) {
            displayedItems.add(DisplayedItem.Parent(item))
            if (item.isExpanded && item.children.isNotEmpty()) {
                displayedItems.add(DisplayedItem.ChildGroup(item.children))
            }
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (displayedItems[position]) {
            is DisplayedItem.Parent -> 0
            is DisplayedItem.ChildGroup -> 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> {
                val view = inflater.inflate(R.layout.item_category_parent, parent, false)
                ParentViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_category_child_group, parent, false)
                ChildGroupViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = displayedItems[position]
        when {
            holder is ParentViewHolder && item is DisplayedItem.Parent -> {
                holder.bind(item.category)
            }
            holder is ChildGroupViewHolder && item is DisplayedItem.ChildGroup -> {
                holder.bind(item.children)
            }
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

    inner class ChildGroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val recyclerView: RecyclerView = view.findViewById(R.id.rvChildGroup)

        fun bind(children: List<CategoryItem>) {
            recyclerView.layoutManager = GridLayoutManager(itemView.context, 3)
            recyclerView.adapter = object : RecyclerView.Adapter<ChildViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_category_child, parent, false)
                    return ChildViewHolder(view)
                }

                override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
                    holder.bind(children[position])
                }

                override fun getItemCount(): Int = children.size
            }
        }
    }
}