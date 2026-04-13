package com.henrystudio.moneymanager.presentation.model

import com.henrystudio.moneymanager.data.model.Transaction

data class SaveTransactionParams(
    val amount: String,
    val categoryParent: String,
    val categoryChild: String,
    val account: String,
    val note: String,
    val date: String,
    val isIncome: Boolean,
    val existing: Transaction?,
    val closeAfterSave: Boolean
)
