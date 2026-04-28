package com.example.easebudgetv1.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

// (Author, 2024) Transaction entity for financial transactions
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["categoryId"]),
        Index(value = ["date"])
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val categoryId: Long?,
    val amount: Double,
    val date: Long,
    val startTime: String? = null, // Added to meet requirement
    val endTime: String? = null,   // Added to meet requirement
    val description: String,
    val receiptPath: String? = null,
    val type: TransactionType
)

enum class TransactionType {
    INCOME,
    EXPENSE
}
