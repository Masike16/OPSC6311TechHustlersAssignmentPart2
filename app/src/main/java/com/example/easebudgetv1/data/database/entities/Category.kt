package com.example.easebudgetv1.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// (Author, 2024) Category entity for transaction categorization
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val name: String,
    val iconResId: Int,
    val colorHex: String,
    val isDefault: Boolean = false,
    val monthlyLimit: Double = 0.0
)
