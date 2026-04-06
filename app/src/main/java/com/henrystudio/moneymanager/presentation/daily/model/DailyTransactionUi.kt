package com.henrystudio.moneymanager.presentation.daily.model

import com.henrystudio.moneymanager.data.model.Transaction

data class DailyTransactionUi(
    val transaction: Transaction,
    val isSelected: Boolean
)
