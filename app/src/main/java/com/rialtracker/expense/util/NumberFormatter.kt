package com.rialtracker.expense.util

/**
 * فرمت‌کننده‌ی اعداد ریالی؛ هر سه رقم با ویرگول جدا می‌شود و به‌صورت پیش‌فرض با ارقام فارسی نمایش داده می‌شود.
 */
object NumberFormatter {

    /** مثال خروجی: ۱۵۰,۰۰۰ ریال */
    fun formatRial(amount: Long, persianDigits: Boolean = true): String {
        val grouped = groupThousands(amount)
        val withUnit = "$grouped ریال"
        return if (persianDigits) PersianDateUtil.toPersianDigits(withUnit) else withUnit
    }

    /** فقط عدد گروه‌بندی‌شده بدون واحد، مثل ۱۵۰,۰۰۰ */
    fun formatAmountOnly(amount: Long, persianDigits: Boolean = true): String {
        val grouped = groupThousands(amount)
        return if (persianDigits) PersianDateUtil.toPersianDigits(grouped) else grouped
    }

    private fun groupThousands(amount: Long): String {
        val negative = amount < 0
        val s = kotlin.math.abs(amount).toString()
        val sb = StringBuilder()
        var count = 0
        for (i in s.length - 1 downTo 0) {
            sb.append(s[i])
            count++
            if (count % 3 == 0 && i != 0) sb.append(',')
        }
        val result = sb.reverse().toString()
        return if (negative) "-$result" else result
    }

    /**
     * رشته‌ی ورودی کاربر (که ممکن است شامل ویرگول یا ارقام فارسی باشد) را به Long تبدیل می‌کند.
     * برای استفاده هنگام تایپ مبلغ در فرم ثبت هزینه.
     */
    fun parseUserInput(input: String): Long? {
        val normalized = input
            .map { c ->
                when (c) {
                    in '۰'..'۹' -> ('0' + (c - '۰'))
                    in '٠'..'٩' -> ('0' + (c - '٠')) // ارقام عربی هم پشتیبانی شود
                    else -> c
                }
            }.joinToString("")
            .replace(",", "")
            .replace("٬", "")
            .trim()
        return normalized.toLongOrNull()
    }
}
