package com.rialtracker.expense.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AccountType { BANK, CASH }

/**
 * حساب بانکی یا "نقدی". برای هر خرج می‌توان اختیاری مشخص کرد از کدام حساب پرداخت شده.
 * یک حساب پیش‌فرض با نوع CASH به نام «نقدی» همیشه در دیتابیس وجود دارد.
 */
@Entity(tableName = "accounts")
data class BankAccount(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,           // مثلا «ملت - کارت اصلی»
    val type: AccountType,
    val bankName: String = "",  // مثلا «بانک ملت»
    val last4Digits: String = "", // ۴ رقم آخر شماره کارت، برای تطبیق خودکار پیامک
    val colorHex: String = "#C7E9FF",
    val isDefault: Boolean = false
)

object DefaultAccounts {
    fun seed(): List<BankAccount> = listOf(
        BankAccount(name = "نقدی", type = AccountType.CASH, colorHex = "#D9F2D0", isDefault = true)
    )
}
