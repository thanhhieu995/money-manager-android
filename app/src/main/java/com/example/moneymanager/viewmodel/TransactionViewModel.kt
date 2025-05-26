package com.example.moneymanager.viewmodel

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.example.moneymanager.model.AppDatabase
import com.example.moneymanager.model.Transaction
import com.example.moneymanager.model.TransactionDao
import com.example.moneymanager.model.TransactionGroup
import kotlinx.coroutines.launch
import java.time.LocalDate

class TransactionViewModel(private val dao: TransactionDao) : ViewModel() {
    private val repository = TransactionRepository(dao)
    val allTransactions: LiveData<List<Transaction>> = repository.allTransactions
    val groupedTransactions: LiveData<List<TransactionGroup>> = repository.getGroupedTransactions()
    @RequiresApi(Build.VERSION_CODES.O)
    val _currentMonthYear = MutableLiveData(LocalDate.now())
    @RequiresApi(Build.VERSION_CODES.O)
    val currentMonthYear: LiveData<LocalDate> = _currentMonthYear

    fun insert(transaction: Transaction) = viewModelScope.launch {
        repository.insert(transaction)
    }

    fun delete(transaction: Transaction) = viewModelScope.launch {
        dao.delete(transaction)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun changeMonth(offset: Long) {
        _currentMonthYear.value = _currentMonthYear.value?.plusMonths(offset)
    }
}