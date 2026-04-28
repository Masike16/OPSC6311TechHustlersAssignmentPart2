/**
 * Group: Tech Hustlers
 * Members:
 * - ST10451774 - Acazia Ammon
 * - ST10452404 - Masike Jr Rasenyalo
 * - ST10452409 - Liyema Masala
 */
package com.example.easebudgetv1.viewmodel

import androidx.lifecycle.*
import com.example.easebudgetv1.data.database.entities.*
import com.example.easebudgetv1.data.repository.EaseBudgetRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

// (Author, 2024) ViewModel for budget management operations
@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModel(
    private val repository: EaseBudgetRepository,
    private val currentUserId: Long
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(Pair(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH)))
    val currentMonth: StateFlow<Pair<Int, Int>> = _currentMonth.asStateFlow()

    private val _budgetState = MutableLiveData<BudgetState>(BudgetState.Idle)
    val budgetState: LiveData<BudgetState> = _budgetState

    // Reactive Categories
    val categories: LiveData<List<Category>> = repository.getUserCategories(currentUserId).asLiveData()

    // Reactive Budget Goal based on current month
    val budgetGoal: LiveData<BudgetGoal?> = _currentMonth.flatMapLatest { (year, month) ->
        repository.getBudgetGoalByMonthYearFlow(currentUserId, year, month)
    }.asLiveData()

    // Reactive Ready to Assign - updates whenever budget goal or categories change
    val readyToAssign: LiveData<Double> = budgetGoal.asFlow().flatMapLatest { budget ->
        repository.getUserCategories(currentUserId).map { categoryList ->
            if (budget == null) 0.0
            else {
                val categoryLimits = categoryList.filter { it.monthlyLimit > 0 }.sumOf { it.monthlyLimit }
                budget.monthlyTotalBudget - categoryLimits
            }
        }
    }.asLiveData()

    // Reactive Category Spending with Info
    val categorySpending: LiveData<List<CategorySpendingWithInfo>> = _currentMonth.flatMapLatest { (year, month) ->
        val (startDate, endDate) = repository.getMonthRange(year, month)
        
        combine(
            repository.getSpendingByCategoryFlow(currentUserId, startDate, endDate),
            repository.getUserCategories(currentUserId)
        ) { spendingList, categoryList ->
            categoryList.filter { it.monthlyLimit > 0 }.map { category ->
                val spending = spendingList.find { it.categoryId == category.id }
                val spent = spending?.total ?: 0.0
                CategorySpendingWithInfo(
                    category = category,
                    spent = spent,
                    remaining = category.monthlyLimit - spent,
                    percentage = if (category.monthlyLimit > 0) (spent / category.monthlyLimit * 100).coerceAtMost(100.0) else 0.0
                )
            }
        }
    }.asLiveData()

    fun createOrUpdateBudgetGoal(
        monthlyTotalBudget: Double,
        savingsGoal: Double,
        year: Int = Calendar.getInstance().get(Calendar.YEAR),
        month: Int = Calendar.getInstance().get(Calendar.MONTH)
    ) {
        if (monthlyTotalBudget <= 0.0) {
            _budgetState.value = BudgetState.Error("Monthly budget must be greater than 0")
            return
        }

        _budgetState.value = BudgetState.Loading
        viewModelScope.launch {
            try {
                val existingBudget = repository.getBudgetGoalByMonthYear(currentUserId, year, month)
                
                val budgetGoal = if (existingBudget != null) {
                    existingBudget.copy(
                        monthlyTotalBudget = monthlyTotalBudget,
                        savingsGoal = savingsGoal
                    )
                } else {
                    BudgetGoal(
                        userId = currentUserId,
                        year = year,
                        month = month,
                        monthlyTotalBudget = monthlyTotalBudget,
                        savingsGoal = savingsGoal
                    )
                }
                
                if (existingBudget != null) {
                    repository.updateBudgetGoal(budgetGoal)
                } else {
                    repository.insertBudgetGoal(budgetGoal)
                }
                
                _budgetState.value = BudgetState.Success("Budget goal updated successfully")
            } catch (e: Exception) {
                _budgetState.value = BudgetState.Error("Failed to save budget goal: ${e.message}")
            }
        }
    }

    fun updateCategoryLimit(categoryId: Long, monthlyLimit: Double) {
        if (monthlyLimit < 0.0) {
            _budgetState.value = BudgetState.Error("Category limit cannot be negative")
            return
        }

        viewModelScope.launch {
            try {
                val category = repository.getCategoryById(categoryId)
                if (category != null && category.userId == currentUserId) {
                    val updatedCategory = category.copy(monthlyLimit = monthlyLimit)
                    repository.updateCategory(updatedCategory)
                    _budgetState.value = BudgetState.Success("Category limit updated successfully")
                } else {
                    _budgetState.value = BudgetState.Error("Category not found")
                }
            } catch (e: Exception) {
                _budgetState.value = BudgetState.Error("Failed to update category limit: ${e.message}")
            }
        }
    }

    fun setCurrentMonth(year: Int, month: Int) {
        _currentMonth.value = Pair(year, month)
    }

    fun resetBudgetState() {
        _budgetState.value = BudgetState.Idle
    }
}

data class CategorySpendingWithInfo(
    val category: Category,
    val spent: Double,
    val remaining: Double,
    val percentage: Double
)

sealed class BudgetState {
    object Idle : BudgetState()
    object Loading : BudgetState()
    data class Success(val message: String) : BudgetState()
    data class Error(val message: String) : BudgetState()
}

class BudgetViewModelFactory(
    private val repository: EaseBudgetRepository,
    private val currentUserId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(repository, currentUserId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
