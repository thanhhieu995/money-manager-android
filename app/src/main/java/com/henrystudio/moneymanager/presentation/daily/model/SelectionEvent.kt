package com.henrystudio.moneymanager.presentation.daily.model

import com.henrystudio.moneymanager.data.model.Transaction

sealed class SelectionEvent {
    data class Selected(val tx: Transaction)
    data class DeSelected(val tx: Transaction)
}
