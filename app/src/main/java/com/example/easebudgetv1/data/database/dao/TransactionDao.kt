package com.example.easebudgetv1.data.database.dao

import androidx.room.*
import com.example.easebudgetv1.data.database.entities.Transaction
import kotlinx.coroutines.flow.Flow

// (Author, 2024) Data Access Object for transaction operations
@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getUserTransactions(userId: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(userId: Long, startDate: Long, endDate: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND categoryId = :categoryId ORDER BY date DESC")
    fun getTransactionsByCategory(userId: Long, categoryId: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND type = :type ORDER BY date DESC")
    fun getTransactionsByType(userId: Long, type: String): Flow<List<Transaction>>

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = :type AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalByTypeAndDateRange(userId: Long, type: String, startDate: Long, endDate: Long): Double?

    @Query("SELECT categoryId, SUM(amount) as total FROM transactions WHERE userId = :userId AND date BETWEEN :startDate AND :endDate AND type = 'EXPENSE' GROUP BY categoryId")
    suspend fun getSpendingByCategory(userId: Long, startDate: Long, endDate: Long): List<CategorySpending>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun findById(id: Long): Transaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM transactions WHERE userId = :userId")
    suspend fun deleteAllUserTransactions(userId: Long)
}

data class CategorySpending(
    val categoryId: Long?,
    val total: Double
)
