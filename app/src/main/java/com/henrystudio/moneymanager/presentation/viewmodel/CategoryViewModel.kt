package com.henrystudio.moneymanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henrystudio.moneymanager.data.model.Category
import com.henrystudio.moneymanager.data.model.CategoryType
import com.henrystudio.moneymanager.domain.usecase.category.CategoryUseCases
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CategoryViewModel(private val categoryUseCases: CategoryUseCases) : ViewModel() {

    fun getParentCategories(type: CategoryType): Flow<List<Category>> {
        return categoryUseCases.getParentCategories(type)
    }

    fun getChildCategories(parentId: Int): Flow<List<Category>> {
        return categoryUseCases.getChildCategories(parentId)
    }

    fun getAll() : Flow<List<Category>> {
        return categoryUseCases.getAllCategories()
    }

    fun getCategoriesByType(type: CategoryType): Flow<List<Category>> {
        return categoryUseCases.getCategoriesByType(type)
    }

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
