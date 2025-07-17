package com.henrystudio.moneymanager.ui.addtransaction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.model.Account

class EditItemDialogAdapter(
    private var itemList: List<EditItem>,
    private val onDeleteClick: (EditItem) -> Unit,
    private val clickItemListener: OnEditClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnEditClickListener {
        fun onEditItemClick(item: EditItem)
    }

    override fun getItemViewType(position: Int): Int {
        return when (itemList[position]) {
            is EditItem.Category -> 0
            is EditItem.AccountItem -> 1
        }
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
                onDeleteClick.invoke(EditItem.Category(item))
            }

            itemView.setOnClickListener {
                clickItemListener.onEditItemClick(EditItem.Category(item))
            }
        }
    }

    inner class EditAccountViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.item_account_name)
        private val icon: ImageView = view.findViewById(R.id.item_account_deleteIcon)

        fun bind(item: Account) {
            name.text = item.name
            // icon.setImage... nếu có icon
            itemView.setOnClickListener {
                clickItemListener?.onEditItemClick(EditItem.AccountItem(item))
            }
            icon.setOnClickListener {
                onDeleteClick.invoke(EditItem.AccountItem(item))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            0 -> EditCategoryViewHolder(inflater.inflate(R.layout.item_category_edit, parent, false))
            1 -> EditAccountViewHolder(inflater.inflate(R.layout.item_account_edit, parent, false))
            else -> throw java.lang.IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = itemList[position]) {
            is EditItem.Category -> (holder as EditCategoryViewHolder).bind(item.item)
            is EditItem.AccountItem -> (holder as EditAccountViewHolder).bind(item.item)
        }
    }

    override fun getItemCount(): Int = itemList.size

    fun submitList(newList: List<EditItem>) {
        val diffCallback = EditItemDialogDiffCallback(itemList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        itemList = newList
        diffResult.dispatchUpdatesTo(this)
    }
}