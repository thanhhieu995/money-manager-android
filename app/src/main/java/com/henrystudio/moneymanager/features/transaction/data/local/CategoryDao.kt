package com.henrystudio.moneymanager.features.transaction.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.henrystudio.moneymanager.model.Category
import com.henrystudio.moneymanager.model.CategoryType

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE type = :type AND parentId IS NULL")
    fun getParentCategoriesByType(type: CategoryType): LiveData<List<Category>>

    @Query("SELECT * FROM categories WHERE parentId = :parentId")
    fun getChildCategories(parentId: Int): LiveData<List<Category>>

    @Query("SELECT * FROM categories WHERE type = :type")
    fun getCategoriesByType(type: CategoryType): LiveData<List<Category>>

    @Query("SELECT * FROM categories")
    fun getAll(): LiveData<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteId(id : Int)

    @Update
    suspend fun update(category: Category)
}