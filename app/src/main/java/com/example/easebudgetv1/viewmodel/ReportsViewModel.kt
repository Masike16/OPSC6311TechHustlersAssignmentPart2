package com.example.easebudgetv1.viewmodel

import androidx.lifecycle.*
import com.example.easebudgetv1.data.database.entities.Category
import com.example.easebudgetv1.data.repository.EaseBudgetRepository
import com.example.easebudgetv1.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// (Author, 2024) ViewModel for reports and analytics operations
class ReportsViewModel(
    private val repository: EaseBudgetRepository,
    private val currentUserId: Long
) : ViewModel() {

    private val _reportFilter = MutableStateFlow(ReportFilter.MONTH)
    val reportFilter = _reportFilter.asStateFlow()

    private val _customDateRange = MutableStateFlow(Pair(System.currentTimeMillis(), System.currentTimeMillis()))
    val customDateRange = _customDateRange.asStateFlow()

    private val _reportState = MutableLiveData<ReportState>()
    val reportState: LiveData<ReportState> = _reportState

    // Categories as LiveData to react to changes
    val categories: LiveData<List<Category>> = repository.getUserCategories(currentUserId).asLiveData()

    // Trigger updates whenever the filter changes
    private val filterTrigger = _reportFilter.asLiveData()

    val totalIncome: LiveData<Double> = filterTrigger.switchMap { filter ->
        liveData {
            val (start, end) = getDateRangeForFilter(filter)
            emit(repository.getTotalByTypeAndDateRange(currentUserId, "INCOME", start, end) ?: 0.0)
        }
    }

    val totalExpenses: LiveData<Double> = filterTrigger.switchMap { filter ->
        liveData {
            val (start, end) = getDateRangeForFilter(filter)
            emit(repository.getTotalByTypeAndDateRange(currentUserId, "EXPENSE", start, end) ?: 0.0)
        }
    }

    val currentBalance: LiveData<Double> = MediatorLiveData<Double>().apply {
        addSource(totalIncome) { income -> value = income - (totalExpenses.value ?: 0.0) }
        addSource(totalExpenses) { expenses -> value = (totalIncome.value ?: 0.0) - expenses }
    }

    // Single source of truth for category spending that reacts to both filter and category changes
    val spendingByCategory: LiveData<List<CategorySpendingData>> = filterTrigger.switchMap { filter ->
        val mediator = MediatorLiveData<List<CategorySpendingData>>()
        
        fun update() {
            viewModelScope.launch {
                val (start, end) = getDateRangeForFilter(filter)
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
                
                mediator.postValue(spendingData)
            }
        }

        mediator.addSource(categories) { update() }
        update()
        
        mediator
    }

    val topCategories: LiveData<List<CategorySpendingData>> = spendingByCategory.map { 
        it.take(5)
    }

    private fun getDateRangeForFilter(filter: ReportFilter): Pair<Long, Long> {
        return when (filter) {
            ReportFilter.TODAY -> repository.getTodayRange()
            ReportFilter.WEEK -> repository.getWeekRange()
            ReportFilter.MONTH -> repository.getMonthRange()
            ReportFilter.CUSTOM -> _customDateRange.value
        }
    }

    fun setReportFilter(filter: ReportFilter) {
        _reportFilter.value = filter
    }

    fun setCustomDateRange(startDate: Long, endDate: Long) {
        _customDateRange.value = Pair(startDate, endDate)
        _reportFilter.value = ReportFilter.CUSTOM
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
