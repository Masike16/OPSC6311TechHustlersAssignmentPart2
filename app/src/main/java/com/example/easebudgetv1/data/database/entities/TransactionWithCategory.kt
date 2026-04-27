package com.example.easebudgetv1.data.database.entities

import androidx.room.Embedded
import androidx.room.Relation

// (Author, 2024) Data class for joined transaction and category information
data class TransactionWithCategory(
    @Embedded val transaction: Transaction,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: Category?
)
