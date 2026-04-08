package com.henrystudio.moneymanager.presentation.viewmodel

import com.henrystudio.moneymanager.data.model.Transaction

sealed class TransactionAction{
    data class OnTransactionClick(val transaction: Transaction) : TransactionAction()
    data class OnTransactionLongClick(val transaction: Transaction) : TransactionAction()
}
