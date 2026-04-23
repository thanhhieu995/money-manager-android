package com.henrystudio.moneymanager.presentation.views.bottomNavigation.statistic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.presentation.model.Note

class NoteAdapter(private var notes: List<Note>): RecyclerView.Adapter<NoteAdapter.ViewHolder>() {
    var onClickListener: ((Note) -> Boolean)? = null
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val nameText : TextView = view.findViewById(R.id.item_statistic_note_name)
        val countText: TextView = view.findViewById(R.id.item_statistic_note_count)
        val amountText: TextView = view.findViewById(R.id.item_statistic_note_amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_statistic_category_note, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notes[position]
        holder.nameText.text = note.note
        holder.countText.text = note.count.toString()
        holder.amountText.text = Helper.formatCurrency(holder.itemView.context, note.amount)
        holder.itemView.setOnClickListener {
            onClickListener?.invoke(note)
        }
    }

    override fun getItemCount(): Int = notes.size

    fun submitList(list: List<Note>) {
        notes = list
        notifyDataSetChanged()
    }
}