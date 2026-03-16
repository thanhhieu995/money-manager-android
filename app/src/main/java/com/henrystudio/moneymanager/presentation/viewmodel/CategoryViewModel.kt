package com.henrystudio.moneymanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henrystudio.moneymanager.data.model.Category
import com.henrystudio.moneymanager.data.model.CategoryType
import com.henrystudio.moneymanager.domain.usecase.category.CategoryUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor (private val categoryUseCases: CategoryUseCases) : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    fun getParentCategories(type: CategoryType): StateFlow<List<Category>> =
        categoryUseCases.getParentCategories(type).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getChildCategories(parentId: Int): StateFlow<List<Category>> =
        categoryUseCases.getChildCategories(parentId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getAll(): StateFlow<List<Category>> =
        categoryUseCases.getAllCategories().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getCategoriesByType(type: CategoryType): StateFlow<List<Category>> =
        categoryUseCases.getCategoriesByType(type).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insert(category: Category) = viewModelScope.launch {
        categoryUseCases.insertCategory(category)
    }

    fun delete(category: Category) = viewModelScope.launch {
        categoryUseCases.deleteCategory(category)
    }

    fun deleteId(id: Int) = viewModelScope.launch {
        categoryUseCases.deleteCategoryById(id)
    }

    fun update(category: Category) = viewModelScope.launch {
        categoryUseCases.updateCategory(category)
    }

    fun updateChildren(children: List<Category>) = viewModelScope.launch {
        children.forEach{categoryUseCases.updateCategory(it)}
    }
}
