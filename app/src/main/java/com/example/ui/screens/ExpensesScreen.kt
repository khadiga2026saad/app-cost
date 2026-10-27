package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.DateUtils
import com.example.core.MoneyUtils
import com.example.data.local.entities.ExpenseEntity
import com.example.domain.model.Department
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.MonthPickerBar
import com.example.ui.theme.BlackPrimary
import com.example.ui.theme.FinancialRed
import com.example.ui.theme.FinancialRedContainer
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.viewmodel.FinanceViewModel
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    viewModel: FinanceViewModel,
    showAddDialogInitially: Boolean = false,
    modifier: Modifier = Modifier
) {
    val expenses by viewModel.filteredExpenses.collectAsStateWithLifecycle()
    val monthFilter by viewModel.currentMonthFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedDept by viewModel.selectedDeptFilter.collectAsStateWithLifecycle()
    val sortByAmount by viewModel.sortByAmount.collectAsStateWithLifecycle()
    val vehicles by viewModel.allVehicles.collectAsStateWithLifecycle()

    var showExpenseDialog by remember { mutableStateOf(showAddDialogInitially) }
    var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }
    var expenseToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }

    val departmentsList = listOf(
        Pair("ALL", "الكل"),
        Pair("HOME", Department.HOME.titleAr),
        Pair("MEDICAL", Department.MEDICAL.titleAr),
        Pair("PERSONAL", Department.PERSONAL.titleAr),
        Pair("WORK", Department.WORK.titleAr),
        Pair("EDUCATION", Department.EDUCATION.titleAr),
        Pair("VEHICLES", Department.VEHICLES.titleAr)
    )

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Month Selector
                MonthPickerBar(
                    year = monthFilter.year,
                    month = monthFilter.month,
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() }
                )
            }

            // Search Bar & Sort Toggle
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("بحث في المصروفات والوصف...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = GoldAccent)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "مسح")
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_search_expenses"),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { viewModel.toggleSortOrder() },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (sortByAmount) BlackPrimary else MaterialTheme.colorScheme.surface)
                            .testTag("btn_sort_expenses")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "ترتيب",
                            tint = if (sortByAmount) GoldPrimary else BlackPrimary
                        )
                    }
                }
            }

            // Department Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(departmentsList) { (id, label) ->
                        val isSelected = selectedDept == id
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setDepartmentFilter(id) },
                            label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BlackPrimary,
                                selectedLabelColor = GoldPrimary,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = BlackPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // Summary of filtered expenses count & total
            item {
                val totalFilteredPiastres = expenses.sumOf { it.amountPiastres }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "عدد المصروفات: ${expenses.size}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = "المجموع: ${MoneyUtils.formatPiastres(totalFilteredPiastres)}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = FinancialRed
                        )
                    )
                }
            }

            // Expenses List
            if (expenses.isEmpty()) {
                item {
                    EmptyStateCard(
                        message = "لا توجد مصروفات تطابق خيارات التصفية لهذا الشهر.",
                        icon = Icons.Default.ReceiptLong,
                        modifier = Modifier.testTag("empty_expenses_list")
                    )
                }
            } else {
                items(expenses, key = { it.id }) { expense ->
                    ExpenseDetailCard(
                        expense = expense,
                        onEdit = {
                            editingExpense = expense
                            showExpenseDialog = true
                        },
                        onDelete = {
                            expenseToDelete = expense
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(88.dp))
            }
        }

        // Floating Action Button to Add Expense
        FloatingActionButton(
            onClick = {
                editingExpense = null
                showExpenseDialog = true
            },
            containerColor = BlackPrimary,
            contentColor = GoldPrimary,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_expense")
        ) {
            Icon(Icons.Default.Add, contentDescription = "إضافة مصروف", modifier = Modifier.size(28.dp))
        }
    }

    // Add / Edit Expense Dialog
    if (showExpenseDialog) {
        ExpenseFormDialog(
            initialExpense = editingExpense,
            vehicles = vehicles,
            onDismiss = {
                showExpenseDialog = false
                editingExpense = null
            },
            onSave = { expenseData ->
                viewModel.saveExpense(
                    id = editingExpense?.id ?: 0L,
                    amount = expenseData.amount,
                    date = expenseData.date,
                    department = expenseData.department,
                    category = expenseData.category,
                    title = expenseData.title,
                    notes = expenseData.notes,
                    vehicleId = expenseData.vehicleId,
                    studentName = expenseData.studentName,
                    institution = expenseData.institution,
                    academicTerm = expenseData.academicTerm,
                    projectName = expenseData.projectName,
                    isTaxDeductible = expenseData.isTaxDeductible
                )
                showExpenseDialog = false
                editingExpense = null
            }
        )
    }

    // Delete Confirmation Dialog
    expenseToDelete?.let { exp ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text("حذف المصروف", fontWeight = FontWeight.Bold) },
            text = {
                Text("هل أنت متأكد من حذف مصروف \"${exp.title}\" بقيمة ${MoneyUtils.format(MoneyUtils.parseBigDecimalAllowZero(exp.amount))}؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteExpense(exp.id)
                        expenseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialRed)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { expenseToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun ExpenseDetailCard(
    expense: ExpenseEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("expense_card_${expense.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val dept = Department.fromId(expense.department)
                    val icon = when (dept) {
                        Department.EDUCATION -> Icons.Default.School
                        Department.WORK -> Icons.Default.Work
                        Department.VEHICLE, Department.VEHICLES -> Icons.Default.DirectionsCar
                        Department.MEDICAL -> Icons.Default.ReceiptLong
                        else -> Icons.Default.ReceiptLong
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FinancialRedContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = FinancialRed, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = expense.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = BlackPrimary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${dept.titleAr} • ${expense.category}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                Text(
                    text = "- " + MoneyUtils.formatPiastres(expense.amountPiastres),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = FinancialRed
                    )
                )
            }

            // Department specific badges / extra information
            val hasExtraInfo = expense.studentName != null ||
                expense.institution != null ||
                expense.projectName != null ||
                expense.isTaxDeductible ||
                (!expense.notes.isNullOrBlank())

            if (hasExtraInfo) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    if (expense.department == Department.EDUCATION.id) {
                        if (!expense.studentName.isNullOrBlank()) {
                            Text("الطالب: ${expense.studentName}", style = MaterialTheme.typography.bodySmall)
                        }
                        if (!expense.institution.isNullOrBlank()) {
                            Text("المؤسسة: ${expense.institution}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if (expense.department == Department.WORK.id) {
                        if (!expense.projectName.isNullOrBlank()) {
                            Text("المشروع / العميل: ${expense.projectName}", style = MaterialTheme.typography.bodySmall)
                        }
                        if (expense.isTaxDeductible) {
                            Text("✓ خاضع للخصم الضريبي", style = MaterialTheme.typography.bodySmall.copy(color = GoldDark, fontWeight = FontWeight.Bold))
                        }
                    }
                    if (!expense.notes.isNullOrBlank()) {
                        Text("ملاحظات: ${expense.notes}", style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateUtils.formatDateArabic(expense.date),
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                )

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = GoldAccent, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

data class ExpenseFormData(
    val amount: BigDecimal,
    val date: Long,
    val department: String,
    val category: String,
    val title: String,
    val notes: String,
    val vehicleId: Long?,
    val studentName: String?,
    val institution: String?,
    val academicTerm: String?,
    val projectName: String?,
    val isTaxDeductible: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseFormDialog(
    initialExpense: ExpenseEntity?,
    vehicles: List<com.example.data.local.entities.VehicleEntity>,
    onDismiss: () -> Unit,
    onSave: (ExpenseFormData) -> Unit
) {
    var amountText by remember { mutableStateOf(initialExpense?.amount ?: "") }
    var title by remember { mutableStateOf(initialExpense?.title ?: "") }
    var selectedDept by remember { mutableStateOf(Department.fromId(initialExpense?.department)) }
    var selectedCategory by remember { mutableStateOf(initialExpense?.category ?: selectedDept.defaultCategories.first()) }
    var notes by remember { mutableStateOf(initialExpense?.notes ?: "") }

    // Department-specific
    var studentName by remember { mutableStateOf(initialExpense?.studentName ?: "") }
    var institution by remember { mutableStateOf(initialExpense?.institution ?: "") }
    var academicTerm by remember { mutableStateOf(initialExpense?.academicTerm ?: "") }
    var projectName by remember { mutableStateOf(initialExpense?.projectName ?: "") }
    var isTaxDeductible by remember { mutableStateOf(initialExpense?.isTaxDeductible ?: false) }
    var selectedVehicleId by remember { mutableStateOf(initialExpense?.vehicleId ?: vehicles.firstOrNull()?.id) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var deptDropdownExpanded by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initialExpense == null) "إضافة مصروف جديد" else "تعديل المصروف",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Amount Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("المبلغ (ج.م) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_expense_amount"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text("العنوان / الوصف *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_expense_title"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Department Selector
                ExposedDropdownMenuBox(
                    expanded = deptDropdownExpanded,
                    onExpandedChange = { deptDropdownExpanded = !deptDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedDept.titleAr,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("القسم") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = deptDropdownExpanded,
                        onDismissRequest = { deptDropdownExpanded = false }
                    ) {
                        Department.entries.forEach { dept ->
                            DropdownMenuItem(
                                text = { Text(dept.titleAr) },
                                onClick = {
                                    selectedDept = dept
                                    selectedCategory = dept.defaultCategories.first()
                                    deptDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Category Selector
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("التصنيف") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        selectedDept.defaultCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Specialized fields based on department
                when (selectedDept) {
                    Department.EDUCATION -> {
                        OutlinedTextField(
                            value = studentName,
                            onValueChange = { studentName = it },
                            label = { Text("اسم الطالب") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = institution,
                            onValueChange = { institution = it },
                            label = { Text("المدرسة / الجامعة") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = academicTerm,
                            onValueChange = { academicTerm = it },
                            label = { Text("الفصل الدراسي / المرحلة") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    Department.WORK -> {
                        OutlinedTextField(
                            value = projectName,
                            onValueChange = { projectName = it },
                            label = { Text("اسم المشروع / العميل") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { isTaxDeductible = !isTaxDeductible }
                        ) {
                            Checkbox(
                                checked = isTaxDeductible,
                                onCheckedChange = { isTaxDeductible = it }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("خاضع للخصم الضريبي", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Department.VEHICLE -> {
                        if (vehicles.isNotEmpty()) {
                            Text("المركبة المرتبطة:", style = MaterialTheme.typography.labelMedium)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(vehicles) { v ->
                                    val isSelected = selectedVehicleId == v.id
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedVehicleId = v.id },
                                        label = { Text("${v.name} (${v.plateNumber})") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = BlackPrimary,
                                            selectedLabelColor = GoldPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }
                    else -> {}
                }

                // Notes Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                errorMessage?.let { err ->
                    Text(
                        text = err,
                        color = FinancialRed,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedAmount = MoneyUtils.parseBigDecimal(amountText)
                    if (parsedAmount == null || parsedAmount <= BigDecimal.ZERO) {
                        errorMessage = "يرجى إدخال مبلغ صحيح أكبر من الصفر"
                        return@Button
                    }
                    if (title.isBlank()) {
                        errorMessage = "يرجى إدخال عنوان المصروف"
                        return@Button
                    }
                    onSave(
                        ExpenseFormData(
                            amount = parsedAmount,
                            date = initialExpense?.date ?: DateUtils.currentEpochMillis(),
                            department = selectedDept.id,
                            category = selectedCategory,
                            title = title.trim(),
                            notes = notes.trim(),
                            vehicleId = if (selectedDept == Department.VEHICLE || selectedDept == Department.VEHICLES) selectedVehicleId else null,
                            studentName = studentName.takeIf { it.isNotBlank() },
                            institution = institution.takeIf { it.isNotBlank() },
                            academicTerm = academicTerm.takeIf { it.isNotBlank() },
                            projectName = projectName.takeIf { it.isNotBlank() },
                            isTaxDeductible = isTaxDeductible
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = BlackPrimary, contentColor = Color.White),
                modifier = Modifier.testTag("btn_save_expense")
            ) {
                Text("حفظ المصروف", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
