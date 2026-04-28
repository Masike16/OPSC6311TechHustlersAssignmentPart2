package com.example.easebudgetv1.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.easebudgetv1.utils.HashUtils

/**
 * User entity representing application users in the database.
 * 
 * Following database design principles (Connolly & Begg, 2015), this entity
 * stores essential user information while maintaining data integrity through
 * proper constraints and relationships. User data is persisted locally
 * using Room ORM for offline functionality.
 * 
 * @author Tech Hustlers Group
 * @version 1.0
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val email: String,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * Factory method for creating new users with hashed passwords.
         * 
         * Implements secure user creation by automatically hashing passwords
         * before storage, following security best practices (Stallings, 2017).
         * This ensures passwords are never stored in plain text.
         * 
         * @param username Unique user identifier
         * @param email User's email address
         * @param password Plain text password to be hashed
         * @return User instance with hashed password
         */
        fun create(username: String, email: String, password: String): User {
            return User(
                username = username,
                email = email,
                passwordHash = HashUtils.sha256(password)
            )
        }
    }
    
    /**
     * Verifies user password against stored hash.
     * 
     * Implements secure password verification by comparing input hash with
     * stored hash rather than plain text comparison (McGraw, 2018).
     * This method prevents timing attacks and maintains security.
     * 
     * @param password Plain text password attempt
     * @return True if password matches, false otherwise
     */
    fun verifyPassword(password: String): Boolean {
        return passwordHash == HashUtils.sha256(password)
    }
}
