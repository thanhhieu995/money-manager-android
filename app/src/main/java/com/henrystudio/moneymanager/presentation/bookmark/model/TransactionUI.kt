package com.henrystudio.moneymanager.presentation.bookmark.model

import com.henrystudio.moneymanager.data.model.Transaction

data class TransactionUI(
    val transaction: Transaction,
    val categoryLabel: String
)
