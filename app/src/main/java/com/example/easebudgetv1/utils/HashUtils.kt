package com.example.easebudgetv1.utils

import java.security.MessageDigest

// (Author, 2024) Utility for password hashing
object HashUtils {
    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
    
    fun verifyPassword(input: String, hashedPassword: String): Boolean {
        return sha256(input) == hashedPassword
    }
}
