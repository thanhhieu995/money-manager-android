package com.henrystudio.moneymanager.repository

import androidx.lifecycle.LiveData
import com.henrystudio.moneymanager.model.Category
import com.henrystudio.moneymanager.model.CategoryDao
import com.henrystudio.moneymanager.model.CategoryType


class CategoryRepository(private val dao: CategoryDao) {

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

    suspend fun insert(category: Category) {
        dao.insert(category)
    }

    suspend fun delete(category: Category) {
        dao.delete(category)
    }

    suspend fun deleteId(id: Int) {
        dao.deleteId(id)
    }

    suspend fun update(category: Category) {
        dao.update(category)
    }
}