package com.example.easebudgetv1.utils

import java.security.MessageDigest

/**
 * Security utility class for password hashing and verification.
 *SHA-256 provides one-way encryption that prevents password
 * recovery even with database access (Li & Zhou, 2022).
 */
object HashUtils {
    
    /**
     * Generates SHA-256 hash of input string
     * @param input Plain text password or data to hash
     * @return Hexadecimal hash string representation
     */
    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Verifies if input matches stored hash.
     * @param input User-provided password attempt
     * @param hashedPassword Stored hash from database
     * @return True if passwords match, false otherwise
     */
    fun verifyPassword(input: String, hashedPassword: String): Boolean {
        return sha256(input) == hashedPassword
    }
}
