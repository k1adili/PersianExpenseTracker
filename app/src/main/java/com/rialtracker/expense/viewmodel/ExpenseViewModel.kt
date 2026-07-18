package com.rialtracker.expense.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rialtracker.expense.ExpenseApplication
import com.rialtracker.expense.data.*
import com.rialtracker.expense.repository.ExpenseRepository
import com.rialtracker.expense.util.PersianDateUtil
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository = (application as ExpenseApplication).repository

    val expenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingExpenses: StateFlow<List<Expense>> = repository.pendingExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<BankAccount>> = repository.accounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addExpense(
        amountRial: Long,
        categoryId: Long?,
        accountId: Long?,
        dateJdn: Long = PersianDateUtil.todayJdn(),
        note: String = ""
    ) {
        viewModelScope.launch {
            repository.addExpense(
                Expense(
                    amountRial = amountRial,
                    categoryId = categoryId,
                    accountId = accountId,
                    dateEpochDay = dateJdn,
                    note = note,
                    isConfirmed = true
                )
            )
        }
    }

    fun confirmPendingExpense(expense: Expense, categoryId: Long?, accountId: Long?, note: String) {
        viewModelScope.launch {
            repository.updateExpense(
                expense.copy(
                    categoryId = categoryId,
                    accountId = accountId,
                    note = note,
                    isConfirmed = true
                )
            )
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch { repository.updateExpense(expense) }
    }

    fun addCategory(name: String, colorHex: String, icon: String = "tag") {
        viewModelScope.launch { repository.addCategory(Category(name = name, colorHex = colorHex, icon = icon)) }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch { repository.updateCategory(category) }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch { repository.deleteCategory(category) }
    }

    fun addAccount(name: String, type: AccountType, bankName: String, last4: String, smsIdentifier: String, colorHex: String) {
        viewModelScope.launch {
            repository.addAccount(
                BankAccount(
                    name = name, type = type, bankName = bankName,
                    last4Digits = last4, smsIdentifier = smsIdentifier, colorHex = colorHex
                )
            )
        }
    }

    fun updateAccount(account: BankAccount) {
        viewModelScope.launch { repository.updateAccount(account) }
    }

    fun deleteAccount(account: BankAccount) {
        viewModelScope.launch { repository.deleteAccount(account) }
    }

    suspend fun getAllExpensesOnce() = repository.getAllExpensesOnce()
    suspend fun getExpensesInRange(startJdn: Long, endJdn: Long) = repository.getExpensesInRange(startJdn, endJdn)

    suspend fun wipeAllData() = repository.wipeAll()
    suspend fun restoreAllData(categories: List<Category>, accounts: List<BankAccount>, expenses: List<Expense>) =
        repository.restoreAll(categories, accounts, expenses)

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ExpenseViewModel(application) as T
                }
            }
    }
}
