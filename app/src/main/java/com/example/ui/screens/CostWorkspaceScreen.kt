package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import com.example.data.local.entities.VehicleEntity
import android.widget.Toast
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream
import com.example.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.ui.text.input.KeyboardType
import java.util.Calendar
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import java.time.ZoneId
import java.time.LocalDate
import java.math.BigDecimal
import com.example.core.DateUtils
import com.example.data.local.entities.ExpenseEntity
import com.example.data.local.entities.GamaeyaInstallmentEntity
import com.example.ui.viewmodel.FinanceViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Calm neutral finance palette: slate, soft grey and white (no metallic/gold accents).
private val CostInk = Color(0xFF263238)
private val CostGold = Color(0xFF78909C)
private val CostGoldDark = Color(0xFF546E7A)
private val CostCanvas = Color(0xFFF4F6F8)
private val CostBorder = Color(0xFFE1E7EC)
private val CostMuted = Color(0xFF687782)

private data class CostSection(val id: String, val title: String, val subtitle: String, val icon: ImageVector, val group: String)
private data class CostTab(val id: String, val title: String)

private val costSections = listOf(
    CostSection("house", "المنزل", "مصروفات البيت والاحتياجات اليومية", Icons.Outlined.Home, "المصروفات"),
    CostSection("medical", "المصروفات الطبية", "الأفراد والمنشآت والتشخيص والعلاج", Icons.Outlined.MedicalServices, "المصروفات"),
    CostSection("work", "العمل", "تكاليف ومصروفات العمل", Icons.Outlined.Work, "المصروفات"),
    CostSection("personal", "المصروفات الشخصية", "المصروفات اليومية والالتزامات الشخصية", Icons.Outlined.Person, "المصروفات"),
    CostSection("associations", "الجمعيات", "قيمة الجمعية والأقساط والقبض", Icons.Outlined.Groups, "المصروفات"),
    CostSection("vehicles", "السيارات", "السيارات والوقود والصيانة والزيوت والحوادث", Icons.Outlined.DirectionsCar, "السيارات"),
    CostSection("education", "التعليم والطلاب", "ملفات الطلاب ومصروفاتهم", Icons.Outlined.MenuBook, "المصروفات"),
    CostSection("income", "الدخل", "مصادر الدخل والإيرادات", Icons.Outlined.TrendingUp, "الأموال والحسابات"),
    CostSection("certificates", "الشهادات البنكية", "بيانات الشهادات والعوائد والاستحقاقات", Icons.Outlined.AccountBalance, "الأموال والحسابات"),
    CostSection("current_account", "الحساب الجاري", "الإيداعات والسحوبات وصافي الحركة", Icons.Outlined.AccountBalanceWallet, "الأموال والحسابات"),
    CostSection("car_income", "دخل رحلات السيارة", "الرحلات والأجرة والتقارير", Icons.Outlined.DirectionsCar, "الأموال والحسابات"),
    CostSection("reports", "التقارير والتحليلات", "تقارير اليوم والأسبوع والشهر", Icons.Outlined.Assessment, "المتابعة والتحليل"),
    CostSection("backup", "النسخ الاحتياطي والاستعادة", "تصدير النسخة ومراجعة الاستعادة", Icons.Outlined.ReceiptLong, "المتابعة والتحليل")
)

private fun tabsFor(id: String): List<CostTab> = when (id) {
    "house" -> listOf(CostTab("records", "سجلات المنزل"), CostTab("form", "إضافة مصروف منزلي"))
    "medical" -> listOf(CostTab("records", "السجلات الطبية"), CostTab("form", "إضافة سجل طبي"))
    "work" -> listOf(CostTab("records", "سجلات العمل"), CostTab("form", "إضافة مصروف عمل"))
    "personal" -> listOf(CostTab("records", "السجلات الشخصية"), CostTab("form", "إضافة مصروف شخصي"))
    "associations" -> listOf(CostTab("association_list", "سجلات الجمعيات"), CostTab("form", "جمعية جديدة"), CostTab("installments", "الأقساط"), CostTab("payouts", "القبض"))
    "vehicles" -> listOf(CostTab("records", "بيانات السيارة"), CostTab("fuel", "الوقود"), CostTab("maintenance", "الصيانة"), CostTab("oils", "تغيير الزيت والفلاتر"), CostTab("accidents", "الحوادث"))
    "education" -> listOf(CostTab("students", "سجلات الطلاب"), CostTab("student_add", "إضافة طالب"), CostTab("student_edit", "تعديل طالب"), CostTab("student_expenses", "سجلات مصروفات الطلاب"), CostTab("student_expense_form", "إضافة مصروف طالب"))
    "income" -> listOf(CostTab("sources", "مصادر الدخل"), CostTab("form", "إضافة مصدر دخل"))
    "certificates" -> listOf(CostTab("list", "الشهادات المسجلة"), CostTab("form", "بيانات الشهادة"), CostTab("payouts", "العوائد"))
    "current_account" -> listOf(CostTab("movements", "كل الحركات"), CostTab("deposits", "إضافة إيداع"), CostTab("withdrawals", "إضافة سحب"))
    "car_income" -> listOf(CostTab("trips", "سجل الرحلات"), CostTab("add_trip", "تسجيل رحلة"), CostTab("trip_report", "تقرير الرحلات"))
    "reports" -> listOf(CostTab("daily", "تقرير يومي"), CostTab("weekly", "تقرير أسبوعي"), CostTab("monthly", "تقرير شهري"))
    "backup" -> listOf(CostTab("export", "تصدير نسخة"), CostTab("restore", "استعادة نسخة"))
    else -> listOf(CostTab("records", "السجلات"), CostTab("form", "نموذج الإدخال"))
}

private fun exportActionsFor(sectionId: String, tabId: String): List<String> = when (sectionId) {
    "house", "work", "personal", "medical", "vehicles", "education", "associations", "income", "certificates", "current_account" -> listOf("تصدير CSV")
    "car_income" -> listOf("تصدير CSV")
    "reports" -> listOf("تصدير CSV")
    else -> emptyList()
}

private fun fieldsFor(sectionId: String, tabId: String): List<String> = when (sectionId) {
    "house" -> listOf("التصنيف", "تصنيف مخصص (عند الحاجة)", "المبلغ", "التاريخ", "طريقة الدفع", "نوع الدفع: مستلزمات أو أجور", "الملاحظات")
    "medical" -> listOf("فرد الأسرة", "اسم المنشأة الطبية", "نوع المنشأة الطبية", "الحالة", "التشخيص", "تكلفة الكشف أو الفحص", "تكلفة الدواء", "التاريخ", "ملاحظات طبية")
    "work" -> listOf("نوع المصروف", "نوع مخصص", "المبلغ", "التاريخ", "الملاحظات")
    "personal" -> listOf("التصنيف", "تصنيف مخصص (عند الحاجة)", "المبلغ", "التاريخ", "طريقة الدفع", "الملاحظات")
    "associations" -> if (tabId == "form") listOf("اسم الجمعية", "قيمة الجمعية", "عدد الأفراد", "قيمة القسط الشهري", "رقم الدور", "تاريخ البداية", "تاريخ القبض", "ملاحظات") else listOf("اسم الجمعية", "الشهر", "قيمة القسط أو القبض", "التاريخ", "ملاحظات")
    "vehicles" -> when (tabId) {
        "fuel" -> listOf("المركبة", "نوع الوقود", "الكمية أو اللترات", "سعر الوحدة", "إجمالي التكلفة", "قراءة العداد", "التاريخ والوقت", "ملاحظات")
        "maintenance" -> listOf("المركبة", "نوع الصيانة", "وصف الصيانة", "اسم المستلزم أو قطعة الغيار", "سعر المستلزمات", "وصف الأجور", "قيمة الأجور", "التاريخ", "الإجمالي")
        "oils" -> listOf("المركبة", "نوع الخدمة", "العلامة التجارية", "اسم المنتج", "الكمية", "السعر", "قراءة العداد", "فترة التغيير أو المسافة المقترحة", "التاريخ", "ملاحظات")
        else -> listOf("المركبة (اختياري)", "عنوان الحادث", "صورة الحادث", "التاريخ", "الوقت", "الموقع (اختياري)", "تقدير الضرر — ليس مبلغًا مدفوعًا تلقائيًا", "ملاحظات")
    }
    "education" -> if (tabId in setOf("student_add", "student_edit")) listOf("اسم الطالب", "المرحلة التعليمية", "العمر", "الرقم القومي أو المرجعي — 14 رقمًا") else listOf("الطالب", "تصنيف المصروف", "عنوان المصروف", "المبلغ", "التاريخ")
    "income" -> listOf("اسم مصدر الدخل", "المبلغ", "التاريخ", "نوع الدخل", "ملاحظات")
    "certificates" -> if (tabId == "payouts") listOf("الشهادة", "تاريخ صرف العائد", "قيمة العائد", "ملاحظات") else listOf("اسم البنك", "رقم الشهادة", "المبلغ الأصلي", "معدل العائد السنوي", "مدة الشهادة", "دورية صرف العائد", "تاريخ الإصدار", "تاريخ الاستحقاق", "الملاحظات")
    "current_account" -> listOf(if (tabId == "deposits") "نوع الحركة: إيداع" else "نوع الحركة: سحب", "الوصف", "المبلغ", "التاريخ")
    "car_income" -> if (tabId == "trip_report") listOf("تاريخ البداية", "تاريخ النهاية", "نوع الرحلة", "بحث") else listOf("تاريخ الرحلة", "نوع الرحلة", "الأجرة", "ملاحظات الرحلة")
    "reports" -> listOf("تاريخ البداية", "تاريخ النهاية", "القسم", "الطالب (عند الحاجة)")
    "backup" -> listOf("ملف النسخة الاحتياطية", "إصدار المخطط", "تاريخ التصدير", "المجموعات التي ستُستبدل")
    else -> listOf("التصنيف", "المبلغ", "التاريخ", "طريقة الدفع", "الملاحظات")
}

private fun choicesFor(sectionId: String, tabId: String): List<String> = when (sectionId) {
    "house" -> listOf("إفطار", "غداء", "عشاء", "إصلاحات كهرباء وسباكة ودهانات", "أجهزة منزلية", "أثاث ومفروشات", "فواتير", "منظفات ومستلزمات", "أخرى", "نقدي", "Visa / Mastercard", "تحويل بنكي / Instapay", "محفظة إلكترونية", "شيك / آجل")
    "work" -> listOf("أدوات مكتبية", "صيانة أجهزة العمل", "اشتراكات برامج", "ضيافة واجتماعات", "انتقالات العمل", "شحن وتوصيل", "أخرى")
    "personal" -> listOf("مأكولات ومشروبات", "انتقالات", "ملابس", "صحة وعناية شخصية", "ترفيه", "هدايا", "اشتراكات شخصية", "فواتير واتصالات", "أخرى", "نقدي", "Visa / Mastercard", "تحويل بنكي / Instapay", "محفظة إلكترونية", "شيك / آجل")
    "vehicles" -> when (tabId) {
        "fuel" -> listOf("بنزين 80", "بنزين 92", "بنزين 95", "سولار", "غاز طبيعي", "شحن كهربائي")
        "oils" -> listOf("زيوت", "فلاتر", "Mobil", "متابعة 5,000 كم")
        else -> emptyList()
    }
    "education" -> if (tabId in setOf("student_add", "student_edit")) listOf("رياض أطفال", "ابتدائي", "إعدادي", "ثانوي", "جامعة") else listOf("دروس خصوصية", "شخصي", "انتقالات", "مصروفات مدرسية", "كتب ومذكرات", "أتوبيس المدرسة", "مستلزمات", "أخرى")
    "income" -> listOf("راتب", "دخل إضافي", "مكافأة", "استثمار", "أخرى")
    "certificates" -> if (tabId == "form") listOf("6 أشهر", "سنة", "سنتان", "3 سنوات", "5 سنوات", "شهري", "ربع سنوي", "نصف سنوي", "سنوي", "عند الاستحقاق") else emptyList()
    "car_income" -> if (tabId == "add_trip") listOf("نقدي", "Uber", "DiDi", "inDrive", "خاص") else if (tabId == "trip_report") listOf("الكل", "نقدي", "Uber", "DiDi", "inDrive", "خاص") else emptyList()
    "reports" -> listOf("الكل", "الدخل", "المنزل", "الشخصي", "الجمعيات", "المركبات", "التعليم")
    else -> emptyList()
}

