package com.example.easebudgetv1.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

// (Author, 2024) BudgetGoal entity for monthly budget planning
@Entity(
    tableName = "budget_goals",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId", "month", "year"], unique = true)
    ]
)
data class BudgetGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val month: Int,
    val year: Int,
    val monthlyTotalBudget: Double,
    val savingsGoal: Double
)
