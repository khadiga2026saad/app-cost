package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entities.HouseholdExpenseEntity
import com.example.data.local.entities.MedicalExpenseEntity
import com.example.ui.viewmodel.HouseholdMedicalViewModel
import org.json.JSONArray
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
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
fun HouseholdMedicalScreen(
    onBack: () -> Unit,
    isEnglish: Boolean = false,
    modifier: Modifier = Modifier,
    vm: HouseholdMedicalViewModel = viewModel(factory = HouseholdMedicalViewModel.Factory)
) {
    var medical by remember { mutableStateOf(false) }
    var showSheet by remember { mutableStateOf(false) }
    var editingHousehold by remember { mutableStateOf<HouseholdExpenseEntity?>(null) }
    var editingMedical by remember { mutableStateOf<MedicalExpenseEntity?>(null) }
    var deleteHouseholdId by remember { mutableStateOf<Long?>(null) }
    var deleteMedicalId by remember { mutableStateOf<Long?>(null) }
    val household by vm.householdRecords.collectAsStateWithLifecycle()
    val medicalRecords by vm.medicalRecords.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val addText = if (isEnglish) if (medical) "Add checkup" else "Add expense" else if (medical) "إضافة كشف" else "إضافة مصروف"

    val monthRecordsHouse = remember(household) { household.filter { isCurrentMonth(it.date) } }
    val monthRecordsMedical = remember(medicalRecords) { medicalRecords.filter { isCurrentMonth(it.date) } }
    val totalPiastres = if (medical) monthRecordsMedical.sumOf { it.totalCostPiastres } else monthRecordsHouse.sumOf { it.amountPiastres }
    val recordCount = if (medical) monthRecordsMedical.size else monthRecordsHouse.size

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
            Button(onClick = {
                editingHousehold = null
                editingMedical = null
                showSheet = true
            }, colors = ButtonDefaults.buttonColors(containerColor = HAccent), shape = RoundedCornerShape(14.dp)) {
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
                    Text(formatEgp(totalPiastres), color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
                    Divider(color = Color.White.copy(alpha = .2f))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isEnglish) if (medical) "Checkups" else "Receipts" else if (medical) "عدد الكشوفات" else "عدد الفواتير", color = Color(0xFFCFD8DC), fontSize = 11.sp)
                        Text(recordCount.toString(), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (isEnglish) if (medical) "Medical records" else "Household records" else if (medical) "سجل الكشوفات الطبية" else "سجل المصروفات المنزلية", color = HInk, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(if (isEnglish) "Newest first" else "الأحدث أولاً", color = HMuted, fontSize = 10.sp)
            }
            Spacer(Modifier.height(9.dp))
            if (medical) {
                if (medicalRecords.isEmpty()) EmptyRecords(true, isEnglish)
                else LazyColumn(contentPadding = PaddingValues(bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    items(medicalRecords, key = { it.id }) { item ->
                        MedicalRecordCard(item, isEnglish, onEdit = { editingMedical = item; editingHousehold = null; showSheet = true }, onDelete = { deleteMedicalId = item.id })
                    }
                }
            } else {
                if (household.isEmpty()) EmptyRecords(false, isEnglish)
                else LazyColumn(contentPadding = PaddingValues(bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    items(household, key = { it.id }) { item ->
                        HouseholdRecordCard(item, isEnglish, onEdit = { editingHousehold = item; editingMedical = null; showSheet = true }, onDelete = { deleteHouseholdId = item.id })
                    }
                }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }, containerColor = Color.White, dragHandle = null) {
            if (medical) {
                MedicalForm(isEnglish, editingMedical, onPick = { /* picker is hosted in the form */ }, onCancel = { showSheet = false }) { item, attachments ->
                    if (editingMedical == null) vm.saveMedical(item, attachments) { result ->
                        result.onSuccess { showSheet = false }.onFailure { scope.launch { snack.showSnackbar(it.message ?: "Save failed") } }
                    } else vm.updateMedical(item, attachments) { result ->
                        result.onSuccess { showSheet = false }.onFailure { scope.launch { snack.showSnackbar(it.message ?: "Update failed") } }
                    }
                }
            } else {
                HouseholdForm(isEnglish, editingHousehold, onCancel = { showSheet = false }) { item, attachments ->
                    if (editingHousehold == null) vm.saveHousehold(item, attachments) { result ->
                        result.onSuccess { showSheet = false }.onFailure { scope.launch { snack.showSnackbar(it.message ?: "Save failed") } }
                    } else vm.updateHousehold(item, attachments) { result ->
                        result.onSuccess { showSheet = false }.onFailure { scope.launch { snack.showSnackbar(it.message ?: "Update failed") } }
                    }
                }
            }
        }
    }

    deleteHouseholdId?.let { id ->
        AlertDialog(onDismissRequest = { deleteHouseholdId = null }, title = { Text(if (isEnglish) "Delete expense?" else "حذف المصروف؟") },
            text = { Text(if (isEnglish) "This record and its attached files will be deleted." else "سيتم حذف السجل والملفات المرفقة به.") },
            confirmButton = { TextButton(onClick = { vm.deleteHousehold(id) { deleteHouseholdId = null } }) { Text(if (isEnglish) "Delete" else "حذف") } },
            dismissButton = { TextButton(onClick = { deleteHouseholdId = null }) { Text(if (isEnglish) "Cancel" else "إلغاء") } })
    }
    deleteMedicalId?.let { id ->
        AlertDialog(onDismissRequest = { deleteMedicalId = null }, title = { Text(if (isEnglish) "Delete checkup?" else "حذف الكشف الطبي؟") },
            text = { Text(if (isEnglish) "This record and its attached files will be deleted." else "سيتم حذف السجل والملفات المرفقة به.") },
            confirmButton = { TextButton(onClick = { vm.deleteMedical(id) { deleteMedicalId = null } }) { Text(if (isEnglish) "Delete" else "حذف") } },
            dismissButton = { TextButton(onClick = { deleteMedicalId = null }) { Text(if (isEnglish) "Cancel" else "إلغاء") } })
    }
}

