package com.henrystudio.moneymanager.ui.bottomNavigation.statistic

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.databinding.FragmentStatisticViewPagerBinding

class StatisticViewPagerFragment : Fragment() {
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: StatisticPagerAdapter
    private var _binding: FragmentStatisticViewPagerBinding?= null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatisticViewPagerBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tabLayout = view.findViewById(R.id.fragment_statistic_view_pager_tabLayout)
        viewPager = view.findViewById(R.id.fragment_statistic_viewPager)
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
    }
}