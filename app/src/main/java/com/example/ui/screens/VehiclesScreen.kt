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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.example.data.local.entities.VehicleEntity
import com.example.domain.model.VehicleLogItem
import com.example.domain.model.VehicleSummary
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.BlackPrimary
import com.example.ui.theme.FinancialGreen
import com.example.ui.theme.FinancialGreenContainer
import com.example.ui.theme.FinancialRed
import com.example.ui.theme.FinancialRedContainer
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.viewmodel.FinanceViewModel
import java.math.BigDecimal

@Composable
fun VehiclesScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val vehicles by viewModel.allVehicles.collectAsStateWithLifecycle()
    val vehicleSummaries by viewModel.vehicleSummaries.collectAsStateWithLifecycle()
    val allLogs by viewModel.allVehicleLogs.collectAsStateWithLifecycle()

    var selectedVehicleIndex by remember { mutableIntStateOf(0) }
    var showAddVehicleDialog by remember { mutableStateOf(false) }
    var editingVehicle by remember { mutableStateOf<VehicleEntity?>(null) }
    var vehicleToDelete by remember { mutableStateOf<VehicleEntity?>(null) }

    var showAddFuelDialog by remember { mutableStateOf(false) }
    var showAddMaintenanceDialog by remember { mutableStateOf(false) }
    var logToDelete by remember { mutableStateOf<VehicleLogItem?>(null) }

    val currentVehicle = vehicles.getOrNull(selectedVehicleIndex)
    val currentSummary = currentVehicle?.let { v ->
        vehicleSummaries.firstOrNull { it.vehicleId == v.id }
    }
    val currentVehicleLogs = currentVehicle?.let { v ->
        allLogs.filter { it.vehicleId == v.id }
    } ?: emptyList()

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "المركبات والأسطول",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = BlackPrimary
                        )
                    )
                    OutlinedButton(
                        onClick = {
                            editingVehicle = null
                            showAddVehicleDialog = true
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة مركبة")
                    }
                }
            }

            // Vehicle Selector Chips
            if (vehicles.isNotEmpty()) {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(vehicles.indices.toList()) { index ->
                            val v = vehicles[index]
                            val isSelected = selectedVehicleIndex == index
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedVehicleIndex = index },
                                label = {
                                    Text(
                                        "${v.name} (${v.plateNumber})",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BlackPrimary,
                                    selectedLabelColor = GoldPrimary,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = BlackPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            if (currentVehicle == null) {
                item {
                    EmptyStateCard(
                        message = "لم يتم إضافة مركبات حتى الآن. أضف مركبتك الأولى لمتابعة تكاليف الوقود والصيانة والإيرادات.",
                        icon = Icons.Default.DirectionsCar
                    )
                }
            } else {
                // Vehicle Stats Overview Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_vehicle_kpis"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = currentVehicle.name,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = BlackPrimary
                                        )
                                    )
                                    Text(
                                        text = "لوحة: ${currentVehicle.plateNumber} • موديل: ${currentVehicle.modelYear}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }

                                Row {
                                    IconButton(onClick = {
                                        editingVehicle = currentVehicle
                                        showAddVehicleDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = GoldAccent)
                                    }
                                    IconButton(onClick = {
                                        vehicleToDelete = currentVehicle
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Gray)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            currentSummary?.let { sum ->
                                // Row of cost badges
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    VehicleMiniStat(
                                        label = "تكلفة الوقود",
                                        value = MoneyUtils.format(sum.fuelExpenses),
                                        color = GoldDark,
                                        modifier = Modifier.weight(1f)
                                    )
                                    VehicleMiniStat(
                                        label = "تكلفة الصيانة",
                                        value = MoneyUtils.format(sum.maintenanceExpenses),
                                        color = FinancialRed,
                                        modifier = Modifier.weight(1f)
                                    )
                                    VehicleMiniStat(
                                        label = "إيرادات التشغيل",
                                        value = MoneyUtils.format(sum.totalRevenues),
                                        color = FinancialGreen,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Fuel Efficiency indicator if available
                                sum.averageFuelConsumptionKmPerLiter?.let { kmPerLiter ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(GoldLight, RoundedCornerShape(10.dp))
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Speed, contentDescription = null, tint = GoldDark, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "معدل استهلاك الوقود: $kmPerLiter كم / لتر",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = BlackPrimary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Action buttons: Add Fuel, Add Maintenance
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showAddFuelDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = BlackPrimary, contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_add_fuel_log")
                        ) {
                            Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تسجيل وقود")
                        }

                        Button(
                            onClick = { showAddMaintenanceDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_add_maintenance_log")
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, tint = BlackPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تسجيل صيانة")
                        }
                    }
                }

                // Logs list header
                item {
                    Text(
                        text = "سجل عمليات الوقود والصيانة",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = BlackPrimary)
                    )
                }

                if (currentVehicleLogs.isEmpty()) {
                    item {
                        EmptyStateCard(
                            message = "لا توجد عمليات وقود أو صيانة مسجلة لهذه المركبة بعد.",
                            icon = Icons.Default.LocalGasStation
                        )
                    }
                } else {
                    items(currentVehicleLogs, key = { it.id }) { log ->
                        VehicleLogRow(
                            log = log,
                            onDelete = { logToDelete = log }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(88.dp))
            }
        }
    }

    // Add / Edit Vehicle Dialog
    if (showAddVehicleDialog) {
        VehicleFormDialog(
            initialVehicle = editingVehicle,
            onDismiss = {
                showAddVehicleDialog = false
                editingVehicle = null
            },
            onSave = { name, plate, year, odo, notes ->
                viewModel.saveVehicle(
                    id = editingVehicle?.id ?: 0L,
                    name = name,
                    plateNumber = plate,
                    modelYear = year,
                    initialOdometer = odo,
                    notes = notes
                )
                showAddVehicleDialog = false
                editingVehicle = null
            }
        )
    }

    // Add Fuel Log Dialog
    if (showAddFuelDialog && currentVehicle != null) {
        FuelLogFormDialog(
            vehicle = currentVehicle,
            onDismiss = { showAddFuelDialog = false },
            onSave = { amount, date, odo, liters, notes ->
                viewModel.saveVehicleLog(
                    vehicleId = currentVehicle.id,
                    logType = "FUEL",
                    amount = amount,
                    date = date,
                    odometer = odo,
                    liters = liters,
                    serviceType = null,
                    serviceCenter = null,
                    notes = notes
                )
                showAddFuelDialog = false
            }
        )
    }

    // Add Maintenance Log Dialog
    if (showAddMaintenanceDialog && currentVehicle != null) {
        MaintenanceLogFormDialog(
            vehicle = currentVehicle,
            onDismiss = { showAddMaintenanceDialog = false },
            onSave = { amount, date, odo, serviceType, serviceCenter, notes ->
                viewModel.saveVehicleLog(
                    vehicleId = currentVehicle.id,
                    logType = "MAINTENANCE",
                    amount = amount,
                    date = date,
                    odometer = odo,
                    liters = null,
                    serviceType = serviceType,
                    serviceCenter = serviceCenter,
                    notes = notes
                )
                showAddMaintenanceDialog = false
            }
        )
    }

    // Delete Log confirmation
    logToDelete?.let { log ->
        AlertDialog(
            onDismissRequest = { logToDelete = null },
            title = { Text("تأكيد حذف السجل", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من حذف هذا السجل بقيمة ${MoneyUtils.format(MoneyUtils.parseBigDecimalAllowZero(log.amount))}؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteVehicleLog(log.id, log.logType)
                        logToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialRed)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { logToDelete = null }) { Text("إلغاء") }
            }
        )
    }

    // Delete Vehicle confirmation
    vehicleToDelete?.let { v ->
        AlertDialog(
            onDismissRequest = { vehicleToDelete = null },
            title = { Text("حذف المركبة", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من حذف المركبة \"${v.name}\" وجميع سجلاتها المرتبطة؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteVehicle(v.id)
                        vehicleToDelete = null
                        selectedVehicleIndex = 0
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialRed)
                ) {
                    Text("حذف نهائي")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { vehicleToDelete = null }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
private fun VehicleMiniStat(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 11.sp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = color))
        }
    }
}

@Composable
private fun VehicleLogRow(
    log: VehicleLogItem,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("vehicle_log_${log.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isFuel = log.logType == "FUEL"
            val icon = if (isFuel) Icons.Default.LocalGasStation else Icons.Default.Build
            val iconColor = if (isFuel) GoldDark else BlackPrimary

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isFuel) GoldLight else FinancialRedContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isFuel) "وقود (${log.liters ?: 0.0} لتر)" else (log.serviceType ?: "صيانة دورية"),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = BlackPrimary)
                )
                val subtitle = buildString {
                    log.odometer?.let { append("$it كم  ") }
                    log.serviceCenter?.let { append("• $it  ") }
                    append("• ${DateUtils.formatDateShort(log.date)}")
                }
                Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "- " + MoneyUtils.format(MoneyUtils.parseBigDecimalAllowZero(log.amount)),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold, color = FinancialRed)
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun VehicleFormDialog(
    initialVehicle: VehicleEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, plate: String, year: String, odo: Int, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(initialVehicle?.name ?: "") }
    var plate by remember { mutableStateOf(initialVehicle?.plateNumber ?: "") }
    var year by remember { mutableStateOf(initialVehicle?.modelYear ?: "") }
    var odoText by remember { mutableStateOf(initialVehicle?.initialOdometer?.toString() ?: "0") }
    var notes by remember { mutableStateOf(initialVehicle?.notes ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialVehicle == null) "إضافة مركبة جديدة" else "تعديل بيانات المركبة", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("اسم / نوع المركبة (مثلاً: تويوتا كورولا) *") },
                    modifier = Modifier.fillMaxWidth().testTag("input_vehicle_name"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = plate,
                    onValueChange = { plate = it; error = null },
                    label = { Text("رقم اللوحة (مثلاً: أ ب ج 123) *") },
                    modifier = Modifier.fillMaxWidth().testTag("input_vehicle_plate"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = year,
                        onValueChange = { year = it },
                        label = { Text("سنة الموديل") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = odoText,
                        onValueChange = { odoText = it },
                        label = { Text("العداد المبدئي (كم)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
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
                    if (name.isBlank() || plate.isBlank()) {
                        error = "يرجى إدخال اسم المركبة ورقم اللوحة"
                        return@Button
                    }
                    onSave(name.trim(), plate.trim(), year.trim(), odoText.toIntOrNull() ?: 0, notes.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = BlackPrimary),
                modifier = Modifier.testTag("btn_save_vehicle")
            ) {
                Text("حفظ المركبة")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
private fun FuelLogFormDialog(
    vehicle: VehicleEntity,
    onDismiss: () -> Unit,
    onSave: (amount: BigDecimal, date: Long, odo: Int?, liters: Double?, notes: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var litersText by remember { mutableStateOf("") }
    var odoText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تسجيل وقود لـ ${vehicle.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it; error = null },
                    label = { Text("تكلفة الوقود (ج.م) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("input_fuel_amount"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = litersText,
                        onValueChange = { litersText = it },
                        label = { Text("كمية الوقود (لتر)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = odoText,
                        onValueChange = { odoText = it },
                        label = { Text("قراءة العداد (كم)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات (مثلاً: بنزين 95 محطة الوطنية)") },
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
                    onSave(
                        parsed,
                        DateUtils.currentEpochMillis(),
                        odoText.toIntOrNull(),
                        litersText.toDoubleOrNull(),
                        notes.trim()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = BlackPrimary),
                modifier = Modifier.testTag("btn_save_fuel_log")
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
private fun MaintenanceLogFormDialog(
    vehicle: VehicleEntity,
    onDismiss: () -> Unit,
    onSave: (amount: BigDecimal, date: Long, odo: Int?, serviceType: String, serviceCenter: String, notes: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var serviceType by remember { mutableStateOf("تغيير زيت وفلتر") }
    var serviceCenter by remember { mutableStateOf("") }
    var odoText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تسجيل صيانة لـ ${vehicle.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it; error = null },
                    label = { Text("تكلفة الصيانة (ج.م) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("input_maint_amount"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = serviceType,
                    onValueChange = { serviceType = it; error = null },
                    label = { Text("نوع الصيانة / الخدمة *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = serviceCenter,
                        onValueChange = { serviceCenter = it },
                        label = { Text("المركز / الورشة") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = odoText,
                        onValueChange = { odoText = it },
                        label = { Text("العداد (كم)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية") },
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
                    if (serviceType.isBlank()) {
                        error = "يرجى تحديد نوع الصيانة"
                        return@Button
                    }
                    onSave(
                        parsed,
                        DateUtils.currentEpochMillis(),
                        odoText.toIntOrNull(),
                        serviceType.trim(),
                        serviceCenter.trim(),
                        notes.trim()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = BlackPrimary),
                modifier = Modifier.testTag("btn_save_maint_log")
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
