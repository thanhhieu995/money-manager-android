package com.example.moneymanager.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
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
    private val _currentMonthYear = MutableLiveData(LocalDate.now())
    @RequiresApi(Build.VERSION_CODES.O)
    val currentMonthYear: LiveData<LocalDate> = _currentMonthYear
    private val _currentTabPosition = MutableLiveData<Int>()
    val currentTabPosition: LiveData<Int> get() = _currentTabPosition
    private val _selectedTransactionIds = MutableLiveData<Set<Int>>(emptySet())
    val selectedTransactionIds: LiveData<Set<Int>> = _selectedTransactionIds
    private val _selectionMode = MutableLiveData<Boolean>(false)
    val selectionMode: LiveData<Boolean> = _selectionMode
    private val _selectedTransactions = MutableLiveData<List<Transaction>>(emptyList())
    val selectedTransactions: LiveData<List<Transaction>> = _selectedTransactions

    fun insert(transaction: Transaction) = viewModelScope.launch {
        repository.insert(transaction)
    }

    fun delete(transaction: Transaction) = viewModelScope.launch {
        dao.delete(transaction)
    }

    fun update(transaction: Transaction) {
        viewModelScope.launch {
            dao.update(transaction)
        }
    }

    fun getBookmarkedTransactions(): LiveData<List<Transaction>> {
        return dao.getBookmarkedTransactions()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun changeMonth(offset: Long) {
        _currentMonthYear.value = _currentMonthYear.value?.plusMonths(offset)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun changeYear(offset: Long) {
        _currentMonthYear.value = _currentMonthYear.value?.plusYears(offset)
    }

    fun setCurrentTab(position: Int) {
        _currentTabPosition.value = position
    }

    fun toggleTransactionSelection(transactionId: Int) {
        val current = _selectedTransactionIds.value ?: emptySet()
        _selectedTransactionIds.value = if (current.contains(transactionId)) {
            current - transactionId
        } else {
            current + transactionId
        }
    }

    private fun clearSelection() {
        _selectedTransactionIds.value = emptySet()
    }

    fun enterSelectionMode() {
        _selectionMode.value = true
    }

    fun exitSelectionMode() {
        _selectionMode.value = false
        clearSelection()
    }

//    fun updateSelection(list: List<Transaction>) {
//        _selectedTransactions.value = list
//        _selectionMode.value = list.isNotEmpty()
//    }
}