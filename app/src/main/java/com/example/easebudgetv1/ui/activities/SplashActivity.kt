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
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.easebudgetv1.R
import com.example.easebudgetv1.utils.SessionManager

// (Author, 2024) Splash screen activity for app initialization and authentication check
class SplashActivity : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private val splashDelay: Long = 2000 // 2 seconds
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        sessionManager = SessionManager(this)
        
        // Force logout on every fresh launch for security as requested
        sessionManager.logout()
        
        // Navigate after splash delay
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, splashDelay)
    }
    
    private fun navigateToNextScreen() {
        // Navigate to LoginActivity
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