@Composable
fun CostWorkspaceScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf("") }
    var orderedSections by remember { mutableStateOf(costSections) }
    var isReorderMode by remember { mutableStateOf(false) }
    var showDesignNotice by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }
    var workspaceSearch by remember { mutableStateOf("") }
    val financeViewModel: FinanceViewModel = viewModel(factory = FinanceViewModel.Factory)
    val savedExpenses by financeViewModel.allExpenses.collectAsState()
    val savedRevenues by financeViewModel.allRevenues.collectAsState()
    val savedVehicles by financeViewModel.allVehicles.collectAsState()
    val savedVehicleLogs by financeViewModel.allVehicleLogs.collectAsState()
    val savedStudents by financeViewModel.allStudents.collectAsState()
    val savedTreasuryMovements by financeViewModel.allTreasuryMovements.collectAsState()
    val financeSummary by financeViewModel.currentMonthSummary.collectAsState()
    val savedCertificates by financeViewModel.allCertificates.collectAsState()
    val savedCertificateSchedules by financeViewModel.allUncollectedCertificateSchedules.collectAsState()
    val savedGamaeyat by financeViewModel.allGamaeyat.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val carTripCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) runCatching {
            val rows = savedRevenues.filter { it.sourceType == "CAR_TRIP" }.sortedByDescending { it.date }
            val csv = buildString {
                append('﻿')
                appendLine("التاريخ,نوع الرحلة,الأجرة_بالقرش,الملاحظات")
                rows.forEach { item ->
                    val fields = listOf(DateUtils.formatDateShort(item.date), item.title, item.amountPiastres.toString(), item.notes.orEmpty())
                    appendLine(fields.joinToString(",") { field -> "\"" + field.replace("\"", "\"\"") + "\"" })
                }
            }
            val output = context.contentResolver.openOutputStream(uri) ?: error("تعذر فتح ملف التصدير")
            output.bufferedWriter(Charsets.UTF_8).use { it.write(csv) }
        }.onSuccess {
            Toast.makeText(context, "تم تصدير سجل الرحلات", Toast.LENGTH_LONG).show()
        }.onFailure {
            Toast.makeText(context, "تعذر تصدير الرحلات: " + (it.localizedMessage ?: "خطأ غير معروف"), Toast.LENGTH_LONG).show()
        }
    }
    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) coroutineScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val database = AppDatabase.getDatabase(context)
                    database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { cursor ->
                        while (cursor.moveToNext()) Unit
                    }
                    val databaseFile = context.getDatabasePath("smart_cost_pro_database")
                    check(databaseFile.exists()) { "ملف قاعدة البيانات غير موجود" }
                    val output = context.contentResolver.openOutputStream(uri) ?: error("تعذر فتح ملف النسخة")
                    FileInputStream(databaseFile).use { input -> output.use { input.copyTo(it) } }
                }
            }
            result.onSuccess {
                Toast.makeText(context, "تم إنشاء نسخة احتياطية من قاعدة البيانات", Toast.LENGTH_LONG).show()
            }.onFailure {
                Toast.makeText(context, "تعذر إنشاء النسخة: " + (it.localizedMessage ?: "خطأ غير معروف"), Toast.LENGTH_LONG).show()
            }
        }
    }
    val homeCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            runCatching {
                val department = selectedSection.orEmpty()
                fun csvRow(vararg fields: String?): String = fields.joinToString(",") { field ->
                    "\"" + field.orEmpty().replace("\"", "\"\"") + "\""
                }
                val csv = buildString {
                    append('\uFEFF')
                    when (department) {
                        "vehicles" -> {
                            appendLine(csvRow("السيارة", "نوع السجل", "التاريخ", "العداد_كم", "المبلغ_جنيه", "التفاصيل", "ملاحظات"))
                            savedVehicleLogs.sortedByDescending { it.date }.forEach { log ->
                                val vehicleName = savedVehicles.firstOrNull { it.vehicleId == log.vehicleId }?.name.orEmpty()
                                appendLine(csvRow(vehicleName, log.logType, DateUtils.formatDateShort(log.date), log.odometer?.toString(), log.amount, log.serviceType ?: log.serviceCenter, log.notes))
                            }
                        }
                        "education" -> {
                            appendLine(csvRow("نوع السجل", "الطالب", "التاريخ", "التصنيف", "البيان", "المبلغ_بالقرش", "ملاحظات"))
                            savedStudents.forEach { student -> appendLine(csvRow("طالب", student.fullName, "", student.academicStage, student.schoolOrInstitution, student.monthlyBudgetPiastres?.toString(), student.academicYear)) }
                            savedExpenses.filter { it.department.equals("EDUCATION", true) }.sortedByDescending { it.date }.forEach { expense ->
                                val studentName = savedStudents.firstOrNull { it.studentId == expense.studentId }?.fullName.orEmpty()
                                appendLine(csvRow("مصروف", studentName, DateUtils.formatDateShort(expense.date), expense.subcategory, expense.title, expense.amountPiastres.toString(), expense.notes))
                            }
                        }
                        "income" -> {
                            appendLine(csvRow("التاريخ", "مصدر الدخل", "البيان", "المبلغ_بالقرش", "الجهة", "ملاحظات"))
                            savedRevenues.sortedByDescending { it.date }.forEach { item -> appendLine(csvRow(DateUtils.formatDateShort(item.date), item.sourceType, item.title, item.amountPiastres.toString(), item.payerOrInstitution, item.notes)) }
                        }
                        "associations" -> {
                            appendLine(csvRow("اسم الجمعية", "قيمة الجمعية_بالقرش", "عدد الأفراد", "قيمة القسط_بالقرش", "رقم الدور", "تاريخ البداية", "تاريخ القبض المتوقع", "الحالة", "تم القبض"))
                            savedGamaeyat.forEach { item -> appendLine(csvRow(item.name, item.totalPoolValuePiastres.toString(), item.numberOfMembers.toString(), item.monthlyInstallmentPiastres.toString(), item.myTurnOrder.toString(), DateUtils.formatDateShort(item.startDate), DateUtils.formatDateShort(item.expectedPayoutDate), item.status, item.isPayoutReceived.toString())) }
                        }
                        "certificates" -> {
                            appendLine(csvRow("الشهادة", "البنك", "أصل الشهادة_بالقرش", "العائد السنوي", "دورية الصرف", "المدة_سنوات", "تاريخ الإصدار", "تاريخ الاستحقاق", "نشطة"))
                            savedCertificates.forEach { item -> appendLine(csvRow(item.name, item.bankName, item.principalPiastres.toString(), item.annualRatePercent, item.payoutFrequency, item.durationYears.toString(), DateUtils.formatDateShort(item.issueDate), DateUtils.formatDateShort(item.maturityDate), item.isActive.toString())) }
                        }
                        "current_account" -> {
                            appendLine(csvRow("التاريخ", "نوع الحركة", "المبلغ_بالقرش", "الرصيد قبل_بالقرش", "الرصيد بعد_بالقرش", "البيان"))
                            savedTreasuryMovements.sortedByDescending { it.date }.forEach { item -> appendLine(csvRow(DateUtils.formatDateShort(item.date), item.movementType, item.amountPiastres.toString(), item.balanceBeforePiastres.toString(), item.balanceAfterPiastres.toString(), item.reason)) }
                        }
                        "reports" -> {
                            val today = LocalDate.now()
                            val start = when (selectedTab) {
                                "daily" -> today
                                "weekly" -> today.with(java.time.DayOfWeek.MONDAY)
                                else -> today.withDayOfMonth(1)
                            }
                            val startEpoch = start.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                            val endEpoch = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1L
                            appendLine(csvRow("نوع الحركة", "التاريخ", "التصنيف", "البيان", "المبلغ_بالقرش", "ملاحظات"))
                            savedExpenses.filter { it.date in startEpoch..endEpoch }.sortedByDescending { it.date }.forEach { item ->
                                appendLine(csvRow("مصروف", DateUtils.formatDateShort(item.date), item.department, item.title, item.amountPiastres.toString(), item.notes))
                            }
                            savedRevenues.filter { it.date in startEpoch..endEpoch }.sortedByDescending { it.date }.forEach { item ->
                                appendLine(csvRow("دخل", DateUtils.formatDateShort(item.date), item.sourceType, item.title, item.amountPiastres.toString(), item.notes))
                            }
                        }
                        "car_income" -> {
                            appendLine(csvRow("التاريخ", "نوع الرحلة", "الأجرة_بالقرش", "الملاحظات"))
                            savedRevenues.filter { it.sourceType == "CAR_TRIP" }.sortedByDescending { it.date }.forEach { item -> appendLine(csvRow(DateUtils.formatDateShort(item.date), item.title, item.amountPiastres.toString(), item.notes)) }
                        }
                        else -> {
                            val rows = savedExpenses.filter { it.department.equals(department.uppercase(), ignoreCase = true) }.sortedByDescending { it.date }
                            appendLine(csvRow("التاريخ", "التصنيف", "البيان", "المبلغ_بالقرش", "الملاحظات"))
                            rows.forEach { expense -> appendLine(csvRow(DateUtils.formatDateShort(expense.date), expense.subcategory, expense.title, expense.amountPiastres.toString(), expense.notes)) }
                        }
                    }
                }
                val output = context.contentResolver.openOutputStream(uri)
                    ?: error("تعذر فتح ملف التصدير")
                output.bufferedWriter(Charsets.UTF_8).use { it.write(csv) }
            }.onSuccess {
                Toast.makeText(context, "تم تصدير سجلات القسم بنجاح", Toast.LENGTH_LONG).show()
            }.onFailure {
                Toast.makeText(context, "تعذر التصدير: " + (it.localizedMessage ?: "خطأ غير معروف"), Toast.LENGTH_LONG).show()
            }
        }
    }
    val unpaidGamaeyaInstallments by financeViewModel.allUnpaidGamaeyaInstallments.collectAsState()
    val section = costSections.firstOrNull { it.id == selectedSection }
    val tabs = section?.let { tabsFor(it.id) }.orEmpty()
    val activeTab = tabs.firstOrNull { it.id == selectedTab }
    fun matchesSearch(vararg fields: String?): Boolean =
        workspaceSearch.isBlank() || fields.any { it?.contains(workspaceSearch.trim(), ignoreCase = true) == true }

    BackHandler(enabled = selectedSection != null) {
        if (selectedTab.isNotBlank()) {
            selectedTab = ""
        } else {
            selectedSection = null
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CostCanvas,
        topBar = {
            Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    if (selectedTab.isNotBlank()) {
                        selectedTab = ""
                    } else if (selectedSection != null) {
                        selectedSection = null
                    } else onNavigateBack()
                }) { Icon(Icons.Outlined.ArrowForward, contentDescription = "رجوع", tint = CostInk) }
                Column(Modifier.weight(1f)) {
                    Text(activeTab?.title ?: section?.title ?: "قسم التكاليف", color = CostInk, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(section?.subtitle ?: "إدارة المصروفات والدخل والحسابات في مكان واحد", color = CostMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFEAF0F3)) {
                    Text("SMART COST", color = CostGoldDark, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp))
                }
            }
        }
    ) { padding ->
        if (section == null) {
            LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), contentPadding = PaddingValues(top = 14.dp, bottom = 26.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("كل الأقسام", color = CostInk, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("اختاري القسم المطلوب لعرض بطاقاته الفرعية وسجلاته ونماذج الإدخال.", color = CostMuted, fontSize = 12.sp)
                        Text("13 قسمًا رئيسيًا", color = CostGoldDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, CostBorder)
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text("إدارة مالية موحدة", color = CostInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "اختاري المجال أولًا، ثم انتقلي إلى سجلاته أو نموذج الإدخال أو تقاريره. لكل مجال بياناته الخاصة.",
                                color = CostMuted,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { isReorderMode = !isReorderMode },
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, CostBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            if (isReorderMode) Icons.Outlined.Check else Icons.Outlined.SwapVert,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(if (isReorderMode) "إنهاء الترتيب" else "إعادة ترتيب الأقسام", fontSize = 11.sp)
                    }
                    if (isReorderMode) {
                        TextButton(onClick = { orderedSections = costSections }) {
                            Text("الترتيب الافتراضي", color = CostGoldDark, fontSize = 10.sp)
                        }
                    }
                }
                if (isReorderMode) {
                    item {
                        Text(
                            "حرّكي الأقسام داخل مجموعتها باستخدام السهمين، ثم اختاري إنهاء الترتيب.",
                            color = CostMuted,
                            fontSize = 11.sp
                        )
                    }
                }
                orderedSections.groupBy { it.group }.forEach { (groupTitle, groupSections) ->
                    item {
                        Row(
                            Modifier.fillMaxWidth().padding(top = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(groupTitle, color = CostInk, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFEAF0F3)
                            ) {
                                Text(
                                    groupSections.size.toString(),
                                    color = CostGoldDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                    groupSections.chunked(2).forEach { rowSections ->
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                rowSections.forEach { sectionItem ->
                                    val positionInGroup = groupSections.indexOf(sectionItem)
                                    CostSectionCard(
                                        section = sectionItem,
                                        modifier = Modifier.weight(1f),
                                        isReorderMode = isReorderMode,
                                        canMoveUp = positionInGroup > 0,
                                        canMoveDown = positionInGroup < groupSections.lastIndex,
                                        onMoveUp = {
                                            val currentIndex = orderedSections.indexOf(sectionItem)
                                            val previousIndex = orderedSections.indexOf(groupSections[positionInGroup - 1])
                                            orderedSections = orderedSections.toMutableList().also { list ->
                                                val item = list[currentIndex]
                                                list[currentIndex] = list[previousIndex]
                                                list[previousIndex] = item
                                            }
                                        },
                                        onMoveDown = {
                                            val currentIndex = orderedSections.indexOf(sectionItem)
                                            val nextIndex = orderedSections.indexOf(groupSections[positionInGroup + 1])
                                            orderedSections = orderedSections.toMutableList().also { list ->
                                                val item = list[currentIndex]
                                                list[currentIndex] = list[nextIndex]
                                                list[nextIndex] = item
                                            }
                                        },
                                        onClick = {
                                            if (!isReorderMode) {
                                                selectedSection = sectionItem.id
                                                selectedTab = ""
                                                workspaceSearch = ""
                                            }
                                        }
                                    )
                                }
                                if (rowSections.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        } else if (activeTab == null) {
            LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), contentPadding = PaddingValues(top = 14.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("الأقسام الفرعية — ${section.title}", color = CostInk, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        Text("اختاري بطاقة لفتح سجلات القسم ونموذج الإدخال الخاص به.", color = CostMuted, fontSize = 12.sp)
                    }
                }
                tabs.chunked(2).forEach { rowTabs ->
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            rowTabs.forEach { tab ->
                                CostTabCard(tab, Modifier.weight(1f)) { selectedTab = tab.id }
                            }
                            if (rowTabs.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                item {
                    Surface(shape = RoundedCornerShape(18.dp), color = CostInk, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(section.icon, null, tint = CostGold, modifier = Modifier.size(25.dp))
                                Spacer(Modifier.width(9.dp))
                                Text(section.title, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(section.subtitle, color = Color(0xFFE7EDF0), fontSize = 11.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                SummaryPill("إجمالي الفترة: —")
                                SummaryPill("عدد السجلات: —")
                            }
                        }
                    }
                }
                if (tabs.size > 1) {
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
                            items(tabs) { tab -> FilterChip(selected = activeTab?.id == tab.id, onClick = { selectedTab = tab.id }, label = { Text(tab.title, fontSize = 10.sp) }) }
                        }
                    }
                }
                if (section.id != "reports" && section.id != "backup") {
                    item {
                        OutlinedTextField(value = workspaceSearch, onValueChange = { workspaceSearch = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("بحث في " + section.title) }, placeholder = { Text("ابحثي بالاسم أو البيان أو التصنيف") }, shape = RoundedCornerShape(13.dp))
                    }
                }
                if (section.id != "reports" && section.id != "backup") {
                    val quickActions = when (section.id) {
                        "house" -> listOf("إضافة مصروف" to "form")
                        "medical" -> listOf("إضافة سجل طبي" to "form")
                        "work" -> listOf("إضافة مصروف عمل" to "form")
                        "personal" -> listOf("إضافة مصروف شخصي" to "form")
                        "associations" -> listOf("جمعية جديدة" to "form")
                        "vehicles" -> listOf("إضافة سيارة" to "records", "إضافة وقود" to "fuel", "إضافة صيانة" to "maintenance")
                        "education" -> listOf("إضافة طالب" to "student_add", "إضافة مصروف طالب" to "student_expense_form")
                        "income" -> listOf("إضافة مصدر دخل" to "form")
                        "certificates" -> listOf("إضافة شهادة" to "form")
                        "current_account" -> listOf("إضافة إيداع" to "deposits", "إضافة سحب" to "withdrawals")
                        "car_income" -> listOf("تسجيل رحلة" to "add_trip")
                        else -> emptyList()
                    }
                    if (quickActions.isNotEmpty()) item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            items(quickActions) { (action, targetTab) ->
                                OutlinedButton(onClick = { selectedTab = targetTab }, border = BorderStroke(1.dp, CostGold), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp)) {
                                    Text(action, color = CostInk, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
                val exportActions = exportActionsFor(section.id, activeTab?.id.orEmpty())
                if (exportActions.isNotEmpty()) {
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            items(exportActions) { action ->
                                OutlinedButton(
                                    onClick = {
                                        if (action.contains("CSV") || action.contains("Excel")) {
                                            if (section.id == "car_income") carTripCsvLauncher.launch("سجل_رحلات_السيارة.csv")
                                            else homeCsvLauncher.launch("تصدير_" + section.title + ".csv")
                                        } else showDesignNotice = true
                                    },
                                    border = BorderStroke(1.dp, CostBorder),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp)
                                ) {
                                    Text(action, color = CostInk, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
                item {
                    val formTitle = "نموذج البيانات — " + (activeTab?.title ?: "السجلات")
                    Text(if (section.id == "reports") "معاينة التقرير" else if (section.id == "backup") "إجراءات النسخ الاحتياطي" else formTitle, color = CostInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                if (section.id == "vehicles" && activeTab?.id == "records") {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                            Text("السيارات المسجلة", color = CostInk, fontWeight = FontWeight.Bold)
                            if (savedVehicles.isEmpty()) {
                                Text("لا توجد سيارات مسجلة بعد. أضيفي بيانات السيارة من النموذج أدناه.", color = CostMuted, fontSize = 12.sp)
                            } else savedVehicles.forEach { vehicle ->
                                Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, CostBorder), shape = RoundedCornerShape(13.dp)) {
                                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(vehicle.name, color = CostInk, fontWeight = FontWeight.Bold)
                                        Text("اللوحة: " + vehicle.plateNumber + " • العداد: " + vehicle.currentOdometerKm + " كم", color = CostMuted, fontSize = 11.sp)
                                        val logs = savedVehicleLogs.filter { it.vehicleId == vehicle.vehicleId }
                                        Text("سجلات الوقود والصيانة: " + logs.size, color = CostMuted, fontSize = 10.sp)
                                        logs.take(5).forEach { log ->
                                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                                    Text(
                                                        (when (log.logType) { "FUEL" -> "وقود"; "OIL" -> "زيوت وفلاتر"; "ACCIDENT" -> "حادث"; else -> "صيانة" }) + " • " +
                                                            DateUtils.formatDateArabic(log.date) + " • " + log.amount + " جنيه",
                                                        color = CostInk,
                                                        fontSize = 11.sp
                                                    )
                                                    val attachment = log.notes.substringAfter("مرفق صورة:", "").trim().takeIf { log.notes.contains("مرفق صورة:") && it.isNotBlank() }
                                                    if (attachment != null) AttachmentPreview(attachment)
                                                }
                                                TextButton(onClick = { financeViewModel.deleteVehicleLog(log.id, log.logType) }) { Text("حذف") }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (section.id == "car_income" && activeTab?.id == "trip_report") {
                    item {
                        val trips = savedRevenues.filter { it.sourceType == "CAR_TRIP" && matchesSearch(it.title, it.notes) }.sortedByDescending { it.date }
                        val total = trips.sumOf { it.amountPiastres }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("إجمالي دخل الرحلات: " + com.example.core.MoneyUtils.formatPiastresPlain(total) + " جنيه", color = CostInk, fontWeight = FontWeight.Bold)
                            Text("عدد الرحلات المسجلة: " + trips.size, color = CostMuted, fontSize = 11.sp)
                            SavedRevenueRecords(records = trips, onDelete = { financeViewModel.deleteRevenue(it.revenueId) })
                        }
                    }
                }
                if (activeTab?.id?.let { it in setOf("records", "list", "trips", "students", "student_expenses", "association_list", "movements", "sources", "installments", "payouts") } == true && !(section.id == "vehicles" && activeTab?.id == "records")) {
                    if (section.id in setOf("house", "medical", "work", "personal")) {
                        item {
                            SavedExpenseRecords(
                                records = savedExpenses.filter { it.department.equals(section.id.uppercase(), ignoreCase = true) && matchesSearch(it.title, it.subcategory, it.notes) },
                                onEdit = { record -> editingExpense = record; selectedTab = "form" },
                                onDelete = { financeViewModel.deleteExpense(it.expenseId) }
                            )
                        }
                    } else if (section.id == "certificates" && activeTab?.id == "list") {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val visibleCertificates = savedCertificates.filter { matchesSearch(it.name, it.bankName) }
                                if (visibleCertificates.isEmpty()) Text("لا توجد شهادات مطابقة للبحث.", color = CostMuted, fontSize = 12.sp)
                                visibleCertificates.forEach { cert ->
                                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, CostBorder)) {
                                        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(cert.name, color = CostInk, fontWeight = FontWeight.Bold)
                                            Text(cert.bankName + " • أصل الشهادة: " + com.example.core.MoneyUtils.formatPiastresPlain(cert.principalPiastres) + " جنيه", color = CostMuted, fontSize = 11.sp)
                                            TextButton(onClick = { financeViewModel.deleteCertificate(cert.certificateId) }) { Text("حذف الشهادة") }
                                            Text("العائد السنوي: " + cert.annualRatePercent + "% • الاستحقاق: " + DateUtils.formatDateArabic(cert.maturityDate), color = CostMuted, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    } else if (section.id == "certificates" && activeTab?.id == "payouts") {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (savedCertificateSchedules.isEmpty()) Text("لا توجد عوائد غير محصلة.", color = CostMuted, fontSize = 12.sp)
                                savedCertificateSchedules.forEach { schedule ->
                                    val cert = savedCertificates.firstOrNull { it.certificateId == schedule.certificateId }
                                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, CostBorder)) {
                                        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(cert?.name ?: "شهادة بنكية", color = CostInk, fontWeight = FontWeight.Bold)
                                            Text("الدفعة " + schedule.cycleNumber + " • " + DateUtils.formatDateArabic(schedule.expectedDueDate), color = CostMuted, fontSize = 11.sp)
                                            Text(com.example.core.MoneyUtils.formatPiastresPlain(schedule.expectedAmountPiastres) + " جنيه", color = CostInk, fontWeight = FontWeight.Bold)
                                            TextButton(onClick = { financeViewModel.collectCertificatePayout(schedule.scheduleId, DateUtils.currentEpochMillis()) }) { Text("تسجيل تحصيل العائد") }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (section.id == "current_account" && activeTab?.id == "movements") {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("الرصيد الحالي: " + com.example.core.MoneyUtils.formatPiastresPlain(financeSummary.currentLiquidBalancePiastres) + " جنيه", color = CostInk, fontWeight = FontWeight.Bold)
                                if (savedTreasuryMovements.isEmpty()) Text("لا توجد حركات مسجلة حتى الآن.", color = CostMuted, fontSize = 12.sp)
                                savedTreasuryMovements.forEach { movement ->
                                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, CostBorder)) {
                                        Column(Modifier.fillMaxWidth().padding(11.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                            Text(when (movement.movementType) { "DEPOSIT" -> "إيداع"; "WITHDRAWAL" -> "سحب"; else -> "تسوية رصيد" }, color = CostInk, fontWeight = FontWeight.Bold)
                                            Text(movement.reason, color = CostMuted, fontSize = 11.sp)
                                            Text(DateUtils.formatDateArabic(movement.date) + " • " + com.example.core.MoneyUtils.formatPiastresPlain(movement.amountPiastres) + " جنيه", color = CostInk, fontSize = 11.sp)
                                            Text("الرصيد بعد الحركة: " + com.example.core.MoneyUtils.formatPiastresPlain(movement.balanceAfterPiastres) + " جنيه", color = CostMuted, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    } else if (section.id == "education" && activeTab?.id == "students") {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val visibleStudents = savedStudents.filter { matchesSearch(it.fullName, it.academicStage, it.academicYear) }
                                if (visibleStudents.isEmpty()) Text("لا يوجد طلاب مطابقون للبحث.", color = CostMuted, fontSize = 12.sp)
                                visibleStudents.forEach { student ->
                                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, CostBorder)) {
                                        Column(Modifier.fillMaxWidth().padding(12.dp)) {
                                            Text(student.fullName, color = CostInk, fontWeight = FontWeight.Bold)
                                            Text(student.academicStage + " • " + student.academicYear, color = CostMuted, fontSize = 11.sp)
                                            Row {
                                                TextButton(onClick = { selectedTab = "student_edit" }) { Text("تعديل") }
                                                TextButton(onClick = { financeViewModel.deleteStudent(student.studentId) }) { Text("حذف") }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (section.id == "education" && activeTab?.id == "student_expenses") {
                        item {
                            SavedExpenseRecords(
                                records = savedExpenses.filter { it.department.equals("EDUCATION", true) && matchesSearch(it.title, it.subcategory, it.notes) },
                                onEdit = { showDesignNotice = true },
                                onDelete = { financeViewModel.deleteExpense(it.expenseId) }
                            )
                        }
                    } else if (section.id == "car_income" && activeTab?.id == "trips") {
                        item { SavedRevenueRecords(records = savedRevenues.filter { it.sourceType == "CAR_TRIP" && matchesSearch(it.title, it.notes) }, onDelete = { financeViewModel.deleteRevenue(it.revenueId) }) }
                    } else if (section.id == "income") {
                        item { SavedRevenueRecords(records = savedRevenues.filter { matchesSearch(it.title, it.notes, it.sourceType) }, onDelete = { financeViewModel.deleteRevenue(it.revenueId) }) }
                    } else if (section.id == "associations") {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                if (savedGamaeyat.isEmpty()) {
                                    Text("لا توجد جمعيات مسجلة حتى الآن.", color = CostMuted, fontSize = 12.sp)
                                } else if (activeTab?.id == "installments") {
                                    val outstanding = unpaidGamaeyaInstallments.sortedBy { it.dueDate }
                                    if (outstanding.isEmpty()) {
                                        Text("لا توجد أقساط غير مسددة.", color = CostMuted, fontSize = 12.sp)
                                    } else outstanding.forEach { installment ->
                                        val owner = savedGamaeyat.firstOrNull { it.gamaeyaId == installment.gamaeyaId }
                                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, CostBorder)) {
                                            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                                Text(owner?.name ?: "جمعية", color = CostInk, fontWeight = FontWeight.Bold)
                                                Text("القسط رقم ${installment.installmentNumber} • الاستحقاق: ${DateUtils.formatDateArabic(installment.dueDate)}", color = CostMuted, fontSize = 11.sp)
                                                Text("${com.example.core.MoneyUtils.formatPiastresPlain(installment.amountPiastres)} جنيه", color = CostGoldDark, fontWeight = FontWeight.Bold)
                                                Button(onClick = { financeViewModel.payGamaeyaInstallment(installment.installmentId) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = CostInk, contentColor = CostGold)) {
                                                    Text("تسجيل سداد القسط")
                                                }
                                            }
                                        }
                                    }
                                } else if (activeTab?.id == "payouts") {
                                    savedGamaeyat.forEach { gamaeya ->
                                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, CostBorder)) {
                                            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                                Text(gamaeya.name, color = CostInk, fontWeight = FontWeight.Bold)
                                                Text("قيمة القبض المتوقعة: ${com.example.core.MoneyUtils.formatPiastresPlain(gamaeya.totalPoolValuePiastres)} جنيه", color = CostMuted, fontSize = 11.sp)
                                                Text(if (gamaeya.isPayoutReceived) "تم تسجيل استلام القبض" else "لم يتم تسجيل استلام القبض", color = if (gamaeya.isPayoutReceived) Color(0xFF26734D) else CostGoldDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                if (!gamaeya.isPayoutReceived) {
                                                    Button(onClick = { financeViewModel.collectGamaeyaPayout(gamaeya.gamaeyaId) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = CostInk, contentColor = CostGold)) {
                                                        Text("تسجيل استلام القبض")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    savedGamaeyat.forEach { gamaeya ->
                                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, CostBorder)) {
                                            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(gamaeya.name, color = CostInk, fontWeight = FontWeight.Bold)
                                                Text("قيمة الجمعية: " + com.example.core.MoneyUtils.formatPiastresPlain(gamaeya.totalPoolValuePiastres) + " ج.م", color = CostMuted, fontSize = 11.sp)
                                                Text("الأفراد: " + gamaeya.numberOfMembers + " • القسط: " + com.example.core.MoneyUtils.formatPiastresPlain(gamaeya.monthlyInstallmentPiastres) + " ج.م • الدور: " + gamaeya.myTurnOrder, color = CostMuted, fontSize = 10.sp)
                                                Text("بداية الجمعية: ${DateUtils.formatDateArabic(gamaeya.startDate)} • موعد القبض: ${DateUtils.formatDateArabic(gamaeya.expectedPayoutDate)}", color = CostMuted, fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        item { RecordsPreviewDesign(section.title, activeTab?.title.orEmpty()) }
                    }
                } else if (section.id == "reports") {
                    item {
                        val today = LocalDate.now()
                        val startDate = when (activeTab?.id) {
                            "daily" -> today
                            "weekly" -> today.with(java.time.DayOfWeek.MONDAY)
                            else -> today.withDayOfMonth(1)
                        }
                        val startEpoch = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        val endEpoch = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1L
                        val reportExpenses = savedExpenses.filter { it.date in startEpoch..endEpoch }
                        val reportRevenues = savedRevenues.filter { it.date in startEpoch..endEpoch }
                        val expensesTotal = reportExpenses.sumOf { it.amountPiastres }
                        val revenuesTotal = reportRevenues.sumOf { it.amountPiastres }
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(when (activeTab?.id) { "daily" -> "التقرير اليومي"; "weekly" -> "التقرير الأسبوعي"; else -> "التقرير الشهري" }, color = CostInk, fontWeight = FontWeight.Bold)
                            Text("من " + DateUtils.formatDateArabic(startEpoch) + " إلى " + DateUtils.formatDateArabic(endEpoch), color = CostMuted, fontSize = 11.sp)
                            Text("إجمالي المصروفات: " + com.example.core.MoneyUtils.formatPiastresPlain(expensesTotal) + " جنيه", color = CostInk, fontWeight = FontWeight.Bold)
                            Text("إجمالي الدخل: " + com.example.core.MoneyUtils.formatPiastresPlain(revenuesTotal) + " جنيه", color = CostInk, fontWeight = FontWeight.Bold)
                            Text("الصافي: " + com.example.core.MoneyUtils.formatPiastresPlain(revenuesTotal - expensesTotal) + " جنيه", color = if (revenuesTotal >= expensesTotal) Color(0xFF26734D) else Color(0xFFB3261E), fontWeight = FontWeight.Bold)
                            Text("عدد المصروفات: " + reportExpenses.size + " • عدد الإيرادات: " + reportRevenues.size, color = CostMuted, fontSize = 11.sp)
                            if (reportExpenses.isNotEmpty()) SavedExpenseRecords(records = reportExpenses, onEdit = { showDesignNotice = true }, onDelete = { financeViewModel.deleteExpense(it.expenseId) })
                            if (reportRevenues.isNotEmpty()) SavedRevenueRecords(records = reportRevenues, onDelete = { financeViewModel.deleteRevenue(it.revenueId) })
                        }
                    }
                } else if (section.id == "backup") {
                    item {
                        BackupPreviewDesign(activeTab?.id.orEmpty()) {
                            if (activeTab?.id == "export") backupLauncher.launch("SMART_COST_PRO_backup.db")
                            else showDesignNotice = true
                        }
                    }
                } else {
                    item {
                        ExpenseEntryPreview(
                            section.id,
                            activeTab?.id.orEmpty(),
                            vehicleOptions = listOf("اختاري السيارة") + savedVehicles.map { it.name },
                            studentOptions = listOf("اختاري الطالب") + savedStudents.map { it.fullName },
                            initialValues = editingExpense?.let { record ->
                                mapOf(
                                    "المبلغ" to com.example.core.MoneyUtils.formatPiastresPlain(record.amountPiastres),
                                    "التاريخ" to DateUtils.formatDateShort(record.date).replace('/', '-'),
                                    "الملاحظات" to record.notes.orEmpty(),
                                    "التصنيف" to record.subcategory,
                                    "نوع مصروف العمل" to record.subcategory,
                                    "اسم الشخص" to record.title,
                                    "التشخيص" to record.title
                                )
                            }.orEmpty(),
                            onActionUnavailable = { showDesignNotice = true },
                            onExport = {
                                if (section.id == "car_income") carTripCsvLauncher.launch("سجل_رحلات_السيارة.csv")
                                else if (section.id in setOf("house", "work", "personal", "medical", "vehicles", "education", "associations", "income", "certificates", "current_account", "reports")) homeCsvLauncher.launch("تصدير_" + section.title + ".csv")
                                else showDesignNotice = true
                            },
                            onSave = { values ->
                                val date = runCatching { LocalDate.parse(values["التاريخ"].orEmpty()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() }.getOrNull()
                                if (section.id == "associations" && activeTab?.id == "form") {
                                    val totalPool = runCatching { BigDecimal(values["قيمة الجمعية"].orEmpty().replace(',', '.')) }.getOrNull()
                                    val members = values["عدد الأفراد"].orEmpty().toIntOrNull()
                                    val monthlyInstallment = if (totalPool != null && members != null && members > 0) {
                                        runCatching { totalPool.divide(BigDecimal(members), 2, java.math.RoundingMode.HALF_UP) }.getOrNull()
                                    } else null
                                    val turnOrder = values["رقم الدور"].orEmpty().toIntOrNull()
                                    val startDate = runCatching { LocalDate.parse(values["تاريخ البداية"].orEmpty()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() }.getOrNull()
                                    val payoutDate = runCatching { LocalDate.parse(values["تاريخ القبض"].orEmpty()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() }.getOrNull()
                                    if (!values["اسم الجمعية"].isNullOrBlank() && totalPool != null && monthlyInstallment != null && members != null && turnOrder != null && startDate != null && payoutDate != null && totalPool > BigDecimal.ZERO && monthlyInstallment > BigDecimal.ZERO && members > 0 && turnOrder in 1..members && payoutDate >= startDate) {
                                        financeViewModel.saveGamaeya(
                                            name = values["اسم الجمعية"].orEmpty(),
                                            totalPoolAmount = totalPool,
                                            numberOfMembers = members,
                                            monthlyInstallment = monthlyInstallment,
                                            myTurnOrder = turnOrder,
                                            startDate = startDate,
                                            expectedPayoutDate = payoutDate
                                        )
                                        selectedTab = "association_list"
                                    } else {
                                        showDesignNotice = true
                                    }
                                } else if (section.id == "car_income" && activeTab?.id == "add_trip") {
                                    val amount = values["الأجرة"].orEmpty().replace(',', '.').toBigDecimalOrNull()
                                    val tripDate = runCatching { LocalDate.parse(values["تاريخ الرحلة"].orEmpty()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() }.getOrNull()
                                    if (amount != null && amount > BigDecimal.ZERO && tripDate != null) {
                                        financeViewModel.saveRevenue(
                                            amount = amount,
                                            date = tripDate,
                                            category = "CAR_TRIP",
                                            title = values["نوع الرحلة"].orEmpty().ifBlank { "رحلة سيارة" },
                                            notes = values["ملاحظات الرحلة"].orEmpty()
                                        )
                                        selectedTab = "trips"
                                    } else showDesignNotice = true
                                } else if (section.id == "income") {
                                    val amount = runCatching { BigDecimal(values["المبلغ"].orEmpty().replace(',', '.')) }.getOrNull()
                                    if (amount != null && amount > BigDecimal.ZERO && date != null) {
                                        financeViewModel.saveRevenue(
                                            amount = amount,
                                            date = date,
                                            category = when (values["نوع الدخل"].orEmpty()) {
                                                "راتب" -> "SALARY"
                                                "مكافأة" -> "BONUSES"
                                                "دخل إضافي" -> "ADDITIONAL_FREELANCE"
                                                "استثمار", "أخرى", "" -> "OTHER_INCOME"
                                                else -> "OTHER_INCOME"
                                            },
                                            title = values["اسم مصدر الدخل"].orEmpty().ifBlank { "مصدر دخل" },
                                            notes = values["ملاحظات"].orEmpty()
                                        )
                                        selectedTab = "sources"
                                    }
                                } else if (section.id == "certificates" && activeTab?.id == "form") {
                                    val principal = values["المبلغ الأصلي"].orEmpty().replace(',', '.').toBigDecimalOrNull()
                                    val rate = values["معدل العائد السنوي"].orEmpty().replace(',', '.').toDoubleOrNull()
                                    val issue = runCatching { LocalDate.parse(values["تاريخ الإصدار"].orEmpty()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() }.getOrNull()
                                    val maturity = runCatching { LocalDate.parse(values["تاريخ الاستحقاق"].orEmpty()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() }.getOrNull()
                                    val frequency = when (values["دورية صرف العائد"].orEmpty()) {
                                        "ربع سنوي" -> "QUARTERLY"
                                        "نصف سنوي" -> "SEMI_ANNUALLY"
                                        "سنوي" -> "ANNUALLY"
                                        "عند الاستحقاق" -> "AT_MATURITY"
                                        else -> "MONTHLY"
                                    }
                                    val duration = when (values["مدة الشهادة"].orEmpty()) {
                                        "سنتان" -> 2
                                        "3 سنوات" -> 3
                                        "5 سنوات" -> 5
                                        else -> 1
                                    }
                                    if (!values["اسم البنك"].isNullOrBlank() && principal != null && principal > BigDecimal.ZERO && rate != null && rate >= 0.0 && issue != null && maturity != null && maturity > issue) {
                                        financeViewModel.saveCertificate(
                                            name = values["رقم الشهادة"].orEmpty().ifBlank { "شهادة " + values["اسم البنك"].orEmpty() },
                                            bankName = values["اسم البنك"].orEmpty(),
                                            principalAmount = principal,
                                            annualInterestRate = rate,
                                            payoutFrequency = frequency,
                                            issueDate = issue,
                                            maturityDate = maturity,
                                            durationYears = duration,
                                            notes = values["الملاحظات"].orEmpty()
                                        )
                                        selectedTab = "list"
                                    } else showDesignNotice = true
                                } else if (section.id == "education") {
                                    when (activeTab?.id) {
                                        "student_add" -> {
                                            val name = values["اسم الطالب"].orEmpty().trim()
                                            val stage = values["المرحلة"].orEmpty().ifBlank { "ابتدائي" }
                                            if (name.isNotBlank()) {
                                                financeViewModel.saveStudent(
                                                    fullName = name,
                                                    academicStage = stage,
                                                    academicYear = values["العام الدراسي"].orEmpty().ifBlank {
                                                        DateUtils.currentYear().toString() + "/" + (DateUtils.currentYear() + 1).toString()
                                                    }
                                                )
                                                selectedTab = "students"
                                            } else showDesignNotice = true
                                        }
                                        "student_edit" -> {
                                            val student = savedStudents.firstOrNull { it.fullName == values["الطالب"] }
                                            if (student != null) {
                                                financeViewModel.saveStudent(id = student.studentId, fullName = student.fullName, academicStage = values["المرحلة"].orEmpty().ifBlank { student.academicStage }, academicYear = student.academicYear)
                                                selectedTab = "students"
                                            } else showDesignNotice = true
                                        }
                                        "student_expense_form" -> {
                                            val student = savedStudents.firstOrNull { it.fullName == values["الطالب"] }
                                            val amount = values["المبلغ"].orEmpty().replace(',', '.').toBigDecimalOrNull()
                                            if (student != null && amount != null && amount > BigDecimal.ZERO && date != null) {
                                                financeViewModel.saveExpense(
                                                    amount = amount, date = date, department = "EDUCATION",
                                                    category = values["تصنيف المصروف"].orEmpty(),
                                                    title = values["عنوان المصروف"].orEmpty().ifBlank { "مصروف تعليمي - " + student.fullName },
                                                    studentId = student.studentId
                                                )
                                                selectedTab = "student_expenses"
                                            } else showDesignNotice = true
                                        }
                                        else -> showDesignNotice = true
                                    }
                                } else if (section.id == "current_account") {
                                    val amount = values["المبلغ"].orEmpty().replace(',', '.').toBigDecimalOrNull()
                                    if (amount != null && amount > BigDecimal.ZERO && date != null) {
                                        financeViewModel.recordTreasuryMovement(
                                            type = if (activeTab?.id == "deposits") "DEPOSIT" else "WITHDRAWAL",
                                            amount = amount,
                                            reason = values["الوصف"].orEmpty(),
                                            date = date
                                        )
                                        selectedTab = "movements"
                                    } else showDesignNotice = true
                                } else if (section.id == "vehicles") {
                                    val vehicle = savedVehicles.firstOrNull { it.name == values["السيارة"] }
                                    when (activeTab?.id) {
                                        "records" -> {
                                            val odometer = values["قراءة العداد الحالية"].orEmpty().toIntOrNull()
                                            if (!values["اسم السيارة"].isNullOrBlank() && odometer != null && odometer >= 0) {
                                                financeViewModel.saveVehicle(
                                                    name = values["اسم السيارة"].orEmpty(),
                                                    plateNumber = values["رقم اللوحة"].orEmpty(),
                                                    modelYear = values["سنة الصنع"].orEmpty(),
                                                    initialOdometer = odometer,
                                                    notes = values["ملاحظات"].orEmpty()
                                                )
                                                selectedTab = "records"
                                            } else showDesignNotice = true
                                        }
                                        "fuel", "maintenance", "oils", "accidents" -> {
                                            val dateText = values["التاريخ والوقت"].orEmpty().ifBlank { values["التاريخ"].orEmpty() }
                                            val date = runCatching { LocalDate.parse(dateText).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() }.getOrNull()
                                            val odometer = values["العداد"].orEmpty().toIntOrNull()
                                            val odometerRequired = activeTab?.id != "accidents"
                                            if (vehicle != null && date != null && (!odometerRequired || odometer != null)) {
                                                val currentOdometer = odometer ?: 0
                                                if (activeTab?.id == "fuel") {
                                                    val liters = values["الكمية"].orEmpty().replace(',', '.').toDoubleOrNull()
                                                    val price = values["سعر اللتر"].orEmpty().replace(',', '.').toBigDecimalOrNull()
                                                    if (liters != null && liters > 0.0 && price != null && price > BigDecimal.ZERO) {
                                                        financeViewModel.saveVehicleLog(
                                                            vehicleId = vehicle.vehicleId,
                                                            logType = "FUEL",
                                                            amount = price.multiply(BigDecimal.valueOf(liters)),
                                                            date = date,
                                                            odometer = currentOdometer,
                                                            liters = liters,
                                                            serviceType = values["نوع الوقود"].orEmpty(),
                                                            serviceCenter = listOf(values["محطة الوقود"].orEmpty(), values["مرفق الصورة"].orEmpty().takeIf { it.isNotBlank() }?.let { "مرفق صورة: $it" }.orEmpty()).filter { it.isNotBlank() }.joinToString(" | "),
                                                            notes = values["ملاحظات"].orEmpty(),
                                                            fuelType = values["نوع الوقود"].orEmpty()
                                                        )
                                                        selectedTab = "records"
                                                    } else showDesignNotice = true
                                                } else if (activeTab?.id == "maintenance") {
                                                    val parts = values["سعر المستلزمات"].orEmpty().replace(',', '.').toBigDecimalOrNull() ?: BigDecimal.ZERO
                                                    val labor = values["قيمة الأجور"].orEmpty().replace(',', '.').toBigDecimalOrNull() ?: BigDecimal.ZERO
                                                    val total = parts.add(labor)
                                                    if (total > BigDecimal.ZERO) {
                                                        financeViewModel.saveVehicleLog(
                                                            vehicleId = vehicle.vehicleId,
                                                            logType = "MAINTENANCE",
                                                            amount = total,
                                                            date = date,
                                                            odometer = currentOdometer,
                                                            liters = null,
                                                            serviceType = values["نوع الصيانة"].orEmpty(),
                                                            serviceCenter = values["وصف الأجور"].orEmpty(),
                                                            notes = values["وصف الصيانة"].orEmpty() + values["مرفق الصورة"].orEmpty().takeIf { it.isNotBlank() }?.let { " | مرفق صورة: $it" }.orEmpty()
                                                        )
                                                        selectedTab = "records"
                                                    } else showDesignNotice = true
                                                } else if (activeTab?.id == "oils") {
                                                    val unitPrice = values["السعر"].orEmpty().replace(',', '.').toBigDecimalOrNull() ?: BigDecimal.ZERO
                                                    val quantity = values["الكمية"].orEmpty().replace(',', '.').toBigDecimalOrNull() ?: BigDecimal.ONE
                                                    val interval = values["فترة التغيير"].orEmpty().filter(Char::isDigit).toLongOrNull() ?: 5000L
                                                    if (unitPrice > BigDecimal.ZERO && quantity > BigDecimal.ZERO) {
                                                        financeViewModel.saveOilLog(vehicle.vehicleId, unitPrice.multiply(quantity), date, currentOdometer, listOf(values["العلامة التجارية"], values["اسم المنتج"]).filterNotNull().filter { it.isNotBlank() }.joinToString(" ") + values["مرفق الصورة"].orEmpty().takeIf { it.isNotBlank() }?.let { " | مرفق صورة: $it" }.orEmpty(), interval, values["نوع الخدمة"].orEmpty().contains("فلتر"))
                                                        selectedTab = "records"
                                                    } else showDesignNotice = true
                                                } else {
                                                    val estimate = values["تقدير الضرر"].orEmpty().replace(',', '.').toBigDecimalOrNull() ?: BigDecimal.ZERO
                                                    val actual = values["تكلفة الإصلاح الفعلية"].orEmpty().replace(',', '.').toBigDecimalOrNull() ?: BigDecimal.ZERO
                                                    val insurance = values["تعويض التأمين"].orEmpty().replace(',', '.').toBigDecimalOrNull() ?: BigDecimal.ZERO
                                                    if (estimate >= BigDecimal.ZERO && actual >= BigDecimal.ZERO && insurance >= BigDecimal.ZERO) {
                                                        financeViewModel.saveAccidentLog(vehicle.vehicleId, date, odometer, listOf(values["عنوان الحادث"], values["الموقع"]).filterNotNull().filter { it.isNotBlank() }.joinToString(" — ") + values["مرفق الصورة"].orEmpty().takeIf { it.isNotBlank() }?.let { " | مرفق صورة: $it" }.orEmpty(), estimate, actual, insurance, values["ملاحظات"].orEmpty())
                                                        selectedTab = "records"
                                                    } else showDesignNotice = true
                                                }
                                            } else showDesignNotice = true
                                        }
                                        else -> showDesignNotice = true
                                    }
                                } else {
                                    val amount = runCatching { BigDecimal(values["المبلغ"].orEmpty().replace(',', '.')) }.getOrNull()
                                    val medicalAmount = listOf("تكلفة الكشف أو الفحص", "تكلفة الدواء / العلاج").sumOf { key ->
                                        runCatching { BigDecimal(values[key].orEmpty().ifBlank { "0" }) }.getOrDefault(BigDecimal.ZERO)
                                    }
                                    val finalAmount = if (section.id == "medical") medicalAmount else (amount ?: BigDecimal.ZERO)
                                    val title = when (section.id) {
                                        "medical" -> listOf(values["اسم الشخص"], values["التشخيص"]).filterNotNull().filter { it.isNotBlank() }.joinToString(" — ")
                                        "personal" -> values["التصنيف"].orEmpty() + if (values["اسم الشخص"].isNullOrBlank()) "" else " — " + values["اسم الشخص"]
                                        "work" -> values["نوع مصروف العمل"].orEmpty()
                                        else -> values["التصنيف"].orEmpty()
                                    }.ifBlank { "مصروف " + section.title }
                                    if (finalAmount > BigDecimal.ZERO && date != null) {
                                        financeViewModel.saveExpense(
                                            id = editingExpense?.expenseId ?: 0L,
                                            amount = finalAmount,
                                            date = date,
                                            department = section.id.uppercase(),
                                            category = values["التصنيف"].orEmpty().ifBlank { values["نوع مصروف العمل"].orEmpty().ifBlank { "عام" } },
                                            title = title,
                                            notes = if (section.id == "medical") {
                                                listOf(
                                                    "المنشأة: " + values["اسم المنشأة الطبية"].orEmpty(),
                                                    "نوع المنشأة: " + values["نوع المنشأة الطبية"].orEmpty(),
                                                    "الحالة: " + values["الحالة"].orEmpty(),
                                                    "التشخيص: " + values["التشخيص"].orEmpty(),
                                                    "تكلفة الكشف: " + values["تكلفة الكشف أو الفحص"].orEmpty(),
                                                    "تكلفة الدواء: " + values["تكلفة الدواء / العلاج"].orEmpty(),
                                                    values["ملاحظات طبية"].orEmpty()
                                                ).filter { it.isNotBlank() && !it.endsWith(": ") }.joinToString(" • ")
                                            } else values["الملاحظات"].orEmpty()
                                        )
                                        editingExpense = null
                                        selectedTab = "records"
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDesignNotice) {
        AlertDialog(onDismissRequest = { showDesignNotice = false }, title = { Text("معاينة المرحلة الأولى") }, text = { Text("هذا العنصر معروض لمراجعة التصميم فقط. سيتم تفعيل الحفظ والعمليات والحسابات في المرحلة الثانية بعد اعتماد الشاشات.") }, confirmButton = { TextButton(onClick = { showDesignNotice = false }) { Text("حسنًا") } }, containerColor = Color.White)
    }
}

@Composable
private fun DashboardSummaryDesign(onAction: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("لوحة المعلومات المالية", color = CostInk, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            OutlinedButton(onClick = onAction, border = BorderStroke(1.dp, CostGold), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                Text("الشهر الحالي", color = CostInk, fontSize = 12.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryMetric("إجمالي الدخل", "يُحسب تلقائيًا", Modifier.weight(1f))
            SummaryMetric("إجمالي المصروفات", "يُحسب تلقائيًا", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryMetric("صافي الدخل", "الدخل − المصروفات", Modifier.weight(1f))
            SummaryMetric("نسبة الادخار", "تُحسب تلقائيًا", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryMetric("نسبة المصروفات", "تُحسب تلقائيًا", Modifier.weight(1f))
            SummaryMetric("تكاليف المركبات", "تُحسب تلقائيًا", Modifier.weight(1f))
        }
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, CostBorder), shape = RoundedCornerShape(14.dp)) {
            Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("توزيع المصروفات", color = CostInk, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("تصنيف المصروفات حسب القسم", color = CostMuted, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(listOf("المنزل", "الطبي", "العمل", "الشخصي", "المركبات", "التعليم")) { label ->
                        Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFFEAF0F3), border = BorderStroke(1.dp, CostBorder)) {
                            Text(label, color = CostInk, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                        }
                    }
                }
                Divider(color = CostBorder)
                Text("أحدث العمليات", color = CostInk, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("التاريخ", color = CostMuted, fontSize = 11.sp)
                    Text("البيان والقسم", color = CostMuted, fontSize = 11.sp)
                    Text("المبلغ", color = CostMuted, fontSize = 11.sp)
                }
                Text("ستظهر العمليات الفعلية هنا بعد ربط البيانات في المرحلة الثانية.", color = CostMuted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun ExpenseEntryPreview(sectionId: String, tabId: String, initialValues: Map<String, String> = emptyMap(), onSave: (Map<String, String>) -> Unit = {}, onActionUnavailable: () -> Unit = {}, onExport: () -> Unit = {}, vehicleOptions: List<String> = listOf("اختاري السيارة"), studentOptions: List<String> = listOf("اختاري الطالب")) {
    var values by remember(sectionId, tabId, initialValues) { mutableStateOf(initialValues) }
    var oilMode by remember(sectionId, tabId) { mutableStateOf("الزيوت") }
    val context = LocalContext.current
    val fuelPricePrefs = remember(context) { context.getSharedPreferences("smart_cost_fuel_prices", Context.MODE_PRIVATE) }
    fun value(label: String) = values[label].orEmpty()
    fun setValue(label: String, newValue: String) { values = values + (label to newValue) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) runCatching {
            val dir = File(context.filesDir, "vehicle_attachments").apply { mkdirs() }
            val file = File(dir, "image_" + System.currentTimeMillis() + ".jpg")
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 88, it) }
            file.absolutePath
        }.onSuccess { setValue("مرفق الصورة", it) }
            .onFailure { Toast.makeText(context, "تعذر حفظ الصورة", Toast.LENGTH_SHORT).show() }
    }
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            val dir = File(context.filesDir, "vehicle_attachments").apply { mkdirs() }
            val file = File(dir, "picked_" + System.currentTimeMillis() + ".jpg")
            val input = context.contentResolver.openInputStream(uri) ?: error("تعذر قراءة الصورة المختارة")
            input.use { source -> FileOutputStream(file).use { target -> source.copyTo(target) } }
            file.absolutePath
        }.onSuccess { setValue("مرفق الصورة", it) }
            .onFailure { Toast.makeText(context, "تعذر حفظ الصورة المختارة", Toast.LENGTH_SHORT).show() }
    }

    val houseTypes = listOf("إفطار", "غداء", "عشاء", "إصلاحات كهربائية", "إصلاحات سباكة", "إصلاحات دهان", "شراء أجهزة منزلية", "شراء مفروشات", "فواتير ومستحقات منزلية", "منظفات ومستلزمات", "أخرى (مخصص)")
    val workTypes = listOf("أدوات مكتبية", "صيانة أجهزة العمل", "اشتراكات برمجيات", "ضيافة واجتماعات", "تنقلات ومواصلات عمل", "شحن وتوصيل مستندات", "أخرى (مخصص)")
    val personalTypes = listOf("طعام ومشروبات", "مستلزمات", "ملابس ومستلزمات شخصية", "عبايات", "مواصلات وتنقلات", "صحة وعناية شخصية", "ترفيه وخروج", "هدايا ومناسبات", "أخرى (مخصص)")
    val vehicleFuel = listOf("بنزين 80", "بنزين 92", "بنزين 95", "سولار", "غاز طبيعي", "شحن كهربائي")
    val maintenanceTypes = listOf("سمكرة وهيكل", "دوكو ودهان", "ميكانيكا", "كهرباء وإلكترونيات", "عفشة", "نظام التبريد والرادياتير", "شكمان ونظام العادم", "تكييف", "ضبط زوايا وترصيص", "إصلاح وتغيير الإطارات", "فحص كمبيوتر", "تنجيد وفرش داخلي", "عناية وتلميع", "زجاج")
    val oilTypes = listOf("تغيير زيت المحرك", "تغيير فلتر زيت المحرك", "تغيير زيت الفتيس", "تغيير فلتر زيت الفتيس", "تغيير زيت الكورونة", "زيت الفرامل", "زيت الباور", "فلتر هواء المحرك", "فلتر تكييف الصالون", "فلتر الوقود", "غسيل دورة المحرك", "سائل الرادياتير", "تشحيم")
    val oilBrands = listOf("موبيل", "شل", "كاسترول", "توتال", "فالفولين", "ليكوي مولي", "أخرى")
    val dateFields = setOf("التاريخ", "تاريخ البداية", "تاريخ القبض", "تاريخ الاستحقاق", "تاريخ الإصدار", "تاريخ الرحلة", "تاريخ ووقت التزوّد", "تاريخ صرف العائد")

    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        when (sectionId) {
            "house", "work", "personal" -> {
                val options = when (sectionId) { "house" -> houseTypes; "work" -> workTypes; else -> personalTypes }
                val label = if (sectionId == "work") "نوع مصروف العمل" else "تصنيف المصروف"
                PreviewDropdown(label, options, value(label).ifBlank { options.first() }) { setValue(label, it) }
                if (sectionId == "personal") PreviewInput("اسم الشخص", value("اسم الشخص")) { setValue("اسم الشخص", it) }
                if (value(label).ifBlank { options.first() } == "أخرى (مخصص)") PreviewInput("اكتب اسم المصروف", value("اكتب اسم المصروف")) { setValue("اكتب اسم المصروف", it) }
                PreviewInput("المبلغ", value("المبلغ"), numeric = true) { setValue("المبلغ", it) }
                PreviewDateInput("التاريخ", value("التاريخ")) { setValue("التاريخ", it) }
                if (sectionId == "house") {
                    Text("نوع الصرف", color = CostInk, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    PreviewDropdown("اختر نوع الصرف", listOf("مشتريات ومستلزمات", "مصنعية وأجرة يد"), value("نوع الصرف").ifBlank { "مشتريات ومستلزمات" }) { setValue("نوع الصرف", it) }
                }
                PreviewInput("الملاحظات", value("الملاحظات"), multiline = true) { setValue("الملاحظات", it) }
            }
            "medical" -> {
                PreviewInput("فرد الأسرة / اسم الشخص", value("اسم الشخص")) { setValue("اسم الشخص", it) }
                PreviewInput("اسم المنشأة الطبية", value("اسم المنشأة الطبية")) { setValue("اسم المنشأة الطبية", it) }
                PreviewDropdown(
                    "نوع المنشأة الطبية",
                    listOf("مستشفى", "عيادة", "مركز طبي", "معمل تحاليل", "مركز أشعة", "صيدلية", "أخرى"),
                    value("نوع المنشأة الطبية").ifBlank { "عيادة" }
                ) { setValue("نوع المنشأة الطبية", it) }
                PreviewDropdown(
                    "الحالة",
                    listOf("كشف", "متابعة", "طوارئ", "تحاليل", "أشعة", "شراء علاج", "أخرى"),
                    value("الحالة").ifBlank { "كشف" }
                ) { setValue("الحالة", it) }
                PreviewInput("التشخيص", value("التشخيص"), multiline = true) { setValue("التشخيص", it) }
                PreviewInput("تكلفة الكشف أو الفحص", value("تكلفة الكشف أو الفحص"), numeric = true) { setValue("تكلفة الكشف أو الفحص", it) }
                PreviewInput("تكلفة الدواء / العلاج", value("تكلفة الدواء / العلاج"), numeric = true) { setValue("تكلفة الدواء / العلاج", it) }
                PreviewDateInput("التاريخ", value("التاريخ")) { setValue("التاريخ", it) }
                PreviewInput("ملاحظات طبية", value("ملاحظات طبية"), multiline = true) { setValue("ملاحظات طبية", it) }
            }
            "associations" -> {
                if (tabId == "form") {
                    PreviewInput("اسم الجمعية", value("اسم الجمعية")) { setValue("اسم الجمعية", it) }
                    PreviewInput("قيمة الجمعية", value("قيمة الجمعية"), numeric = true) { setValue("قيمة الجمعية", it) }
                    PreviewInput("عدد الأفراد", value("عدد الأفراد"), numeric = true) { setValue("عدد الأفراد", it) }
                    val poolValue = value("قيمة الجمعية").replace(',', '.').toBigDecimalOrNull()
                    val memberCount = value("عدد الأفراد").toIntOrNull()
                    val calculatedInstallment = if (poolValue != null && memberCount != null && memberCount > 0) {
                        poolValue.divide(BigDecimal(memberCount), 2, java.math.RoundingMode.HALF_UP)
                    } else null
                    Surface(
                        color = Color(0xFFF4F6F8),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("القسط الشهري المحسوب", color = CostMuted, fontSize = 10.sp)
                            Text(
                                calculatedInstallment?.let { com.example.core.MoneyUtils.formatPiastresPlain(com.example.core.MoneyUtils.egpToPiastres(it)) + " جنيه" }
                                    ?: "أدخلي قيمة الجمعية وعدد الأفراد لحساب القسط",
                                color = CostInk,
                                fontWeight = FontWeight.Bold
                            )
                            Text("المعادلة: قيمة الجمعية ÷ عدد الأفراد.", color = CostMuted, fontSize = 10.sp)
                        }
                    }
                    PreviewInput("رقم الدور", value("رقم الدور"), numeric = true) { setValue("رقم الدور", it) }
                    PreviewDateInput("تاريخ البداية", value("تاريخ البداية")) { setValue("تاريخ البداية", it) }
                    PreviewDateInput("تاريخ القبض", value("تاريخ القبض")) { setValue("تاريخ القبض", it) }
                    PreviewInput("ملاحظات", value("ملاحظات"), multiline = true) { setValue("ملاحظات", it) }
                } else {
                    PreviewDropdown("اسم الجمعية", listOf("اختاري الجمعية"), value("اسم الجمعية").ifBlank { "اختاري الجمعية" }) { setValue("اسم الجمعية", it) }
                    PreviewInput("الشهر / رقم القسط", value("الشهر / رقم القسط")) { setValue("الشهر / رقم القسط", it) }
                    PreviewInput("قيمة القسط أو القبض", value("قيمة القسط أو القبض"), numeric = true) { setValue("قيمة القسط أو القبض", it) }
                    PreviewDateInput("التاريخ", value("التاريخ")) { setValue("التاريخ", it) }
                }
            }
            "vehicles" -> when (tabId) {
                "fuel" -> {
                    PreviewDropdown("السيارة", vehicleOptions, value("السيارة").ifBlank { vehicleOptions.first() }) { setValue("السيارة", it) }
                    PreviewDropdown("نوع الوقود / مصدر الطاقة", vehicleFuel, value("نوع الوقود").ifBlank { "بنزين 92" }) {
                        setValue("نوع الوقود", it)
                        setValue("سعر اللتر", fuelPricePrefs.getString("price_" + it, "").orEmpty())
                    }
                    PreviewInput("الكمية / عدد اللترات", value("الكمية"), numeric = true) { setValue("الكمية", it) }
                    PreviewInput("سعر اللتر (جنيه)", value("سعر اللتر"), numeric = true) {
                        setValue("سعر اللتر", it)
                        fuelPricePrefs.edit().putString("price_" + value("نوع الوقود").ifBlank { "بنزين 92" }, it).apply()
                    }
                    val fuelTotal = (value("الكمية").replace(',', '.').toBigDecimalOrNull() ?: BigDecimal.ZERO)
                        .multiply(value("سعر اللتر").replace(',', '.').toBigDecimalOrNull() ?: BigDecimal.ZERO)
                    Text("إجمالي التعبئة: " + com.example.core.MoneyUtils.formatPiastresPlain(com.example.core.MoneyUtils.egpToPiastres(fuelTotal)) + " جنيه", color = CostInk, fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PreviewInput("قراءة عداد السيارة", value("العداد"), numeric = true, modifier = Modifier.weight(1f)) { setValue("العداد", it) }
                        IconButton(onClick = { cameraLauncher.launch(null) }) { Icon(Icons.Outlined.CameraAlt, contentDescription = "تصوير عداد السيارة", tint = CostGoldDark) }
                    }
                    PreviewDateInput("تاريخ ووقت التزوّد", value("التاريخ والوقت")) { setValue("التاريخ والوقت", it) }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { imagePickerLauncher.launch(arrayOf("image/*")) }) { Text("اختيار صورة") }
                        if (value("مرفق الصورة").isNotBlank()) Text("تم إرفاق الصورة", color = CostMuted, fontSize = 11.sp)
                    }
                    PreviewInput("محطة الوقود (اختياري)", value("محطة الوقود")) { setValue("محطة الوقود", it) }
                }
                "maintenance" -> {
                    PreviewDropdown("السيارة", vehicleOptions, value("السيارة").ifBlank { vehicleOptions.first() }) { setValue("السيارة", it) }
                    PreviewDropdown("نوع الصيانة", maintenanceTypes, value("نوع الصيانة").ifBlank { maintenanceTypes.first() }) { setValue("نوع الصيانة", it) }
                    PreviewInput("وصف الصيانة", value("وصف الصيانة"), multiline = true) { setValue("وصف الصيانة", it) }
                    PreviewInput("اسم المستلزم أو قطعة الغيار", value("قطعة الغيار")) { setValue("قطعة الغيار", it) }
                    PreviewInput("سعر المستلزمات", value("سعر المستلزمات"), numeric = true) { setValue("سعر المستلزمات", it) }
                    PreviewInput("وصف الأجور", value("وصف الأجور")) { setValue("وصف الأجور", it) }
                    PreviewInput("قيمة الأجور", value("قيمة الأجور"), numeric = true) { setValue("قيمة الأجور", it) }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { cameraLauncher.launch(null) }, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.CameraAlt, null); Spacer(Modifier.width(5.dp)); Text("تصوير") }
                        OutlinedButton(onClick = { imagePickerLauncher.launch(arrayOf("image/*")) }, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.AddPhotoAlternate, null); Spacer(Modifier.width(5.dp)); Text("اختيار صورة") }
                    }
                    if (value("مرفق الصورة").isNotBlank()) Text("تم إرفاق صورة الصيانة", color = CostMuted, fontSize = 11.sp)
                    PreviewDateInput("التاريخ", value("التاريخ")) { setValue("التاريخ", it) }
                }
                "oils" -> {
                    PreviewDropdown("السيارة", vehicleOptions, value("السيارة").ifBlank { vehicleOptions.first() }) { setValue("السيارة", it) }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("الزيوت", "الفلاتر").forEach { mode -> OutlinedButton(onClick = { oilMode = mode }, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(containerColor = if (oilMode == mode) CostInk else Color.White, contentColor = if (oilMode == mode) CostGold else CostInk), border = BorderStroke(1.dp, CostGold)) { Text(mode) } }
                    }
                    val serviceOptions = if (oilMode == "الزيوت") oilTypes.filter { it.contains("زيت") || it.contains("غسيل") || it.contains("سائل") || it.contains("تشحيم") } else oilTypes.filter { it.contains("فلتر") }
                    PreviewDropdown("نوع الخدمة", serviceOptions, value("نوع الخدمة").ifBlank { serviceOptions.first() }) { setValue("نوع الخدمة", it) }
                    if (oilMode == "الزيوت" && value("نوع الخدمة").ifBlank { serviceOptions.first() } == "تغيير زيت المحرك") {
                        PreviewDropdown("فترة تغيير الزيت", listOf("5,000 كم", "10,000 كم"), value("فترة التغيير").ifBlank { "5,000 كم" }) { setValue("فترة التغيير", it) }
                        PreviewDropdown("ماركة الزيت", oilBrands, value("العلامة التجارية").ifBlank { oilBrands.first() }) { setValue("العلامة التجارية", it) }
                    }
                    PreviewInput("اسم المنتج", value("اسم المنتج")) { setValue("اسم المنتج", it) }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { cameraLauncher.launch(null) }, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.CameraAlt, null); Spacer(Modifier.width(5.dp)); Text("تصوير") }
                        OutlinedButton(onClick = { imagePickerLauncher.launch(arrayOf("image/*")) }, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.AddPhotoAlternate, null); Spacer(Modifier.width(5.dp)); Text("اختيار صورة") }
                    }
                    if (value("مرفق الصورة").isNotBlank()) Text("تم إرفاق صورة الزيت أو الفلتر", color = CostMuted, fontSize = 11.sp)
                    PreviewInput("الكمية", value("الكمية"), numeric = true) { setValue("الكمية", it) }
                    PreviewInput("السعر", value("السعر"), numeric = true) { setValue("السعر", it) }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PreviewInput("قراءة العداد", value("العداد"), numeric = true, modifier = Modifier.weight(1f)) { setValue("العداد", it) }
                        IconButton(onClick = { cameraLauncher.launch(null) }) { Icon(Icons.Outlined.CameraAlt, contentDescription = "تصوير عداد السيارة", tint = CostGoldDark) }
                    }
                    PreviewDateInput("التاريخ", value("التاريخ")) { setValue("التاريخ", it) }
                    PreviewInput("ملاحظات", value("ملاحظات"), multiline = true) { setValue("ملاحظات", it) }
                }
                "accidents" -> {
                    PreviewDropdown("السيارة", vehicleOptions, value("السيارة").ifBlank { vehicleOptions.first() }) { setValue("السيارة", it) }
                    PreviewInput("عنوان الحادث", value("عنوان الحادث")) { setValue("عنوان الحادث", it) }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { cameraLauncher.launch(null) }, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.CameraAlt, null); Spacer(Modifier.width(5.dp)); Text("تصوير") }
                        OutlinedButton(onClick = { imagePickerLauncher.launch(arrayOf("image/*")) }, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.AddPhotoAlternate, null); Spacer(Modifier.width(5.dp)); Text("اختيار صورة") }
                    }
                    if (value("مرفق الصورة").isNotBlank()) Text("تم إرفاق صورة الحادث", color = CostMuted, fontSize = 11.sp)
                    PreviewDateInput("التاريخ", value("التاريخ")) { setValue("التاريخ", it) }
                    PreviewInput("الوقت", value("الوقت")) { setValue("الوقت", it) }
                    PreviewInput("الموقع (اختياري)", value("الموقع")) { setValue("الموقع", it) }
                    PreviewInput("تقدير الضرر — ليس مبلغًا مدفوعًا", value("تقدير الضرر"), numeric = true) { setValue("تقدير الضرر", it) }
                    PreviewInput("تكلفة الإصلاح الفعلية", value("تكلفة الإصلاح الفعلية"), numeric = true) { setValue("تكلفة الإصلاح الفعلية", it) }
                    PreviewInput("تعويض التأمين المستلم", value("تعويض التأمين"), numeric = true) { setValue("تعويض التأمين", it) }
                    Text("التكلفة التي تخصم = الإصلاح الفعلي − تعويض التأمين، ولا يخصم تقدير الضرر.", color = CostMuted, fontSize = 10.sp)
                    PreviewInput("ملاحظات", value("ملاحظات"), multiline = true) { setValue("ملاحظات", it) }
                }
                else -> {
                    PreviewInput("اسم السيارة / اللقب", value("اسم السيارة")) { setValue("اسم السيارة", it) }
                    PreviewInput("الماركة / الشركة المصنعة", value("الماركة")) { setValue("الماركة", it) }
                    PreviewInput("الطراز / الفئة", value("الطراز")) { setValue("الطراز", it) }
                    PreviewInput("الموديل", value("الموديل")) { setValue("الموديل", it) }
                    PreviewInput("سنة الصنع", value("سنة الصنع"), numeric = true) { setValue("سنة الصنع", it) }
                    PreviewInput("اللون", value("اللون")) { setValue("اللون", it) }
                    PreviewDropdown("نوع السيارة", listOf("سيدان", "هاتشباك", "SUV", "MPV", "نقل", "ميكروباص", "أخرى"), value("نوع السيارة").ifBlank { "سيدان" }) { setValue("نوع السيارة", it) }
                    PreviewDropdown("ناقل الحركة", listOf("مانيوال", "أوتوماتيك", "CVT", "أخرى"), value("ناقل الحركة").ifBlank { "مانيوال" }) { setValue("ناقل الحركة", it) }
                    PreviewDropdown("نوع الوقود", vehicleFuel, value("نوع الوقود").ifBlank { "بنزين 92" }) { setValue("نوع الوقود", it) }
                    PreviewInput("سعة المحرك (اختياري)", value("سعة المحرك"), numeric = true) { setValue("سعة المحرك", it) }
                    PreviewInput("رقم اللوحة", value("رقم اللوحة")) { setValue("رقم اللوحة", it) }
                    PreviewInput("رقم الشاسيه (اختياري)", value("رقم الشاسيه")) { setValue("رقم الشاسيه", it) }
                    PreviewInput("قراءة العداد الحالية", value("قراءة العداد الحالية"), numeric = true) { setValue("قراءة العداد الحالية", it) }
                    PreviewInput("ملاحظات", value("ملاحظات"), multiline = true) { setValue("ملاحظات", it) }
                }
            }
            "income" -> {
                PreviewInput("اسم مصدر الدخل", value("اسم مصدر الدخل")) { setValue("اسم مصدر الدخل", it) }
                PreviewInput("المبلغ", value("المبلغ"), numeric = true) { setValue("المبلغ", it) }
                PreviewDateInput("التاريخ", value("التاريخ")) { setValue("التاريخ", it) }
                val incomeTypes = listOf("راتب", "دخل إضافي", "مكافأة", "استثمار", "أخرى")
                PreviewDropdown("نوع الدخل", incomeTypes, value("نوع الدخل").ifBlank { incomeTypes.first() }) { setValue("نوع الدخل", it) }
                PreviewInput("ملاحظات", value("ملاحظات"), multiline = true) { setValue("ملاحظات", it) }
            }
            "certificates" -> if (tabId == "payouts") {
                PreviewDropdown("الشهادة", listOf("اختاري الشهادة المسجلة"), value("الشهادة").ifBlank { "اختاري الشهادة المسجلة" }) { setValue("الشهادة", it) }
                PreviewDateInput("تاريخ صرف العائد", value("تاريخ صرف العائد")) { setValue("تاريخ صرف العائد", it) }
                PreviewInput("قيمة العائد", value("قيمة العائد"), numeric = true) { setValue("قيمة العائد", it) }
                PreviewInput("ملاحظات", value("ملاحظات"), multiline = true) { setValue("ملاحظات", it) }
            } else {
                PreviewInput("اسم البنك", value("اسم البنك")) { setValue("اسم البنك", it) }
                PreviewInput("رقم الشهادة", value("رقم الشهادة")) { setValue("رقم الشهادة", it) }
                PreviewInput("المبلغ الأصلي", value("المبلغ الأصلي"), numeric = true) { setValue("المبلغ الأصلي", it) }
                PreviewInput("معدل العائد السنوي (%)", value("معدل العائد السنوي"), numeric = true) { setValue("معدل العائد السنوي", it) }
                val durations = listOf("6 أشهر", "سنة", "سنتان", "3 سنوات", "5 سنوات")
                PreviewDropdown("مدة الشهادة", durations, value("مدة الشهادة").ifBlank { durations[1] }) { setValue("مدة الشهادة", it) }
                val frequencies = listOf("شهري", "ربع سنوي", "نصف سنوي", "سنوي", "عند الاستحقاق")
                PreviewDropdown("دورية صرف العائد", frequencies, value("دورية صرف العائد").ifBlank { frequencies.first() }) { setValue("دورية صرف العائد", it) }
                PreviewDateInput("تاريخ الإصدار", value("تاريخ الإصدار")) { setValue("تاريخ الإصدار", it) }
                PreviewDateInput("تاريخ الاستحقاق", value("تاريخ الاستحقاق")) { setValue("تاريخ الاستحقاق", it) }
                PreviewInput("الملاحظات", value("الملاحظات"), multiline = true) { setValue("الملاحظات", it) }
            }
            "current_account" -> {
                Text(if (tabId == "deposits") "تسجيل إيداع في الحساب الجاري" else "تسجيل سحب من الحساب الجاري", color = CostInk, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                PreviewInput("الوصف", value("الوصف")) { setValue("الوصف", it) }
                PreviewInput("المبلغ", value("المبلغ"), numeric = true) { setValue("المبلغ", it) }
                PreviewDateInput("التاريخ", value("التاريخ")) { setValue("التاريخ", it) }
                Text(if (tabId == "deposits") "سيُسجل الإيداع كحركة موجبة." else "سيُسجل السحب كحركة سالبة مرة واحدة.", color = CostMuted, fontSize = 10.sp)
            }
            "car_income" -> if (tabId == "add_trip") {
                PreviewDateInput("تاريخ الرحلة", value("تاريخ الرحلة")) { setValue("تاريخ الرحلة", it) }
                val tripTypes = listOf("نقدي", "Uber", "DiDi", "inDrive", "خاص")
                PreviewDropdown("نوع الرحلة", tripTypes, value("نوع الرحلة").ifBlank { tripTypes.first() }) { setValue("نوع الرحلة", it) }
                PreviewInput("الأجرة", value("الأجرة"), numeric = true) { setValue("الأجرة", it) }
                PreviewInput("ملاحظات الرحلة", value("ملاحظات الرحلة"), multiline = true) { setValue("ملاحظات الرحلة", it) }
            } else if (tabId == "trip_report") {
                PreviewDateInput("تاريخ البداية", value("تاريخ البداية")) { setValue("تاريخ البداية", it) }
                PreviewDateInput("تاريخ النهاية", value("تاريخ النهاية")) { setValue("تاريخ النهاية", it) }
                PreviewDropdown("نوع الرحلة", listOf("الكل", "نقدي", "Uber", "DiDi", "inDrive", "خاص"), value("نوع الرحلة").ifBlank { "الكل" }) { setValue("نوع الرحلة", it) }
                PreviewInput("بحث في الرحلات", value("بحث")) { setValue("بحث", it) }
                Text("التقرير يعرض إجمالي الأجرة ومتوسطها وأعلى وأقل أجرة وعدد الرحلات؛ لا يُفترض حساب صافي ربح دون بيانات تكلفة موثقة.", color = CostMuted, fontSize = 10.sp)
            } else {
                Text("سيظهر سجل الرحلات المسجلة هنا مع إمكانية فتح الرحلة للتعديل أو الحذف.", color = CostMuted, fontSize = 11.sp)
            }
            "education" -> when (tabId) {
                "student_add" -> {
                    PreviewInput("اسم الطالب", value("اسم الطالب")) { setValue("اسم الطالب", it) }
                    PreviewDropdown("المرحلة التعليمية", listOf("رياض أطفال", "ابتدائي", "إعدادي", "ثانوي", "جامعة"), value("المرحلة").ifBlank { "ابتدائي" }) { setValue("المرحلة", it) }
                    PreviewInput("العام الدراسي", value("العام الدراسي").ifBlank { DateUtils.currentYear().toString() + "/" + (DateUtils.currentYear() + 1).toString() }) { setValue("العام الدراسي", it) }
                    Text("يُحفظ العام الدراسي ضمن ملف الطالب.", color = CostMuted, fontSize = 10.sp)
                }
                "student_edit" -> {
                    PreviewDropdown("اختاري الطالب المراد تعديله", studentOptions, value("الطالب").ifBlank { studentOptions.first() }) { setValue("الطالب", it) }
                    PreviewDropdown("المرحلة التعليمية", listOf("رياض أطفال", "ابتدائي", "إعدادي", "ثانوي", "جامعة"), value("المرحلة").ifBlank { "ابتدائي" }) { setValue("المرحلة", it) }
                }
                "student_expense_form" -> {
                    PreviewDropdown("اختاري الطالب", studentOptions, value("الطالب").ifBlank { studentOptions.first() }) { setValue("الطالب", it) }
                    PreviewDropdown("تصنيف المصروف", listOf("دروس خصوصية", "مصروفات مدرسية", "كتب ومذكرات", "أتوبيس المدرسة", "مستلزمات", "انتقالات", "أخرى"), value("تصنيف المصروف").ifBlank { "دروس خصوصية" }) { setValue("تصنيف المصروف", it) }
                    PreviewInput("عنوان المصروف", value("عنوان المصروف")) { setValue("عنوان المصروف", it) }
                    PreviewInput("المبلغ", value("المبلغ"), numeric = true) { setValue("المبلغ", it) }
                    PreviewDateInput("التاريخ", value("التاريخ")) { setValue("التاريخ", it) }
                }
                else -> {
                    Text("اختاري «إضافة طالب» لإنشاء ملف جديد، أو «تعديل طالب» لاختيار طالب مسجل، أو «إضافة مصروف طالب» لربط المصروف بطالب.", color = CostMuted, fontSize = 12.sp)
                }
            }
            else -> {
                fieldsFor(sectionId, tabId).forEach { field ->
                    val opts = choicesFor(sectionId, tabId)
                    if (opts.isNotEmpty() && (field.contains("تصنيف") || field.contains("نوع") || field.contains("المرحلة") || field.contains("الدورية"))) PreviewDropdown(field, opts, value(field).ifBlank { opts.first() }) { setValue(field, it) }
                    else if (field.contains("تاريخ")) PreviewDateInput(field, value(field)) { setValue(field, it) }
                    else PreviewInput(field, value(field), numeric = field.contains("مبلغ") || field.contains("قيمة") || field.contains("تكلفة") || field.contains("العمر")) { setValue(field, it) }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Button(
                onClick = {
                    val saveSupported = sectionId in setOf("house", "medical", "work", "personal", "income", "current_account") ||
                        (sectionId == "associations" && tabId == "form") ||
                        (sectionId == "education" && tabId in setOf("student_add", "student_edit", "student_expense_form")) ||
                        (sectionId == "car_income" && tabId == "add_trip") ||
                        (sectionId == "certificates" && tabId == "form") ||
                        (sectionId == "vehicles" && tabId in setOf("records", "fuel", "maintenance", "oils", "accidents"))
                    if (saveSupported) onSave(values) else onActionUnavailable()
                },
                modifier = Modifier.weight(1.15f),
                colors = ButtonDefaults.buttonColors(containerColor = CostInk, contentColor = CostGold),
                shape = RoundedCornerShape(13.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (initialValues.isEmpty()) "حفظ البيان" else "حفظ التعديلات", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = onExport,
                modifier = Modifier.weight(0.85f),
                border = BorderStroke(1.dp, CostGold),
                shape = RoundedCornerShape(13.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Outlined.FileDownload, contentDescription = null, tint = CostInk, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("تصدير", color = CostInk, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
        Text(
            if (sectionId in setOf("house", "work", "personal", "medical", "vehicles")) "يمكنك تصدير سجلات القسم المحفوظة إلى ملف CSV متوافق مع Excel."
            else "التصدير لهذا القسم غير مفعّل بعد.",
            color = CostMuted,
            fontSize = 10.sp
        )

    }
}

@Composable
private fun AttachmentPreview(reference: String) {
    val context = LocalContext.current
    val bitmap = remember(reference) {
        runCatching {
            if (reference.startsWith("content://")) {
                context.contentResolver.openInputStream(Uri.parse(reference))?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, BitmapFactory.Options().apply { inSampleSize = 4 })
                }
            } else {
                BitmapFactory.decodeFile(reference, BitmapFactory.Options().apply { inSampleSize = 2 })
            }
        }.getOrNull()
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "الصورة المرفقة بالسجل",
            modifier = Modifier.fillMaxWidth().heightIn(max = 180.dp),
            contentScale = ContentScale.Fit
        )
    } else {
        Text("الصورة محفوظة لكن تعذر عرض المعاينة.", color = CostMuted, fontSize = 10.sp)
    }
}

@Composable
private fun PreviewDateInput(label: String, value: String, onValueChange: (String) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    val parsedDate = remember(value) {
        val parts = value.split("-")
        if (parts.size == 3) {
            val y = parts[0].toIntOrNull()
            val m = parts[1].toIntOrNull()
            val d = parts[2].toIntOrNull()
            if (y != null && m != null && d != null && m in 1..12 && d in 1..31) Triple(y, m - 1, d) else null
        } else null
    }
    val today = remember { Calendar.getInstance() }
    var displayedYear by remember { mutableIntStateOf(parsedDate?.first ?: today.get(Calendar.YEAR)) }
    var displayedMonth by remember { mutableIntStateOf(parsedDate?.second ?: today.get(Calendar.MONTH)) }

    OutlinedButton(
        onClick = {
            displayedYear = parsedDate?.first ?: today.get(Calendar.YEAR)
            displayedMonth = parsedDate?.second ?: today.get(Calendar.MONTH)
            showPicker = true
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CostBorder),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = CostInk)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = CostGoldDark)
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                Text(label, color = CostMuted, fontSize = 10.sp)
                Text(value.ifBlank { "اختاري التاريخ من التقويم" }, color = CostInk, fontSize = 14.sp)
            }
        }
    }

    if (showPicker) {
        val monthNames = listOf("يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو", "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر")
        val firstDayOffset = Calendar.getInstance().apply {
            set(displayedYear, displayedMonth, 1, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
        val daysInMonth = Calendar.getInstance().apply {
            set(displayedYear, displayedMonth, 1, 12, 0, 0)
        }.getActualMaximum(Calendar.DAY_OF_MONTH)
        val cells: List<Int?> = List(firstDayOffset) { null } + (1..daysInMonth).map { it }

        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text("اختيار $label", color = CostInk) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        IconButton(onClick = {
                            if (displayedMonth == 0) { displayedMonth = 11; displayedYear-- } else displayedMonth--
                        }) { Icon(Icons.Outlined.ChevronLeft, contentDescription = "الشهر السابق") }
                        Text("${monthNames[displayedMonth]} $displayedYear", color = CostInk, fontWeight = FontWeight.Bold)
                        IconButton(onClick = {
                            if (displayedMonth == 11) { displayedMonth = 0; displayedYear++ } else displayedMonth++
                        }) { Icon(Icons.Outlined.ChevronRight, contentDescription = "الشهر التالي") }
                    }
                    Row(Modifier.fillMaxWidth()) {
                        listOf("ح", "ن", "ث", "ر", "خ", "ج", "س").forEach { day ->
                            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Text(day, color = CostMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    cells.chunked(7).forEach { week ->
                        Row(Modifier.fillMaxWidth()) {
                            (week + List(7 - week.size) { null }).forEach { day ->
                                Box(Modifier.weight(1f).height(40.dp), contentAlignment = Alignment.Center) {
                                    if (day != null) {
                                        TextButton(
                                            onClick = {
                                                onValueChange(String.format(java.util.Locale.US, "%04d-%02d-%02d", displayedYear, displayedMonth + 1, day))
                                                showPicker = false
                                            },
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(day.toString(), color = CostInk, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPicker = false }) { Text("إغلاق") }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun PreviewDropdown(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember(label) { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(label, color = CostInk, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, CostBorder), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 13.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(selected, color = CostInk, fontSize = 13.sp)
                    Icon(Icons.Outlined.ArrowDropDown, contentDescription = "عرض الاختيارات", tint = CostGoldDark)
                }
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.fillMaxWidth(0.92f).heightIn(max = 320.dp).background(Color.White)) {
                options.forEach { option ->
                    DropdownMenuItem(text = { Text(option, color = CostInk, fontSize = 13.sp) }, onClick = { onSelect(option); expanded = false })
                }
            }
        }
    }
}

@Composable
private fun PreviewInput(label: String, value: String, numeric: Boolean = false, multiline: Boolean = false, modifier: Modifier = Modifier, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = !multiline,
        minLines = if (multiline) 3 else 1,
        keyboardOptions = KeyboardOptions(keyboardType = if (numeric) KeyboardType.Decimal else KeyboardType.Text),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CostGoldDark, focusedLabelColor = CostGoldDark)
    )
}

@Composable
private fun OptionsPreview(options: List<String>) {
    if (options.isNotEmpty()) {
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, CostBorder), shape = RoundedCornerShape(14.dp)) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("الاختيارات المتاحة", color = CostInk, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(options) { option ->
                        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFEAF0F3), border = BorderStroke(1.dp, CostBorder)) {
                            Text(option, color = CostInk, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedExpenseRecords(records: List<ExpenseEntity>, onEdit: (ExpenseEntity) -> Unit, onDelete: (ExpenseEntity) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, CostBorder), shape = RoundedCornerShape(15.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text("البيانات المحفوظة", color = CostInk, fontWeight = FontWeight.Bold)
            if (records.isEmpty()) Text("لا توجد بيانات محفوظة في هذا القسم حتى الآن.", color = CostMuted, fontSize = 12.sp)
            records.sortedByDescending { it.date }.take(100).forEach { record ->
                Divider(color = CostBorder)
                Text(record.title, color = CostInk, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("${DateUtils.formatDateArabic(record.date)} • ${record.subcategory}", color = CostMuted, fontSize = 10.sp)
                Text("${record.amount} جنيه", color = CostGoldDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { onEdit(record) }) { Text("تعديل") }
                    TextButton(onClick = { onDelete(record) }) { Text("حذف", color = Color(0xFFB3261E)) }
                }
            }
        }
    }
}

@Composable
private fun SavedRevenueRecords(records: List<com.example.data.local.entities.RevenueEntity>, onDelete: (com.example.data.local.entities.RevenueEntity) -> Unit = {}) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, CostBorder), shape = RoundedCornerShape(15.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("مصادر الدخل المحفوظة", color = CostInk, fontWeight = FontWeight.Bold)
            if (records.isEmpty()) Text("لا توجد إيرادات محفوظة حتى الآن.", color = CostMuted, fontSize = 12.sp)
            records.sortedByDescending { it.date }.take(100).forEach { record ->
                Divider(color = CostBorder)
                Text(record.title, color = CostInk, fontWeight = FontWeight.SemiBold)
                Text("${DateUtils.formatDateArabic(record.date)} • ${record.sourceType}", color = CostMuted, fontSize = 10.sp)
                Text("${com.example.core.MoneyUtils.formatPiastresPlain(record.amountPiastres)} جنيه", color = CostGoldDark, fontWeight = FontWeight.Bold)
                TextButton(onClick = { onDelete(record) }) { Text("حذف") }
            }
        }
    }
}

@Composable
private fun RecordsPreviewDesign(sectionTitle: String, tabTitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CostBorder),
        shape = RoundedCornerShape(15.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text(tabTitle + " — " + sectionTitle, color = CostInk, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("التاريخ", color = CostMuted, fontSize = 10.sp)
                Text("البيان", color = CostMuted, fontSize = 10.sp)
                Text("المبلغ", color = CostMuted, fontSize = 10.sp)
            }
            Divider(color = CostBorder)
            Text("لا توجد سجلات فعلية معروضة في معاينة التصميم.", color = CostMuted, fontSize = 11.sp)
            Text("سيظهر هنا جدول السجلات مع إجراءات العرض والتعديل والحذف بعد تفعيل المرحلة الثانية.", color = CostMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun SummaryPill(label: String) {
    Surface(shape = RoundedCornerShape(30.dp), color = Color(0xFF455A64)) {
        Text(label, color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp))
    }
}

@Composable
private fun CostTabCard(tab: CostTab, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier.height(112.dp), shape = RoundedCornerShape(15.dp), color = Color.White, border = BorderStroke(1.dp, CostBorder), shadowElevation = 1.dp) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.Start) {
            Surface(shape = RoundedCornerShape(9.dp), color = Color(0xFFEAF0F3)) {
                Icon(Icons.Outlined.FolderOpen, contentDescription = null, modifier = Modifier.padding(7.dp).size(20.dp), tint = CostInk)
            }
            Text(tab.title, color = CostInk, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("عرض التفاصيل ←", color = CostGoldDark, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CostSectionCard(
    section: CostSection,
    modifier: Modifier,
    isReorderMode: Boolean = false,
    canMoveUp: Boolean = false,
    canMoveDown: Boolean = false,
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {},
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(if (isReorderMode) 184.dp else 152.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, CostBorder),
        shadowElevation = 1.dp
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFEAF0F3)) {
                    Icon(section.icon, contentDescription = null, modifier = Modifier.padding(8.dp).size(21.dp), tint = CostGoldDark)
                }
                if (isReorderMode) {
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Outlined.KeyboardArrowUp, contentDescription = "تحريك لأعلى", tint = if (canMoveUp) CostGoldDark else CostMuted.copy(alpha = 0.35f))
                    }
                    IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = "تحريك لأسفل", tint = if (canMoveDown) CostGoldDark else CostMuted.copy(alpha = 0.35f))
                    }
                }
            }
            Text(section.title, color = CostInk, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(section.subtitle, color = CostMuted, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(if (isReorderMode) "ترتيب داخل المجموعة" else "عرض التفاصيل  ←", color = CostGoldDark, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ReportPreviewDesign(onAction: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            SummaryMetric("إجمالي الدخل", "—", Modifier.weight(1f))
            SummaryMetric("إجمالي المصروفات", "—", Modifier.weight(1f))
            SummaryMetric("صافي الدخل", "—", Modifier.weight(1f))
        }
        listOf("الفترة الزمنية", "القسم", "الطالب (اختياري)").forEach { label ->
            OutlinedButton(onClick = onAction, modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, CostBorder)) { Text(label, color = CostInk) }
        }
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, CostBorder)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("تفاصيل التقرير", color = CostInk, fontWeight = FontWeight.Bold)
                OptionsPreview(listOf("الكل", "الدخل", "المنزل", "الشخصي", "الجمعيات", "المركبات", "التعليم"))
                Text("التاريخ • البيان • القسم • المبلغ • النوع", color = CostMuted, fontSize = 11.sp)
                Text("ستظهر صفوف التقرير بعد تحديد الفترة وتفعيل مصادر البيانات.", color = CostMuted, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun BackupPreviewDesign(tabId: String, onAction: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, CostBorder)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(if (tabId == "restore") "استعادة نسخة مالية" else "تصدير نسخة مالية", color = CostInk, fontWeight = FontWeight.Bold)
            Text(if (tabId == "restore") "اختيار الملف، فحص المخطط، عرض المجموعات التي ستُستبدل ثم طلب التأكيد." else "إنشاء نسخة فعلية من قاعدة بيانات التطبيق المحلية بصيغة DB.", color = CostMuted, fontSize = 11.sp)
            OutlinedButton(onClick = onAction, modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, CostGold)) {
                Text(if (tabId == "restore") "اختيار ملف النسخة" else "تجهيز نسخة احتياطية", color = CostInk)
            }
            Text("لن تُنفّذ أي استعادة أو استبدال بيانات في المرحلة الأولى.", color = CostGoldDark, fontSize = 10.sp)
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: String, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, CostBorder), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(label, color = CostMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 2, minLines = 2)
            Text(value, color = CostInk, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 2)
        }
    }
}
