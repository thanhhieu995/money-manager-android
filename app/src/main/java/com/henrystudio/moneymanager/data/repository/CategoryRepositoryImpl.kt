package com.henrystudio.moneymanager.data.repository

import androidx.lifecycle.LiveData
import com.henrystudio.moneymanager.data.model.Category
import com.henrystudio.moneymanager.data.local.CategoryDao
import com.henrystudio.moneymanager.domain.repository.CategoryRepository
import com.henrystudio.moneymanager.data.model.CategoryType


class CategoryRepositoryImpl(private val dao: CategoryDao) : CategoryRepository{

    override fun getParentCategories(type: CategoryType): LiveData<List<Category>> {
        return dao.getParentCategoriesByType(type)
    }

    override fun getChildCategories(parentId: Int): LiveData<List<Category>> {
        return dao.getChildCategories(parentId)
    }

    override fun getAll() : LiveData<List<Category>> {
        return dao.getAll()
    }

    override  fun getCategoriesByType(type: CategoryType): LiveData<List<Category>> {
        return dao.getCategoriesByType(type)
    }

    override suspend fun insert(category: Category) {
        dao.insert(category)
    }

    override  suspend fun delete(category: Category) {
        dao.delete(category)
    }

    override suspend fun deleteId(id: Int) {
        dao.deleteId(id)
    }

    override suspend fun update(category: Category) {
        dao.update(category)
    }
}