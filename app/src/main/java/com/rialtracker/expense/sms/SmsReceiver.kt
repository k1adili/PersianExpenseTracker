package com.rialtracker.expense.sms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.rialtracker.expense.MainActivity
import com.rialtracker.expense.data.AppDatabase
import com.rialtracker.expense.data.Expense
import com.rialtracker.expense.util.PersianDateUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.telephony.SmsMessage
import android.provider.Telephony

/**
 * وقتی پیامکی می‌رسد، اگر شبیه پیامک تراکنش بانکی باشد، مبلغ آن استخراج و به‌صورت
 * یک هزینه‌ی «تاییدنشده» (isConfirmed = false) در دیتابیس ذخیره می‌شود.
 * کاربر باید از داخل اپ، دسته‌بندی را انتخاب و آن را تایید کند - این‌طوری از ثبت اشتباهیِ
 * پیامک‌های غیرمالی جلوگیری می‌شود و در عین حال دیگر لازم نیست مبلغ را دستی تایپ کند.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages: Array<SmsMessage> = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isEmpty()) return

        val sender = messages[0].originatingAddress ?: ""
        val fullBody = messages.joinToString("") { it.messageBody ?: "" }

        val parsed = SmsParser.parse(fullBody) ?: return
        if (!parsed.isWithdrawal) return // واریزی‌ها را به‌عنوان هزینه ثبت نمی‌کنیم

        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(appContext)

            // اگر شماره حساب/کارت با یکی از حساب‌های ثبت‌شده مطابقت داشت، همان را پیشنهاد بده
            val matchedAccount = parsed.last4Digits?.let { db.accountDao().findByLast4(it) }

            val expense = Expense(
                amountRial = parsed.amountRial,
                categoryId = null,
                accountId = matchedAccount?.id,
                dateEpochDay = PersianDateUtil.todayJdn(),
                note = "",
                isFromSms = true,
                smsRawText = parsed.rawText,
                smsSender = sender,
                isConfirmed = false
            )
            val id = db.expenseDao().insert(expense)
            showNotification(appContext, id, parsed.amountRial)
        }
    }

    private fun showNotification(context: Context, expenseId: Long, amount: Long) {
        val channelId = "sms_pending_channel"
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "هزینه‌های شناسایی‌شده از پیامک", NotificationManager.IMPORTANCE_DEFAULT
            )
            nm.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("open_pending_sms", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, expenseId.toInt(), openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formattedAmount = com.rialtracker.expense.util.NumberFormatter.formatRial(amount)
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle("هزینه‌ی جدید شناسایی شد")
            .setContentText("مبلغ $formattedAmount از پیامک بانک - برای تکمیل دسته‌بندی لمس کنید")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        nm.notify(expenseId.toInt(), notification)
    }
}
