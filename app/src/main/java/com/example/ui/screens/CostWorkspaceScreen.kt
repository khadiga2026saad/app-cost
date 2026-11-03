package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import android.app.DatePickerDialog
import java.util.Calendar
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CostInk = Color(0xFF171717)
private val CostGold = Color(0xFFD4AF37)
private val CostGoldDark = Color(0xFFAA8010)
private val CostCanvas = Color(0xFFF7F6F2)
private val CostBorder = Color(0xFFE8DFC9)
private val CostMuted = Color(0xFF77736A)

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
    "house" -> listOf(CostTab("records", "سجلات المنزل"), CostTab("form", "إضافة مصروف منزلي"), CostTab("categories", "إدارة تصنيفات المنزل"))
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
    "house", "work", "personal" -> listOf("تصدير CSV", "طباعة")
    "medical" -> listOf("تصدير Excel", "طباعة")
    "vehicles" -> if (tabId == "oils") listOf("PDF", "Excel", "Word", "طباعة") else emptyList()
    "education" -> listOf("تصدير HTML / XLS", "طباعة")
    "car_income" -> listOf("Excel", "Word", "طباعة", "نسخ نص التقرير")
    "reports" -> listOf("CSV", "طباعة", "حفظ في الخزنة", "إغلاق")
    else -> emptyList()
}

