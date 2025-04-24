package com.example.moneymanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneymanager.model.Transaction
import com.example.moneymanager.model.TransactionDao
import kotlinx.coroutines.launch

class TransactionViewModel(private val dao: TransactionDao) : ViewModel() {
    val transactions: LiveData<List<Transaction>> = dao.getAll()

    fun insert(transaction: Transaction) {
        viewModelScope.launch {
            dao.insert(transaction)
        }
    }
}
