package com.example.moneymanager.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE type = :type")
    fun getCategoriesByType(type: CategoryType): LiveData<List<Category>>

    @Query("SELECT * FROM categories")
    fun getAll(): LiveData<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Update
    suspend fun update(category: Category)
}