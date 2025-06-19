package com.example.moneymanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneymanager.model.Category
import com.example.moneymanager.model.CategoryDao
import com.example.moneymanager.model.CategoryType
import kotlinx.coroutines.launch

class CategoryViewModel(private val dao: CategoryDao) : ViewModel() {
    fun getParentCategories(type: CategoryType): LiveData<List<Category>> {
        return dao.getParentCategoriesByType(type)
    }

    fun getChildCategories(parentId: Int): LiveData<List<Category>> {
        return dao.getChildCategories(parentId)
    }

    fun getAll() : LiveData<List<Category>> {
        return dao.getAll()
    }

    fun getCategoriesByType(type: CategoryType): LiveData<List<Category>> {
        return dao.getCategoriesByType(type)
    }

    fun insert(category: Category) = viewModelScope.launch {
        dao.insert(category)
    }

    fun delete(category: Category) = viewModelScope.launch {
        dao.delete(category)
    }

    fun update(category: Category) = viewModelScope.launch {
        dao.update(category)
    }
}
