/**
 * Group: Tech Hustlers
 * Members:
 * - ST10451774 - Acazia Ammon
 * - ST10452404 - Masike Jr Rasenyalo
 * - ST10452409 - Liyema Masala
 */
package com.example.easebudgetv1.viewmodel

import androidx.lifecycle.*
import com.example.easebudgetv1.R
import com.example.easebudgetv1.data.database.entities.Category
import com.example.easebudgetv1.data.database.entities.User
import com.example.easebudgetv1.data.repository.EaseBudgetRepository
import com.example.easebudgetv1.utils.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// (Author, 2024) ViewModel for user authentication operations
class AuthViewModel(
    private val repository: EaseBudgetRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    private val _registrationState = MutableLiveData<RegistrationState>()
    val registrationState: LiveData<RegistrationState> = _registrationState

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        if (sessionManager.isLoggedIn()) {
            val userId = sessionManager.getUserId()
            viewModelScope.launch {
                val user = repository.getUserById(userId)
                if (user != null) {
                    _currentUser.value = user
                    ensureDefaultCategories(userId)
                    _loginState.value = LoginState.Success(user)
                } else {
                    _loginState.value = LoginState.Idle
                }
            }
        } else {
            _loginState.value = LoginState.Idle
        }
    }

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Username and password cannot be empty")
            return
        }

        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            try {
                val user = repository.getUserByUsername(username)
                if (user != null && user.verifyPassword(password)) {
                    sessionManager.saveLoginSession(user.id, user.username)
                    _currentUser.value = user
                    ensureDefaultCategories(user.id)
                    _loginState.value = LoginState.Success(user)
                } else {
                    _loginState.value = LoginState.Error("Invalid username or password")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Login failed: ${e.message}")
            }
        }
    }

    fun register(username: String, email: String, password: String, confirmPassword: String) {
        when {
            username.isBlank() -> _registrationState.value = RegistrationState.Error("Username cannot be empty")
            email.isBlank() -> _registrationState.value = RegistrationState.Error("Email cannot be empty")
            password.isBlank() -> _registrationState.value = RegistrationState.Error("Password cannot be empty")
            password != confirmPassword -> _registrationState.value = RegistrationState.Error("Passwords do not match")
            password.length < 6 -> _registrationState.value = RegistrationState.Error("Password must be at least 6 characters")
            else -> {
                _registrationState.value = RegistrationState.Loading
                viewModelScope.launch {
                    try {
                        val existingUser = repository.getUserByUsername(username)
                        
                        when {
                            existingUser != null -> _registrationState.value = RegistrationState.Error("Username already exists")
                            else -> {
                                val user = User.create(username, email, password)
                                val userId = repository.insertUser(user)
                                val createdUser = user.copy(id = userId)
                                
                                ensureDefaultCategories(userId)
                                
                                sessionManager.saveLoginSession(userId, username)
                                _currentUser.value = createdUser
                                _registrationState.value = RegistrationState.Success(createdUser)
                            }
                        }
                    } catch (e: Exception) {
                        _registrationState.value = RegistrationState.Error("Registration failed: ${e.message}")
                    }
                }
            }
        }
    }

    private suspend fun ensureDefaultCategories(userId: Long) {
        val currentCategories = repository.getUserCategories(userId).first()
        if (currentCategories.isEmpty()) {
            val defaultCategories = listOf(
                Category(userId = userId, name = "Food \u0026 Dining", iconResId = R.drawable.ic_budget, colorHex = "#FF9800", isDefault = true),
                Category(userId = userId, name = "Transportation", iconResId = R.drawable.ic_budget, colorHex = "#2196F3", isDefault = true),
                Category(userId = userId, name = "Shopping", iconResId = R.drawable.ic_budget, colorHex = "#E91E63", isDefault = true),
                Category(userId = userId, name = "Entertainment", iconResId = R.drawable.ic_budget, colorHex = "#9C27B0", isDefault = true),
                Category(userId = userId, name = "Health", iconResId = R.drawable.ic_budget, colorHex = "#4CAF50", isDefault = true),
                Category(userId = userId, name = "Utilities", iconResId = R.drawable.ic_budget, colorHex = "#607D8B", isDefault = true),
                Category(userId = userId, name = "Income", iconResId = R.drawable.ic_budget, colorHex = "#009688", isDefault = true)
            )
            repository.insertCategories(defaultCategories)
        }
    }

    fun logout() {
        sessionManager.logout()
        _currentUser.value = null
        _loginState.value = LoginState.Idle
    }

    fun updateProfile(username: String, email: String) {
        val currentUser = _currentUser.value ?: return
        
        viewModelScope.launch {
            try {
                val updatedUser = currentUser.copy(username = username, email = email)
                repository.updateUser(updatedUser)
                sessionManager.saveLoginSession(updatedUser.id, username)
                _currentUser.value = updatedUser
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Profile update failed: ${e.message}")
            }
        }
    }

    fun deleteAccount() {
        val currentUser = _currentUser.value ?: return
        
        viewModelScope.launch {
            try {
                repository.deleteUserById(currentUser.id)
                logout()
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Account deletion failed: ${e.message}")
            }
        }
    }

    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }

    fun resetRegistrationState() {
        _registrationState.value = RegistrationState.Idle
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class RegistrationState {
    object Idle : RegistrationState()
    object Loading : RegistrationState()
    data class Success(val user: User) : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}

class AuthViewModelFactory(
    private val repository: EaseBudgetRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
