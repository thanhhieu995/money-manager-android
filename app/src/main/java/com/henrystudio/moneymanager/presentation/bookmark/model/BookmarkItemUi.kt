package com.henrystudio.moneymanager.presentation.bookmark.model

import com.henrystudio.moneymanager.data.model.Transaction
import java.time.LocalDate

data class BookmarkItemUi(
    val transaction: Transaction,
    val date: LocalDate,
    val category: String,
    val content: String,
    val account: String,
    val amount: Long,
    val isIncome: Boolean
)
