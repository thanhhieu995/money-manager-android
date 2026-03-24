package com.henrystudio.moneymanager.presentation.addtransaction.components.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.presentation.addtransaction.model.CategoryItem

class DetailCategoryAdapter(
    private var categoryItems: List<CategoryItem>,
    private val onDeleteClick: (CategoryItem) -> Unit,
    private val onItemClick: (CategoryItem) -> Unit
): RecyclerView.Adapter<DetailCategoryAdapter.DetailCategoryViewHolder>() {

    inner class DetailCategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.item_category_detail_tvCategoryName)
        private val deleteIcon: ImageView = view.findViewById(R.id.item_category_detail_deleteIcon)
        fun bind(item: CategoryItem) {
            name.text = "${item.emoji} ${item.name}"
            deleteIcon.setOnClickListener {
                onDeleteClick.invoke(item)
            }
            itemView.setOnClickListener {
                onItemClick.invoke(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailCategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_detail, parent, false)
        return DetailCategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetailCategoryViewHolder, position: Int) {
        holder.bind(categoryItems[position])
    }

    override fun getItemCount(): Int = categoryItems.size

    fun submitList(newItems: List<CategoryItem>) {
        categoryItems = newItems
        notifyDataSetChanged()
    }
}