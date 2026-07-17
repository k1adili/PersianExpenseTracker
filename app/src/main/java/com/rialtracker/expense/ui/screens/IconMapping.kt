package com.rialtracker.expense.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.rialtracker.expense.data.AccountType

/** فهرست کلید-آیکون‌هایی که کاربر هنگام ساخت دسته‌بندی می‌تواند انتخاب کند */
val categoryIconChoices: List<Pair<String, ImageVector>> = listOf(
    "food" to Icons.Filled.Restaurant,
    "car" to Icons.Filled.Commute, // حمل‌ونقل: ترکیب خودرو/اتوبوس
    "bill" to Icons.Filled.ReceiptLong,
    "shopping" to Icons.Filled.ShoppingBag,
    "health" to Icons.Filled.LocalHospital,
    "fun" to Icons.Filled.Celebration,
    "home" to Icons.Filled.House,
    "book" to Icons.Filled.MenuBook,
    "gift" to Icons.Filled.CardGiftcard,
    "phone" to Icons.Filled.PhoneAndroid,
    "pet" to Icons.Filled.Pets,
    "sport" to Icons.Filled.SportsSoccer,
    "other" to Icons.Filled.Label
)

private val categoryIconMap = categoryIconChoices.toMap()

fun categoryIconFor(key: String): ImageVector = categoryIconMap[key] ?: Icons.Filled.Label

fun accountIconFor(type: AccountType): ImageVector =
    if (type == AccountType.CASH) Icons.Filled.Payments else Icons.Filled.AccountBalance
