package com.henrystudio.moneymanager.presentation.views.bottomNavigation.statistic

import com.henrystudio.moneymanager.data.model.CategoryType
import com.henrystudio.moneymanager.presentation.model.CategoryStat
import com.henrystudio.moneymanager.presentation.model.FilterOption
import com.henrystudio.moneymanager.presentation.model.FilterPeriodStatistic
import com.henrystudio.moneymanager.presentation.model.KeyFilter
import com.henrystudio.moneymanager.presentation.model.LineChartPoint
import java.time.LocalDate

data class StatisticCategoryUiState(
    val categoryName: String = "",
    val categoryType: CategoryType = CategoryType.EXPENSE,
    val filterOption: FilterOption = FilterOption(
        FilterPeriodStatistic.Monthly,
        LocalDate.now()
    ),
    val keyFilter: KeyFilter? = null,
    val chartPoints: List<LineChartPoint> = emptyList(),
    val selectedChartIndex: Int = 0,
    val childCategoryStats: List<CategoryStat> = emptyList(),
    val showCategorySumView: Boolean = false,
    val showDailyContainer: Boolean = false,
    val filterOptionPushChild: FilterOption = FilterOption(
        FilterPeriodStatistic.Monthly,
        LocalDate.now()
    ),
    val categorySumName: String = "",
    val categorySumAmount: Double = 0.0,
    val chartLabelText: String = "",
    val canGoPrev: Boolean = false,
    val canGoNext: Boolean = false,
    val parentId: Int = -1
)
