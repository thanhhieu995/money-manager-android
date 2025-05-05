package com.example.moneymanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneymanager.model.AppDatabase
import com.example.moneymanager.model.Transaction
import com.example.moneymanager.model.TransactionDao
import kotlinx.coroutines.launch

class TransactionViewModel(private val dao: TransactionDao) : ViewModel() {
    private val repository = TransactionRepository(dao)
    val allTransactions: LiveData<List<Transaction>> = repository.allTransactions

    fun insert(transaction: Transaction) = viewModelScope.launch {
        repository.insert(transaction)
    }
}