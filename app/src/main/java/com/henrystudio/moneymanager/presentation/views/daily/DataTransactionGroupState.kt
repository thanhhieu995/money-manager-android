package com.henrystudio.moneymanager.presentation.views.daily

sealed class DataTransactionGroupState<out T> {
    object Loading: DataTransactionGroupState<Nothing>()
    object Empty: DataTransactionGroupState<Nothing>()
    data class Success<out T>(val data: T): DataTransactionGroupState<T>()
    data class Error(val message: String) : DataTransactionGroupState<Nothing>()
}