package com.henrystudio.moneymanager.presentation.views.monthly

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.henrystudio.moneymanager.databinding.FragmentMonthlyBinding
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.presentation.addtransaction.components.viewholder.SharedTransactionHolder
import com.henrystudio.moneymanager.presentation.model.FilterOption
import com.henrystudio.moneymanager.presentation.model.FilterPeriodStatistic
import com.henrystudio.moneymanager.presentation.viewmodel.MonthlyViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.SharedTransactionViewModel
import com.henrystudio.moneymanager.presentation.views.bottomNavigation.statistic.StatisticListActivity
import com.henrystudio.moneymanager.presentation.views.daily.DataTransactionGroupState
import com.henrystudio.moneymanager.presentation.views.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MonthlyFragment : Fragment() {
    private var _binding: FragmentMonthlyBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MonthlyAdapter
    private var listMonthlyData: List<MonthlyData> = emptyList()

    // Shared (cross-screen) transaction state + operations
    private val sharedViewModel: SharedTransactionViewModel by activityViewModels()

    // Monthly screen-specific UiState
    private val viewModel: MonthlyViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMonthlyBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.monthlyListSummary.layoutManager = LinearLayoutManager(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    sharedViewModel.combineGroupAndDate.collect { (state, date) ->
                        viewModel.updateMonthlyData(groups = if (state is DataTransactionGroupState.Success)
                            state.data else emptyList(), anchorDate = date)
                    }
                }
                launch {
                    viewModel.uiState.collect { uiState ->
                        listMonthlyData = uiState.monthlyData
                        adapter = MonthlyAdapter(
                            listMonthlyData,
                            onMonthClick = { month ->
                                val activity = requireActivity()
                                if (activity is MainActivity) {
                                    month.isExpanded = !month.isExpanded
                                    val index = listMonthlyData.indexOf(month)
                                    adapter.notifyItemChanged(index)
                                } else if (activity is StatisticListActivity) {
                                    SharedTransactionHolder.currentFilterDate =
                                        Helper.formatDateFromFilterOptionToDateDaily(month.monthStart.toString())
                                    SharedTransactionHolder.filterOption =
                                        FilterOption(FilterPeriodStatistic.Monthly, month.monthStart)
                                    activity.onBackAnimation()
                                }
                            },
                            onWeekClick = { weeklyData ->
                                SharedTransactionHolder.navigateFromMonthly = true
                                sharedViewModel.navigateToWeekFromMonthly(weeklyData.weekStart)
                            }
                        )
                        adapter.updateData(listMonthlyData)
                        binding.monthlyListSummary.adapter = adapter
                        binding.monthlyNoData.visibility =
                            if (uiState.isEmpty) View.VISIBLE else View.GONE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
