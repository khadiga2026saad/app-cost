package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.FinanceViewModel

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
    CostSection("vehicles", "المركبات", "الوقود والصيانة والزيوت والحوادث", Icons.Outlined.DirectionsCar, "المركبات"),
    CostSection("education", "التعليم والطلاب", "ملفات الطلاب ومصروفاتهم", Icons.Outlined.MenuBook, "المصروفات"),
    CostSection("income", "الدخل", "مصادر الدخل والإيرادات", Icons.Outlined.TrendingUp, "الأموال والحسابات"),
    CostSection("certificates", "الشهادات البنكية", "بيانات الشهادات والعوائد والاستحقاقات", Icons.Outlined.AccountBalance, "الأموال والحسابات"),
    CostSection("current_account", "الحساب الجاري", "الإيداعات والسحوبات وصافي الحركة", Icons.Outlined.AccountBalanceWallet, "الأموال والحسابات"),
    CostSection("car_income", "دخل رحلات السيارة", "الرحلات والأجرة والتقارير", Icons.Outlined.DirectionsCar, "الأموال والحسابات"),
    CostSection("reports", "التقارير والتحليلات", "تقارير اليوم والأسبوع والشهر", Icons.Outlined.Assessment, "المتابعة والتحليل"),
    CostSection("backup", "النسخ الاحتياطي والاستعادة", "تصدير النسخة ومراجعة الاستعادة", Icons.Outlined.ReceiptLong, "المتابعة والتحليل")
)

private fun tabsFor(id: String): List<CostTab> = when (id) {
    "vehicles" -> listOf(CostTab("fuel", "الوقود"), CostTab("maintenance", "الصيانة"), CostTab("oils", "الزيوت والفلاتر"), CostTab("accidents", "الحوادث"))
    "education" -> listOf(CostTab("students", "ملفات الطلاب"), CostTab("student_expenses", "مصروفات الطلاب"))
    "certificates" -> listOf(CostTab("certificates", "الشهادات"), CostTab("payouts", "العوائد"))
    "associations" -> listOf(CostTab("association_list", "الجمعيات"), CostTab("installments", "الأقساط"), CostTab("payouts", "القبض"))
    "current_account" -> listOf(CostTab("movements", "كل الحركات"), CostTab("deposits", "الإيداعات"), CostTab("withdrawals", "السحوبات"))
    "car_income" -> listOf(CostTab("trips", "سجل الرحلات"), CostTab("trip_report", "تقرير الرحلات"))
    "reports" -> listOf(CostTab("daily", "يومي"), CostTab("weekly", "أسبوعي"), CostTab("monthly", "شهري"))
    "backup" -> listOf(CostTab("export", "تصدير نسخة"), CostTab("restore", "استعادة نسخة"))
    else -> listOf(CostTab("records", "السجلات"), CostTab("form", "نموذج الإدخال"))
}

