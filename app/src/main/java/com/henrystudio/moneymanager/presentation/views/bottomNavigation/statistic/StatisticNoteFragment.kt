package com.henrystudio.moneymanager.presentation.views.bottomNavigation.statistic

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.databinding.FragmentStatisticNoteBinding
import com.henrystudio.moneymanager.presentation.model.KeyFilter
import com.henrystudio.moneymanager.presentation.model.SortField
import com.henrystudio.moneymanager.presentation.viewmodel.SharedTransactionViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.StatisticNoteViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StatisticNoteFragment : Fragment() {
    private var _binding: FragmentStatisticNoteBinding? = null
    private val binding get() = _binding!!
    private lateinit var tvNote: TextView
    private lateinit var tvCount: TextView
    private lateinit var tvAmount: TextView
    private lateinit var tvNoData: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NoteAdapter

    private val sharedViewModel: SharedTransactionViewModel by activityViewModels()
    private val viewModel: StatisticNoteViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        adapter = NoteAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    combine(
                        sharedViewModel.allTransactions,
                        sharedViewModel.filterOption,
                        sharedViewModel.statisticTransactionType
                    ) { transactions, option, type ->
                        Triple(transactions, option, type)
                    }.collect { (transactions, option, type) ->
                        viewModel.updateAllTransactions(transactions)
                        viewModel.updateFilterOption(option)
                        viewModel.updateTransactionType(type)
                    }
                }
                launch {
                    viewModel.uiState.collect { state ->
                        sharedViewModel.setStatisticTransactionFilter(state.filteredTransactions)
                        tvNoData.visibility = if (state.isEmpty) View.VISIBLE else View.GONE
                        adapter.submitList(state.notes)
                        updateSortIndicators(state.sortField, state.sortOrder)
                    }
                }
            }
        }

        tvNote.setOnClickListener {
            viewModel.onSortFieldClicked(SortField.NOTE)
        }
        tvCount.setOnClickListener {
            viewModel.onSortFieldClicked(SortField.COUNT)
        }
        tvAmount.setOnClickListener {
            viewModel.onSortFieldClicked(SortField.AMOUNT)
        }

        adapter.onClickListener = { note ->
            val state = viewModel.uiState.value
            val intent = Intent(requireContext(), StatisticCategoryActivity::class.java)
            intent.putExtra("item_click_statistic_category_name", note.note)
            intent.putExtra("item_click_statistic_filterOption", state.filterOption)
            intent.putExtra("item_click_statistic_keyWord", KeyFilter.Note)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.no_animation)
            true
        }
    }

    private fun init() {
        tvNote = binding.fragmentStatisticNoteHeaderNote
        tvCount = binding.fragmentStatisticNoteHeaderCount
        tvAmount = binding.fragmentStatisticNoteHeaderAmount
        tvNoData = binding.fragmentStatisticNoteNoDataText
        recyclerView = binding.fragmentStatisticNoteRecyclerView
    }

    private fun updateSortIndicators(sortField: SortField, sortOrder: com.henrystudio.moneymanager.presentation.model.SortOrder) {
        val upIcon = R.drawable.ic_baseline_arrow_upward_24
        val downIcon = R.drawable.ic_baseline_arrow_downward_24
        val noneIcon = 0
        fun setDrawable(textView: TextView, isActive: Boolean) {
            val icon = if (!isActive) noneIcon
            else if (sortOrder == com.henrystudio.moneymanager.presentation.model.SortOrder.ASC) upIcon else downIcon
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0)
        }
        setDrawable(tvNote, sortField == SortField.NOTE)
        setDrawable(tvCount, sortField == SortField.COUNT)
        setDrawable(tvAmount, sortField == SortField.AMOUNT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
