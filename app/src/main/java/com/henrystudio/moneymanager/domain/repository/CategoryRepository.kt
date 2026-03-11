package com.henrystudio.moneymanager.domain.repository

import com.henrystudio.moneymanager.data.model.Category
import com.henrystudio.moneymanager.data.model.CategoryType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun getParentCategories(type: CategoryType): Flow<List<Category>>

    fun getChildCategories(parentId: Int): Flow<List<Category>>

    fun getAll(): Flow<List<Category>>

    fun getCategoriesByType(type: CategoryType): Flow<List<Category>>

    suspend fun insert(category: Category)

    suspend fun delete(category: Category)

    suspend fun deleteId(id: Int)

    suspend fun update(category: Category)

}