@Composable
private fun EmptyRecords(medical: Boolean, en: Boolean) {
    Surface(Modifier.fillMaxWidth().heightIn(min = 190.dp), shape = RoundedCornerShape(18.dp), color = Color.White, border = BorderStroke(1.dp, HBorder)) {
        Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(if (medical) Icons.Outlined.LocalHospital else Icons.Outlined.ReceiptLong, null, tint = HAccent, modifier = Modifier.size(38.dp))
            Spacer(Modifier.height(10.dp))
            Text(if (en) "No records yet" else if (medical) "لا توجد كشوفات طبية مسجلة" else "لا توجد مصروفات منزلية مسجلة", color = HInk, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(if (en) "Use the add button to create your first record." else "استخدم زر الإضافة لتسجيل أول عملية.", color = HMuted, fontSize = 11.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun HouseholdRecordCard(item: HouseholdExpenseEntity, en: Boolean, onEdit: () -> Unit, onDelete: () -> Unit) {
    RecordCard(
        title = item.category,
        subtitle = if (item.expenseType == "LABOR_AND_SERVICES") if (en) "Labor and services" else "مصنعية وأجرة يد" else if (en) "Purchases and supplies" else "مشتريات ومستلزمات",
        amount = formatEgp(item.amountPiastres),
        date = formatDate(item.date),
        details = item.notes,
        attachmentCount = attachmentCount(item.attachmentsJson),
        en = en, onEdit = onEdit, onDelete = onDelete
    )
}

@Composable
private fun MedicalRecordCard(item: MedicalExpenseEntity, en: Boolean, onEdit: () -> Unit, onDelete: () -> Unit) {
    val total = item.totalCostPiastres
    RecordCard(
        title = item.patientName,
        subtitle = "${item.facilityName.ifBlank { item.facilityType }} • ${if (en) "Checkup" else "كشف"}: ${formatEgp(item.checkupCostPiastres)} • ${if (en) "Medicine" else "دواء"}: ${formatEgp(item.medicationCostPiastres)}",
        amount = formatEgp(total),
        date = formatDate(item.date),
        details = listOf(item.conditionDescription, item.doctorDiagnosis, item.notes).filter { it.isNotBlank() }.joinToString(" • "),
        attachmentCount = attachmentCount(item.attachmentsJson),
        en = en, onEdit = onEdit, onDelete = onDelete
    )
}

@Composable
private fun RecordCard(title: String, subtitle: String, amount: String, date: String, details: String, attachmentCount: Int, en: Boolean, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, HBorder)) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(title, color = HInk, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(subtitle, color = HMuted, fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Text(amount, color = HAccent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            if (details.isNotBlank()) Text(details, color = HMuted, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarMonth, null, tint = HMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(date, color = HMuted, fontSize = 10.sp)
                if (attachmentCount > 0) {
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.Outlined.AttachFile, null, tint = HAccent, modifier = Modifier.size(14.dp))
                    Text(attachmentCount.toString(), color = HAccent, fontSize = 10.sp)
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) { Icon(Icons.Outlined.Edit, if (en) "Edit" else "تعديل", tint = HAccent, modifier = Modifier.size(18.dp)) }
                IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) { Icon(Icons.Outlined.DeleteOutline, if (en) "Delete" else "حذف", tint = Color(0xFFB44C4C), modifier = Modifier.size(18.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HouseholdForm(en: Boolean, existing: HouseholdExpenseEntity?, onCancel: () -> Unit, onSave: (HouseholdExpenseEntity, List<Uri>) -> Unit) {
    val context = LocalContext.current
    val cats = if (en) listOf("Breakfast", "Food", "Supplies", "Repairs") else listOf("فطار", "طعام", "مستلزمات", "إصلاحات")
    var category by remember(existing?.id) { mutableStateOf(existing?.category ?: cats.first()) }
    var categoryMenu by remember { mutableStateOf(false) }
    var amount by remember(existing?.id) { mutableStateOf(existing?.amountPiastres?.let { BigDecimal.valueOf(it, 2).toPlainString() } ?: "") }
    var notes by remember(existing?.id) { mutableStateOf(existing?.notes.orEmpty()) }
    var type by remember(existing?.id) { mutableStateOf(existing?.expenseType ?: "PURCHASES_AND_SUPPLIES") }
    var dateOpen by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val dateState = rememberDatePickerState(initialSelectedDateMillis = existing?.date ?: System.currentTimeMillis())
    val dateLabel = dateState.selectedDateMillis?.let(::formatDate) ?: formatDate(System.currentTimeMillis())
    val picked = remember { mutableStateListOf<Uri>() }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris.forEach { uri -> runCatching { context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) } }
        picked.addAll(uris)
    }
    SheetHeader(if (existing == null) if (en) "Add household expense" else "إضافة مصروف منزل جديد" else if (en) "Edit household expense" else "تعديل المصروف المنزلي", if (en) "Expense details" else "بيانات المصروف", onCancel)
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        Box {
            OutlinedTextField(category, {}, Modifier.fillMaxWidth(), readOnly = true, label = { Text(if (en) "Expense category *" else "تصنيف المصروف *") }, shape = RoundedCornerShape(13.dp), trailingIcon = { Icon(Icons.Outlined.ArrowDropDown, null) })
            Box(Modifier.matchParentSize().clickable { categoryMenu = true })
            DropdownMenu(categoryMenu, { categoryMenu = false }) { cats.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { category = it; categoryMenu = false }) } }
        }
        OutlinedTextField(amount, { amount = it.filter { ch -> ch.isDigit() || ch == '.' }; error = null }, Modifier.fillMaxWidth(), label = { Text(if (en) "Amount (EGP) *" else "المبلغ (جنيه مصري) *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, isError = error != null, shape = RoundedCornerShape(13.dp))
        DateInput(dateLabel, if (en) "Expense date *" else "تاريخ المصروف *") { dateOpen = true }
        Text(if (en) "Spending type" else "نوع الصرف", color = HInk, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            FilterChip(type == "PURCHASES_AND_SUPPLIES", { type = "PURCHASES_AND_SUPPLIES" }, label = { Text(if (en) "Purchases & supplies" else "مشتريات ومستلزمات", fontSize = 10.sp) })
            FilterChip(type == "LABOR_AND_SERVICES", { type = "LABOR_AND_SERVICES" }, label = { Text(if (en) "Labor & services" else "مصنعية وأجرة يد", fontSize = 10.sp) })
        }
        OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth(), label = { Text(if (en) "Notes and details" else "ملاحظات وتفاصيل") }, placeholder = { Text(if (en) "Additional details..." else "تفاصيل إضافية...") }, minLines = 2, maxLines = 4, shape = RoundedCornerShape(13.dp))
        AttachmentInput(if (en) "Receipts and invoices" else "صور الفواتير وإيصالات الشراء", picked, { picker.launch(arrayOf("image/*", "application/pdf")) }, en, attachmentCount(existing?.attachmentsJson.orEmpty()) + picked.size)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp) }
        FormActions(if (existing == null) if (en) "Save expense" else "حفظ المصروف" else if (en) "Update" else "تحديث", if (en) "Cancel" else "إلغاء", {
            val parsed = runCatching { BigDecimal(amount.trim()).movePointRight(2).longValueExact() }.getOrNull()
            if (parsed == null || parsed <= 0L) error = if (en) "Enter a valid amount greater than zero." else "أدخل مبلغًا صحيحًا أكبر من صفر."
            else onSave(HouseholdExpenseEntity(id = existing?.id ?: 0, category = category, amountPiastres = parsed, date = dateState.selectedDateMillis ?: existing?.date ?: System.currentTimeMillis(), expenseType = type, notes = notes.trim(), attachmentsJson = existing?.attachmentsJson ?: "[]", createdAt = existing?.createdAt ?: System.currentTimeMillis()), picked.toList())
        }, onCancel)
    }
    if (dateOpen) DatePickerDialog(onDismissRequest = { dateOpen = false }, confirmButton = { TextButton(onClick = { dateOpen = false }) { Text(if (en) "Done" else "تم") } }, dismissButton = { TextButton(onClick = { dateOpen = false }) { Text(if (en) "Cancel" else "إلغاء") } }) { DatePicker(state = dateState) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedicalForm(en: Boolean, existing: MedicalExpenseEntity?, onPick: () -> Unit, onCancel: () -> Unit, onSave: (MedicalExpenseEntity, List<Uri>) -> Unit) {
    val context = LocalContext.current
    var patient by remember(existing?.id) { mutableStateOf(existing?.patientName.orEmpty()) }
    var facility by remember(existing?.id) { mutableStateOf(existing?.facilityType ?: "HOSPITAL") }
    var facilityMenu by remember { mutableStateOf(false) }
    var facilityName by remember(existing?.id) { mutableStateOf(existing?.facilityName.orEmpty()) }
    var condition by remember(existing?.id) { mutableStateOf(existing?.conditionDescription.orEmpty()) }
    var diagnosis by remember(existing?.id) { mutableStateOf(existing?.doctorDiagnosis.orEmpty()) }
    var checkup by remember(existing?.id) { mutableStateOf(existing?.checkupCostPiastres?.let { BigDecimal.valueOf(it, 2).toPlainString() } ?: "") }
    var medicine by remember(existing?.id) { mutableStateOf(existing?.medicationCostPiastres?.let { BigDecimal.valueOf(it, 2).toPlainString() } ?: "") }
    var notes by remember(existing?.id) { mutableStateOf(existing?.notes.orEmpty()) }
    var dateOpen by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val dateState = rememberDatePickerState(initialSelectedDateMillis = existing?.date ?: System.currentTimeMillis())
    val dateLabel = dateState.selectedDateMillis?.let(::formatDate) ?: formatDate(System.currentTimeMillis())
    val picked = remember { mutableStateListOf<Uri>() }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris.forEach { uri -> runCatching { context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) } }
        picked.addAll(uris)
    }
    SheetHeader(if (existing == null) if (en) "Add medical checkup" else "إضافة كشف طبي" else if (en) "Edit medical checkup" else "تعديل الكشف الطبي", if (en) "Patient and visit details" else "بيانات المريض وتفاصيل الزيارة", onCancel)
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(patient, { patient = it }, Modifier.fillMaxWidth(), label = { Text(if (en) "Patient name *" else "اسم المريض *") }, singleLine = true, shape = RoundedCornerShape(13.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(.8f)) {
                OutlinedTextField(if (facility == "CLINIC") if (en) "Clinic" else "عيادة" else if (en) "Hospital" else "مستشفى", {}, Modifier.fillMaxWidth(), readOnly = true, label = { Text(if (en) "Facility type" else "نوع المنشأة") }, shape = RoundedCornerShape(13.dp))
                Box(Modifier.matchParentSize().clickable { facilityMenu = true })
                DropdownMenu(facilityMenu, { facilityMenu = false }) {
                    DropdownMenuItem(text = { Text(if (en) "Hospital" else "مستشفى") }, onClick = { facility = "HOSPITAL"; facilityMenu = false })
                    DropdownMenuItem(text = { Text(if (en) "Clinic" else "عيادة") }, onClick = { facility = "CLINIC"; facilityMenu = false })
                }
            }
            OutlinedTextField(facilityName, { facilityName = it }, Modifier.weight(1.2f), label = { Text(if (en) "Hospital / clinic name" else "اسم المستشفى / العيادة") }, singleLine = true, shape = RoundedCornerShape(13.dp))
        }
        OutlinedTextField(condition, { condition = it }, Modifier.fillMaxWidth(), label = { Text(if (en) "Medical condition *" else "توصيف الحالة المرضية *") }, minLines = 2, maxLines = 3, shape = RoundedCornerShape(13.dp))
        OutlinedTextField(diagnosis, { diagnosis = it }, Modifier.fillMaxWidth(), label = { Text(if (en) "Doctor's diagnosis" else "تشخيص الدكتور") }, minLines = 2, maxLines = 3, shape = RoundedCornerShape(13.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(checkup, { checkup = it.filter { ch -> ch.isDigit() || ch == '.' }; error = null }, Modifier.weight(1f), label = { Text(if (en) "Checkup cost" else "تكلفة الكشف") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, shape = RoundedCornerShape(13.dp))
            OutlinedTextField(medicine, { medicine = it.filter { ch -> ch.isDigit() || ch == '.' }; error = null }, Modifier.weight(1f), label = { Text(if (en) "Medication cost" else "تكلفة الأدوية") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, shape = RoundedCornerShape(13.dp))
        }
        DateInput(dateLabel, if (en) "Visit date" else "تاريخ الكشف") { dateOpen = true }
        OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth(), label = { Text(if (en) "Additional notes" else "ملاحظات إضافية") }, minLines = 2, maxLines = 3, shape = RoundedCornerShape(13.dp))
        AttachmentInput(if (en) "Prescriptions and medical documents" else "الروشتات وأوراق الكشف الطبي", picked, { picker.launch(arrayOf("image/*", "application/pdf")) }, en, attachmentCount(existing?.attachmentsJson.orEmpty()) + picked.size)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp) }
        FormActions(if (existing == null) if (en) "Save" else "حفظ" else if (en) "Update" else "تحديث", if (en) "Cancel" else "إلغاء", {
            val check = parseMoney(checkup)
            val meds = parseMoney(medicine)
            when {
                patient.isBlank() -> error = if (en) "Enter the patient name." else "اكتب اسم المريض."
                condition.isBlank() -> error = if (en) "Describe the medical condition." else "اكتب توصيف الحالة المرضية."
                check == null || meds == null -> error = if (en) "Enter valid non-negative costs." else "أدخل تكاليف صحيحة غير سالبة."
                else -> onSave(MedicalExpenseEntity(id = existing?.id ?: 0, patientName = patient.trim(), facilityType = facility, facilityName = facilityName.trim(), conditionDescription = condition.trim(), doctorDiagnosis = diagnosis.trim(), checkupCostPiastres = check, medicationCostPiastres = meds, date = dateState.selectedDateMillis ?: existing?.date ?: System.currentTimeMillis(), notes = notes.trim(), attachmentsJson = existing?.attachmentsJson ?: "[]", createdAt = existing?.createdAt ?: System.currentTimeMillis()), picked.toList())
            }
        }, onCancel)
    }
    if (dateOpen) DatePickerDialog(onDismissRequest = { dateOpen = false }, confirmButton = { TextButton(onClick = { dateOpen = false }) { Text(if (en) "Done" else "تم") } }, dismissButton = { TextButton(onClick = { dateOpen = false }) { Text(if (en) "Cancel" else "إلغاء") } }) { DatePicker(state = dateState) }
}

@Composable
private fun SheetHeader(title: String, subtitle: String, close: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title, color = HInk, fontSize = 17.sp, fontWeight = FontWeight.Bold); Text(subtitle, color = HMuted, fontSize = 10.sp) }
        IconButton(onClick = close) { Icon(Icons.Outlined.Close, "Close", tint = HMuted) }
    }
    Divider(color = HBorder)
}

