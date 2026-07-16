package com.rialtracker.expense.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromAccountType(type: AccountType): String = type.name

    @TypeConverter
    fun toAccountType(value: String): AccountType = AccountType.valueOf(value)
}
