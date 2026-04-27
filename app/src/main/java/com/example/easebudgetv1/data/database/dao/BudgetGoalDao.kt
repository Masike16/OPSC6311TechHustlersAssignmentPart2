package com.example.easebudgetv1.data.database.dao

import androidx.room.*
import com.example.easebudgetv1.data.database.entities.BudgetGoal
import kotlinx.coroutines.flow.Flow

// (Author, 2024) DAO for BudgetGoal entity operations
@Dao
interface BudgetGoalDao {
    @Query("SELECT * FROM budget_goals WHERE userId = :userId ORDER BY year DESC, month DESC")
    fun getUserBudgetGoals(userId: Long): Flow<List<BudgetGoal>>

    @Query("SELECT * FROM budget_goals WHERE userId = :userId AND year = :year AND month = :month LIMIT 1")
    suspend fun findByMonthYear(userId: Long, year: Int, month: Int): BudgetGoal?

    @Query("SELECT * FROM budget_goals WHERE id = :budgetId LIMIT 1")
    suspend fun findById(budgetId: Long): BudgetGoal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budgetGoal: BudgetGoal): Long

    @Update
    suspend fun update(budgetGoal: BudgetGoal)

    @Delete
    suspend fun delete(budgetGoal: BudgetGoal)

    @Query("DELETE FROM budget_goals WHERE id = :budgetId")
    suspend fun deleteById(budgetId: Long)

    @Query("DELETE FROM budget_goals WHERE userId = :userId")
    suspend fun deleteAllUserBudgetGoals(userId: Long)
}
