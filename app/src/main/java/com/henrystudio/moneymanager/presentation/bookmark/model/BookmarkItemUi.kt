package com.henrystudio.moneymanager.presentation.bookmark.model

import com.henrystudio.moneymanager.data.model.Transaction

data class BookmarkItemUi(
    val transaction: Transaction,
    val date: String,
    val category: String,
    val content: String,
    val account: String,
    val amount: String,
    val isIncome: Boolean
)
