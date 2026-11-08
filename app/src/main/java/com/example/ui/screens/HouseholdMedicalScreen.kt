package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val HInk = Color(0xFF263238)
private val HMuted = Color(0xFF71808A)
private val HCanvas = Color(0xFFF4F6F8)
private val HBorder = Color(0xFFE1E7EC)
private val HAccent = Color(0xFF52796F)
private val HSoft = Color(0xFFE8F0ED)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseholdMedicalScreen(onBack: () -> Unit, isEnglish: Boolean = false, modifier: Modifier = Modifier) {
    var medical by remember { mutableStateOf(false) }
    var showSheet by remember { mutableStateOf(false) }
    val files = remember { mutableStateListOf<Uri>() }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { files.addAll(it) }
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val addText = if (isEnglish) if (medical) "Add checkup" else "Add expense" else if (medical) "إضافة كشف" else "إضافة مصروف"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = HCanvas,
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            Surface(color = Color.White, shadowElevation = 1.dp) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, if (isEnglish) "Back" else "رجوع", tint = HInk) }
                    Column(Modifier.weight(1f)) {
                        Text(if (isEnglish) "Household expenses" else "مصروفات منزلية", color = HInk, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(if (isEnglish) "Food, repairs, furnishings and medical care" else "متابعة الطعام والإصلاحات والمفروشات والقسم الطبي", color = HMuted, fontSize = 10.sp)
                    }
                    Icon(if (medical) Icons.Outlined.MedicalServices else Icons.Outlined.Home, null, tint = HAccent)
                }
            }
        },
        floatingActionButton = {
            Button(onClick = { files.clear(); showSheet = true }, colors = ButtonDefaults.buttonColors(containerColor = HAccent), shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Outlined.Add, null)
                Spacer(Modifier.width(6.dp))
                Text(addText, fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            TabRow(selectedTabIndex = if (medical) 1 else 0, containerColor = HCanvas, contentColor = HAccent) {
                Tab(!medical, { medical = false }, text = { Text(if (isEnglish) "Household" else "مصروفات منزلية", fontSize = 12.sp) }, icon = { Icon(Icons.Outlined.Home, null) })
                Tab(medical, { medical = true }, text = { Text(if (isEnglish) "Medical" else "القسم الطبي", fontSize = 12.sp) }, icon = { Icon(Icons.Outlined.MedicalServices, null) })
            }
            Spacer(Modifier.height(14.dp))
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = HInk)) {
                Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text(if (isEnglish) "CURRENT MONTH SUMMARY" else "ملخص الشهر الحالي", color = Color(0xFFCFD8DC), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(if (isEnglish) "Total expenses" else "إجمالي المصروفات", color = Color.White.copy(alpha = .88f), fontSize = 12.sp)
                    Text("— " + if (isEnglish) "EGP" else "ج.م", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
                    Divider(color = Color.White.copy(alpha = .2f))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isEnglish) if (medical) "Checkups" else "Receipts" else if (medical) "عدد الكشوفات" else "عدد الفواتير", color = Color(0xFFCFD8DC), fontSize = 11.sp)
                        Text("—", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (isEnglish) if (medical) "Medical records" else "Household records" else if (medical) "سجل الكشوفات الطبية" else "سجل المصروفات المنزلية", color = HInk, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(if (isEnglish) "Newest first" else "الأحدث أولاً", color = HMuted, fontSize = 10.sp)
            }
            Spacer(Modifier.height(9.dp))
            Surface(Modifier.fillMaxWidth().heightIn(min = 190.dp), shape = RoundedCornerShape(18.dp), color = Color.White, border = BorderStroke(1.dp, HBorder)) {
                Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(if (medical) Icons.Outlined.LocalHospital else Icons.Outlined.ReceiptLong, null, tint = HAccent, modifier = Modifier.size(38.dp))
                    Spacer(Modifier.height(10.dp))
                    Text(if (isEnglish) "No records yet" else if (medical) "لا توجد كشوفات طبية مسجلة" else "لا توجد مصروفات منزلية مسجلة", color = HInk, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(if (isEnglish) "Use the add button to create your first record." else "استخدم زر الإضافة لتسجيل أول عملية.", color = HMuted, fontSize = 11.sp, textAlign = TextAlign.Center)
                }
            }
            Spacer(Modifier.height(90.dp))
        }
    }

    if (showSheet) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }, containerColor = Color.White, dragHandle = null) {
            if (medical) {
                MedicalDesignForm(isEnglish, files, { picker.launch("image/*") }, { showSheet = false }) {
                    scope.launch { snack.showSnackbar(if (isEnglish) "Saving will be enabled after design approval." else "سيتم تفعيل الحفظ بعد اعتماد التصميم.") }
                }
            } else {
                HouseholdDesignForm(isEnglish, files, { picker.launch("image/*") }, { showSheet = false }) {
                    scope.launch { snack.showSnackbar(if (isEnglish) "Saving will be enabled after design approval." else "سيتم تفعيل الحفظ بعد اعتماد التصميم.") }
                }
            }
        }
    }
}

