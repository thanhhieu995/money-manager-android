package com.henrystudio.moneymanager.presentation.views.addtransaction

sealed class AddTransactionEvent {
    object NavigateBackToDaily: AddTransactionEvent()
}