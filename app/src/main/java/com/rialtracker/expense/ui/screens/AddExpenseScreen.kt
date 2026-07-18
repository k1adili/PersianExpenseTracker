package com.rialtracker.expense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rialtracker.expense.data.AccountType
import com.rialtracker.expense.data.BankAccount
import com.rialtracker.expense.data.Category
import com.rialtracker.expense.util.NumberFormatter
import com.rialtracker.expense.util.PersianDateUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    categories: List<Category>,
    accounts: List<BankAccount>,
    initialAmount: Long = 0,
    initialCategoryId: Long? = null,
    initialAccountId: Long? = null,
    initialNote: String = "",
    initialDateJdn: Long = PersianDateUtil.todayJdn(),
    smsRawText: String? = null,
    title: String = "ثبت هزینه‌ی جدید",
    onBack: () -> Unit,
    onSave: (amount: Long, categoryId: Long?, accountId: Long?, note: String, dateJdn: Long) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var amountText by remember {
        mutableStateOf(if (initialAmount > 0) NumberFormatter.formatAmountOnly(initialAmount, persianDigits = false) else "")
    }
    var selectedCategory by remember { mutableStateOf(initialCategoryId) }
    var selectedAccount by remember { mutableStateOf(initialAccountId) }
    var note by remember { mutableStateOf(initialNote) }
    var dateJdn by remember { mutableStateOf(initialDateJdn) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت") }
                },
                actions = {
                    if (onDelete != null) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = "حذف هزینه")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (smsRawText != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0B8)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "این هزینه از پیامک بانک شناسایی شده:\n$smsRawText",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it; showError = false },
                label = { Text("مبلغ (ریال)") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = showError,
                supportingText = { if (showError) Text("لطفاً مبلغ معتبر وارد کنید") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(PersianDateUtil.formatFull(dateJdn))
            }

            Spacer(Modifier.height(20.dp))
            Text("دسته‌بندی", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val selected = selectedCategory == cat.id
                    val color = runCatching { Color(android.graphics.Color.parseColor(cat.colorHex)) }.getOrDefault(Color(0xFFE6E6E6))
                    FilterChip(
                        selected = selected,
                        onClick = { selectedCategory = if (selected) null else cat.id },
                        label = { Text(cat.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color,
                            containerColor = color.copy(alpha = 0.35f)
                        )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("حساب (اختیاری)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(accounts) { acc ->
                    val selected = selectedAccount == acc.id
                    val color = runCatching { Color(android.graphics.Color.parseColor(acc.colorHex)) }.getOrDefault(Color(0xFFE6E6E6))
                    FilterChip(
                        selected = selected,
                        onClick = { selectedAccount = if (selected) null else acc.id },
                        label = { Text(if (acc.type == AccountType.CASH) "💵 ${acc.name}" else "🏦 ${acc.name}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color,
                            containerColor = color.copy(alpha = 0.35f)
                        )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("توضیحات (اختیاری)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(Modifier.height(28.dp))
            Button(
                onClick = {
                    val amount = NumberFormatter.parseUserInput(amountText)
                    if (amount == null || amount <= 0) {
                        showError = true
                    } else {
                        onSave(amount, selectedCategory, selectedAccount, note, dateJdn)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("ذخیره", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showDatePicker) {
        JalaliDatePickerDialog(
            initialJdn = dateJdn,
            onDismiss = { showDatePicker = false },
            onDateSelected = { dateJdn = it; showDatePicker = false }
        )
    }

    if (showDeleteConfirm && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("حذف هزینه") },
            text = { Text("این هزینه برای همیشه حذف می‌شود. مطمئنید؟") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) { Text("حذف", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("انصراف") }
            }
        )
    }
}
