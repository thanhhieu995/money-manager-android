package com.henrystudio.moneymanager.presentation.views.addtransaction

sealed class SaveResult {

    data class Success(
        val closeAfterSave: Boolean
    ) : SaveResult()

    data class Error(
        val message: String
    ) : SaveResult()
}