package com.rialtracker.expense.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses WHERE isConfirmed = 1 ORDER BY dateEpochDay DESC, createdAt DESC")
    fun getAllConfirmed(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE isConfirmed = 0 ORDER BY createdAt DESC")
    fun getPending(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE isConfirmed = 1 AND dateEpochDay BETWEEN :start AND :end ORDER BY dateEpochDay DESC, createdAt DESC")
    fun getInRange(start: Long, end: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE isConfirmed = 1 AND dateEpochDay BETWEEN :start AND :end ORDER BY dateEpochDay DESC, createdAt DESC")
    suspend fun getInRangeOnce(start: Long, end: Long): List<Expense>

    @Query("SELECT * FROM expenses ORDER BY dateEpochDay DESC, createdAt DESC")
    suspend fun getAllOnce(): List<Expense>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Long): Expense?

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COALESCE(SUM(amountRial),0) FROM expenses WHERE isConfirmed = 1 AND dateEpochDay BETWEEN :start AND :end")
    fun sumInRange(start: Long, end: Long): Flow<Long>

    @Query("DELETE FROM expenses")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Expense>)
}
