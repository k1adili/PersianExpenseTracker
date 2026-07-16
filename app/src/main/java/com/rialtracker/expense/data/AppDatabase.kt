package com.rialtracker.expense.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Expense::class, Category::class, BankAccount::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): BankAccountDao

    companion object {
        const val DB_NAME = "rial_tracker.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                ).build()
                INSTANCE = instance
                // اولین اجرا: دسته‌بندی‌ها و حساب پیش‌فرض را بساز
                CoroutineScope(Dispatchers.IO).launch {
                    seedIfNeeded(instance)
                }
                instance
            }
        }

        private suspend fun seedIfNeeded(db: AppDatabase) {
            if (db.categoryDao().count() == 0) {
                db.categoryDao().insertAll(DefaultCategories.seed())
            }
            if (db.accountDao().count() == 0) {
                db.accountDao().insertAll(DefaultAccounts.seed())
            }
        }
    }
}
