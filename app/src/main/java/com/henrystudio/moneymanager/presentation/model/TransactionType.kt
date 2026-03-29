package com.henrystudio.moneymanager.presentation.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class TransactionType : Parcelable {
    INCOME,
    EXPENSE
}