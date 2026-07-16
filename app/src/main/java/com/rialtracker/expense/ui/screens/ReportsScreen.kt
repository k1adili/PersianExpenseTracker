package com.rialtracker.expense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rialtracker.expense.data.BankAccount
import com.rialtracker.expense.data.Category
import com.rialtracker.expense.data.Expense
import com.rialtracker.expense.util.NumberFormatter
import com.rialtracker.expense.util.PersianDateUtil
import com.rialtracker.expense.util.ReportPeriod

enum class GroupBy { CATEGORY, ACCOUNT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    expenses: List<Expense>,
    categories: List<Category>,
    accounts: List<BankAccount>,
    onBack: () -> Unit,
    onExport: (List<Expense>, ReportPeriod) -> Unit
) {
    var period by remember { mutableStateOf(ReportPeriod.MONTH) }
    var groupBy by remember { mutableStateOf(GroupBy.CATEGORY) }

    val today = PersianDateUtil.todayJdn()
    val filtered = remember(expenses, period) {
        when (period) {
            ReportPeriod.DAY -> expenses.filter { it.dateEpochDay == today }
            ReportPeriod.WEEK -> {
                val start = PersianDateUtil.startOfWeek(today)
                expenses.filter { it.dateEpochDay in start..today }
            }
            ReportPeriod.MONTH -> {
                val start = PersianDateUtil.startOfJalaliMonth(today)
                expenses.filter { it.dateEpochDay in start..today }
            }
            ReportPeriod.YEAR -> {
                val start = PersianDateUtil.startOfJalaliYear(today)
                expenses.filter { it.dateEpochDay in start..today }
            }
            ReportPeriod.ALL -> expenses
        }
    }

    val categoryMap = categories.associateBy { it.id }
    val accountMap = accounts.associateBy { it.id }
    val total = filtered.sumOf { it.amountRial }

    val grouped: List<Triple<String, Long, Color>> = remember(filtered, groupBy) {
        when (groupBy) {
            GroupBy.CATEGORY -> filtered.groupBy { it.categoryId }
                .map { (id, list) ->
                    val cat = id?.let { categoryMap[it] }
                    val color = cat?.colorHex?.let { runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull() } ?: Color(0xFFE6E6E6)
                    Triple(cat?.name ?: "بدون دسته", list.sumOf { it.amountRial }, color)
                }.sortedByDescending { it.second }
            GroupBy.ACCOUNT -> filtered.groupBy { it.accountId }
                .map { (id, list) ->
                    val acc = id?.let { accountMap[it] }
                    val color = acc?.colorHex?.let { runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull() } ?: Color(0xFFE6E6E6)
                    Triple(acc?.name ?: "ثبت‌نشده", list.sumOf { it.amountRial }, color)
                }.sortedByDescending { it.second }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("گزارش‌ها و نمودار") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت") }
                },
                actions = {
                    IconButton(onClick = { onExport(filtered, period) }) {
                        Icon(Icons.Filled.Share, contentDescription = "خروجی اکسل")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Text("بازه‌ی زمانی", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PeriodChip("روز", period == ReportPeriod.DAY) { period = ReportPeriod.DAY }
                    PeriodChip("هفته", period == ReportPeriod.WEEK) { period = ReportPeriod.WEEK }
                    PeriodChip("ماه", period == ReportPeriod.MONTH) { period = ReportPeriod.MONTH }
                    PeriodChip("سال", period == ReportPeriod.YEAR) { period = ReportPeriod.YEAR }
                    PeriodChip("همه", period == ReportPeriod.ALL) { period = ReportPeriod.ALL }
                }

                Spacer(Modifier.height(16.dp))
                Text("گروه‌بندی بر اساس", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PeriodChip("دسته‌بندی", groupBy == GroupBy.CATEGORY) { groupBy = GroupBy.CATEGORY }
                    PeriodChip("حساب", groupBy == GroupBy.ACCOUNT) { groupBy = GroupBy.ACCOUNT }
                }

                Spacer(Modifier.height(24.dp))

                if (filtered.isEmpty()) {
                    Text("در این بازه هزینه‌ای ثبت نشده.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        DonutChart(
                            slices = grouped.map { DonutSlice(it.second.toFloat(), it.third, it.first) },
                            centerContent = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("جمع کل", style = MaterialTheme.typography.labelMedium)
                                    Text(
                                        NumberFormatter.formatAmountOnly(total),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }

            items(grouped) { (label, amount, color) ->
                val percent = if (total > 0) (amount * 100 / total) else 0
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(color, RoundedCornerShape(4.dp))
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                        LinearProgressIndicator(
                            progress = { (percent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            color = color,
                            trackColor = color.copy(alpha = 0.2f)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(NumberFormatter.formatAmountOnly(amount), fontWeight = FontWeight.SemiBold)
                        Text(
                            PersianDateUtil.toPersianDigits("$percent%"),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}