private fun fieldsFor(sectionId: String, tabId: String): List<String> = when (sectionId) {
    "house" -> listOf("التصنيف", "تصنيف مخصص (عند الحاجة)", "المبلغ", "التاريخ", "طريقة الدفع", "نوع الدفع: مستلزمات أو أجور", "الملاحظات")
    "medical" -> listOf("فرد الأسرة", "اسم المنشأة الطبية", "نوع المنشأة الطبية", "الحالة", "التشخيص", "تكلفة الكشف أو الفحص", "تكلفة الدواء", "التاريخ", "ملاحظات طبية")
    "work" -> listOf("نوع المصروف", "نوع مخصص", "المبلغ", "التاريخ", "الملاحظات")
    "personal" -> listOf("التصنيف", "تصنيف مخصص (عند الحاجة)", "المبلغ", "التاريخ", "طريقة الدفع", "الملاحظات")
    "associations" -> if (tabId == "association_list") listOf("اسم الجمعية", "قيمة الجمعية", "عدد الأفراد", "القسط", "رقم الدور", "تاريخ البداية", "تاريخ القبض", "ملاحظات") else listOf("اسم الجمعية", "الشهر", "قيمة القسط أو القبض", "التاريخ", "ملاحظات")
    "vehicles" -> when (tabId) {
        "fuel" -> listOf("المركبة", "نوع الوقود", "الكمية أو اللترات", "سعر الوحدة", "إجمالي التكلفة", "قراءة العداد", "التاريخ والوقت", "ملاحظات")
        "maintenance" -> listOf("المركبة", "نوع الصيانة", "وصف الصيانة", "اسم المستلزم أو قطعة الغيار", "سعر المستلزمات", "وصف الأجور", "قيمة الأجور", "التاريخ", "الإجمالي")
        "oils" -> listOf("المركبة", "نوع الخدمة", "العلامة التجارية", "اسم المنتج", "الكمية", "السعر", "قراءة العداد", "فترة التغيير أو المسافة المقترحة", "التاريخ", "ملاحظات")
        else -> listOf("المركبة (اختياري)", "عنوان الحادث", "صورة الحادث", "التاريخ", "الوقت", "الموقع (اختياري)", "تقدير الضرر — ليس مبلغًا مدفوعًا تلقائيًا", "ملاحظات")
    }
    "education" -> if (tabId == "students") listOf("اسم الطالب", "المرحلة التعليمية", "العمر", "الرقم القومي أو المرجعي — 14 رقمًا") else listOf("الطالب", "تصنيف المصروف", "عنوان المصروف", "المبلغ", "التاريخ")
    "income" -> listOf("اسم مصدر الدخل", "المبلغ", "التاريخ", "نوع الدخل", "ملاحظات")
    "certificates" -> listOf("اسم البنك", "رقم الشهادة", "المبلغ الأصلي", "معدل العائد السنوي", "مدة الشهادة", "دورية صرف العائد", "تاريخ الإصدار", "تاريخ الاستحقاق", "الملاحظات")
    "current_account" -> listOf("نوع الحركة: إيداع أو سحب", "الوصف", "المبلغ", "التاريخ")
    "car_income" -> listOf("تاريخ الرحلة", "نوع الرحلة", "الأجرة", "ملاحظات الرحلة")
    "reports" -> listOf("تاريخ البداية", "تاريخ النهاية", "القسم", "الطالب (عند الحاجة)")
    "backup" -> listOf("ملف النسخة الاحتياطية", "إصدار المخطط", "تاريخ التصدير", "المجموعات التي ستُستبدل")
    else -> listOf("التصنيف", "المبلغ", "التاريخ", "طريقة الدفع", "الملاحظات")
}

