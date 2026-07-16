package com.rialtracker.expense.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rialtracker.expense.data.Expense
import com.rialtracker.expense.util.NumberFormatter
import com.rialtracker.expense.util.PersianDateUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingSmsScreen(
    pending: List<Expense>,
    onBack: () -> Unit,
    onConfirmClick: (Expense) -> Unit,
    onDelete: (Expense) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("در انتظار تایید (${PersianDateUtil.toPersianDigits(pending.size.toString())})") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت") }
                }
            )
        }
    ) { padding ->
        if (pending.isEmpty()) {
            Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("چیزی برای تایید نیست 🎉", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(pending) { expense ->
                ElevatedCard(shape = RoundedCornerShape(14.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                NumberFormatter.formatRial(expense.amountRial),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                PersianDateUtil.formatShort(expense.dateEpochDay),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (expense.smsRawText != null) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                expense.smsRawText,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Row {
                            Button(onClick = { onConfirmClick(expense) }, modifier = Modifier.weight(1f)) {
                                Text("تکمیل و تایید")
                            }
                            Spacer(Modifier.width(8.dp))
                            OutlinedIconButton(onClick = { onDelete(expense) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "حذف")
                            }
                        }
                    }
                }
            }
        }
    }
}
