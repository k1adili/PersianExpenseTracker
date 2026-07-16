package com.rialtracker.expense

import android.app.Application
import com.rialtracker.expense.data.AppDatabase
import com.rialtracker.expense.repository.ExpenseRepository

class ExpenseApplication : Application() {
    lateinit var repository: ExpenseRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(this)
        repository = ExpenseRepository(db)
    }
}
