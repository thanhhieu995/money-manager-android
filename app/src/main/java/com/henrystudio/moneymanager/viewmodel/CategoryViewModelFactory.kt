package com.henrystudio.moneymanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.henrystudio.moneymanager.features.transaction.data.local.CategoryDao

class CategoryViewModelFactory(private val dao: CategoryDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CategoryViewModel(dao) as T
    }
}