private fun fieldsFor(sectionId: String, tabId: String): List<String> = when (sectionId) {
    "house" -> listOf("التصنيف", "تصنيف مخصص (عند الحاجة)", "المبلغ", "التاريخ", "طريقة الدفع", "نوع الدفع: مستلزمات أو أجور", "الملاحظات")
    "medical" -> listOf("فرد الأسرة", "اسم المنشأة الطبية", "نوع المنشأة الطبية", "الحالة", "التشخيص", "تكلفة الكشف أو الفحص", "تكلفة الدواء", "التاريخ", "ملاحظات طبية")
    "work" -> listOf("نوع المصروف", "نوع مخصص", "المبلغ", "التاريخ", "الملاحظات")
    "personal" -> listOf("التصنيف", "تصنيف مخصص (عند الحاجة)", "المبلغ", "التاريخ", "طريقة الدفع", "الملاحظات")
    "associations" -> if (tabId == "form") listOf("اسم الجمعية", "قيمة الجمعية", "عدد الأفراد", "القسط", "رقم الدور", "تاريخ البداية", "تاريخ القبض", "ملاحظات") else listOf("اسم الجمعية", "الشهر", "قيمة القسط أو القبض", "التاريخ", "ملاحظات")
    "vehicles" -> when (tabId) {
        "fuel" -> listOf("المركبة", "نوع الوقود", "الكمية أو اللترات", "سعر الوحدة", "إجمالي التكلفة", "قراءة العداد", "التاريخ والوقت", "ملاحظات")
        "maintenance" -> listOf("المركبة", "نوع الصيانة", "وصف الصيانة", "اسم المستلزم أو قطعة الغيار", "سعر المستلزمات", "وصف الأجور", "قيمة الأجور", "التاريخ", "الإجمالي")
        "oils" -> listOf("المركبة", "نوع الخدمة", "العلامة التجارية", "اسم المنتج", "الكمية", "السعر", "قراءة العداد", "فترة التغيير أو المسافة المقترحة", "التاريخ", "ملاحظات")
        else -> listOf("المركبة (اختياري)", "عنوان الحادث", "صورة الحادث", "التاريخ", "الوقت", "الموقع (اختياري)", "تقدير الضرر — ليس مبلغًا مدفوعًا تلقائيًا", "ملاحظات")
    }
    "education" -> if (tabId == "student_form") listOf("اسم الطالب", "المرحلة التعليمية", "العمر", "الرقم القومي أو المرجعي — 14 رقمًا") else listOf("الطالب", "تصنيف المصروف", "عنوان المصروف", "المبلغ", "التاريخ")
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
    "education" -> if (tabId == "student_form") listOf("رياض أطفال", "ابتدائي", "إعدادي", "ثانوي", "جامعة") else listOf("دروس خصوصية", "شخصي", "انتقالات", "مصروفات مدرسية", "كتب ومذكرات", "أتوبيس المدرسة", "مستلزمات", "أخرى")
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
    var showDesignNotice by remember { mutableStateOf(false) }
    val section = costSections.firstOrNull { it.id == selectedSection }
    val tabs = section?.let { tabsFor(it.id) }.orEmpty()
    val activeTab = tabs.firstOrNull { it.id == selectedTab }

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
                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF8F1D9)) {
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
                costSections.chunked(2).forEach { rowSections ->
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            rowSections.forEach { sectionItem ->
                                CostSectionCard(sectionItem, Modifier.weight(1f)) {
                                    selectedSection = sectionItem.id
                                    selectedTab = ""
                                }
                            }
                            if (rowSections.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
                item {
                    OutlinedButton(onClick = { showDesignNotice = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, CostGold)) {
                        Text("إضافة قسم مالي جديد", color = CostInk, fontWeight = FontWeight.Bold)
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
                            Text(section.subtitle, color = Color(0xFFE7E3D8), fontSize = 11.sp)
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
                        OutlinedTextField(value = "", onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("بحث في " + section.title) }, placeholder = { Text("سيُفعّل البحث في المرحلة الثانية") }, shape = RoundedCornerShape(13.dp))
                    }
                }
                if (section.id != "reports" && section.id != "backup") {
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            items(if (section.id == "education" && activeTab?.id == "students") listOf("إضافة طالب", "تعديل طالب") else listOf("إضافة", "تعديل", "حذف", "تصفية")) { action ->
                                OutlinedButton(onClick = { showDesignNotice = true }, border = BorderStroke(1.dp, CostGold), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp)) {
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
                                OutlinedButton(onClick = { showDesignNotice = true }, border = BorderStroke(1.dp, CostBorder), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp)) {
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
                if (activeTab?.id?.let { it in setOf("records", "list", "trips", "students", "student_expenses", "association_list", "movements", "sources", "installments", "payouts") } == true) {
                    item { RecordsPreviewDesign(section.title, activeTab?.title.orEmpty()) }
                } else if (section.id == "reports") {
                    item { ReportPreviewDesign { showDesignNotice = true } }
                } else if (section.id == "backup") {
                    item { BackupPreviewDesign(activeTab?.id.orEmpty()) { showDesignNotice = true } }
                } else {
                    item { ExpenseEntryPreview(section.id, activeTab?.id.orEmpty()) }
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, CostBorder), shape = RoundedCornerShape(15.dp)) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("السجلات المسجلة", color = CostInk, fontWeight = FontWeight.Bold)
                                Text("مكان عرض السجلات ونتائجها بعد تفعيل الحفظ في المرحلة الثانية.", color = CostMuted, fontSize = 11.sp)
                                Text("المعاينة الحالية لا تحفظ أي بيانات.", color = CostMuted, fontSize = 10.sp)
                            }
                        }
                    }
                    item {
                        Button(onClick = { showDesignNotice = true }, modifier = Modifier.fillMaxWidth(), enabled = false, colors = ButtonDefaults.buttonColors(disabledContainerColor = Color(0xFFE9E6DE), disabledContentColor = CostMuted), shape = RoundedCornerShape(13.dp)) {
                            Text("الحفظ غير مفعّل — المرحلة الثانية", fontWeight = FontWeight.Bold)
                        }
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
                        Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFFF8F1D9), border = BorderStroke(1.dp, CostBorder)) {
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
private fun ExpenseEntryPreview(sectionId: String, tabId: String) {
    var values by remember(sectionId, tabId) { mutableStateOf(emptyMap<String, String>()) }
    var oilMode by remember(sectionId, tabId) { mutableStateOf("الزيوت") }
    fun value(label: String) = values[label].orEmpty()
    fun setValue(label: String, newValue: String) { values = values + (label to newValue) }

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
                PreviewDropdown("فرد الأسرة", listOf("اختاري فرد الأسرة", "إضافة فرد أسرة لاحقًا"), value("فرد الأسرة").ifBlank { "اختاري فرد الأسرة" }) { setValue("فرد الأسرة", it) }
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
                    PreviewInput("عدد الأشهر", value("عدد الأشهر"), numeric = true) { setValue("عدد الأشهر", it) }
                    PreviewInput("قيمة القسط الشهري", value("قيمة القسط الشهري"), numeric = true) { setValue("قيمة القسط الشهري", it) }
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
                    PreviewDropdown("السيارة", listOf("اختاري السيارة"), value("السيارة").ifBlank { "اختاري السيارة" }) { setValue("السيارة", it) }
                    PreviewDropdown("نوع الوقود / مصدر الطاقة", vehicleFuel, value("نوع الوقود").ifBlank { "بنزين 92" }) { setValue("نوع الوقود", it) }
                    PreviewInput("الكمية / عدد اللترات", value("الكمية"), numeric = true) { setValue("الكمية", it) }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PreviewInput("قراءة عداد السيارة", value("العداد"), numeric = true) { setValue("العداد", it) }
                        IconButton(onClick = { }) { Icon(Icons.Outlined.CameraAlt, contentDescription = "تصوير قراءة العداد", tint = CostGoldDark) }
                    }
                    PreviewDateInput("تاريخ ووقت التزوّد", value("التاريخ والوقت")) { setValue("التاريخ والوقت", it) }
                    Text("زر الكاميرا مخصص لقراءة العداد تلقائيًا عند تنفيذ وظائف الكاميرا لاحقًا.", color = CostMuted, fontSize = 10.sp)
                    Text("سعر الوحدة والإجمالي يظهران بعد تفعيل الحسابات.", color = CostMuted, fontSize = 11.sp)
                }
                "maintenance" -> {
                    PreviewDropdown("السيارة", listOf("اختاري السيارة"), value("السيارة").ifBlank { "اختاري السيارة" }) { setValue("السيارة", it) }
                    PreviewDropdown("نوع الصيانة", maintenanceTypes, value("نوع الصيانة").ifBlank { maintenanceTypes.first() }) { setValue("نوع الصيانة", it) }
                    PreviewInput("وصف الصيانة", value("وصف الصيانة"), multiline = true) { setValue("وصف الصيانة", it) }
                    PreviewInput("اسم المستلزم أو قطعة الغيار", value("قطعة الغيار")) { setValue("قطعة الغيار", it) }
                    PreviewInput("سعر المستلزمات", value("سعر المستلزمات"), numeric = true) { setValue("سعر المستلزمات", it) }
                    PreviewInput("وصف الأجور", value("وصف الأجور")) { setValue("وصف الأجور", it) }
                    PreviewInput("قيمة الأجور", value("قيمة الأجور"), numeric = true) { setValue("قيمة الأجور", it) }
                    PreviewDateInput("التاريخ", value("التاريخ")) { setValue("التاريخ", it) }
                }
                "oils" -> {
                    PreviewDropdown("السيارة", listOf("اختاري السيارة"), value("السيارة").ifBlank { "اختاري السيارة" }) { setValue("السيارة", it) }
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
                    PreviewInput("الكمية", value("الكمية"), numeric = true) { setValue("الكمية", it) }
                    PreviewInput("السعر", value("السعر"), numeric = true) { setValue("السعر", it) }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PreviewInput("قراءة العداد", value("العداد"), numeric = true) { setValue("العداد", it) }
                        IconButton(onClick = { }) { Icon(Icons.Outlined.CameraAlt, contentDescription = "تصوير قراءة العداد", tint = CostGoldDark) }
                    }
                    PreviewDateInput("التاريخ", value("التاريخ")) { setValue("التاريخ", it) }
                    PreviewInput("ملاحظات", value("ملاحظات"), multiline = true) { setValue("ملاحظات", it) }
                }
                "accidents" -> {
                    PreviewDropdown("السيارة", listOf("اختاري السيارة"), value("السيارة").ifBlank { "اختاري السيارة" }) { setValue("السيارة", it) }
                    PreviewInput("عنوان الحادث", value("عنوان الحادث")) { setValue("عنوان الحادث", it) }
                    PreviewDateInput("التاريخ", value("التاريخ")) { setValue("التاريخ", it) }
                    PreviewInput("الوقت", value("الوقت")) { setValue("الوقت", it) }
                    PreviewInput("الموقع (اختياري)", value("الموقع")) { setValue("الموقع", it) }
                    PreviewInput("تقدير الضرر — ليس مبلغًا مدفوعًا", value("تقدير الضرر"), numeric = true) { setValue("تقدير الضرر", it) }
                    PreviewInput("ملاحظات", value("ملاحظات"), multiline = true) { setValue("ملاحظات", it) }
                }
                else -> {
                    PreviewInput("اسم السيارة", value("اسم السيارة")) { setValue("اسم السيارة", it) }
                    PreviewInput("الماركة", value("الماركة")) { setValue("الماركة", it) }
                    PreviewInput("الطراز", value("الطراز")) { setValue("الطراز", it) }
                    PreviewInput("الموديل", value("الموديل")) { setValue("الموديل", it) }
                    PreviewInput("سنة الصنع", value("سنة الصنع"), numeric = true) { setValue("سنة الصنع", it) }
                    PreviewInput("اللون", value("اللون")) { setValue("اللون", it) }
                    PreviewInput("رقم اللوحة", value("رقم اللوحة")) { setValue("رقم اللوحة", it) }
                    PreviewInput("رقم الشاسيه (اختياري)", value("رقم الشاسيه")) { setValue("رقم الشاسيه", it) }
                }
            }
            "education" -> when (tabId) {
                "student_add" -> {
                    PreviewInput("اسم الطالب", value("اسم الطالب")) { setValue("اسم الطالب", it) }
                    PreviewDropdown("المرحلة التعليمية", listOf("رياض أطفال", "ابتدائي", "إعدادي", "ثانوي", "جامعة"), value("المرحلة").ifBlank { "ابتدائي" }) { setValue("المرحلة", it) }
                    PreviewInput("العمر", value("العمر"), numeric = true) { setValue("العمر", it) }
                    PreviewInput("رقم مرجعي (اختياري)", value("رقم مرجعي")) { setValue("رقم مرجعي", it) }
                }
                "student_edit" -> {
                    PreviewDropdown("اختاري الطالب المراد تعديله", listOf("لا يوجد طلاب مسجلون في المعاينة"), value("الطالب").ifBlank { "لا يوجد طلاب مسجلون في المعاينة" }) { setValue("الطالب", it) }
                    Text("بعد ربط السجلات ستظهر أسماء الطلاب المسجلين هنا للاختيار ثم التعديل أو الحذف.", color = CostMuted, fontSize = 11.sp)
                }
                "student_expense_form" -> {
                    PreviewDropdown("اختاري الطالب", listOf("لا يوجد طلاب مسجلون في المعاينة"), value("الطالب").ifBlank { "لا يوجد طلاب مسجلون في المعاينة" }) { setValue("الطالب", it) }
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
        Text("معاينة إدخال فقط — البيانات لا تُحفظ في المرحلة الأولى.", color = CostMuted, fontSize = 10.sp)
    }
}

