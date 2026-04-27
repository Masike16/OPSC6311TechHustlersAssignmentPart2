package com.example.easebudgetv1.viewmodel

import androidx.lifecycle.*
import com.example.easebudgetv1.data.database.entities.Category
import com.example.easebudgetv1.data.repository.EaseBudgetRepository
import com.example.easebudgetv1.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

// (Author, 2024) ViewModel for reports and analytics operations
class ReportsViewModel(
    private val repository: EaseBudgetRepository,
    private val currentUserId: Long
) : ViewModel() {

    private val _reportFilter = MutableStateFlow(ReportFilter.MONTH)
    val reportFilter: StateFlow<ReportFilter> = _reportFilter

    private val _customDateRange = MutableStateFlow(Pair(System.currentTimeMillis(), System.currentTimeMillis()))
    val customDateRange: StateFlow<Pair<Long, Long>> = _customDateRange

    private val _reportState = MutableLiveData<ReportState>()
    val reportState: LiveData<ReportState> = _reportState

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    val categorySpending = _reportFilter.flatMapLatest { filter ->
        val (start, end) = getDateRangeForFilter(filter)
        repository.getTransactionsByDateRange(currentUserId, start, end)
    }.asLiveData()

    val totalIncome = _reportFilter.flatMapLatest { filter ->
        val (start, end) = getDateRangeForFilter(filter)
        flow {
            emit(repository.getTotalByTypeAndDateRange(currentUserId, "INCOME", start, end) ?: 0.0)
        }
    }.asLiveData()

    val totalExpenses = _reportFilter.flatMapLatest { filter ->
        val (start, end) = getDateRangeForFilter(filter)
        flow {
            emit(repository.getTotalByTypeAndDateRange(currentUserId, "EXPENSE", start, end) ?: 0.0)
        }
    }.asLiveData()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                repository.getUserCategories(currentUserId).collect { categoryList ->
                    _categories.value = categoryList
                }
            } catch (e: Exception) {
                _reportState.value = ReportState.Error("Failed to load categories: ${e.message}")
            }
        }
    }

    private fun getDateRangeForFilter(filter: ReportFilter): Pair<Long, Long> {
        return when (filter) {
            ReportFilter.TODAY -> repository.getTodayRange()
            ReportFilter.WEEK -> repository.getWeekRange()
            ReportFilter.MONTH -> repository.getMonthRange()
            ReportFilter.CUSTOM -> _customDateRange.value
        }
    }

    fun getSpendingByCategory(): LiveData<List<CategorySpendingData>> = liveData {
        try {
            val (start, end) = getDateRangeForFilter(_reportFilter.value)
            val spending = repository.getSpendingByCategory(currentUserId, start, end)
            val categoryList = categories.value ?: emptyList()
            
            val spendingData = spending.mapNotNull { categorySpending ->
                val category = categoryList.find { it.id == categorySpending.categoryId }
                if (category != null) {
                    CategorySpendingData(
                        categoryId = category.id,
                        categoryName = category.name,
                        categoryColor = category.colorHex,
                        amount = categorySpending.total
                    )
                } else null
            }.sortedByDescending { it.amount }
            
            emit(spendingData)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    fun getMonthlyTrends(): LiveData<List<MonthlyTrendData>> = liveData {
        try {
            val trends = mutableListOf<MonthlyTrendData>()
            val calendar = java.util.Calendar.getInstance()
            
            for (i in 5 downTo 0) {
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.add(java.util.Calendar.MONTH, -i)
                calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val startOfMonth = calendar.timeInMillis
                
                calendar.add(java.util.Calendar.MONTH, 1)
                val endOfMonth = calendar.timeInMillis
                
                val income = repository.getTotalByTypeAndDateRange(currentUserId, "INCOME", startOfMonth, endOfMonth) ?: 0.0
                val expenses = repository.getTotalByTypeAndDateRange(currentUserId, "EXPENSE", startOfMonth, endOfMonth) ?: 0.0
                
                trends.add(
                    MonthlyTrendData(
                        month = DateUtils.getMonthName(calendar.get(java.util.Calendar.MONTH) - 1),
                        year = calendar.get(java.util.Calendar.YEAR),
                        income = income,
                        expenses = expenses,
                        savings = income - expenses
                    )
                )
            }
            
            emit(trends)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    fun getTopCategories(): LiveData<List<CategorySpendingData>> = liveData {
        try {
            val (start, end) = getDateRangeForFilter(_reportFilter.value)
            val spending = repository.getSpendingByCategory(currentUserId, start, end)
            val categoryList = categories.value ?: emptyList()
            
            val topCategories = spending.mapNotNull { categorySpending ->
                val category = categoryList.find { it.id == categorySpending.categoryId }
                if (category != null) {
                    CategorySpendingData(
                        categoryId = category.id,
                        categoryName = category.name,
                        categoryColor = category.colorHex,
                        amount = categorySpending.total
                    )
                } else null
            }.sortedByDescending { it.amount }.take(5)
            
            emit(topCategories)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    fun setReportFilter(filter: ReportFilter) {
        _reportFilter.value = filter
    }

    fun setCustomDateRange(startDate: Long, endDate: Long) {
        _customDateRange.value = Pair(startDate, endDate)
        _reportFilter.value = ReportFilter.CUSTOM
    }

    fun getCurrentBalance(): LiveData<Double> = liveData {
        try {
            val (start, end) = getDateRangeForFilter(_reportFilter.value)
            val income = repository.getTotalByTypeAndDateRange(currentUserId, "INCOME", start, end) ?: 0.0
            val expenses = repository.getTotalByTypeAndDateRange(currentUserId, "EXPENSE", start, end) ?: 0.0
            emit(income - expenses)
        } catch (e: Exception) {
            emit(0.0)
        }
    }

    fun getAverageDailySpending(): LiveData<Double> = liveData {
        try {
            val (start, end) = getDateRangeForFilter(_reportFilter.value)
            val expenses = repository.getTotalByTypeAndDateRange(currentUserId, "EXPENSE", start, end) ?: 0.0
            val days = ((end - start) / (24 * 60 * 60 * 1000)).toInt()
            if (days > 0) {
                emit(expenses / days)
            } else {
                emit(0.0)
            }
        } catch (e: Exception) {
            emit(0.0)
        }
    }

    fun resetReportState() {
        _reportState.value = ReportState.Idle
    }
}

enum class ReportFilter {
    TODAY, WEEK, MONTH, CUSTOM
}

data class CategorySpendingData(
    val categoryId: Long,
    val categoryName: String,
    val categoryColor: String,
    val amount: Double
)

data class MonthlyTrendData(
    val month: String,
    val year: Int,
    val income: Double,
    val expenses: Double,
    val savings: Double
)

sealed class ReportState {
    object Idle : ReportState()
    object Loading : ReportState()
    data class Success(val message: String) : ReportState()
    data class Error(val message: String) : ReportState()
}

class ReportsViewModelFactory(
    private val repository: EaseBudgetRepository,
    private val currentUserId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReportsViewModel(repository, currentUserId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
