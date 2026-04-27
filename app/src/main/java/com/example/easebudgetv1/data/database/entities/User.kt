package com.example.easebudgetv1.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.easebudgetv1.utils.HashUtils

// (Author, 2024) User entity for authentication and profile management
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
        fun create(username: String, email: String, password: String): User {
            return User(
                username = username,
                email = email,
                passwordHash = HashUtils.sha256(password)
            )
        }
    }
    
    fun verifyPassword(password: String): Boolean {
        return passwordHash == HashUtils.sha256(password)
    }
}
