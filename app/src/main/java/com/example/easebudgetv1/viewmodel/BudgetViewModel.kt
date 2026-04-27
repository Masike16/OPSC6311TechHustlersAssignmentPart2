package com.example.easebudgetv1.viewmodel

import androidx.lifecycle.*
import com.example.easebudgetv1.data.database.entities.*
import com.example.easebudgetv1.data.repository.EaseBudgetRepository
import com.example.easebudgetv1.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.*

// (Author, 2024) ViewModel for budget management operations
class BudgetViewModel(
    private val repository: EaseBudgetRepository,
    private val currentUserId: Long
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(Pair(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH)))
    val currentMonth: StateFlow<Pair<Int, Int>> = _currentMonth

    val categories = repository.getUserCategories(currentUserId).asLiveData()

    private val _budgetGoal = MutableLiveData<BudgetGoal?>()
    val budgetGoal: LiveData<BudgetGoal?> = _budgetGoal

    private val _budgetState = MutableLiveData<BudgetState>()
    val budgetState: LiveData<BudgetState> = _budgetState

    private val _readyToAssign = MutableLiveData<Double>()
    val readyToAssign: LiveData<Double> = _readyToAssign

    private val _categorySpending = MutableLiveData<List<CategorySpendingWithInfo>>()
    val categorySpending: LiveData<List<CategorySpendingWithInfo>> = _categorySpending

    init {
        loadCurrentBudget()
    }

    private fun loadCurrentBudget() {
        viewModelScope.launch {
            try {
                val (year, month) = _currentMonth.value
                val budget = repository.getBudgetGoalByMonthYear(currentUserId, year, month)
                _budgetGoal.value = budget
                
                if (budget != null) {
                    calculateReadyToAssign(budget)
                    loadCategorySpending(year, month)
                } else {
                    _readyToAssign.value = 0.0
                    _categorySpending.value = emptyList()
                }
            } catch (e: Exception) {
                _budgetState.value = BudgetState.Error("Failed to load budget: ${e.message}")
            }
        }
    }

    private fun calculateReadyToAssign(budget: BudgetGoal) {
        viewModelScope.launch {
            try {
                val categoryList = repository.getUserCategories(currentUserId).first()
                val categoryLimits = categoryList.filter { it.monthlyLimit > 0 }.sumOf { it.monthlyLimit }
                
                _readyToAssign.value = budget.monthlyTotalBudget - categoryLimits
            } catch (e: Exception) {
                _readyToAssign.value = 0.0
            }
        }
    }

    private suspend fun loadCategorySpending(year: Int, month: Int) {
        try {
            val calendar = Calendar.getInstance()
            calendar.set(year, month, 1, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startDate = calendar.timeInMillis
            
            calendar.add(Calendar.MONTH, 1)
            val endDate = calendar.timeInMillis
            
            val spendingList = repository.getSpendingByCategory(currentUserId, startDate, endDate)
            val categoryList = repository.getUserCategories(currentUserId).first()
            
            val spendingWithInfo = categoryList.filter { it.monthlyLimit > 0 }.map { category ->
                val spending = spendingList.find { it.categoryId == category.id }
                val spent = spending?.total ?: 0.0
                CategorySpendingWithInfo(
                    category = category,
                    spent = spent,
                    remaining = category.monthlyLimit - spent,
                    percentage = if (category.monthlyLimit > 0) (spent / category.monthlyLimit * 100).coerceAtMost(100.0) else 0.0
                )
            }
            
            _categorySpending.postValue(spendingWithInfo)
        } catch (e: Exception) {
            _categorySpending.postValue(emptyList())
        }
    }

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

        if (savingsGoal < 0.0) {
            _budgetState.value = BudgetState.Error("Savings goal cannot be negative")
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
                
                _budgetGoal.value = budgetGoal
                calculateReadyToAssign(budgetGoal)
                loadCategorySpending(year, month)
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
                    
                    val budget = _budgetGoal.value
                    if (budget != null) {
                        calculateReadyToAssign(budget)
                        loadCategorySpending(budget.year, budget.month)
                    }
                    
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
        loadCurrentBudget()
    }

    fun getCurrentMonthBudget(): LiveData<Double> = liveData {
        val (year, month) = _currentMonth.value
        val budget = repository.getBudgetGoalByMonthYear(currentUserId, year, month)
        emit(budget?.monthlyTotalBudget ?: 0.0)
    }

    fun getCurrentMonthSavingsGoal(): LiveData<Double> = liveData {
        val (year, month) = _currentMonth.value
        val budget = repository.getBudgetGoalByMonthYear(currentUserId, year, month)
        emit(budget?.savingsGoal ?: 0.0)
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
