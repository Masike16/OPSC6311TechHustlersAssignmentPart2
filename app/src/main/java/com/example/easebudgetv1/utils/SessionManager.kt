package com.example.easebudgetv1.utils

import android.content.Context
import android.content.SharedPreferences

// (Author, 2024) Session management for user authentication
class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "EaseBudgetSession"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_HELP_DISABLED_PREFIX = "help_disabled_"
    }

    fun saveLoginSession(userId: Long, username: String) {
        prefs.edit().apply {
            putLong(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun getUserId(): Long {
        return prefs.getLong(KEY_USER_ID, -1)
    }

    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun logout() {
        // We only clear the transient login keys, but keep the help preference which is tied to userId
        val editor = prefs.edit()
        editor.remove(KEY_USER_ID)
        editor.remove(KEY_USERNAME)
        editor.remove(KEY_IS_LOGGED_IN)
        editor.apply()
    }

    fun setHelpDisabled(disabled: Boolean) {
        val userId = getUserId()
        if (userId != -1L) {
            prefs.edit().putBoolean(KEY_HELP_DISABLED_PREFIX + userId, disabled).apply()
        }
    }

    fun isHelpDisabled(): Boolean {
        val userId = getUserId()
        if (userId == -1L) return false
        return prefs.getBoolean(KEY_HELP_DISABLED_PREFIX + userId, false)
    }
}
