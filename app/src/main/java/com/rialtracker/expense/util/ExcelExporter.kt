package com.rialtracker.expense.util

import android.content.Context
import com.rialtracker.expense.data.AccountType
import com.rialtracker.expense.data.BankAccount
import com.rialtracker.expense.data.Category
import com.rialtracker.expense.data.Expense
import java.io.File
import java.io.FileOutputStream

enum class ReportPeriod { DAY, WEEK, MONTH, YEAR, ALL }

object ExcelExporter {

    /**
     * یک فایل اکسل چهار-شیتی می‌سازد:
     * ۱) لیست کامل تراکنش‌ها  ۲) خلاصه بر اساس دسته‌بندی  ۳) خلاصه بر اساس حساب  ۴) خلاصه بر اساس بازه‌ی زمانی
     */
    fun export(
        context: Context,
        expenses: List<Expense>,
        categories: List<Category>,
        accounts: List<BankAccount>,
        period: ReportPeriod,
        fileName: String = "گزارش-مخارج"
    ): File {
        val categoryMap = categories.associateBy { it.id }
        val accountMap = accounts.associateBy { it.id }

        val sheets = mutableListOf<XlsxWriter.Sheet>()
        sheets += buildTransactionsSheet(expenses, categoryMap, accountMap)
        sheets += buildCategorySummarySheet(expenses, categoryMap)
        sheets += buildAccountSummarySheet(expenses, accountMap)
        sheets += buildPeriodSummarySheet(expenses, period)

        val dir = File(context.getExternalFilesDir(null), "exports").apply { mkdirs() }
        val safeName = fileName.replace(Regex("""[\\/:*?"<>|]"""), "_")
        val file = File(dir, "$safeName-${PersianDateUtil.formatShort(PersianDateUtil.todayJdn()).replace('/', '-')}.xlsx")
        FileOutputStream(file).use { fos ->
            XlsxWriter.write(fos, sheets)
        }
        return file
    }

    private fun buildTransactionsSheet(
        expenses: List<Expense>,
        categoryMap: Map<Long, Category>,
        accountMap: Map<Long, BankAccount>
    ): XlsxWriter.Sheet {
        val sheet = XlsxWriter.Sheet("لیست تراکنش‌ها")
        sheet.addRow(
            XlsxWriter.Cell.of("تاریخ"),
            XlsxWriter.Cell.of("روز هفته"),
            XlsxWriter.Cell.of("مبلغ (ریال)"),
            XlsxWriter.Cell.of("دسته‌بندی"),
            XlsxWriter.Cell.of("حساب"),
            XlsxWriter.Cell.of("نوع حساب"),
            XlsxWriter.Cell.of("توضیحات"),
            XlsxWriter.Cell.of("منبع ثبت")
        )
        expenses.sortedByDescending { it.dateEpochDay }.forEach { e ->
            val cat = e.categoryId?.let { categoryMap[it] }
            val acc = e.accountId?.let { accountMap[it] }
            val jdn = e.dateEpochDay
            sheet.addRow(
                XlsxWriter.Cell.of(PersianDateUtil.formatShort(jdn)),
                XlsxWriter.Cell.of(PersianDateUtil.weekdayNamesFull[PersianDateUtil.weekdayIndex(jdn)]),
                XlsxWriter.Cell.of(e.amountRial),
                XlsxWriter.Cell.of(cat?.name ?: "بدون دسته"),
                XlsxWriter.Cell.of(acc?.name ?: "ثبت‌نشده"),
                XlsxWriter.Cell.of(
                    when (acc?.type) {
                        AccountType.BANK -> "بانکی"
                        AccountType.CASH -> "نقدی"
                        null -> "-"
                    }
                ),
                XlsxWriter.Cell.of(e.note),
                XlsxWriter.Cell.of(if (e.isFromSms) "پیامک بانک" else "دستی")
            )
        }
        return sheet
    }

    private fun buildCategorySummarySheet(
        expenses: List<Expense>,
        categoryMap: Map<Long, Category>
    ): XlsxWriter.Sheet {
        val sheet = XlsxWriter.Sheet("خلاصه بر اساس دسته")
        sheet.addRow(XlsxWriter.Cell.of("دسته‌بندی"), XlsxWriter.Cell.of("تعداد تراکنش"), XlsxWriter.Cell.of("جمع مبلغ (ریال)"))
        val grouped = expenses.groupBy { it.categoryId }
        val total = expenses.sumOf { it.amountRial }
        grouped.entries.sortedByDescending { entry -> entry.value.sumOf { it.amountRial } }.forEach { (catId, list) ->
            val name = catId?.let { categoryMap[it]?.name } ?: "بدون دسته"
            sheet.addRow(
                XlsxWriter.Cell.of(name),
                XlsxWriter.Cell.of(list.size),
                XlsxWriter.Cell.of(list.sumOf { it.amountRial })
            )
        }
        sheet.addRow(XlsxWriter.Cell.of("جمع کل"), XlsxWriter.Cell.of(expenses.size), XlsxWriter.Cell.of(total))
        return sheet
    }

    private fun buildAccountSummarySheet(
        expenses: List<Expense>,
        accountMap: Map<Long, BankAccount>
    ): XlsxWriter.Sheet {
        val sheet = XlsxWriter.Sheet("خلاصه بر اساس حساب")
        sheet.addRow(XlsxWriter.Cell.of("حساب"), XlsxWriter.Cell.of("تعداد تراکنش"), XlsxWriter.Cell.of("جمع مبلغ (ریال)"))
        val grouped = expenses.groupBy { it.accountId }
        grouped.entries.sortedByDescending { entry -> entry.value.sumOf { it.amountRial } }.forEach { (accId, list) ->
            val name = accId?.let { accountMap[it]?.name } ?: "ثبت‌نشده"
            sheet.addRow(
                XlsxWriter.Cell.of(name),
                XlsxWriter.Cell.of(list.size),
                XlsxWriter.Cell.of(list.sumOf { it.amountRial })
            )
        }
        return sheet
    }

    private fun buildPeriodSummarySheet(expenses: List<Expense>, period: ReportPeriod): XlsxWriter.Sheet {
        val sheet = XlsxWriter.Sheet("خلاصه بر اساس بازه")
        sheet.addRow(XlsxWriter.Cell.of("بازه"), XlsxWriter.Cell.of("جمع مبلغ (ریال)"))

        val groupKey: (Expense) -> String = when (period) {
            ReportPeriod.DAY -> { e -> PersianDateUtil.formatShort(e.dateEpochDay) }
            ReportPeriod.WEEK -> { e -> PersianDateUtil.formatShort(PersianDateUtil.startOfWeek(e.dateEpochDay)) + " (شروع هفته)" }
            ReportPeriod.MONTH -> { e -> PersianDateUtil.formatYearMonth(e.dateEpochDay) }
            ReportPeriod.YEAR -> { e -> PersianDateUtil.toPersianDigits(PersianDateUtil.jdnToJalali(e.dateEpochDay).year.toString()) }
            ReportPeriod.ALL -> { _ -> "کل بازه" }
        }

        val grouped = expenses.groupBy(groupKey)
        grouped.entries.sortedByDescending { it.key }.forEach { (label, list) ->
            sheet.addRow(XlsxWriter.Cell.of(label), XlsxWriter.Cell.of(list.sumOf { it.amountRial }))
        }
        return sheet
    }
}