@Composable
private fun SheetHeader(title: String, subtitle: String, close: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = HInk, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = HMuted, fontSize = 10.sp)
        }
        IconButton(onClick = close) { Icon(Icons.Outlined.Close, "Close", tint = HMuted) }
    }
    Divider(color = HBorder)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HouseholdDesignForm(en: Boolean, files: List<Uri>, pick: () -> Unit, close: () -> Unit, save: () -> Unit) {
    val cats = if (en) listOf("Breakfast", "Food", "Supplies", "Repairs") else listOf("فطار", "طعام", "مستلزمات", "إصلاحات")
    var category by remember { mutableStateOf(cats[0]) }
    var menu by remember { mutableStateOf(false) }
    var amount by remember { mutableStateOf("0.00") }
    var notes by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(0) }
    var dateOpen by remember { mutableStateOf(false) }
    val date = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    val dateLabel = date.selectedDateMillis?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it)) } ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    SheetHeader(if (en) "Add household expense" else "إضافة مصروف منزل جديد", if (en) "Expense details" else "بيانات المصروف", close)
    Column(Modifier.fillMaxWidth().heightIn(max = 680.dp).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        Box {
            OutlinedTextField(category, {}, Modifier.fillMaxWidth().clickable { menu = true }, readOnly = true, label = { Text(if (en) "Expense category *" else "تصنيف المصروف *") }, shape = RoundedCornerShape(13.dp), trailingIcon = { Text("⌄") })
            DropdownMenu(menu, { menu = false }) { cats.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { category = it; menu = false }) } }
        }
        OutlinedTextField(amount, { amount = it.filter { c -> c.isDigit() || c == '.' } }, Modifier.fillMaxWidth(), label = { Text(if (en) "Amount (EGP) *" else "المبلغ (جنيه مصري) *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, shape = RoundedCornerShape(13.dp))
        DateInput(dateLabel, if (en) "Expense date *" else "تاريخ المصروف *") { dateOpen = true }
        Text(if (en) "Spending type" else "نوع الصرف", color = HInk, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            FilterChip(type == 0, { type = 0 }, label = { Text(if (en) "Purchases & supplies" else "مشتريات ومستلزمات", fontSize = 10.sp) })
            FilterChip(type == 1, { type = 1 }, label = { Text(if (en) "Labor & services" else "مصنعية وأجرة يد", fontSize = 10.sp) })
        }
        OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth(), label = { Text(if (en) "Notes and details" else "ملاحظات وتفاصيل") }, placeholder = { Text(if (en) "Additional details..." else "تفاصيل إضافية...") }, minLines = 2, maxLines = 4, shape = RoundedCornerShape(13.dp))
        AttachmentInput(if (en) "Receipts and invoices" else "صور الفواتير وإيصالات الشراء", if (en) "Attach one or more images" else "إرفاق صورة أو أكثر للفاتورة أو الإيصال", files, pick, en)
        FormActions(if (en) "Add expense" else "إضافة المصروف", if (en) "Cancel" else "إلغاء", save, close)
    }
    if (dateOpen) DatePickerDialog(onDismissRequest = { dateOpen = false }, confirmButton = { TextButton(onClick = { dateOpen = false }) { Text(if (en) "Done" else "تم") } }, dismissButton = { TextButton(onClick = { dateOpen = false }) { Text(if (en) "Cancel" else "إلغاء") } }) { DatePicker(state = date) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedicalDesignForm(en: Boolean, files: List<Uri>, pick: () -> Unit, close: () -> Unit, save: () -> Unit) {
    var patient by remember { mutableStateOf("") }
    var facility by remember { mutableStateOf(if (en) "Hospital" else "مستشفى") }
    var facilityMenu by remember { mutableStateOf(false) }
    var facilityName by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("") }
    var diagnosis by remember { mutableStateOf("") }
    var checkup by remember { mutableStateOf("0.00") }
    var medicine by remember { mutableStateOf("0.00") }
    var notes by remember { mutableStateOf("") }
    var dateOpen by remember { mutableStateOf(false) }
    val date = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    val dateLabel = date.selectedDateMillis?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it)) } ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    val facilities = if (en) listOf("Hospital", "Clinic") else listOf("مستشفى", "عيادة")
    SheetHeader(if (en) "Add medical checkup" else "إضافة كشف طبي", if (en) "Patient and visit details" else "بيانات المريض وتفاصيل الزيارة", close)
    Column(Modifier.fillMaxWidth().heightIn(max = 720.dp).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(patient, { patient = it }, Modifier.fillMaxWidth(), label = { Text(if (en) "Patient name *" else "اسم المريض *") }, singleLine = true, shape = RoundedCornerShape(13.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(.8f)) {
                OutlinedTextField(facility, {}, Modifier.fillMaxWidth().clickable { facilityMenu = true }, readOnly = true, label = { Text(if (en) "Facility type" else "نوع المنشأة") }, shape = RoundedCornerShape(13.dp))
                DropdownMenu(facilityMenu, { facilityMenu = false }) { facilities.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { facility = it; facilityMenu = false }) } }
            }
            OutlinedTextField(facilityName, { facilityName = it }, Modifier.weight(1.2f), label = { Text(if (en) "Hospital / clinic name" else "اسم المستشفى / العيادة") }, singleLine = true, shape = RoundedCornerShape(13.dp))
        }
        OutlinedTextField(condition, { condition = it }, Modifier.fillMaxWidth(), label = { Text(if (en) "Medical condition *" else "توصيف الحالة المرضية *") }, minLines = 2, maxLines = 3, shape = RoundedCornerShape(13.dp))
        OutlinedTextField(diagnosis, { diagnosis = it }, Modifier.fillMaxWidth(), label = { Text(if (en) "Doctor's diagnosis" else "تشخيص الدكتور") }, minLines = 2, maxLines = 3, shape = RoundedCornerShape(13.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(checkup, { checkup = it.filter { c -> c.isDigit() || c == '.' } }, Modifier.weight(1f), label = { Text(if (en) "Checkup cost" else "تكلفة الكشف") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, shape = RoundedCornerShape(13.dp))
            OutlinedTextField(medicine, { medicine = it.filter { c -> c.isDigit() || c == '.' } }, Modifier.weight(1f), label = { Text(if (en) "Medication cost" else "تكلفة الأدوية") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, shape = RoundedCornerShape(13.dp))
        }
        DateInput(dateLabel, if (en) "Visit date" else "تاريخ الكشف") { dateOpen = true }
        OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth(), label = { Text(if (en) "Additional notes" else "ملاحظات إضافية") }, minLines = 2, maxLines = 3, shape = RoundedCornerShape(13.dp))
        AttachmentInput(if (en) "Prescriptions and medical documents" else "الروشتات وأوراق الكشف الطبي", if (en) "Attach prescriptions, reports or test results" else "إرفاق الروشتات والتقارير ونتائج التحاليل", files, pick, en)
        FormActions(if (en) "Save" else "حفظ", if (en) "Cancel" else "إلغاء", save, close)
    }
    if (dateOpen) DatePickerDialog(onDismissRequest = { dateOpen = false }, confirmButton = { TextButton(onClick = { dateOpen = false }) { Text(if (en) "Done" else "تم") } }, dismissButton = { TextButton(onClick = { dateOpen = false }) { Text(if (en) "Cancel" else "إلغاء") } }) { DatePicker(state = date) }
}

