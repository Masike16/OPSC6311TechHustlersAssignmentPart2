/**
 * Group: Tech Hustlers
 * Members:
 * - ST10451774 - Acazia Ammon
 * - ST10452404 - Masike Jr Rasenyalo
 * - ST10452409 - Liyema Masala
 */
package com.example.easebudgetv1.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.easebudgetv1.R
import com.example.easebudgetv1.data.database.EaseBudgetDatabase
import com.example.easebudgetv1.data.repository.EaseBudgetRepository
import com.example.easebudgetv1.utils.SessionManager
import com.example.easebudgetv1.viewmodel.AuthViewModel
import com.example.easebudgetv1.viewmodel.AuthViewModelFactory
import com.example.easebudgetv1.viewmodel.LoginState
import com.google.android.material.textfield.TextInputEditText

// (Author, 2024) Login activity for user authentication
class LoginActivity : AppCompatActivity() {
    
    private lateinit var authViewModel: AuthViewModel
    private lateinit var sessionManager: SessionManager
    
    private lateinit var usernameEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: com.google.android.material.button.MaterialButton
    private lateinit var signUpButton: android.widget.TextView
    private lateinit var progressBar: android.widget.ProgressBar
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        // Initialize ViewModel
        val database = EaseBudgetDatabase.getDatabase(this)
        val repository = EaseBudgetRepository(
            database.userDao(),
            database.categoryDao(),
            database.transactionDao(),
            database.budgetGoalDao()
        )
        sessionManager = SessionManager(this)
        authViewModel = viewModels<AuthViewModel> {
            AuthViewModelFactory(repository, sessionManager)
        }.value
        
        initializeViews()
        setupObservers()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        signUpButton = findViewById(R.id.signUpButton)
        progressBar = findViewById(R.id.progressBar)
    }
    
    private fun setupObservers() {
        authViewModel.loginState.observe(this, Observer { state ->
            when (state) {
                is LoginState.Loading -> {
                    showLoading(true)
                }
                is LoginState.Success -> {
                    showLoading(false)
                    navigateToMain()
                }
                is LoginState.Error -> {
                    showLoading(false)
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                LoginState.Idle -> {
                    showLoading(false)
                }
            }
        })
    }
    
    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            val username = usernameEditText.text?.toString()?.trim() ?: ""
            val password = passwordEditText.text?.toString() ?: ""
            
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            authViewModel.login(username, password)
        }
        
        signUpButton.setOnClickListener {
            navigateToRegister()
        }
    }
    
    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        loginButton.isEnabled = !show
        usernameEditText.isEnabled = !show
        passwordEditText.isEnabled = !show
    }
    
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
    
    private fun navigateToRegister() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }
}
