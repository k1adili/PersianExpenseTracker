package com.rialtracker.expense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rialtracker.expense.util.PersianDateUtil

/**
 * دیالوگ انتخاب تاریخ شمسی. onDateSelected مقدار JDN تاریخ انتخاب‌شده را برمی‌گرداند.
 */
@Composable
fun JalaliDatePickerDialog(
    initialJdn: Long,
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit
) {
    val initial = PersianDateUtil.jdnToJalali(initialJdn)
    var year by remember { mutableIntStateOf(initial.year) }
    var month by remember { mutableIntStateOf(initial.month) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = null,
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (month == 1) { month = 12; year -= 1 } else month -= 1
                    }) { Icon(Icons.Filled.ChevronRight, contentDescription = "ماه قبل") }

                    Text(
                        text = PersianDateUtil.toPersianDigits("${PersianDateUtil.monthNames[month - 1]} $year"),
                        style = MaterialTheme.typography.titleMedium
                    )

                    IconButton(onClick = {
                        if (month == 12) { month = 1; year += 1 } else month += 1
                    }) { Icon(Icons.Filled.ChevronLeft, contentDescription = "ماه بعد") }
                }

                Spacer(Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    PersianDateUtil.weekdayNamesShort.forEach { w ->
                        Text(
                            text = w,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val daysInMonth = PersianDateUtil.daysInJalaliMonth(year, month)
                val firstDayJdn = PersianDateUtil.jalaliToJdn(year, month, 1)
                val firstWeekdayOffset = PersianDateUtil.weekdayIndex(firstDayJdn)

                val cells = remember(year, month) {
                    val list = mutableListOf<Int?>()
                    repeat(firstWeekdayOffset) { list.add(null) }
                    for (d in 1..daysInMonth) list.add(d)
                    list
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(240.dp)
                ) {
                    items(cells) { day ->
                        if (day == null) {
                            Box(modifier = Modifier.aspectRatio(1f))
                        } else {
                            val thisJdn = PersianDateUtil.jalaliToJdn(year, month, day)
                            val isToday = thisJdn == PersianDateUtil.todayJdn()
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isToday) MaterialTheme.colorScheme.primaryContainer
                                        else androidx.compose.ui.graphics.Color.Transparent
                                    )
                                    .clickable { onDateSelected(thisJdn) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = PersianDateUtil.toPersianDigits(day.toString()),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}
