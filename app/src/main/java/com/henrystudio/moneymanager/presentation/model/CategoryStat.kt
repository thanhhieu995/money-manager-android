package com.henrystudio.moneymanager.presentation.model

import java.io.Serializable

data class CategoryStat(
    val name: String,
    val percent: Float,
    val amount: Float,
    val color: Int
) : Serializable

