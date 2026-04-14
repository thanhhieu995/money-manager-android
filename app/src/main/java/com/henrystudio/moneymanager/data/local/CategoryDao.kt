package com.henrystudio.moneymanager.data.local

import androidx.room.*
import com.henrystudio.moneymanager.data.model.Category
import com.henrystudio.moneymanager.presentation.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE type = :type AND parentId IS NULL")
    fun getParentCategoriesByType(type: TransactionType): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE parentId = :parentId")
    fun getChildCategories(parentId: Int): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE type = :type")
    fun getCategoriesByType(type: TransactionType): Flow<List<Category>>

    @Query("SELECT * FROM categories")
    fun getAll(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<Category>)

    @Delete
    suspend fun delete(category: Category)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteId(id : Int)

    @Update
    suspend fun update(category: Category)

    @Query("SELECT * FROM categories")
    suspend fun getAllOnce(): List<Category>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Category?

    @Query("UPDATE categories SET usageCount = usageCount + 1, lastUsed = :timestamp WHERE id = :categoryId")
    suspend fun increaseUsageCount(categoryId: Int, timestamp: Long)
}