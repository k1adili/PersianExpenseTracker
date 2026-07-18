package com.rialtracker.expense.util

import android.content.Context
import com.rialtracker.expense.data.AccountType
import com.rialtracker.expense.data.BankAccount
import com.rialtracker.expense.data.Category
import com.rialtracker.expense.data.Expense
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

data class BackupData(
    val categories: List<Category>,
    val accounts: List<BankAccount>,
    val expenses: List<Expense>
)

/**
 * بکاپ‌گیری/بازیابی کل داده‌های اپ (دسته‌بندی‌ها، حساب‌ها، تراکنش‌ها) در قالب یک فایل JSON خوانا.
 * این فایل هم برای انتقال بین گوشی‌ها و هم به‌عنوان یک پشتیبان امن (مثلاً در گوگل‌درایو) قابل استفاده است.
 */
object BackupManager {

    private const val SCHEMA_VERSION = 1

    fun buildJson(data: BackupData): String {
        val root = JSONObject()
        root.put("schemaVersion", SCHEMA_VERSION)
        root.put("exportedAtJdn", PersianDateUtil.todayJdn())

        val catArr = JSONArray()
        data.categories.forEach { c ->
            catArr.put(
                JSONObject().apply {
                    put("id", c.id); put("name", c.name); put("colorHex", c.colorHex)
                    put("icon", c.icon); put("isDefault", c.isDefault)
                }
            )
        }
        root.put("categories", catArr)

        val accArr = JSONArray()
        data.accounts.forEach { a ->
            accArr.put(
                JSONObject().apply {
                    put("id", a.id); put("name", a.name); put("type", a.type.name)
                    put("bankName", a.bankName); put("last4Digits", a.last4Digits)
                    put("smsIdentifier", a.smsIdentifier)
                    put("colorHex", a.colorHex); put("isDefault", a.isDefault)
                }
            )
        }
        root.put("accounts", accArr)

        val expArr = JSONArray()
        data.expenses.forEach { e ->
            expArr.put(
                JSONObject().apply {
                    put("id", e.id); put("amountRial", e.amountRial)
                    put("categoryId", e.categoryId ?: JSONObject.NULL)
                    put("accountId", e.accountId ?: JSONObject.NULL)
                    put("dateEpochDay", e.dateEpochDay); put("note", e.note)
                    put("isFromSms", e.isFromSms)
                    put("smsRawText", e.smsRawText ?: JSONObject.NULL)
                    put("smsSender", e.smsSender ?: JSONObject.NULL)
                    put("isConfirmed", e.isConfirmed); put("createdAt", e.createdAt)
                }
            )
        }
        root.put("expenses", expArr)
        return root.toString(2)
    }

    fun parseJson(json: String): BackupData {
        val root = JSONObject(json)

        val categories = mutableListOf<Category>()
        val catArr = root.optJSONArray("categories") ?: JSONArray()
        for (i in 0 until catArr.length()) {
            val o = catArr.getJSONObject(i)
            categories += Category(
                id = o.optLong("id", 0),
                name = o.getString("name"),
                colorHex = o.optString("colorHex", "#E6E6E6"),
                icon = o.optString("icon", "tag"),
                isDefault = o.optBoolean("isDefault", false)
            )
        }

        val accounts = mutableListOf<BankAccount>()
        val accArr = root.optJSONArray("accounts") ?: JSONArray()
        for (i in 0 until accArr.length()) {
            val o = accArr.getJSONObject(i)
            accounts += BankAccount(
                id = o.optLong("id", 0),
                name = o.getString("name"),
                type = AccountType.valueOf(o.optString("type", "CASH")),
                bankName = o.optString("bankName", ""),
                last4Digits = o.optString("last4Digits", ""),
                smsIdentifier = o.optString("smsIdentifier", ""),
                colorHex = o.optString("colorHex", "#C7E9FF"),
                isDefault = o.optBoolean("isDefault", false)
            )
        }

        val expenses = mutableListOf<Expense>()
        val expArr = root.optJSONArray("expenses") ?: JSONArray()
        for (i in 0 until expArr.length()) {
            val o = expArr.getJSONObject(i)
            expenses += Expense(
                id = o.optLong("id", 0),
                amountRial = o.getLong("amountRial"),
                categoryId = if (o.isNull("categoryId")) null else o.getLong("categoryId"),
                accountId = if (o.isNull("accountId")) null else o.getLong("accountId"),
                dateEpochDay = o.getLong("dateEpochDay"),
                note = o.optString("note", ""),
                isFromSms = o.optBoolean("isFromSms", false),
                smsRawText = if (o.isNull("smsRawText")) null else o.optString("smsRawText"),
                smsSender = if (o.isNull("smsSender")) null else o.optString("smsSender"),
                isConfirmed = o.optBoolean("isConfirmed", true),
                createdAt = o.optLong("createdAt", System.currentTimeMillis())
            )
        }

        return BackupData(categories, accounts, expenses)
    }

    /** فایل بکاپ را در پوشه‌ی داخلی اپ می‌سازد و مسیر آن را برمی‌گرداند (برای اشتراک‌گذاری/ذخیره) */
    fun writeBackupFile(context: Context, data: BackupData): File {
        val dir = File(context.getExternalFilesDir(null), "backups").apply { mkdirs() }
        val dateLabel = PersianDateUtil.formatShort(PersianDateUtil.todayJdn()).replace('/', '-')
        val file = File(dir, "backup-$dateLabel.json")
        FileOutputStream(file).use { it.write(buildJson(data).toByteArray(Charsets.UTF_8)) }
        return file
    }

    fun readBackupFile(inputStream: InputStream): BackupData {
        val text = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        return parseJson(text)
    }
}
