package com.rialtracker.expense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rialtracker.expense.data.BankAccount
import com.rialtracker.expense.data.Category
import com.rialtracker.expense.data.Expense
import com.rialtracker.expense.util.NumberFormatter
import com.rialtracker.expense.util.PersianDateUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    expenses: List<Expense>,
    pendingCount: Int,
    categories: List<Category>,
    accounts: List<BankAccount>,
    onAddClick: () -> Unit,
    onPendingClick: () -> Unit,
    onExpenseClick: (Expense) -> Unit
) {
    val today = PersianDateUtil.todayJdn()
    val startOfWeek = PersianDateUtil.startOfWeek(today)
    val startOfMonth = PersianDateUtil.startOfJalaliMonth(today)

    val todaySum = expenses.filter { it.dateEpochDay == today }.sumOf { it.amountRial }
    val weekSum = expenses.filter { it.dateEpochDay in startOfWeek..today }.sumOf { it.amountRial }
    val monthSum = expenses.filter { it.dateEpochDay in startOfMonth..today }.sumOf { it.amountRial }

    val categoryMap = categories.associateBy { it.id }
    val accountMap = accounts.associateBy { it.id }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("حساب و کتاب من", fontWeight = FontWeight.SemiBold) },
                actions = {
                    if (pendingCount > 0) {
                        BadgedBox(
                            badge = { Badge { Text(PersianDateUtil.toPersianDigits(pendingCount.toString())) } },
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            IconButton(onClick = onPendingClick) {
                                Icon(Icons.Filled.NotificationsActive, contentDescription = "در انتظار تایید")
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAddClick, text = { Text("ثبت هزینه") }, icon = {})
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryCard("امروز", todaySum, Color(0xFFC7E9FF), Modifier.weight(1f))
                    SummaryCard("این هفته", weekSum, Color(0xFFD9F2D0), Modifier.weight(1f))
                    SummaryCard("این ماه", monthSum, Color(0xFFFFD6BA), Modifier.weight(1f))
                }
                Spacer(Modifier.height(20.dp))
                Text("تراکنش‌های اخیر", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }

            if (expenses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "هنوز هزینه‌ای ثبت نشده. با دکمه‌ی «ثبت هزینه» شروع کنید.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(expenses.take(50)) { expense ->
                ExpenseRow(
                    expense = expense,
                    category = expense.categoryId?.let { categoryMap[it] },
                    account = expense.accountId?.let { accountMap[it] },
                    onClick = { onExpenseClick(expense) }
                )
                Spacer(Modifier.height(6.dp))
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun SummaryCard(label: String, amount: Long, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .padding(12.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color(0xFF3A3A3A))
        Spacer(Modifier.height(4.dp))
        Text(
            NumberFormatter.formatAmountOnly(amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF3A3A3A)
        )
    }
}

@Composable
fun ExpenseRow(
    expense: Expense,
    category: Category?,
    account: BankAccount?,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        category?.colorHex?.let { runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull() }
                            ?: Color(0xFFE6E6E6)
                    )
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(category?.name ?: "بدون دسته", style = MaterialTheme.typography.bodyLarge)
                Text(
                    listOfNotNull(
                        PersianDateUtil.formatShort(expense.dateEpochDay),
                        account?.name
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                NumberFormatter.formatRial(expense.amountRial),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
