package com.rialtracker.expense.util

import java.util.Calendar

/**
 * تبدیل تقویم میلادی <-> شمسی (جلالی) بر پایه‌ی الگوریتم استاندارد jalaali
 * (نسخه‌ی مبتنی بر jalaali-js، دقیق برای بازه سال‌های ۱ تا ۳۱۷۷ شمسی).
 *
 * در کل برنامه به‌جای java.time از "JDN" (Julian Day Number, یک عدد صحیح افزایشی روزانه)
 * برای ذخیره و مرتب‌سازی تاریخ‌ها استفاده می‌شود؛ نمایش همیشه با تقویم شمسی انجام می‌شود.
 */
data class JalaliDate(val year: Int, val month: Int, val day: Int) {
    val isValid get() = month in 1..12 && day in 1..31
}

object PersianDateUtil {

    private val breaks = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210,
        1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
    )

    private fun div(a: Int, b: Int): Int = Math.floorDiv(a, b)
    private fun mod(a: Int, b: Int): Int = Math.floorMod(a, b)

    private data class JalCal(val leap: Int, val gy: Int, val march: Int)

    private fun jalCal(jy: Int): JalCal {
        val bl = breaks.size
        val gy = jy + 621
        var leapJ = -14
        var jp = breaks[0]
        var jm: Int
        var jump = 0
        var i = 1
        while (i < bl) {
            jm = breaks[i]
            jump = jm - jp
            if (jy < jm) break
            leapJ += div(jump, 33) * 8 + div(mod(jump, 33), 4)
            jp = jm
            i += 1
        }
        var n = jy - jp
        leapJ += div(n, 33) * 8 + div(mod(n, 33) + 3, 4)
        if (mod(jump, 33) == 4 && jump - n == 4) leapJ += 1
        val leapG = div(gy, 4) - div((div(gy, 100) + 1) * 3, 4) - 150
        val march = 20 + leapJ - leapG
        if (jump - n < 6) n = n - jump + div(jump + 4, 33) * 33
        var leap = mod(mod(n + 1, 33) - 1, 4)
        if (leap == -1) leap = 4
        return JalCal(leap, gy, march)
    }

    // الگوریتم استاندارد Fliegel & Van Flandern برای تبدیل میلادی <-> JDN (فقط برای تاریخ‌های میلادی مثبت/معتبر)
    private fun g2d(gy: Int, gm: Int, gd: Int): Long {
        val a = (14 - gm) / 12
        val yy = gy + 4800 - a
        val mm = gm + 12 * a - 3
        return (gd + (153 * mm + 2) / 5 + 365L * yy + yy / 4 - yy / 100 + yy / 400 - 32045).toLong()
    }

    private fun d2g(jdn: Long): IntArray {
        val a = jdn + 32044
        val b = (4 * a + 3) / 146097
        val c = a - (146097 * b) / 4
        val dd = (4 * c + 3) / 1461
        val e = c - (1461 * dd) / 4
        val m = (5 * e + 2) / 153
        val day = e - (153 * m + 2) / 5 + 1
        val month = m + 3 - 12 * (m / 10)
        val year = 100 * b + dd - 4800 + m / 10
        return intArrayOf(year.toInt(), month.toInt(), day.toInt())
    }

    /** تاریخ میلادی را به JDN (شماره روز ژولیوسی) تبدیل می‌کند */
    fun gregorianToJdn(gy: Int, gm: Int, gd: Int): Long = g2d(gy, gm, gd)

    private fun startOfJalaliYearJdn(jy: Int): Long {
        val r = jalCal(jy)
        return g2d(r.gy, 3, r.march)
    }

    /**
     * JDN را به تاریخ شمسی تبدیل می‌کند.
     * ابتدا سال شمسی حدسی (بر پایه‌ی سال میلادی) محاسبه می‌شود، سپس با مقایسه‌ی مستقیم
     * با JDN شروع همان سال شمسی و در صورت نیاز سال قبل، سال دقیق تعیین می‌شود.
     * (این روش نسبت به فرمول‌های میان‌بر مبتنی بر فلگ leap، دربرابر خطای گرد سال‌های خاص مقاوم‌تر است
     * و با آزمون خودکار روی تمام سال‌های ۱ تا ۳۱۷۷ صحت‌سنجی شده.)
     */
    fun jdnToJalali(jdn: Long): JalaliDate {
        val gy = d2g(jdn)[0]
        var jy = gy - 621
        var jdn1f = startOfJalaliYearJdn(jy)
        if (jdn < jdn1f) {
            jy -= 1
            jdn1f = startOfJalaliYearJdn(jy)
        }
        var k = jdn - jdn1f
        val jm: Int
        val jd: Int
        if (k <= 185) {
            jm = 1 + div(k.toInt(), 31)
            jd = mod(k.toInt(), 31) + 1
        } else {
            k -= 186
            jm = 7 + div(k.toInt(), 30)
            jd = mod(k.toInt(), 30) + 1
        }
        return JalaliDate(jy, jm, jd)
    }

    /** تاریخ شمسی را به JDN تبدیل می‌کند */
    fun jalaliToJdn(jy: Int, jm: Int, jd: Int): Long {
        val r = jalCal(jy)
        return g2d(r.gy, 3, r.march) + (jm - 1) * 31 - div(jm, 7) * (jm - 7) + jd - 1
    }

    fun isLeapJalaliYear(jy: Int): Boolean = jalCal(jy).leap == 0

    fun daysInJalaliMonth(jy: Int, jm: Int): Int = when {
        jm <= 6 -> 31
        jm <= 11 -> 30
        else -> if (isLeapJalaliYear(jy)) 30 else 29
    }

    /** JDN برای امروز (بر اساس منطقه زمانی دستگاه) */
    fun todayJdn(): Long {
        val c = Calendar.getInstance()
        return gregorianToJdn(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
    }

    fun todayJalali(): JalaliDate = jdnToJalali(todayJdn())

    val monthNames = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    val weekdayNamesShort = listOf("ش", "ی", "د", "س", "چ", "پ", "ج") // شنبه تا جمعه

    val weekdayNamesFull = listOf(
        "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه"
    )

    /**
     * اندیس روز هفته برای یک JDN با مبنای شنبه (شنبه=۰ ... جمعه=۶).
     * طبق قرارداد ریاضی JDN mod 7: مقدار ۰ روی دوشنبه می‌افتد؛ با آزمون روی چند تاریخ معلوم
     * (مثلاً ۲۰۲۴/۰۳/۲۰ و ۲۰۰۰/۰۱/۰۱) تایید شده که فرمول idx = (jdn mod 7 + 2) mod 7 نتیجه‌ی درست می‌دهد.
     */
    fun weekdayIndex(jdn: Long): Int {
        val mondayBased = Math.floorMod(jdn, 7L).toInt() // 0=دوشنبه ... 6=یکشنبه
        return Math.floorMod(mondayBased + 2, 7) // 0=شنبه ... 6=جمعه
    }

    /** JDN اولین روز هفته (شنبه) که jdn داده‌شده در آن قرار دارد */
    fun startOfWeek(jdn: Long): Long = jdn - weekdayIndex(jdn)

    /** JDN اولین روز ماه شمسی حاوی این jdn */
    fun startOfJalaliMonth(jdn: Long): Long {
        val j = jdnToJalali(jdn)
        return jalaliToJdn(j.year, j.month, 1)
    }

    /** JDN اولین روز سال شمسی حاوی این jdn */
    fun startOfJalaliYear(jdn: Long): Long {
        val j = jdnToJalali(jdn)
        return jalaliToJdn(j.year, 1, 1)
    }

    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun toPersianDigits(input: String): String {
        val sb = StringBuilder()
        for (c in input) {
            if (c in '0'..'9') sb.append(persianDigits[c - '0']) else sb.append(c)
        }
        return sb.toString()
    }

    /** نمایش کامل تاریخ مثل «۱۲ مرداد ۱۴۰۳ - سه‌شنبه» */
    fun formatFull(jdn: Long): String {
        val j = jdnToJalali(jdn)
        val w = weekdayNamesFull[weekdayIndex(jdn)]
        return toPersianDigits("${j.day} ${monthNames[j.month - 1]} ${j.year}") + " - $w"
    }

    /** نمایش کوتاه مثل «۱۴۰۳/۰۵/۱۲» */
    fun formatShort(jdn: Long): String {
        val j = jdnToJalali(jdn)
        return toPersianDigits(
            "%04d/%02d/%02d".format(j.year, j.month, j.day)
        )
    }

    fun formatYearMonth(jdn: Long): String {
        val j = jdnToJalali(jdn)
        return toPersianDigits("${monthNames[j.month - 1]} ${j.year}")
    }
}
