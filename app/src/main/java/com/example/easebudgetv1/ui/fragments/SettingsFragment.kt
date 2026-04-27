package com.example.easebudgetv1.ui.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.easebudgetv1.R
import com.example.easebudgetv1.data.database.EaseBudgetDatabase
import com.example.easebudgetv1.data.repository.EaseBudgetRepository
import com.example.easebudgetv1.utils.HelpOverlayManager
import com.example.easebudgetv1.utils.SessionManager
import com.example.easebudgetv1.viewmodel.AuthViewModel
import com.example.easebudgetv1.viewmodel.AuthViewModelFactory
import com.example.easebudgetv1.viewmodel.LoginState
import com.google.android.material.textfield.TextInputEditText

// (Author, 2024) Settings fragment for app preferences and user profile management
class SettingsFragment : Fragment() {
    
    private lateinit var authViewModel: AuthViewModel
    private lateinit var sessionManager: SessionManager
    private lateinit var helpOverlayManager: HelpOverlayManager
    
    // Views
    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var editProfile: ImageView
    private lateinit var helpSwitch: Switch
    private lateinit var notificationsSwitch: Switch
    private lateinit var exportDataLayout: LinearLayout
    private lateinit var backupDataLayout: LinearLayout
    private lateinit var deleteAccountButton: Button
    private lateinit var logoutButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize ViewModel
        val database = EaseBudgetDatabase.getDatabase(requireContext())
        val repository = EaseBudgetRepository(
            database.userDao(),
            database.categoryDao(),
            database.transactionDao(),
            database.budgetGoalDao()
        )
        sessionManager = SessionManager(requireContext())
        helpOverlayManager = HelpOverlayManager(requireContext())
        
        authViewModel = viewModels<AuthViewModel> {
            AuthViewModelFactory(repository, sessionManager)
        }.value
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupObservers()
        setupClickListeners()
        loadUserData()
    }
    
    private fun initializeViews(view: View) {
        usernameTextView = view.findViewById(R.id.usernameTextView)
        emailTextView = view.findViewById(R.id.emailTextView)
        editProfile = view.findViewById(R.id.editProfile)
        helpSwitch = view.findViewById(R.id.helpSwitch)
        notificationsSwitch = view.findViewById(R.id.notificationsSwitch)
        exportDataLayout = view.findViewById(R.id.exportDataLayout)
        backupDataLayout = view.findViewById(R.id.backupDataLayout)
        deleteAccountButton = view.findViewById(R.id.deleteAccountButton)
        logoutButton = view.findViewById(R.id.logoutButton)
    }
    
    private fun setupObservers() {
        authViewModel.currentUser.observe(viewLifecycleOwner, Observer { user ->
            user?.let {
                usernameTextView.text = it.username
                emailTextView.text = it.email
            }
        })
        
        authViewModel.loginState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is LoginState.Success -> {
                    // Do nothing here to prevent accidental logout on fragment load
                }
                is LoginState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
                LoginState.Idle -> {
                    // Handle explicit logout
                    if (sessionManager.getUserId() == -1L) {
                        navigateToLogin()
                    }
                }
                else -> {}
            }
        })
    }
    
    private fun setupClickListeners() {
        editProfile.setOnClickListener {
            showEditProfileDialog()
        }
        
        helpSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                helpOverlayManager.enableHelp()
            }
        }
        
        exportDataLayout.setOnClickListener {
            exportData()
        }
        
        backupDataLayout.setOnClickListener {
            backupData()
        }
        
        deleteAccountButton.setOnClickListener {
            showDeleteAccountConfirmation()
        }
        
        logoutButton.setOnClickListener {
            logout()
        }
    }
    
    private fun loadUserData() {
        // Load help preference
        helpSwitch.isChecked = !sessionManager.isHelpDisabled()
    }
    
    private fun showEditProfileDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null)
        val usernameEditText = dialogView.findViewById<TextInputEditText>(R.id.usernameEditText)
        val emailEditText = dialogView.findViewById<TextInputEditText>(R.id.emailEditText)
        
        // Set current values
        val currentUser = authViewModel.currentUser.value
        usernameEditText.setText(currentUser?.username ?: "")
        emailEditText.setText(currentUser?.email ?: "")
        
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val username = usernameEditText.text?.toString()?.trim() ?: ""
                val email = emailEditText.text?.toString()?.trim() ?: ""
                
                if (username.isNotEmpty() && email.isNotEmpty()) {
                    authViewModel.updateProfile(username, email)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun exportData() {
        // TODO: Implement data export functionality
        Toast.makeText(requireContext(), "Export feature coming soon", Toast.LENGTH_SHORT).show()
    }
    
    private fun backupData() {
        // TODO: Implement data backup functionality
        Toast.makeText(requireContext(), "Backup feature coming soon", Toast.LENGTH_SHORT).show()
    }
    
    private fun showDeleteAccountConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("This will permanently delete your account and all associated data. This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                showFinalDeleteConfirmation()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showFinalDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Final Confirmation")
            .setMessage("Are you absolutely sure you want to delete your account? Type 'DELETE' to confirm.")
            .setView(R.layout.dialog_delete_confirmation)
            .setPositiveButton("Delete Account") { _, _ ->
                // TODO: Verify DELETE input before proceeding
                authViewModel.deleteAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun logout() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                authViewModel.logout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun navigateToLogin() {
        val intent = Intent(requireContext(), com.example.easebudgetv1.ui.activities.LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}
