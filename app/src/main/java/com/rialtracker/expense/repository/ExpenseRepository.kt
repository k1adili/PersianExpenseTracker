package com.rialtracker.expense.repository

import com.rialtracker.expense.data.*
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val db: AppDatabase) {

    val allExpenses: Flow<List<Expense>> = db.expenseDao().getAllConfirmed()
    val pendingExpenses: Flow<List<Expense>> = db.expenseDao().getPending()
    val categories: Flow<List<Category>> = db.categoryDao().getAll()
    val accounts: Flow<List<BankAccount>> = db.accountDao().getAll()

    suspend fun addExpense(expense: Expense): Long = db.expenseDao().insert(expense)
    suspend fun updateExpense(expense: Expense) = db.expenseDao().update(expense)
    suspend fun deleteExpense(expense: Expense) = db.expenseDao().delete(expense)
    suspend fun getExpensesInRange(startJdn: Long, endJdn: Long) = db.expenseDao().getInRangeOnce(startJdn, endJdn)
    suspend fun getAllExpensesOnce() = db.expenseDao().getAllOnce()

    suspend fun addCategory(category: Category): Long = db.categoryDao().insert(category)
    suspend fun updateCategory(category: Category) = db.categoryDao().update(category)
    suspend fun deleteCategory(category: Category) = db.categoryDao().delete(category)
    suspend fun getCategoriesOnce() = db.categoryDao().getAllOnce()

    suspend fun addAccount(account: BankAccount): Long = db.accountDao().insert(account)
    suspend fun updateAccount(account: BankAccount) = db.accountDao().update(account)
    suspend fun deleteAccount(account: BankAccount) = db.accountDao().delete(account)
    suspend fun getAccountsOnce() = db.accountDao().getAllOnce()

    // --- برای بکاپ/ریستور ---
    suspend fun wipeAll() {
        db.expenseDao().deleteAll()
        db.categoryDao().deleteAll()
        db.accountDao().deleteAll()
    }

    suspend fun restoreAll(categories: List<Category>, accounts: List<BankAccount>, expenses: List<Expense>) {
        db.categoryDao().insertAll(categories)
        db.accountDao().insertAll(accounts)
        db.expenseDao().insertAll(expenses)
    }
}