@Composable
private fun DateInput(value: String, label: String, click: () -> Unit) {
    Box {
        OutlinedTextField(value, {}, Modifier.fillMaxWidth().clickable(onClick = click), readOnly = true, label = { Text(label) }, trailingIcon = { Icon(Icons.Outlined.CalendarMonth, null, tint = HAccent) }, shape = RoundedCornerShape(13.dp))
        Box(Modifier.matchParentSize().clickable(onClick = click))
    }
}

@Composable
private fun AttachmentInput(title: String, subtitle: String, files: List<Uri>, pick: () -> Unit, en: Boolean) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = HSoft, border = BorderStroke(1.dp, Color(0xFFD4E3DD))) {
        Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.AttachFile, null, tint = HAccent)
                Spacer(Modifier.width(7.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, color = HInk, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(subtitle, color = HMuted, fontSize = 10.sp)
                }
                OutlinedButton(onClick = pick, shape = RoundedCornerShape(10.dp)) {
                    Icon(Icons.Outlined.UploadFile, null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(3.dp))
                    Text(if (files.isEmpty()) if (en) "Attach" else "إرفاق" else files.size.toString(), fontSize = 10.sp)
                }
            }
            files.forEach { uri -> Text(uri.lastPathSegment ?: "Attachment", color = HMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        }
    }
}

@Composable
private fun FormActions(primary: String, cancel: String, save: () -> Unit, close: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(bottom = 18.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = save, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = HAccent), shape = RoundedCornerShape(12.dp)) { Text(primary) }
        OutlinedButton(onClick = close, Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text(cancel) }
    }
}
