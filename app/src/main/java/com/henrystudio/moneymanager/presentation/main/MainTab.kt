package com.henrystudio.moneymanager.presentation.main

sealed class MainTab(val route: String) {
    object Daily : MainTab("daily")
    object Stats : MainTab("stats")
    object More : MainTab("more")

    companion object {
        fun fromRoute(route: String?): MainTab {
            return when (route) {
                Daily.route -> Daily
                Stats.route -> Stats
                More.route -> More
                else -> Daily
            }
        }
    }
}