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
    onAddCategory: (name: String, colorHex: String, icon: String) -> Unit,
    onUpdateCategory: (Category) -> Unit,
    onDeleteCategory: (Category) -> Unit,
    onAddAccount: (name: String, type: AccountType, bankName: String, last4: String, smsIdentifier: String, colorHex: String) -> Unit,
    onUpdateAccount: (BankAccount) -> Unit,
    onDeleteAccount: (BankAccount) -> Unit,
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit
) {
    var showAddCategory by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var showAddAccount by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<BankAccount?>(null) }

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
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "نکته: چون بیشتر پیامک‌های بانکی شماره‌ی کارت را نمی‌آورند، برای تطبیق دقیق‌تر هر تراکنش با حساب مربوطه، هنگام افزودن حساب بانکی «شماره فرستنده یا کلیدواژه‌ی پیامک» را هم وارد کنید.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    icon = categoryIconFor(cat.icon),
                    deletable = !cat.isDefault,
                    onClick = { editingCategory = cat },
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
                    name = acc.name,
                    subtitle = if (acc.type == AccountType.BANK && acc.smsIdentifier.isNotBlank()) "شناسه پیامک: ${acc.smsIdentifier}" else null,
                    colorHex = acc.colorHex,
                    icon = accountIconFor(acc.type),
                    deletable = !acc.isDefault,
                    onClick = { editingAccount = acc },
                    onDelete = { onDeleteAccount(acc) }
                )
            }
        }
    }

    if (showAddCategory) {
        CategoryDialog(
            title = "دسته‌بندی جدید",
            initialName = "",
            initialColor = PaletteChoices.first(),
            initialIcon = categoryIconChoices.first().first,
            onDismiss = { showAddCategory = false },
            onConfirm = { name, color, icon -> onAddCategory(name, color, icon); showAddCategory = false }
        )
    }
    editingCategory?.let { cat ->
        CategoryDialog(
            title = "ویرایش دسته‌بندی",
            initialName = cat.name,
            initialColor = runCatching { Color(android.graphics.Color.parseColor(cat.colorHex)) }.getOrDefault(PaletteChoices.first()),
            initialIcon = cat.icon,
            onDismiss = { editingCategory = null },
            onConfirm = { name, color, icon ->
                onUpdateCategory(cat.copy(name = name, colorHex = color, icon = icon))
                editingCategory = null
            }
        )
    }

    if (showAddAccount) {
        AccountDialog(
            title = "حساب جدید",
            initialName = "", initialBank = "", initialLast4 = "", initialSmsId = "",
            initialType = AccountType.BANK,
            initialColor = PaletteChoices[1],
            onDismiss = { showAddAccount = false },
            onConfirm = { name, type, bank, last4, smsId, color ->
                onAddAccount(name, type, bank, last4, smsId, color); showAddAccount = false
            }
        )
    }
    editingAccount?.let { acc ->
        AccountDialog(
            title = "ویرایش حساب",
            initialName = acc.name, initialBank = acc.bankName, initialLast4 = acc.last4Digits,
            initialSmsId = acc.smsIdentifier, initialType = acc.type,
            initialColor = runCatching { Color(android.graphics.Color.parseColor(acc.colorHex)) }.getOrDefault(PaletteChoices[1]),
            onDismiss = { editingAccount = null },
            onConfirm = { name, type, bank, last4, smsId, color ->
                onUpdateAccount(
                    acc.copy(name = name, type = type, bankName = bank, last4Digits = last4, smsIdentifier = smsId, colorHex = color)
                )
                editingAccount = null
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
private fun SettingsRow(
    name: String,
    subtitle: String? = null,
    colorHex: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    deletable: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val color = runCatching { Color(android.graphics.Color.parseColor(colorHex)) }.getOrDefault(Color(0xFFE6E6E6))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(34.dp).clip(CircleShape).background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF3A3A3A), modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(Icons.Filled.Edit, contentDescription = "ویرایش", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        if (deletable) {
            IconButton(onClick = onDelete) { Icon(Icons.Filled.DeleteOutline, contentDescription = "حذف") }
        } else {
            Spacer(Modifier.width(48.dp))
        }
    }
}

@Composable
private fun CategoryDialog(
    title: String,
    initialName: String,
    initialColor: Color,
    initialIcon: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedColor by remember { mutableStateOf(initialColor) }
    var selectedIcon by remember { mutableStateOf(initialIcon) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("نام دسته") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                Text("آیکون", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categoryIconChoices) { (key, icon) ->
                        val selected = key == selectedIcon
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (selected) selectedColor else selectedColor.copy(alpha = 0.35f))
                                .then(if (selected) Modifier.border(2.dp, Color(0xFF3A3A3A), CircleShape) else Modifier)
                                .clickable { selectedIcon = key },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = key, tint = Color(0xFF3A3A3A), modifier = Modifier.size(20.dp))
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("رنگ", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(PaletteChoices) { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(if (color == selectedColor) Modifier.border(3.dp, Color(0xFF3A3A3A), CircleShape) else Modifier)
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
                    onConfirm(name.trim(), hex, selectedIcon)
                }
            }) { Text("ذخیره") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

@Composable
private fun AccountDialog(
    title: String,
    initialName: String,
    initialBank: String,
    initialLast4: String,
    initialSmsId: String,
    initialType: AccountType,
    initialColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (String, AccountType, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var bank by remember { mutableStateOf(initialBank) }
    var last4 by remember { mutableStateOf(initialLast4) }
    var smsId by remember { mutableStateOf(initialSmsId) }
    var type by remember { mutableStateOf(initialType) }
    var selectedColor by remember { mutableStateOf(initialColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
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
                        value = smsId, onValueChange = { smsId = it },
                        label = { Text("شماره فرستنده یا کلیدواژه‌ی پیامک") },
                        supportingText = { Text("مثلاً 3000xxxxxx یا نام بانک به‌همان‌شکلی که در متن پیامک می‌آید") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = last4, onValueChange = { if (it.length <= 4) last4 = it.filter { c -> c.isDigit() } },
                        label = { Text("۴ رقم آخر کارت (اختیاری، اگر پیامک بانک آن را نشان می‌دهد)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text("رنگ", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(PaletteChoices) { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(if (color == selectedColor) Modifier.border(3.dp, Color(0xFF3A3A3A), CircleShape) else Modifier)
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
                    onConfirm(name.trim(), type, bank.trim(), last4.trim(), smsId.trim(), hex)
                }
            }) { Text("ذخیره") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}
