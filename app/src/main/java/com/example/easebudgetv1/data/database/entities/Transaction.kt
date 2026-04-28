package com.example.easebudgetv1.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Transaction entity representing financial transactions in the budget system.
 * 
 * Following relational database design principles (Elmasri & Navathe, 2016),
 * this entity maintains referential integrity through foreign key relationships
 * with Users and Categories. Transactions support both income and expense
 * tracking with optional receipt attachments for expense verification.
 * 
 * @author Tech Hustlers Group
 * @version 1.0
 */
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
    val startTime: String? = null, // Meeting assignment requirement for time tracking
    val endTime: String? = null,   // Meeting assignment requirement for time tracking
    val description: String,
    val receiptPath: String? = null,
    val type: TransactionType
)

/**
 * Enumeration defining transaction types for budget categorization.
 * 
 * Supports dual-entry accounting principles where expenses reduce available
 * funds while income increases them (Kieso et al., 2019). This enables
 * comprehensive budget tracking and financial analysis.
 */
enum class TransactionType {
    INCOME,
    EXPENSE
}
