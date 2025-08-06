package com.henrystudio.moneymanager.ui.bottomNavigation.statistic

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.databinding.FragmentStatisticNoteBinding
import com.henrystudio.moneymanager.model.*
import com.henrystudio.moneymanager.viewmodel.TransactionViewModel
import com.henrystudio.moneymanager.viewmodel.TransactionViewModelFactory

class StatisticNoteFragment : Fragment() {
    private var _binding: FragmentStatisticNoteBinding? = null
    private val binding get() = _binding!!
    private lateinit var tvNote: TextView
    private lateinit var tvCount: TextView
    private lateinit var tvAmount: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NoteAdapter
    private var listNotes: List<Note> = emptyList()
    private var currentSortField = SortField.AMOUNT
    private var currentSortOrder = SortOrder.DESC

    private val viewModel: TransactionViewModel by activityViewModels {
        TransactionViewModelFactory(
            AppDatabase.getDatabase(requireActivity().application).transactionDao()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentStatisticNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        adapter = NoteAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        viewModel.noteList.observe(viewLifecycleOwner) {list ->
            listNotes = list
            adapter.submitList(list)
            sortAndUpdate()
            updateSortIndicators()
        }

        tvNote.setOnClickListener {
            if (currentSortField == SortField.NOTE) {
                toggleSortOrder()
            } else {
                currentSortField = SortField.NOTE
                currentSortOrder = SortOrder.ASC
            }
            sortAndUpdate()
            updateSortIndicators()
        }
        tvCount.setOnClickListener {
            if (currentSortField == SortField.COUNT) {
                toggleSortOrder()
            } else {
                currentSortField = SortField.COUNT
                currentSortOrder = SortOrder.ASC
            }
            sortAndUpdate()
            updateSortIndicators()
        }
        tvAmount.setOnClickListener {
            if (currentSortField == SortField.AMOUNT) {
                toggleSortOrder()
            } else {
                currentSortField = SortField.AMOUNT
                currentSortOrder = SortOrder.ASC
            }
            sortAndUpdate()
            updateSortIndicators()
        }
    }

    private fun init() {
        tvNote = binding.fragmentStatisticNoteHeaderNote
        tvCount = binding.fragmentStatisticNoteHeaderCount
        tvAmount = binding.fragmentStatisticNoteHeaderAmount
        recyclerView = binding.fragmentStatisticNoteRecyclerView
    }

    private fun sortAndUpdate() {
        val sortedList = when (currentSortField) {
            SortField.NOTE -> {
                if (currentSortOrder == SortOrder.ASC) listNotes.sortedBy { it.note }
                else listNotes.sortedByDescending { it.note }
            }
            SortField.COUNT -> {
                if (currentSortOrder == SortOrder.ASC) listNotes.sortedBy { it.count }
                else listNotes.sortedByDescending { it.count }
            }
            SortField.AMOUNT -> {
                if (currentSortOrder == SortOrder.ASC) listNotes.sortedBy { it.amount }
                else listNotes.sortedByDescending { it.amount }
            }
        }
        adapter.submitList(sortedList)
    }

    private fun toggleSortOrder() {
        currentSortOrder = if (currentSortOrder == SortOrder.ASC) SortOrder.DESC else SortOrder.ASC
    }

    private fun updateSortIndicators() {
        val upIcon = R.drawable.ic_baseline_arrow_upward_24
        val downIcon = R.drawable.ic_baseline_arrow_downward_24
        val noneIcon = 0

        fun setDrawable(textView: TextView, isActive: Boolean) {
            val icon = if (!isActive) noneIcon
            else if (currentSortOrder == SortOrder.ASC) upIcon else downIcon
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0)
        }

        setDrawable(tvNote, currentSortField == SortField.NOTE)
        setDrawable(tvCount, currentSortField == SortField.COUNT)
        setDrawable(tvAmount, currentSortField == SortField.AMOUNT)
    }
}