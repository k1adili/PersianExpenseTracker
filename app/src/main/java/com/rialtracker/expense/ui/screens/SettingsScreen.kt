package com.rialtracker.expense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rialtracker.expense.data.AccountType
import com.rialtracker.expense.data.BankAccount
import com.rialtracker.expense.data.Category
import com.rialtracker.expense.ui.theme.PaletteChoices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    categories: List<Category>,
    accounts: List<BankAccount>,
    smsPermissionGranted: Boolean,
    onRequestSmsPermission: () -> Unit,
    onAddCategory: (name: String, colorHex: String) -> Unit,
    onDeleteCategory: (Category) -> Unit,
    onAddAccount: (name: String, type: AccountType, bankName: String, last4: String, colorHex: String) -> Unit,
    onDeleteAccount: (BankAccount) -> Unit,
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit
) {
    var showAddCategory by remember { mutableStateOf(false) }
    var showAddAccount by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("تنظیمات") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            item {
                SectionHeader("پیامک بانکی")
                Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            if (smsPermissionGranted)
                                "دسترسی خواندن پیامک فعال است. مبلغ تراکنش‌های بانکی به‌صورت خودکار شناسایی می‌شود."
                            else
                                "برای شناسایی خودکار مبلغ از پیامک بانک، دسترسی به پیامک لازم است. هیچ پیامکی خارج از گوشی شما ارسال نمی‌شود.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (!smsPermissionGranted) {
                            Spacer(Modifier.height(10.dp))
                            Button(onClick = onRequestSmsPermission) { Text("فعال‌سازی دسترسی پیامک") }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))

                SectionHeader("پشتیبان‌گیری")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onBackupClick, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Backup, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("تهیه بکاپ")
                    }
                    OutlinedButton(onClick = onRestoreClick, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Restore, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("بازیابی")
                    }
                }
                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader("دسته‌بندی‌های مخارج")
                    IconButton(onClick = { showAddCategory = true }) { Icon(Icons.Filled.Add, contentDescription = "افزودن") }
                }
            }

            items(categories) { cat ->
                SettingsRow(
                    name = cat.name,
                    colorHex = cat.colorHex,
                    deletable = !cat.isDefault,
                    onDelete = { onDeleteCategory(cat) }
                )
            }

            item {
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader("حساب‌های بانکی و نقدی")
                    IconButton(onClick = { showAddAccount = true }) { Icon(Icons.Filled.Add, contentDescription = "افزودن") }
                }
            }

            items(accounts) { acc ->
                SettingsRow(
                    name = if (acc.type == AccountType.CASH) acc.name else "${acc.name} (${acc.last4Digits})",
                    colorHex = acc.colorHex,
                    deletable = !acc.isDefault,
                    onDelete = { onDeleteAccount(acc) }
                )
            }
        }
    }

    if (showAddCategory) {
        AddCategoryDialog(
            onDismiss = { showAddCategory = false },
            onConfirm = { name, color -> onAddCategory(name, color); showAddCategory = false }
        )
    }
    if (showAddAccount) {
        AddAccountDialog(
            onDismiss = { showAddAccount = false },
            onConfirm = { name, type, bank, last4, color ->
                onAddAccount(name, type, bank, last4, color); showAddAccount = false
            }
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun SettingsRow(name: String, colorHex: String, deletable: Boolean, onDelete: () -> Unit) {
    val color = runCatching { Color(android.graphics.Color.parseColor(colorHex)) }.getOrDefault(Color(0xFFE6E6E6))
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(10.dp))
        Text(name, modifier = Modifier.weight(1f))
        if (deletable) {
            IconButton(onClick = onDelete) { Icon(Icons.Filled.DeleteOutline, contentDescription = "حذف") }
        }
    }
}

@Composable
private fun AddCategoryDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(PaletteChoices.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("دسته‌بندی جدید") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("نام دسته") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(PaletteChoices) { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (color == selectedColor)
                                        Modifier.border(3.dp, Color(0xFF3A3A3A), CircleShape)
                                    else Modifier
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    val hex = String.format("#%06X", 0xFFFFFF and selectedColor.toArgb())
                    onConfirm(name.trim(), hex)
                }
            }) { Text("افزودن") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

@Composable
private fun AddAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, AccountType, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var bank by remember { mutableStateOf("") }
    var last4 by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(AccountType.BANK) }
    var selectedColor by remember { mutableStateOf(PaletteChoices[1]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("حساب جدید") },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = type == AccountType.BANK, onClick = { type = AccountType.BANK }, label = { Text("بانکی") })
                    FilterChip(selected = type == AccountType.CASH, onClick = { type = AccountType.CASH }, label = { Text("نقدی") })
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("نام حساب") }, modifier = Modifier.fillMaxWidth())
                if (type == AccountType.BANK) {
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(value = bank, onValueChange = { bank = it }, label = { Text("نام بانک") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = last4, onValueChange = { if (it.length <= 4) last4 = it.filter { c -> c.isDigit() } },
                        label = { Text("۴ رقم آخر کارت (برای تطبیق خودکار پیامک)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    val hex = String.format("#%06X", 0xFFFFFF and selectedColor.toArgb())
                    onConfirm(name.trim(), type, bank.trim(), last4.trim(), hex)
                }
            }) { Text("افزودن") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

