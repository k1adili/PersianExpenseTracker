package com.rialtracker.expense.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * یک رکورد هزینه.
 * amountRial: مبلغ به ریال (Long برای جلوگیری از خطای اعشار).
 * dateEpochDay: روز میلادی (epoch day) برای امکان کوئری سریع بر اساس بازه؛ نمایش همیشه با تقویم شمسی انجام می‌شود.
 * accountId: اختیاری - اگر مشخص نشود یعنی کاربر نخواسته حساب را ثبت کند (نه لزوما نقدی).
 * isConfirmed: رکوردهای واردشده از پیامک بانک تا وقتی کاربر دسته‌بندی و صحتشان را تایید نکند، false هستند
 * و در بخش «در انتظار تایید» نمایش داده می‌شوند، نه در گزارش‌های اصلی.
 */
@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(entity = Category::class, parentColumns = ["id"], childColumns = ["categoryId"]),
        ForeignKey(entity = BankAccount::class, parentColumns = ["id"], childColumns = ["accountId"])
    ],
    indices = [Index("categoryId"), Index("accountId"), Index("dateEpochDay")]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountRial: Long,
    val categoryId: Long?,
    val accountId: Long?,
    val dateEpochDay: Long,
    val note: String = "",
    val isFromSms: Boolean = false,
    val smsRawText: String? = null,
    val smsSender: String? = null,
    val isConfirmed: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
