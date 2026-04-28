/*
 * EasEBudget - Personal Finance Management App
 * 
 * Developed by Tech Hustlers Group:
 * - ST10451774 - Acazia Ammon
 * - ST10452404 - Masike Jr Rasenyalo  
 * - ST10452409 - Liyema Masala
 */

package com.example.easebudgetv1.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.easebudgetv1.R
import com.example.easebudgetv1.databinding.ActivityMainBinding
import com.example.easebudgetv1.utils.HelpOverlayManager
import com.example.easebudgetv1.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Main activity implementing bottom navigation architecture.
 * This activity
 * serves as the primary container for all app screens after
 * successful authentication.
 * @authors Tech Hustlers Group
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var helpOverlayManager: HelpOverlayManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sessionManager = SessionManager(this)
        
        // Security Guard: Prevent access if not logged in
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin()
            return
        }
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Set up the toolbar as the action bar
        setSupportActionBar(binding.toolbar)
        
        helpOverlayManager = HelpOverlayManager(this)
        
        setupNavigation()
        setupFloatingActionButtons()
        showHelpIfNeeded()
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        // Setup bottom navigation
        binding.bottomNavigationView.setupWithNavController(navController)
        
        // Setup ActionBar with navigation
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_dashboard,
                R.id.navigation_transactions,
                R.id.navigation_budget,
                R.id.navigation_reports,
                R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
    }
    
    private fun setupFloatingActionButtons() {
        // Add Transaction FAB
        binding.addTransactionFab.setOnClickListener {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            
            // Navigate to add transaction dialog
            navController.navigate(R.id.action_global_addEditTransactionFragment)
        }
        
        // Help FAB
        binding.helpFab.setOnClickListener {
            helpOverlayManager.showHelpDialog()
        }
    }
    
    private fun showHelpIfNeeded() {
        helpOverlayManager.showHelpIfNeeded()
    }
    // Navigates to the login activity if the user is not logged in.
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    // Handles the up navigation action.
    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