@Composable
private fun DateInput(value: String, label: String, click: () -> Unit) {
    Box {
        OutlinedTextField(value, {}, Modifier.fillMaxWidth(), readOnly = true, label = { Text(label) }, trailingIcon = { Icon(Icons.Outlined.CalendarMonth, null, tint = HAccent) }, shape = RoundedCornerShape(13.dp))
        Box(Modifier.matchParentSize().clickable(onClick = click))
    }
}

@Composable
private fun AttachmentInput(title: String, files: List<Uri>, pick: () -> Unit, en: Boolean, totalCount: Int) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = HSoft, border = BorderStroke(1.dp, Color(0xFFD4E3DD))) {
        Row(Modifier.padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.AttachFile, null, tint = HAccent)
            Spacer(Modifier.width(7.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = HInk, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(if (totalCount == 0) if (en) "No files attached" else "لم يتم إرفاق ملفات" else if (en) "$totalCount file(s) attached" else "عدد الملفات: $totalCount", color = HMuted, fontSize = 10.sp)
            }
            OutlinedButton(onClick = pick, shape = RoundedCornerShape(10.dp)) {
                Icon(Icons.Outlined.UploadFile, null, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(3.dp)); Text(if (en) "Attach" else "إرفاق", fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun FormActions(primary: String, cancel: String, save: () -> Unit, close: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(bottom = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = save, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = HAccent), shape = RoundedCornerShape(12.dp)) { Text(primary) }
        OutlinedButton(onClick = close, Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text(cancel) }
    }
}

private fun parseMoney(value: String): Long? = runCatching {
    if (value.isBlank()) 0L else BigDecimal(value.trim()).movePointRight(2).longValueExact().also { require(it >= 0L) }
}.getOrNull()

private fun formatEgp(piastres: Long): String = "${DecimalFormat("#,##0.00").format(piastres / 100.0)} ج.م"

private fun formatDate(epoch: Long): String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(epoch))

private fun attachmentCount(json: String): Int = runCatching { JSONArray(json).length() }.getOrDefault(0)

private fun isCurrentMonth(epoch: Long): Boolean {
    val record = Calendar.getInstance().apply { timeInMillis = epoch }
    val now = Calendar.getInstance()
    return record.get(Calendar.YEAR) == now.get(Calendar.YEAR) && record.get(Calendar.MONTH) == now.get(Calendar.MONTH)
}
