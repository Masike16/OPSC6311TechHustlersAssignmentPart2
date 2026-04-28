package com.example.easebudgetv1.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Session management class for persistent user authentication state.
 * 
 * Implements session persistence using SharedPreferences as recommended
 * for Android applications (Android Developers, 2021). This approach
 * maintains login state across app restarts while providing
 * secure storage of user session data.
 * 
 * @author Tech Hustlers Group
 * @version 1.0
 */
class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "EaseBudgetSession"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_HELP_DISABLED_PREFIX = "help_disabled_"
    }

    /**
     * Saves user login session to persistent storage.
     * 
     * Stores essential session data for maintaining authentication state
     * across application lifecycle events (Android Developers, 2021).
     * 
     * @param userId Unique identifier of logged in user
     * @param username Display name of logged in user
     */
    fun saveLoginSession(userId: Long, username: String) {
        prefs.edit().apply {
            putLong(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    /**
     * Retrieves the current user's unique identifier.
     * 
     * Returns -1 if no user is currently logged in, providing
     * a clear indicator for authentication state checking.
     * 
     * @return User ID if logged in, -1 otherwise
     */
    fun getUserId(): Long {
        return prefs.getLong(KEY_USER_ID, -1)
    }

    /**
     * Retrieves the current user's display name.
     * 
     * @return Username if logged in, null otherwise
     */
    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    /**
     * Checks if user is currently authenticated.
     * 
     * Provides quick authentication state checking for UI components
     * and navigation decisions (Android Developers, 2022).
     * 
     * @return True if user is logged in, false otherwise
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Clears user session data on logout.
     * 
     * Implements secure logout by removing authentication tokens
     * while preserving user preferences like help settings
     * (Android Developers, 2022).
     */
    fun logout() {
        // We only clear the transient login keys, but keep the help preference which is tied to userId
        val editor = prefs.edit()
        editor.remove(KEY_USER_ID)
        editor.remove(KEY_USERNAME)
        editor.remove(KEY_IS_LOGGED_IN)
        editor.apply()
    }

    /**
     * Sets help overlay preference for current user.
     * 
     * User-specific help settings allow personalization of
     * onboarding experience (Miller, 2020).
     * 
     * @param disabled True to disable help, false to enable
     */
    fun setHelpDisabled(disabled: Boolean) {
        val userId = getUserId()
        if (userId != -1L) {
            prefs.edit().putBoolean(KEY_HELP_DISABLED_PREFIX + userId, disabled).apply()
        }
    }

    /**
     * Checks if help overlay is disabled for current user.
     * 
     * @return True if help is disabled, false otherwise
     */
    fun isHelpDisabled(): Boolean {
        val userId = getUserId()
        if (userId == -1L) return false
        return prefs.getBoolean(KEY_HELP_DISABLED_PREFIX + userId, false)
    }
}
