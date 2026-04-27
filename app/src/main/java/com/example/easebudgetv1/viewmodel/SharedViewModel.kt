package com.example.easebudgetv1.viewmodel

import androidx.lifecycle.*
import com.example.easebudgetv1.data.database.entities.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// (Author, 2024) Shared ViewModel for communication between fragments
class SharedViewModel : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()

    private val _refreshData = MutableStateFlow(false)
    val refreshData: StateFlow<Boolean> = _refreshData.asStateFlow()

    fun setCurrentUser(user: User?) {
        _currentUser.value = user
    }

    fun navigateToAddTransaction() {
        _navigationEvent.value = NavigationEvent.ADD_TRANSACTION
    }

    fun navigateToEditTransaction(transactionId: Long) {
        _navigationEvent.value = NavigationEvent.EDIT_TRANSACTION(transactionId)
    }

    fun navigateToSettings() {
        _navigationEvent.value = NavigationEvent.SETTINGS
    }

    fun navigateToReports() {
        _navigationEvent.value = NavigationEvent.REPORTS
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }

    fun triggerRefresh() {
        _refreshData.value = !_refreshData.value
    }

    fun showHelp(feature: String) {
        _navigationEvent.value = NavigationEvent.SHOW_HELP(feature)
    }
}

sealed class NavigationEvent {
    object ADD_TRANSACTION : NavigationEvent()
    data class EDIT_TRANSACTION(val transactionId: Long) : NavigationEvent()
    object SETTINGS : NavigationEvent()
    object REPORTS : NavigationEvent()
    data class SHOW_HELP(val feature: String) : NavigationEvent()
}

class SharedViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SharedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SharedViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
