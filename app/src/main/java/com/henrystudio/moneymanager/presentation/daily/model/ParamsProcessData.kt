package com.henrystudio.moneymanager.presentation.daily.model

import com.henrystudio.moneymanager.presentation.model.KeyFilter
import com.henrystudio.moneymanager.presentation.model.TransactionType

data class ParamsProcessData(
    var categoryName: String? = null,
    val transactionType: TransactionType? = null,
    val keyFilter: KeyFilter? = null,
    val isFromMainActivity: Boolean = false
) {
    fun isReady(): Boolean {
        return transactionType != null
    }
}
