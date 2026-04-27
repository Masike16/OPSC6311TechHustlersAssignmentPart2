package com.example.easebudgetv1.data.database

import androidx.room.TypeConverter
import com.example.easebudgetv1.data.database.entities.TransactionType

// (Author, 2024) Type converters for Room database
class Converters {
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String {
        return type.name
    }

    @TypeConverter
    fun toTransactionType(type: String): TransactionType {
        return TransactionType.valueOf(type)
    }
}
