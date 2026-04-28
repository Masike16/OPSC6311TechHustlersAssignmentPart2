package com.example.easebudgetv1.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.easebudgetv1.data.database.dao.*
import com.example.easebudgetv1.data.database.entities.*

// (Author, 2024) Main Room database for EasEBudget application
@Database(
    entities = [
        User::class,
        Category::class,
        Transaction::class,
        BudgetGoal::class
    ],
    version = 2, // Bumped version due to Transaction entity changes
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class EaseBudgetDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetGoalDao(): BudgetGoalDao

    companion object {
        @Volatile
        private var INSTANCE: EaseBudgetDatabase? = null

        fun getDatabase(context: Context): EaseBudgetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EaseBudgetDatabase::class.java,
                    "easebudget_database"
                )
                .fallbackToDestructiveMigration() // Handle schema changes during development
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
