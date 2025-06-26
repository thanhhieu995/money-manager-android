package com.example.moneymanager.ui.addtransaction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanager.R

class EditCategoryAdapter(
    var categories: List<CategoryItem>,
    private val onDeleteClick: (CategoryItem) -> Unit
) : RecyclerView.Adapter<EditCategoryAdapter.EditCategoryViewHolder>() {

    private val clickItemListener: OnEditCategoryClickListener?= null

    interface OnEditCategoryClickListener {
        fun onEditCategoryClick(item: CategoryItem)
    }

    inner class EditCategoryViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val deleteIcon: ImageView = view.findViewById(R.id.item_category_edit_deleteIcon)
        private val categoryName: TextView = view.findViewById(R.id.item_category_edit_tvCategoryName)
        private val categorySub: TextView = view.findViewById(R.id.item_category_edit_tvSubcategories)
        fun bind(item: CategoryItem) {
            if (item.children.isEmpty()) {
                categorySub.visibility = View.GONE
                categoryName.text = item.emoji + item.name
            } else {
                categorySub.visibility = View.VISIBLE
                categoryName.text = item.emoji + item.name + " (" + item.children.size +")"
                categorySub.text = item.children.map { it.name }.toString()
            }
            deleteIcon.setOnClickListener {
                onDeleteClick.invoke(item)
            }

            itemView.setOnClickListener {
                clickItemListener?.onEditCategoryClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditCategoryViewHolder {
       val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_edit, parent, false)
        return EditCategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: EditCategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    fun submitList(newList: List<CategoryItem>) {
        val diffCallback = EditCategoryDiffCallback(categories, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        categories = newList
        diffResult.dispatchUpdatesTo(this)
    }
}