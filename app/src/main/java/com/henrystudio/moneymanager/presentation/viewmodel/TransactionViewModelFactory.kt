package com.henrystudio.moneymanager.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.henrystudio.moneymanager.data.repository.TransactionRepositoryImpl
import com.henrystudio.moneymanager.domain.usecase.transaction.TransactionUseCases

class TransactionViewModelFactory(
    private val transactionUseCases: TransactionUseCases
) : ViewModelProvider.Factory {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            return TransactionViewModel(transactionUseCases) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}