@Composable
fun CostWorkspaceScreen(
    viewModel: FinanceViewModel,
    onNavigateBack: () -> Unit,
    onAccountClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf("") }
    var showDesignNotice by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    val section = costSections.firstOrNull { it.id == selectedSection }
    val tabs = section?.let { tabsFor(it.id) }.orEmpty()
    val activeTab = tabs.firstOrNull { it.id == selectedTab } ?: tabs.firstOrNull()

    BackHandler(enabled = selectedSection != null) {
        selectedSection = null
        selectedTab = ""
        searchText = ""
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CostCanvas,
        topBar = {
            Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    if (selectedSection != null) {
                        selectedSection = null
                        selectedTab = ""
                        searchText = ""
                    } else onNavigateBack()
                }) { Text("‹", color = CostInk, fontSize = 30.sp, fontWeight = FontWeight.Bold) }
                Column(Modifier.weight(1f)) {
                    Text(section?.title ?: "قسم التكاليف", color = CostInk, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(section?.subtitle ?: "إدارة المصروفات والدخل والحسابات في مكان واحد", color = CostMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF8F1D9)) {
                    Text("SMART COST", color = CostGoldDark, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp))
                }
            }
        }
    ) { padding ->
        if (section == null) {
            LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), contentPadding = PaddingValues(top = 14.dp, bottom = 26.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    Surface(shape = RoundedCornerShape(20.dp), color = CostInk, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text("SMART COST PRO", color = CostGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("إدارة مالية متكاملة", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("اختر القسم لعرض سجلاته ونماذج الإدخال والتقارير الخاصة به", color = Color(0xFFE7E3D8), fontSize = 12.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                SummaryPill("المصروفات")
                                SummaryPill("الدخل")
                                SummaryPill("المتابعة")
                            }
                        }
                    }
                }
                costSections.groupBy { it.group }.forEach { (groupName, sections) ->
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(groupName, color = CostInk, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(8.dp))
                                Text(sections.size.toString() + " أقسام", color = CostMuted, fontSize = 10.sp)
                            }
                            sections.chunked(2).forEach { rowSections ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    rowSections.forEach { item ->
                                        CostSectionCard(item, Modifier.weight(1f)) {
                                            selectedSection = item.id
                                            selectedTab = tabsFor(item.id).firstOrNull()?.id.orEmpty()
                                        }
                                    }
                                    if (rowSections.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
                item {
                    OutlinedButton(onClick = { showDesignNotice = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, CostGold)) {
                        Text("إضافة قسم مالي جديد", color = CostInk, fontWeight = FontWeight.Bold)
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
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
                            tabs.forEach { tab -> FilterChip(selected = activeTab?.id == tab.id, onClick = { selectedTab = tab.id }, label = { Text(tab.title, fontSize = 10.sp) }) }
                        }
                    }
                }
                item {
                    OutlinedTextField(value = searchText, onValueChange = { searchText = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("بحث في " + section.title) }, placeholder = { Text("الاسم أو التصنيف أو الملاحظات") }, shape = RoundedCornerShape(13.dp))
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        ActionOutline("إضافة", Modifier.weight(1f)) { showDesignNotice = true }
                        ActionOutline("تعديل", Modifier.weight(1f)) { showDesignNotice = true }
                        ActionOutline("حذف", Modifier.weight(1f)) { showDesignNotice = true }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        ActionOutline("تصفية", Modifier.weight(1f)) { showDesignNotice = true }
                        ActionOutline("تصدير", Modifier.weight(1f)) { showDesignNotice = true }
                        ActionOutline("طباعة", Modifier.weight(1f)) { showDesignNotice = true }
                    }
                }
                item {
                    val formTitle = "نموذج البيانات — " + (activeTab?.title ?: "السجلات")
                    Text(if (section.id == "reports") "معاينة التقرير" else if (section.id == "backup") "إجراءات النسخ الاحتياطي" else formTitle, color = CostInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                if (section.id == "reports") {
                    item { ReportPreviewDesign { showDesignNotice = true } }
                } else if (section.id == "backup") {
                    item { BackupPreviewDesign(activeTab?.id.orEmpty()) { showDesignNotice = true } }
                } else {
                    items(fieldsFor(section.id, activeTab?.id.orEmpty())) { field ->
                        OutlinedTextField(value = "", onValueChange = {}, modifier = Modifier.fillMaxWidth(), readOnly = true, label = { Text(field) }, placeholder = { Text("يظهر الحقل عند تفعيل الإدخال", fontSize = 10.sp) }, singleLine = !field.contains("ملاحظات"), minLines = if (field.contains("ملاحظات")) 2 else 1, shape = RoundedCornerShape(13.dp))
                    }
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, CostBorder), shape = RoundedCornerShape(15.dp)) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("السجلات المسجلة", color = CostInk, fontWeight = FontWeight.Bold)
                                Text("ستظهر هنا قائمة السجلات وتفاصيلها بعد تفعيل الوظائف في المرحلة الثانية.", color = CostMuted, fontSize = 11.sp)
                                Text("لا توجد بيانات فعلية معروضة في معاينة التصميم.", color = CostMuted, fontSize = 10.sp)
                            }
                        }
                    }
                    item {
                        Button(onClick = { showDesignNotice = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = CostInk), shape = RoundedCornerShape(13.dp)) {
                            Text("حفظ / اعتماد النموذج — مرحلة التصميم", color = CostGold, fontWeight = FontWeight.Bold)
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
private fun SummaryPill(label: String) {
    Surface(shape = RoundedCornerShape(30.dp), color = Color(0xFF343434)) {
        Text(label, color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp))
    }
}

@Composable
private fun CostSectionCard(section: CostSection, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier.height(145.dp), shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, CostBorder), shadowElevation = 1.dp) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF8F1D9)) {
                Icon(section.icon, contentDescription = null, modifier = Modifier.padding(8.dp).size(21.dp), tint = CostInk)
            }
            Text(section.title, color = CostInk, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(section.subtitle, color = CostMuted, fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("فتح القسم  ←", color = CostGoldDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ActionOutline(label: String, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 9.dp), shape = RoundedCornerShape(11.dp), border = BorderStroke(1.dp, CostGold)) {
        Text(label, color = CostInk, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
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
        Column(Modifier.padding(9.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(label, color = CostMuted, fontSize = 9.sp)
            Text(value, color = CostInk, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
    }
}
