package com.rialtracker.expense.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AccountType { BANK, CASH }

/**
 * حساب بانکی یا "نقدی". برای هر خرج می‌توان اختیاری مشخص کرد از کدام حساب پرداخت شده.
 * یک حساب پیش‌فرض با نوع CASH به نام «نقدی» همیشه در دیتابیس وجود دارد.
 *
 * smsIdentifier: برای تطبیق خودکار پیامک بانک با این حساب استفاده می‌شود. چون بیشتر پیامک‌های بانکی ایرانی
 * شماره کارت را داخل متن نمی‌آورند، به‌جای تکیه بر ۴ رقم آخر کارت، این فیلد با شماره‌ی فرستنده‌ی پیامک
 * (مثلاً 3000xxxx یا +98912xxxxxxx) یا یک کلیدواژه‌ی متمایز از متن پیامک (مثلاً نام بانک: «ملت»، «سامان») مقایسه می‌شود.
 */
@Entity(tableName = "accounts")
data class BankAccount(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,           // مثلا «ملت - کارت اصلی»
    val type: AccountType,
    val bankName: String = "",  // مثلا «بانک ملت»
    val last4Digits: String = "", // ۴ رقم آخر شماره کارت (اگر پیامک‌های این بانک شماره کارت را نشان می‌دهند)
    val smsIdentifier: String = "", // شماره‌ی فرستنده‌ی پیامک یا کلیدواژه‌ی متن، برای تطبیق دقیق‌تر
    val colorHex: String = "#C7E9FF",
    val isDefault: Boolean = false
)

object DefaultAccounts {
    fun seed(): List<BankAccount> = listOf(
        BankAccount(name = "نقدی", type = AccountType.CASH, colorHex = "#D9F2D0", isDefault = true)
    )
}
