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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.DateUtils
import com.example.core.MoneyUtils
import com.example.data.local.entities.FinancialCertificateEntity
import com.example.data.local.entities.RevenueEntity
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.MonthPickerBar
import com.example.ui.theme.BlackPrimary
import com.example.ui.theme.FinancialGreen
import com.example.ui.theme.FinancialGreenContainer
import com.example.ui.theme.FinancialRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.viewmodel.FinanceViewModel
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenuesScreen(
    viewModel: FinanceViewModel,
    showAddRevenueInitially: Boolean = false,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: الإيرادات, 1: الشهادات والودائع
    val revenues by viewModel.currentMonthRevenues.collectAsStateWithLifecycle()
    val certificates by viewModel.allCertificates.collectAsStateWithLifecycle()
    val monthFilter by viewModel.currentMonthFilter.collectAsStateWithLifecycle()
    val vehicles by viewModel.allVehicles.collectAsStateWithLifecycle()

    var showAddRevenueDialog by remember { mutableStateOf(showAddRevenueInitially) }
    var editingRevenue by remember { mutableStateOf<RevenueEntity?>(null) }
    var revenueToDelete by remember { mutableStateOf<RevenueEntity?>(null) }

    var showAddCertDialog by remember { mutableStateOf(false) }
    var editingCert by remember { mutableStateOf<FinancialCertificateEntity?>(null) }
    var certToDelete by remember { mutableStateOf<FinancialCertificateEntity?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = BlackPrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "سجل الإيرادات (${revenues.size})",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) BlackPrimary else Color.Gray
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "الشهادات والودائع (${certificates.size})",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) BlackPrimary else Color.Gray
                            )
                        }
                    )
                }
            }

            if (selectedTab == 0) {
                // Month Picker
                item {
                    MonthPickerBar(
                        year = monthFilter.year,
                        month = monthFilter.month,
                        onPreviousMonth = { viewModel.previousMonth() },
                        onNextMonth = { viewModel.nextMonth() }
                    )
                }

                // Total Revenues Banner
                item {
                    val totalRevenuesPiastres = revenues.sumOf { it.amountPiastres }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_total_revenues_banner"),
                        colors = CardDefaults.cardColors(containerColor = FinancialGreenContainer),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("إجمالي إيرادات الشهر", style = MaterialTheme.typography.labelMedium.copy(color = FinancialGreen))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    MoneyUtils.formatPiastres(totalRevenuesPiastres),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, color = FinancialGreen)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(FinancialGreen.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = FinancialGreen)
                            }
                        }
                    }
                }

                if (revenues.isEmpty()) {
                    item {
                        EmptyStateCard(
                            message = "لا توجد إيرادات مسجلة لهذا الشهر. اضغط على الزر بالأسفل لتسجيل راتب، عمل حر، أو إيراد مركبة.",
                            icon = Icons.Default.AccountBalance
                        )
                    }
                } else {
                    items(revenues, key = { it.id }) { revenue ->
                        RevenueDetailCard(
                            revenue = revenue,
                            onEdit = {
                                editingRevenue = revenue
                                showAddRevenueDialog = true
                            },
                            onDelete = {
                                revenueToDelete = revenue
                            }
                        )
                    }
                }
            } else {
                // Certificates Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الشهادات والودائع البنكية",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = BlackPrimary)
                        )
                        OutlinedButton(
                            onClick = {
                                editingCert = null
                                showAddCertDialog = true
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة شهادة")
                        }
                    }
                }

                if (certificates.isEmpty()) {
                    item {
                        EmptyStateCard(
                            message = "لا توجد شهادات بنكية مسجلة. أضف شهاداتك لمتابعة العوائد وتواريخ الاستحقاق.",
                            icon = Icons.Default.CardMembership
                        )
                    }
                } else {
                    items(certificates, key = { it.id }) { cert ->
                        CertificateDetailCard(
                            cert = cert,
                            onRecordPayout = { payoutAmount ->
                                // Automatically records an income entry for this payout
                                viewModel.saveRevenue(
                                    amount = payoutAmount,
                                    date = DateUtils.currentEpochMillis(),
                                    category = "عوائد شهادات واستثمار",
                                    title = "صرف عائد: ${cert.name}",
                                    notes = "عائد دوري (${cert.payoutFrequency}) من ${cert.bankName}",
                                    certificateId = cert.id
                                )
                            },
                            onEdit = {
                                editingCert = cert
                                showAddCertDialog = true
                            },
                            onDelete = {
                                certToDelete = cert
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(88.dp))
            }
        }

        // FAB to add revenue if on revenues tab
        if (selectedTab == 0) {
            FloatingActionButton(
                onClick = {
                    editingRevenue = null
                    showAddRevenueDialog = true
                },
                containerColor = BlackPrimary,
                contentColor = GoldPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("fab_add_revenue")
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة إيراد", modifier = Modifier.size(28.dp))
            }
        }
    }

    // Add / Edit Revenue Dialog
    if (showAddRevenueDialog) {
        RevenueFormDialog(
            initialRevenue = editingRevenue,
            vehicles = vehicles,
            onDismiss = {
                showAddRevenueDialog = false
                editingRevenue = null
            },
            onSave = { amount, category, title, notes, vehicleId ->
                viewModel.saveRevenue(
                    id = editingRevenue?.id ?: 0L,
                    amount = amount,
                    date = editingRevenue?.date ?: DateUtils.currentEpochMillis(),
                    category = category,
                    title = title,
                    notes = notes,
                    vehicleId = vehicleId
                )
                showAddRevenueDialog = false
                editingRevenue = null
            }
        )
    }

    // Add / Edit Certificate Dialog
    if (showAddCertDialog) {
        CertificateFormDialog(
            initialCert = editingCert,
            onDismiss = {
                showAddCertDialog = false
                editingCert = null
            },
            onSave = { name, bank, principal, rate, freq, issueDate, matDate, notes ->
                viewModel.saveCertificate(
                    id = editingCert?.id ?: 0L,
                    name = name,
                    bankName = bank,
                    principalAmount = principal,
                    annualInterestRate = rate,
                    payoutFrequency = freq,
                    issueDate = issueDate,
                    maturityDate = matDate,
                    notes = notes
                )
                showAddCertDialog = false
                editingCert = null
            }
        )
    }

    // Delete revenue confirmation
    revenueToDelete?.let { rev ->
        AlertDialog(
            onDismissRequest = { revenueToDelete = null },
            title = { Text("حذف الإيراد", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من حذف إيراد \"${rev.title}\" بقيمة ${MoneyUtils.formatPiastres(rev.amountPiastres)}؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRevenue(rev.id)
                        revenueToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialRed)
                ) { Text("حذف") }
            },
            dismissButton = {
                OutlinedButton(onClick = { revenueToDelete = null }) { Text("إلغاء") }
            }
        )
    }

    // Delete certificate confirmation
    certToDelete?.let { c ->
        AlertDialog(
            onDismissRequest = { certToDelete = null },
            title = { Text("حذف الشهادة", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من حذف الشهادة \"${c.name}\"؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCertificate(c.id)
                        certToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialRed)
                ) { Text("حذف") }
            },
            dismissButton = {
                OutlinedButton(onClick = { certToDelete = null }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
private fun RevenueDetailCard(
    revenue: RevenueEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("revenue_card_${revenue.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(FinancialGreenContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = FinancialGreen, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = revenue.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = BlackPrimary)
                        )
                        Text(
                            text = revenue.category,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                Text(
                    text = "+ " + MoneyUtils.formatPiastres(revenue.amountPiastres),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = FinancialGreen)
                )
            }

            if (!revenue.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ملاحظات: ${revenue.notes}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateUtils.formatDateArabic(revenue.date),
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                )
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = GoldAccent, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CertificateDetailCard(
    cert: FinancialCertificateEntity,
    onRecordPayout: (BigDecimal) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRecordedSuccess by remember { mutableStateOf(false) }

    // Calculate periodic payout
    val principal = MoneyUtils.parseBigDecimalAllowZero(cert.principalAmount)
    val rateDecimal = BigDecimal(cert.annualInterestRate).divide(BigDecimal(100), 6, RoundingMode.HALF_UP)
    val annualReturn = principal.multiply(rateDecimal).setScale(2, RoundingMode.HALF_UP)
    val periodicReturn = when (cert.payoutFrequency) {
        "شهري" -> annualReturn.divide(BigDecimal(12), 2, RoundingMode.HALF_UP)
        "ربع سنوي" -> annualReturn.divide(BigDecimal(4), 2, RoundingMode.HALF_UP)
        "نصف سنوي" -> annualReturn.divide(BigDecimal(2), 2, RoundingMode.HALF_UP)
        else -> annualReturn
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("certificate_card_${cert.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(cert.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = BlackPrimary))
                    Text(cert.bankName, style = MaterialTheme.typography.bodySmall.copy(color = GoldDark, fontWeight = FontWeight.SemiBold))
                }
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = GoldAccent, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(GoldLight, RoundedCornerShape(10.dp))
                        .padding(8.dp)
                ) {
                    Text("أصل المبلغ", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Color.Gray))
                    Text(MoneyUtils.format(principal), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = BlackPrimary))
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                        .padding(8.dp)
                ) {
                    Text("نسبة العائد", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Color.Gray))
                    Text("${cert.annualInterestRate}% سنويًا", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = FinancialGreen))
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(FinancialGreenContainer, RoundedCornerShape(10.dp))
                        .padding(8.dp)
                ) {
                    Text("العائد الدوري", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = FinancialGreen))
                    Text(MoneyUtils.format(periodicReturn), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = FinancialGreen))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "دورية الصرف: ${cert.payoutFrequency}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                )

                Button(
                    onClick = {
                        onRecordPayout(periodicReturn)
                        showRecordedSuccess = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BlackPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    if (showRecordedSuccess) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تم تسجيل العائد", fontSize = 12.sp)
                    } else {
                        Text("تسجيل صرف العائد", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun RevenueFormDialog(
    initialRevenue: RevenueEntity?,
    vehicles: List<com.example.data.local.entities.VehicleEntity>,
    onDismiss: () -> Unit,
    onSave: (amount: BigDecimal, category: String, title: String, notes: String, vehicleId: Long?) -> Unit
) {
    var amountText by remember { mutableStateOf(initialRevenue?.amount ?: "") }
    var title by remember { mutableStateOf(initialRevenue?.title ?: "") }
    var category by remember { mutableStateOf(initialRevenue?.category ?: "راتب شهري") }
    var notes by remember { mutableStateOf(initialRevenue?.notes ?: "") }
    var selectedVehicleId by remember { mutableStateOf(initialRevenue?.vehicleId) }
    var error by remember { mutableStateOf<String?>(null) }

    val categories = listOf("راتب شهري", "عمل حر واستشارات", "إيرادات مركبات", "عوائد شهادات واستثمار", "تجارة ومبيعات", "أخرى")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialRevenue == null) "إضافة إيراد جديد" else "تعديل الإيراد", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it; error = null },
                    label = { Text("المبلغ (ج.م) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("input_revenue_amount"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; error = null },
                    label = { Text("الوصف / المصدر *") },
                    modifier = Modifier.fillMaxWidth().testTag("input_revenue_title"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Text("التصنيف:", style = MaterialTheme.typography.labelMedium)
                categories.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        row.forEach { cat ->
                            val isSel = category == cat
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) BlackPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { category = cat }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    cat,
                                    fontSize = 11.sp,
                                    color = if (isSel) GoldPrimary else BlackPrimary,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                if (category == "إيرادات مركبات" && vehicles.isNotEmpty()) {
                    Text("اختر المركبة:", style = MaterialTheme.typography.labelMedium)
                    vehicles.forEach { v ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { selectedVehicleId = v.id }
                        ) {
                            Text(
                                "${v.name} (${v.plateNumber})",
                                fontWeight = if (selectedVehicleId == v.id) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedVehicleId == v.id) GoldDark else Color.DarkGray
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                error?.let { Text(it, color = FinancialRed, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = MoneyUtils.parseBigDecimal(amountText)
                    if (parsed == null || parsed <= BigDecimal.ZERO) {
                        error = "يرجى إدخال مبلغ صحيح أكبر من الصفر"
                        return@Button
                    }
                    if (title.isBlank()) {
                        error = "يرجى إدخال وصف الإيراد"
                        return@Button
                    }
                    onSave(parsed, category, title.trim(), notes.trim(), selectedVehicleId)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BlackPrimary),
                modifier = Modifier.testTag("btn_save_revenue")
            ) { Text("حفظ الإيراد") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
private fun CertificateFormDialog(
    initialCert: FinancialCertificateEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, bank: String, principal: BigDecimal, rate: Double, freq: String, issueDate: Long, matDate: Long, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(initialCert?.name ?: "") }
    var bank by remember { mutableStateOf(initialCert?.bankName ?: "") }
    var principalText by remember { mutableStateOf(initialCert?.principalAmount ?: "") }
    var rateText by remember { mutableStateOf(initialCert?.annualInterestRate?.toString() ?: "23.5") }
    var frequency by remember { mutableStateOf(initialCert?.payoutFrequency ?: "شهري") }
    var notes by remember { mutableStateOf(initialCert?.notes ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    val frequencies = listOf("شهري", "ربع سنوي", "سنوي", "نهاية المدة")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialCert == null) "إضافة شهادة أو وديعة" else "تعديل الشهادة", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("اسم الشهادة (مثلاً: شهادة القمة) *") },
                    modifier = Modifier.fillMaxWidth().testTag("input_cert_name"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = bank,
                    onValueChange = { bank = it; error = null },
                    label = { Text("البنك / المؤسسة (مثلاً: البنك الأهلي) *") },
                    modifier = Modifier.fillMaxWidth().testTag("input_cert_bank"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = principalText,
                        onValueChange = { principalText = it; error = null },
                        label = { Text("أصل المبلغ *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("input_cert_principal"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = rateText,
                        onValueChange = { rateText = it; error = null },
                        label = { Text("العائد السنوي % *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Text("دورية صرف العائد:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    frequencies.forEach { f ->
                        val isSel = frequency == f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) BlackPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { frequency = f }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                f,
                                fontSize = 11.sp,
                                color = if (isSel) GoldPrimary else BlackPrimary,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                error?.let { Text(it, color = FinancialRed, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedPrincipal = MoneyUtils.parseBigDecimal(principalText)
                    val parsedRate = rateText.toDoubleOrNull()
                    if (name.isBlank() || bank.isBlank()) {
                        error = "يرجى إدخال اسم الشهادة واسم البنك"
                        return@Button
                    }
                    if (parsedPrincipal == null || parsedPrincipal <= BigDecimal.ZERO) {
                        error = "يرجى إدخال أصل مبلغ صحيح"
                        return@Button
                    }
                    if (parsedRate == null || parsedRate <= 0.0) {
                        error = "يرجى إدخال نسبة عائد صحيحة"
                        return@Button
                    }
                    val now = DateUtils.currentEpochMillis()
                    val oneYearLater = now + (365L * 86_400_000L)
                    onSave(name.trim(), bank.trim(), parsedPrincipal, parsedRate, frequency, now, oneYearLater, notes.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = BlackPrimary),
                modifier = Modifier.testTag("btn_save_cert")
            ) { Text("حفظ الشهادة") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