@Composable
private fun PreviewDateInput(label: String, value: String, onValueChange: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    OutlinedButton(
        onClick = {
            val calendar = Calendar.getInstance()
            val parts = value.split("-")
            if (parts.size == 3) {
                parts[0].toIntOrNull()?.let { calendar.set(Calendar.YEAR, it) }
                parts[1].toIntOrNull()?.let { calendar.set(Calendar.MONTH, it - 1) }
                parts[2].toIntOrNull()?.let { calendar.set(Calendar.DAY_OF_MONTH, it) }
            }
            DatePickerDialog(context, { _, year, month, day ->
                onValueChange(String.format("%04d-%02d-%02d", year, month + 1, day))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
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
private fun PreviewInput(label: String, value: String, numeric: Boolean = false, multiline: Boolean = false, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
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
                        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFF8F1D9), border = BorderStroke(1.dp, CostBorder)) {
                            Text(option, color = CostInk, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp))
                        }
                    }
                }
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
    Surface(shape = RoundedCornerShape(30.dp), color = Color(0xFF343434)) {
        Text(label, color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp))
    }
}

@Composable
private fun CostTabCard(tab: CostTab, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier.height(112.dp), shape = RoundedCornerShape(15.dp), color = Color.White, border = BorderStroke(1.dp, CostBorder), shadowElevation = 1.dp) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.Start) {
            Surface(shape = RoundedCornerShape(9.dp), color = Color(0xFFF8F1D9)) {
                Icon(Icons.Outlined.FolderOpen, contentDescription = null, modifier = Modifier.padding(7.dp).size(20.dp), tint = CostInk)
            }
            Text(tab.title, color = CostInk, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("فتح القسم ←", color = CostGoldDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CostSectionCard(section: CostSection, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier.height(152.dp), shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, CostBorder), shadowElevation = 1.dp) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF8F1D9)) {
                Icon(section.icon, contentDescription = null, modifier = Modifier.padding(8.dp).size(21.dp), tint = CostInk)
            }
            Text(section.title, color = CostInk, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(section.subtitle, color = CostMuted, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("فتح القسم  ←", color = CostGoldDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
            Text(if (tabId == "restore") "اختيار الملف، فحص المخطط، عرض المجموعات التي ستُستبدل ثم طلب التأكيد." else "إنشاء ملف نسخة يتضمن المجموعات المدعومة مع إصدار المخطط وتاريخ التصدير.", color = CostMuted, fontSize = 11.sp)
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
