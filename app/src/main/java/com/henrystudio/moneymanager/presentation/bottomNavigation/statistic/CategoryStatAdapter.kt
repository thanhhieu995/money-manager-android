package com.henrystudio.moneymanager.presentation.views.bottomNavigation.statistic

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.presentation.model.CategoryStat

class CategoryStatAdapter(private var items: List<CategoryStat>) : RecyclerView.Adapter<CategoryStatAdapter.ViewHolder>() {
    var onClickListener: ((CategoryStat) -> Boolean) ?= null
    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val colorBox: View = view.findViewById(R.id.item_statistic_category_colorBox)
        val percentText: TextView = view.findViewById(R.id.item_statistic_category_percent)
        val nameText: TextView = view.findViewById(R.id.item_statistic_category_category_name)
        val amountText: TextView = view.findViewById(R.id.item_statistic_category_amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_statistic_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val drawable = ContextCompat.getDrawable(holder.itemView.context, R.drawable.bg_rounded_color_box)?.mutate()
        (drawable as GradientDrawable).setColor(item.color) // Dùng màu từ item
        holder.colorBox.background = drawable

        holder.nameText.text = item.name
        holder.percentText.text = "${"%.1f".format(item.percent)}%"
        holder.amountText.text = Helper.formatCurrency(item.amount.toDouble())

        holder.itemView.setOnClickListener {
            onClickListener?.invoke(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newList: List<CategoryStat>) {
        items = newList
        notifyDataSetChanged()
    }
}