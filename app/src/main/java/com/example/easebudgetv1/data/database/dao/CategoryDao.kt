package com.example.easebudgetv1.data.database.dao

import androidx.room.*
import com.example.easebudgetv1.data.database.entities.Category
import kotlinx.coroutines.flow.Flow

// (Author, 2024) DAO for Category entity operations
@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    fun getUserCategories(userId: Long): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE userId = :userId AND isDefault = 1 ORDER BY name ASC")
    suspend fun getDefaultCategories(userId: Long): List<Category>

    @Query("SELECT * FROM categories WHERE userId = :userId AND isDefault = 0 ORDER BY name ASC")
    suspend fun getCustomCategories(userId: Long): List<Category>

    @Query("SELECT * FROM categories WHERE id = :categoryId LIMIT 1")
    suspend fun findById(categoryId: Long): Category?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<Category>)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteById(categoryId: Long)

    @Query("DELETE FROM categories WHERE userId = :userId")
    suspend fun deleteAllUserCategories(userId: Long)
}
