package com.rialtracker.expense.sms

/**
 * نتیجه‌ی تحلیل یک پیامک بانکی.
 * amountRial همیشه به ریال است (اگر پیامک به تومان بود، در همین‌جا ضربدر ۱۰ شده).
 */
data class ParsedSms(
    val amountRial: Long,
    val last4Digits: String?,
    val isWithdrawal: Boolean, // true = هزینه/برداشت/خرید ، false = واریز (که به آن هزینه اضافه نمی‌کنیم)
    val rawText: String
)

object SmsParser {

    // کلیدواژه‌هایی که نشان می‌دهند پیامک واقعاً بانکی و مربوط به تراکنش است (برای کاهش تشخیص اشتباه)
    private val transactionKeywords = listOf(
        "برداشت", "خرید", "پرداخت", "کسر", "انتقال", "واریز", "کارتخوان", "خودپرداز", "موجودی"
    )

    private val withdrawalKeywords = listOf("برداشت", "خرید", "پرداخت", "کسر", "انتقال از", "خرید کالا و خدمات")
    private val depositKeywords = listOf("واریز", "واریزی", "به حساب شما")

    // عدد (با احتمال جداکننده هزارگان با ویرگول یا ممیز) بلافاصله قبل یا بعد از "ریال" یا "تومان"
    private val amountRegex = Regex(
        """([\d,،٬]{4,})\s*(ریال|تومان|ریالی)|(?:ریال|تومان)\s*[:\s]?\s*([\d,،٬]{4,})"""
    )

    // ۴ رقم انتهایی شماره کارت/حساب که معمولاً بعد از ستاره یا x می‌آید، مثل ****1234 یا 6104********1234
    private val last4Regex = Regex("""[\*xX•]{2,}\s*(\d{4})(?!\d)""")
    // حالت جایگزین: «حساب 1234» یا «کارت ...1234»
    private val last4FallbackRegex = Regex("""(?:کارت|حساب)[^\d]{0,10}(\d{4})(?!\d)""")

    fun isLikelyBankSms(text: String): Boolean {
        if (!text.contains("ریال") && !text.contains("تومان")) return false
        return transactionKeywords.any { text.contains(it) }
    }

    fun parse(text: String): ParsedSms? {
        if (!isLikelyBankSms(text)) return null

        val match = amountRegex.find(text) ?: return null
        val rawNumber = (match.groupValues[1].ifBlank { match.groupValues[3] })
            .replace(",", "").replace("،", "").replace("٬", "")
        val amount = rawNumber.toLongOrNull() ?: return null
        if (amount <= 0) return null

        val isToman = text.contains("تومان") && !text.contains("ریال")
        val amountRial = if (isToman) amount * 10 else amount

        val last4 = last4Regex.find(text)?.groupValues?.get(1)
            ?: last4FallbackRegex.find(text)?.groupValues?.get(1)

        val isWithdrawal = when {
            withdrawalKeywords.any { text.contains(it) } -> true
            depositKeywords.any { text.contains(it) } -> false
            else -> true // پیش‌فرض: هزینه در نظر گرفته شود چون هدف اصلی اپ ثبت مخارج است
        }

        return ParsedSms(
            amountRial = amountRial,
            last4Digits = last4,
            isWithdrawal = isWithdrawal,
            rawText = text
        )
    }
}
