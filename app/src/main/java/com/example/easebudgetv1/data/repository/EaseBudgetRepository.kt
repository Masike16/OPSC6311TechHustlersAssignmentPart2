package com.example.easebudgetv1.data.repository

import androidx.annotation.WorkerThread
import com.example.easebudgetv1.data.database.dao.*
import com.example.easebudgetv1.data.database.entities.*
import kotlinx.coroutines.flow.Flow
import java.util.*

// (Author, 2024) Repository for EasEBudget data operations
class EaseBudgetRepository(
    private val userDao: UserDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val budgetGoalDao: BudgetGoalDao
) {

    // User operations
    @WorkerThread
    suspend fun insertUser(user: User): Long = userDao.insert(user)

    @WorkerThread
    suspend fun getUserByUsername(username: String): User? = userDao.findByUsername(username)

    @WorkerThread
    suspend fun getUserById(userId: Long): User? = userDao.findById(userId)

    @WorkerThread
    suspend fun updateUser(user: User) = userDao.update(user)

    @WorkerThread
    suspend fun deleteUser(user: User) = userDao.delete(user)

    @WorkerThread
    suspend fun deleteUserById(userId: Long) = userDao.deleteById(userId.toInt())

    // Category operations
    fun getUserCategories(userId: Long): Flow<List<Category>> = categoryDao.getUserCategories(userId)

    @WorkerThread
    suspend fun getDefaultCategories(userId: Long): List<Category> = categoryDao.getDefaultCategories(userId)

    @WorkerThread
    suspend fun getCustomCategories(userId: Long): List<Category> = categoryDao.getCustomCategories(userId)

    @WorkerThread
    suspend fun getCategoryById(categoryId: Long): Category? = categoryDao.findById(categoryId)

    @WorkerThread
    suspend fun insertCategory(category: Category): Long = categoryDao.insert(category)

    @WorkerThread
    suspend fun insertCategories(categories: List<Category>) = categoryDao.insertAll(categories)

    @WorkerThread
    suspend fun updateCategory(category: Category) = categoryDao.update(category)

    @WorkerThread
    suspend fun deleteCategory(category: Category) = categoryDao.delete(category)

    @WorkerThread
    suspend fun deleteCategoryById(categoryId: Long) = categoryDao.deleteById(categoryId)

    @WorkerThread
    suspend fun deleteAllUserCategories(userId: Long) = categoryDao.deleteAllUserCategories(userId)

    // Transaction operations
    fun getUserTransactions(userId: Long): Flow<List<Transaction>> = transactionDao.getUserTransactions(userId)

    fun getTransactionsByDateRange(userId: Long, startDate: Long, endDate: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByDateRange(userId, startDate, endDate)

    fun getTransactionsByCategory(userId: Long, categoryId: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByCategory(userId, categoryId)

    fun getTransactionsByType(userId: Long, type: String): Flow<List<Transaction>> =
        transactionDao.getTransactionsByType(userId, type)

    @WorkerThread
    suspend fun getTotalByTypeAndDateRange(userId: Long, type: String, startDate: Long, endDate: Long): Double? =
        transactionDao.getTotalByTypeAndDateRange(userId, type, startDate, endDate)

    @WorkerThread
    suspend fun getSpendingByCategory(userId: Long, startDate: Long, endDate: Long): List<CategorySpending> =
        transactionDao.getSpendingByCategory(userId, startDate, endDate)
        
    fun getSpendingByCategoryFlow(userId: Long, startDate: Long, endDate: Long): Flow<List<CategorySpending>> = kotlinx.coroutines.flow.flow {
        // Since getSpendingByCategory is a suspend function returning a list, we wrap it.
        // In a real reactive app, TransactionDao would return Flow<List<CategorySpending>> directly.
        // But we can approximate it by reacting to any transaction changes.
        transactionDao.getUserTransactions(userId).collect {
            emit(transactionDao.getSpendingByCategory(userId, startDate, endDate))
        }
    }

    @WorkerThread
    suspend fun getTransactionById(transactionId: Long): Transaction? = transactionDao.findById(transactionId)

    @WorkerThread
    suspend fun insertTransaction(transaction: Transaction): Long = transactionDao.insert(transaction)

    @WorkerThread
    suspend fun updateTransaction(transaction: Transaction) = transactionDao.update(transaction)

    @WorkerThread
    suspend fun deleteTransaction(transaction: Transaction) = transactionDao.delete(transaction)

    @WorkerThread
    suspend fun deleteTransactionById(transactionId: Long) = transactionDao.deleteById(transactionId)

    @WorkerThread
    suspend fun deleteAllUserTransactions(userId: Long) = transactionDao.deleteAllUserTransactions(userId)

    // Budget Goal operations
    fun getUserBudgetGoals(userId: Long): Flow<List<BudgetGoal>> = budgetGoalDao.getUserBudgetGoals(userId)

    @WorkerThread
    suspend fun getBudgetGoalByMonthYear(userId: Long, year: Int, month: Int): BudgetGoal? =
        budgetGoalDao.findByMonthYear(userId, year, month)
        
    fun getBudgetGoalByMonthYearFlow(userId: Long, year: Int, month: Int): Flow<BudgetGoal?> =
        budgetGoalDao.findByMonthYearFlow(userId, year, month)

    @WorkerThread
    suspend fun getBudgetGoalById(budgetId: Long): BudgetGoal? = budgetGoalDao.findById(budgetId)

    @WorkerThread
    suspend fun insertBudgetGoal(budgetGoal: BudgetGoal): Long = budgetGoalDao.insert(budgetGoal)

    @WorkerThread
    suspend fun updateBudgetGoal(budgetGoal: BudgetGoal) = budgetGoalDao.update(budgetGoal)

    @WorkerThread
    suspend fun deleteBudgetGoal(budgetGoal: BudgetGoal) = budgetGoalDao.delete(budgetGoal)

    @WorkerThread
    suspend fun deleteBudgetGoalById(budgetId: Long) = budgetGoalDao.deleteById(budgetId)

    @WorkerThread
    suspend fun deleteAllUserBudgetGoals(userId: Long) = budgetGoalDao.deleteAllUserBudgetGoals(userId)

    // Helper methods for date ranges
    fun getTodayRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endOfDay = calendar.timeInMillis
        
        return Pair(startOfDay, endOfDay)
    }

    fun getWeekRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfWeek = calendar.timeInMillis
        
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val endOfWeek = calendar.timeInMillis
        
        return Pair(startOfWeek, endOfWeek)
    }

    fun getMonthRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis
        
        calendar.add(Calendar.MONTH, 1)
        val endOfMonth = calendar.timeInMillis
        
        return Pair(startOfMonth, endOfMonth)
    }
    
    fun getMonthRange(year: Int, month: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis
        
        calendar.add(Calendar.MONTH, 1)
        val endOfMonth = calendar.timeInMillis
        
        return Pair(startOfMonth, endOfMonth)
    }
}
