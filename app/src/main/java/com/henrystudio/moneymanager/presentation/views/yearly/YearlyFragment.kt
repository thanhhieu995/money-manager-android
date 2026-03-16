package com.henrystudio.moneymanager.presentation.views.yearly

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.henrystudio.moneymanager.databinding.FragmentYearlyBinding
import com.henrystudio.moneymanager.presentation.viewmodel.YearlyViewModel
import com.henrystudio.moneymanager.presentation.views.addtransaction.SharedTransactionHolder
import com.henrystudio.moneymanager.presentation.views.bottomNavigation.statistic.StatisticListActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class YearlyFragment : Fragment() {
    private var _binding: FragmentYearlyBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var noData: TextView
    private lateinit var adapter: YearlyAdapter

    private val viewModel: YearlyViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentYearlyBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        adapter = YearlyAdapter(emptyList(), onClickYear = { data ->
            val (filterDateStr, filterOption) = viewModel.getFilterForYearSelection(data)
            SharedTransactionHolder.currentFilterDate = filterDateStr
            SharedTransactionHolder.filterOption = filterOption
            (requireActivity() as StatisticListActivity).onBackAnimation()
        })
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    adapter.updateData(uiState.years)
                    noData.visibility = if (uiState.isEmpty) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun init() {
        recyclerView = binding.fragmentYearlyRecyclerView
        noData = binding.fragmentYearlyNoDataText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
