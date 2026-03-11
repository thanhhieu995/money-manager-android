package com.henrystudio.moneymanager.domain.repository

import androidx.lifecycle.LiveData
import com.henrystudio.moneymanager.data.model.Category
import com.henrystudio.moneymanager.data.model.CategoryType

interface CategoryRepository {

    fun getParentCategories(type: CategoryType): LiveData<List<Category>>

    fun getChildCategories(parentId: Int): LiveData<List<Category>>

    fun getAll(): LiveData<List<Category>>

    fun getCategoriesByType(type: CategoryType): LiveData<List<Category>>

    suspend fun insert(category: Category)

    suspend fun delete(category: Category)

    suspend fun deleteId(id: Int)

    suspend fun update(category: Category)

}