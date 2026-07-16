package com.rialtracker.expense.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BankAccountDao {
    @Insert
    suspend fun insert(account: BankAccount): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<BankAccount>)

    @Update
    suspend fun update(account: BankAccount)

    @Delete
    suspend fun delete(account: BankAccount)

    @Query("SELECT * FROM accounts ORDER BY isDefault DESC, name ASC")
    fun getAll(): Flow<List<BankAccount>>

    @Query("SELECT * FROM accounts ORDER BY isDefault DESC, name ASC")
    suspend fun getAllOnce(): List<BankAccount>

    @Query("SELECT * FROM accounts WHERE last4Digits = :last4 AND last4Digits != '' LIMIT 1")
    suspend fun findByLast4(last4: String): BankAccount?

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun count(): Int

    @Query("DELETE FROM accounts")
    suspend fun deleteAll()
}
