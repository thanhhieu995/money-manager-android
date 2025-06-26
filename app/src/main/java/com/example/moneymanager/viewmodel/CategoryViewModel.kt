package com.example.moneymanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneymanager.model.Category
import com.example.moneymanager.model.CategoryDao
import com.example.moneymanager.model.CategoryType
import kotlinx.coroutines.launch

class CategoryViewModel(private val dao: CategoryDao) : ViewModel() {

    private val repository = CategoryRepository(dao)

    fun getParentCategories(type: CategoryType): LiveData<List<Category>> {
        return repository.getParentCategories(type)
    }

    fun getChildCategories(parentId: Int): LiveData<List<Category>> {
        return repository.getChildCategories(parentId)
    }

    fun getAll() : LiveData<List<Category>> {
        return repository.getAll()
    }

    fun getCategoriesByType(type: CategoryType): LiveData<List<Category>> {
        return repository.getCategoriesByType(type)
    }

    fun insert(category: Category) = viewModelScope.launch {
        repository.insert(category)
    }

    fun delete(category: Category) = viewModelScope.launch {
        repository.delete(category)
    }

    fun deleteId(id: Int) = viewModelScope.launch {
        repository.deleteId(id)
    }

    fun update(category: Category) = viewModelScope.launch {
        repository.update(category)
    }
}
