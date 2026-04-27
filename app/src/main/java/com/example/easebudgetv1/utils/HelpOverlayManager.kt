package com.example.easebudgetv1.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.easebudgetv1.R

// (Author, 2024) Help overlay management for user onboarding
class HelpOverlayManager(private val context: Context) {
    
    private val sessionManager = SessionManager(context)
    
    fun showHelpIfNeeded() {
        if (!sessionManager.isHelpDisabled()) {
            showHelpDialog()
        }
    }
    
    fun showHelpDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_help_tooltip, null)
        val titleView = dialogView.findViewById<TextView>(R.id.helpTitle)
        val messageView = dialogView.findViewById<TextView>(R.id.helpMessage)
        val dontShowAgainCheckbox = dialogView.findViewById<CheckBox>(R.id.dontShowAgainCheckbox)
        val closeButton = dialogView.findViewById<Button>(R.id.closeButton)
        
        titleView.text = "Welcome to EasEBudget!"
        messageView.text = buildHelpMessage()
        
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        
        closeButton.setOnClickListener {
            if (dontShowAgainCheckbox.isChecked) {
                sessionManager.setHelpDisabled(true)
            }
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun buildHelpMessage(): String {
        return """
            • Tap the + button to add your first expense or income
            • Set monthly budgets to track your spending limits
            • Attach receipt photos by tapping the camera icon
            • Check your spending by category in the Reports section
            • Use the Settings menu to manage your profile and preferences
            
            Getting started is easy - just add your first transaction and see how EasEBudget helps you manage your finances!
        """.trimIndent()
    }
    
    fun enableHelp() {
        sessionManager.setHelpDisabled(false)
    }
    
    fun showContextualHelp(feature: String) {
        val message = when (feature) {
            "transactions" -> """
                • Add expenses and income with the + button
                • Categorize each transaction for better tracking
                • Add receipt photos for documentation
                • Edit or delete transactions by long-pressing
            """.trimIndent()
            
            "budget" -> """
                • Set monthly budget limits for overall spending
                • Create category-specific limits for better control
                • Monitor your progress with visual indicators
                • Adjust budgets anytime as needed
            """.trimIndent()
            
            "reports" -> """
                • View spending distribution by category
                • Filter reports by date ranges
                • Track income vs expense trends
                • Export data for external analysis
            """.trimIndent()
            
            "settings" -> """
                • Update your profile information
                • Enable/disable help tooltips
                • Manage data backup and export
                • Customize app preferences
            """.trimIndent()
            
            else -> buildHelpMessage()
        }
        
        showSimpleHelpDialog("Help: $feature", message)
    }
    
    private fun showSimpleHelpDialog(title: String, message: String) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Got it") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
