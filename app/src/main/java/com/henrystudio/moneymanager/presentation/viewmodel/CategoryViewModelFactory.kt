package com.henrystudio.moneymanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.henrystudio.moneymanager.domain.usecase.category.CategoryUseCases

class CategoryViewModelFactory(private val categoryUseCases: CategoryUseCases) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CategoryViewModel(categoryUseCases) as T
    }
}