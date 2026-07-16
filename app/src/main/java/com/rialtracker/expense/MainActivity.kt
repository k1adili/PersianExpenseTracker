package com.rialtracker.expense

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.rialtracker.expense.data.Expense
import com.rialtracker.expense.ui.navigation.AppNavGraph
import com.rialtracker.expense.ui.navigation.Routes
import com.rialtracker.expense.ui.theme.RialTrackerTheme
import com.rialtracker.expense.util.BackupManager
import com.rialtracker.expense.util.ExcelExporter
import com.rialtracker.expense.util.ReportPeriod
import com.rialtracker.expense.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: ExpenseViewModel by viewModels { ExpenseViewModel.factory(application) }

    private var smsPermissionGranted by mutableStateOf(false)

    private val requestSmsPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        smsPermissionGranted = results.values.all { it }
        if (!smsPermissionGranted) {
            Toast.makeText(this, "بدون این دسترسی، شناسایی خودکار مبلغ از پیامک ممکن نیست.", Toast.LENGTH_LONG).show()
        }
    }

    private val restoreFileLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) restoreFromUri(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        smsPermissionGranted = hasSmsPermission()
        val openPending = intent?.getBooleanExtra("open_pending_sms", false) == true

        setContent {
            RialTrackerTheme {
                CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        AppNavGraph(
                            viewModel = viewModel,
                            smsPermissionGranted = smsPermissionGranted,
                            onRequestSmsPermission = { requestSmsPermission.launch(smsPermissions()) },
                            onExportExcel = { expenses, period -> exportAndShareExcel(expenses, period) },
                            onBackupClick = { backupAndShare() },
                            onRestoreClick = { restoreFileLauncher.launch(arrayOf("application/json", "text/*", "*/*")) },
                            startDestination = if (openPending) Routes.PENDING else Routes.DASHBOARD
                        )
                    }
                }
            }
        }
    }

    private fun smsPermissions() = arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)

    private fun hasSmsPermission(): Boolean {
        return smsPermissions().all {
            checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun exportAndShareExcel(expenses: List<Expense>, period: ReportPeriod) {
        lifecycleScope.launch {
            val categories = viewModel.categories.value
            val accounts = viewModel.accounts.value
            val file = ExcelExporter.export(this@MainActivity, expenses, categories, accounts, period)
            shareFile(file, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "اشتراک‌گذاری گزارش اکسل")
        }
    }

    private fun backupAndShare() {
        lifecycleScope.launch {
            val categories = viewModel.categories.value
            val accounts = viewModel.accounts.value
            val expenses = viewModel.getAllExpensesOnce()
            val data = com.rialtracker.expense.util.BackupData(categories, accounts, expenses)
            val file = BackupManager.writeBackupFile(this@MainActivity, data)
            shareFile(file, "application/json", "ذخیره‌ی فایل پشتیبان")
        }
    }

    private fun shareFile(file: java.io.File, mimeType: String, chooserTitle: String) {
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, chooserTitle))
    }

    private fun restoreFromUri(uri: Uri) {
        lifecycleScope.launch {
            try {
                val data = contentResolver.openInputStream(uri)?.use { BackupManager.readBackupFile(it) }
                    ?: throw IllegalStateException("فایل قابل خواندن نیست")
                viewModel.wipeAllData()
                viewModel.restoreAllData(data.categories, data.accounts, data.expenses)
                Toast.makeText(this@MainActivity, "بازیابی با موفقیت انجام شد.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "خطا در بازیابی فایل پشتیبان: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
