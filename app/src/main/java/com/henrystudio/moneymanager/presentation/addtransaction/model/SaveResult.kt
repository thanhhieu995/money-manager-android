package com.henrystudio.moneymanager.presentation.addtransaction.model

sealed class SaveResult {

    data class Success(
        val closeAfterSave: Boolean
    ) : SaveResult()

    data class Error(
        val message: String
    ) : SaveResult()
}