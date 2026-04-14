package com.henrystudio.moneymanager.data.repository

import com.henrystudio.moneymanager.data.model.Category
import com.henrystudio.moneymanager.data.local.CategoryDao
import com.henrystudio.moneymanager.domain.repository.CategoryRepository
import com.henrystudio.moneymanager.presentation.model.TransactionType
import kotlinx.coroutines.flow.Flow


class CategoryRepositoryImpl(private val dao: CategoryDao) : CategoryRepository{

    override fun getParentCategories(type: TransactionType): Flow<List<Category>> {
        return dao.getParentCategoriesByType(type)
    }

    override fun getChildCategories(parentId: Int): Flow<List<Category>> {
        return dao.getChildCategories(parentId)
    }

    override fun getAll() : Flow<List<Category>> {
        return dao.getAll()
    }

    override  fun getCategoriesByType(type: TransactionType): Flow<List<Category>> {
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

    override suspend fun increaseUsageCount(categoryId: Int) {
        dao.increaseUsageCount(categoryId, System.currentTimeMillis())
    }

    override suspend fun getById(id: Int): Category? {
        return dao.getById(id)
    }
}