package com.henrystudio.moneymanager.ui.bottomNavigation.statistic

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.databinding.FragmentStatisticViewPagerBinding
import com.henrystudio.moneymanager.model.AppDatabase
import com.henrystudio.moneymanager.ui.search.FilterPeriod
import com.henrystudio.moneymanager.viewmodel.TransactionViewModel
import com.henrystudio.moneymanager.viewmodel.TransactionViewModelFactory
import java.time.LocalDate

class StatisticViewPagerFragment : Fragment() {
    private lateinit var tabLayout: TabLayout
    private lateinit var filterDropdown: TextView
    private lateinit var viewPager: ViewPager2
    private var selectedOption: String = "Monthly"
    private lateinit var adapter: StatisticPagerAdapter
    private var _binding: FragmentStatisticViewPagerBinding?= null
    private val binding get() = _binding!!

    private val viewModel : TransactionViewModel by activityViewModels {
        TransactionViewModelFactory(AppDatabase.getDatabase(requireActivity().application).transactionDao())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatisticViewPagerBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        init()
        adapter = StatisticPagerAdapter(this)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) {tab, position ->
            tab.text = when (position) {
                0 -> "Stats"
                1 -> "Budget"
                2 -> "Note"
                else -> ""
            }
        }.attach()

        filterDropdown.setOnClickListener {
            showDialogOption()
        }
    }

    fun init() {
        tabLayout = binding.fragmentStatisticViewPagerTabLayout
        viewPager = binding.fragmentStatisticViewPager
        filterDropdown = binding.statisticFilterDropdown
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDialogOption() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.item_search_arrange, null)
        bottomSheetDialog.setContentView(view)
        val checkViews = mapOf(
            "All" to view.findViewById<ImageView>(R.id.search_optionTotalCheck),
            "Weekly" to view.findViewById<ImageView>(R.id.search_optionWeeklyCheck),
            "Monthly" to view.findViewById<ImageView>(R.id.search_optionMonthlyCheck),
            "Yearly" to view.findViewById<ImageView>(R.id.search_optionYearlyCheck),
        )
        val optionConfigs = listOf(
            Triple("All", R.id.optionTotalLayout, FilterPeriod.All),
            Triple("Weekly", R.id.optionWeeklyLayout, FilterPeriod.Weekly),
            Triple("Monthly", R.id.optionMonthlyLayout, FilterPeriod.Monthly),
            Triple("Yearly", R.id.optionYearlyLayout, FilterPeriod.Yearly)
        )

        fun updateCheckMarks(selected: String) {
            filterDropdown.text = selected
            checkViews.forEach { (option, imageView) ->
                imageView.visibility = if (option == selected) View.VISIBLE else View.GONE
            }
        }
        updateCheckMarks(selectedOption) // cập nhật ban đầu
        optionConfigs.forEach { (optionName, layoutId, filterPeriod) ->
            view.findViewById<LinearLayout>(layoutId).setOnClickListener {
                selectedOption = optionName
                updateCheckMarks(optionName)
                viewModel.setFilter(filterPeriod, LocalDate.now())
            }
        }
        view.findViewById<TextView>(R.id.search_optionCancel).setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }
}