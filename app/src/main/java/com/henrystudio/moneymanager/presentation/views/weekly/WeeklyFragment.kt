package com.henrystudio.moneymanager.presentation.views.weekly

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
import com.henrystudio.moneymanager.databinding.FragmentWeeklyBinding
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.presentation.addtransaction.components.viewholder.SharedTransactionHolder
import com.henrystudio.moneymanager.presentation.model.FilterOption
import com.henrystudio.moneymanager.presentation.model.FilterPeriodStatistic
import com.henrystudio.moneymanager.presentation.viewmodel.SharedTransactionViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.WeeklyViewModel
import com.henrystudio.moneymanager.presentation.views.bottomNavigation.statistic.StatisticListActivity
import com.henrystudio.moneymanager.presentation.addtransaction.model.UiState
import com.henrystudio.moneymanager.presentation.views.monthly.WeeklyAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WeeklyFragment : Fragment() {
    private var _binding: FragmentWeeklyBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvNoData: TextView
    private lateinit var adapter: WeeklyAdapter

    private val sharedViewModel: SharedTransactionViewModel by activityViewModels()
    private val viewModel: WeeklyViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeeklyBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        adapter = WeeklyAdapter(
            emptyList(),
            onWeekClick = { data ->
                SharedTransactionHolder.currentFilterDate =
                    Helper.formatDateFromFilterOptionToDateDaily(data.weekStart.toString())
                SharedTransactionHolder.filterOption =
                    FilterOption(FilterPeriodStatistic.Weekly, data.weekStart)
                (requireActivity() as StatisticListActivity).onBackAnimation()
            })
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    sharedViewModel.combineGroupAndDate.collect { (state, localDate) ->
                        viewModel.updateData(
                            if (state is UiState.Success) state.data else emptyList(),
                            localDate)
                    }
                }
                launch {
                    viewModel.uiState.collect { uiState ->
                        tvNoData.visibility = if (uiState.isEmpty) View.VISIBLE else View.GONE
                        adapter.updateData(uiState.weeklyData)
                    }
                }
            }
        }
    }

    private fun init() {
        recyclerView = binding.fragmentWeeklyListSummary
        tvNoData = binding.fragmentWeeklyNoData
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
