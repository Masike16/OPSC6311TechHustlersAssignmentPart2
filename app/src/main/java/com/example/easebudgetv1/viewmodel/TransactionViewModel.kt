package com.example.easebudgetv1.viewmodel

import androidx.lifecycle.*
import androidx.lifecycle.asLiveData
import com.example.easebudgetv1.data.database.dao.CategorySpending
import com.example.easebudgetv1.data.database.entities.*
import com.example.easebudgetv1.data.repository.EaseBudgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

// (Author, 2024) ViewModel for transaction management operations
class TransactionViewModel(
    private val repository: EaseBudgetRepository,
    private val currentUserId: Long
) : ViewModel() {

    private val _dateFilter = MutableStateFlow(DateFilter.MONTH)
    val dateFilter: StateFlow<DateFilter> = _dateFilter

    private val _selectedCategory = MutableStateFlow<Long?>(null)
    val selectedCategory: StateFlow<Long?> = _selectedCategory

    private val _transactionState = MutableLiveData<TransactionState>()
    val transactionState: LiveData<TransactionState> = _transactionState

    val categories: LiveData<List<Category>> = repository.getUserCategories(currentUserId).asLiveData()

    val transactions: LiveData<List<Transaction>> = _dateFilter.flatMapLatest { filter ->
        when (filter) {
            DateFilter.TODAY -> {
                val (start, end) = repository.getTodayRange()
                repository.getTransactionsByDateRange(currentUserId, start, end)
            }
            DateFilter.WEEK -> {
                val (start, end) = repository.getWeekRange()
                repository.getTransactionsByDateRange(currentUserId, start, end)
            }
            DateFilter.MONTH -> {
                val (start, end) = repository.getMonthRange()
                repository.getTransactionsByDateRange(currentUserId, start, end)
            }
            DateFilter.CUSTOM -> {
                // For custom, we might need more state, or use a default
                val (start, end) = repository.getMonthRange()
                repository.getTransactionsByDateRange(currentUserId, start, end)
            }
        }
    }.asLiveData()

    fun setDateFilter(filter: DateFilter) {
        _dateFilter.value = filter
    }

    fun addTransaction(transaction: Transaction) {
        _transactionState.value = TransactionState.Loading
        viewModelScope.launch {
            try {
                repository.insertTransaction(transaction)
                _transactionState.value = TransactionState.Success("Transaction added successfully")
            } catch (e: Exception) {
                _transactionState.value = TransactionState.Error("Failed to add transaction: ${e.message}")
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        _transactionState.value = TransactionState.Loading
        viewModelScope.launch {
            try {
                repository.updateTransaction(transaction)
                _transactionState.value = TransactionState.Success("Transaction updated successfully")
            } catch (e: Exception) {
                _transactionState.value = TransactionState.Error("Failed to update transaction: ${e.message}")
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        _transactionState.value = TransactionState.Loading
        viewModelScope.launch {
            try {
                repository.deleteTransaction(transaction)
                _transactionState.value = TransactionState.Success("Transaction deleted successfully")
            } catch (e: Exception) {
                _transactionState.value = TransactionState.Error("Failed to delete transaction: ${e.message}")
            }
        }
    }

    fun getTransactionById(id: Long): LiveData<Transaction?> = liveData {
        emit(repository.getTransactionById(id))
    }

    fun getTotalIncome(startDate: Long, endDate: Long): LiveData<Double> = liveData {
        try {
            val total = repository.getTotalByTypeAndDateRange(currentUserId, "INCOME", startDate, endDate) ?: 0.0
            emit(total)
        } catch (e: Exception) {
            emit(0.0)
        }
    }

    fun getTotalExpenses(startDate: Long, endDate: Long): LiveData<Double> = liveData {
        try {
            val total = repository.getTotalByTypeAndDateRange(currentUserId, "EXPENSE", startDate, endDate) ?: 0.0
            emit(total)
        } catch (e: Exception) {
            emit(0.0)
        }
    }

    fun getSpendingByCategory(startDate: Long, endDate: Long): LiveData<List<CategorySpending>> = liveData {
        try {
            val spending = repository.getSpendingByCategory(currentUserId, startDate, endDate)
            emit(spending)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    fun resetTransactionState() {
        _transactionState.value = TransactionState.Idle
    }
}

enum class DateFilter {
    TODAY, WEEK, MONTH, CUSTOM
}

sealed class TransactionState {
    object Idle : TransactionState()
    object Loading : TransactionState()
    data class Success(val message: String) : TransactionState()
    data class Error(val message: String) : TransactionState()
}

class TransactionViewModelFactory(
    private val repository: EaseBudgetRepository,
    private val currentUserId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(repository, currentUserId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
