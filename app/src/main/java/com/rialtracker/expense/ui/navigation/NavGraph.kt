package com.rialtracker.expense.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rialtracker.expense.data.Expense
import com.rialtracker.expense.ui.screens.*
import com.rialtracker.expense.util.ReportPeriod
import com.rialtracker.expense.viewmodel.ExpenseViewModel

object Routes {
    const val DASHBOARD = "dashboard"
    const val ADD_EXPENSE = "add_expense"
    const val EDIT_EXPENSE = "edit_expense/{id}"
    const val CONFIRM_SMS = "confirm_sms/{id}"
    const val REPORTS = "reports"
    const val PENDING = "pending"
    const val SETTINGS = "settings"
}

private data class BottomItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val bottomItems = listOf(
    BottomItem(Routes.DASHBOARD, "خانه", Icons.Filled.Home),
    BottomItem(Routes.REPORTS, "گزارش‌ها", Icons.Filled.ShowChart),
    BottomItem(Routes.SETTINGS, "تنظیمات", Icons.Filled.Settings)
)

@Composable
fun AppNavGraph(
    viewModel: ExpenseViewModel,
    smsPermissionGranted: Boolean,
    onRequestSmsPermission: () -> Unit,
    onExportExcel: (List<Expense>, ReportPeriod) -> Unit,
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit,
    startDestination: String = Routes.DASHBOARD
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val expenses by viewModel.expenses.collectAsState()
    val pending by viewModel.pendingExpenses.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomItems.map { it.route }) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(Routes.DASHBOARD) { inclusive = false }
                                    launchSingleTop = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    expenses = expenses,
                    pendingCount = pending.size,
                    categories = categories,
                    accounts = accounts,
                    onAddClick = { navController.navigate(Routes.ADD_EXPENSE) },
                    onPendingClick = { navController.navigate(Routes.PENDING) },
                    onExpenseClick = { navController.navigate("edit_expense/${it.id}") }
                )
            }

            composable(Routes.ADD_EXPENSE) {
                AddExpenseScreen(
                    categories = categories,
                    accounts = accounts,
                    onBack = { navController.popBackStack() },
                    onSave = { amount, catId, accId, note, date ->
                        viewModel.addExpense(amount, catId, accId, date, note)
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.EDIT_EXPENSE) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toLongOrNull()
                val expense = expenses.find { it.id == id }
                if (expense != null) {
                    AddExpenseScreen(
                        categories = categories,
                        accounts = accounts,
                        initialAmount = expense.amountRial,
                        initialCategoryId = expense.categoryId,
                        initialAccountId = expense.accountId,
                        initialNote = expense.note,
                        initialDateJdn = expense.dateEpochDay,
                        title = "ویرایش هزینه",
                        onBack = { navController.popBackStack() },
                        onSave = { amount, catId, accId, note, date ->
                            viewModel.updateExpense(
                                expense.copy(amountRial = amount, categoryId = catId, accountId = accId, note = note, dateEpochDay = date)
                            )
                            navController.popBackStack()
                        }
                    )
                }
            }

            composable(Routes.CONFIRM_SMS) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toLongOrNull()
                val expense = pending.find { it.id == id }
                if (expense != null) {
                    AddExpenseScreen(
                        categories = categories,
                        accounts = accounts,
                        initialAmount = expense.amountRial,
                        initialCategoryId = expense.categoryId,
                        initialAccountId = expense.accountId,
                        initialDateJdn = expense.dateEpochDay,
                        smsRawText = expense.smsRawText,
                        title = "تایید هزینه‌ی پیامکی",
                        onBack = { navController.popBackStack() },
                        onSave = { amount, catId, accId, note, date ->
                            viewModel.confirmPendingExpense(
                                expense.copy(amountRial = amount, dateEpochDay = date), catId, accId, note
                            )
                            navController.popBackStack()
                        }
                    )
                }
            }

            composable(Routes.PENDING) {
                PendingSmsScreen(
                    pending = pending,
                    onBack = { navController.popBackStack() },
                    onConfirmClick = { navController.navigate("confirm_sms/${it.id}") },
                    onDelete = { viewModel.deleteExpense(it) }
                )
            }

            composable(Routes.REPORTS) {
                ReportsScreen(
                    expenses = expenses,
                    categories = categories,
                    accounts = accounts,
                    onBack = { navController.popBackStack() },
                    onExport = onExportExcel
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    categories = categories,
                    accounts = accounts,
                    smsPermissionGranted = smsPermissionGranted,
                    onRequestSmsPermission = onRequestSmsPermission,
                    onAddCategory = { name, color, icon -> viewModel.addCategory(name, color, icon) },
                    onUpdateCategory = { viewModel.updateCategory(it) },
                    onDeleteCategory = { viewModel.deleteCategory(it) },
                    onAddAccount = { name, type, bank, last4, smsId, color -> viewModel.addAccount(name, type, bank, last4, smsId, color) },
                    onUpdateAccount = { viewModel.updateAccount(it) },
                    onDeleteAccount = { viewModel.deleteAccount(it) },
                    onBackupClick = onBackupClick,
                    onRestoreClick = onRestoreClick
                )
            }
        }
    }
}
