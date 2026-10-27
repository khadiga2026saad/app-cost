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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.MoneyUtils
import com.example.domain.model.TransactionItem
import com.example.domain.model.TransactionType
import com.example.ui.components.DepartmentProgressRow
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FinancialStatCard
import com.example.ui.components.MonthPickerBar
import com.example.ui.components.TransactionRowCard
import com.example.ui.theme.BlackPrimary
import com.example.ui.theme.BlackSecondary
import com.example.ui.theme.FinancialGreen
import com.example.ui.theme.FinancialRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.viewmodel.FinanceViewModel
import java.math.BigDecimal

@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    onNavigateToExpenses: () -> Unit,
    onNavigateToRevenues: () -> Unit,
    onNavigateToVehicles: () -> Unit,
    onOpenAddExpense: () -> Unit,
    onOpenAddRevenue: () -> Unit,
    onOpenAddVehicleLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.currentMonthSummary.collectAsStateWithLifecycle()
    val monthFilter by viewModel.currentMonthFilter.collectAsStateWithLifecycle()

    var transactionToDelete by remember { mutableStateOf<TransactionItem?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Month Picker Bar
            MonthPickerBar(
                year = monthFilter.year,
                month = monthFilter.month,
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() }
            )
        }

        // Financial KPIs
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FinancialStatCard(
                        title = "إجمالي المصروفات",
                        amount = summary.totalExpenses,
                        icon = Icons.Default.ArrowUpward,
                        iconColor = FinancialRed,
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_total_expenses")
                    )
                    FinancialStatCard(
                        title = "إجمالي الإيرادات",
                        amount = summary.totalRevenues,
                        icon = Icons.Default.ArrowDownward,
                        iconColor = FinancialGreen,
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_total_revenues")
                    )
                }

                // Net Balance Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stat_net_balance"),
                    colors = CardDefaults.cardColors(containerColor = BlackPrimary),
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
                            Text(
                                text = "صافي الفارق المالي",
                                style = MaterialTheme.typography.labelMedium.copy(color = GoldAccent)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val isPositive = summary.netBalance >= BigDecimal.ZERO
                            Text(
                                text = MoneyUtils.format(summary.netBalance),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isPositive) GoldPrimary else FinancialRed,
                                    fontSize = 22.sp
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions
        item {
            Column {
                Text(
                    text = "إجراءات سريعة",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = BlackPrimary
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionButton(
                        title = "إضافة مصروف",
                        icon = Icons.Default.ReceiptLong,
                        color = BlackPrimary,
                        onClick = onOpenAddExpense,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_quick_add_expense")
                    )
                    QuickActionButton(
                        title = "إضافة إيراد",
                        icon = Icons.Default.AttachMoney,
                        color = FinancialGreen,
                        onClick = onOpenAddRevenue,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_quick_add_revenue")
                    )
                    QuickActionButton(
                        title = "وقود / صيانة",
                        icon = Icons.Default.LocalGasStation,
                        color = GoldDark,
                        onClick = onOpenAddVehicleLog,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_quick_add_vehicle_log")
                    )
                }
            }
        }

        // Department Spending Summary
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_department_breakdown"),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "توزيع المصروفات حسب الأقسام",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BlackPrimary
                                )
                            )
                        }

                        TextButton(onClick = onNavigateToExpenses) {
                            Text("التفاصيل", color = GoldDark, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (summary.departmentSummaries.none { it.totalAmount > BigDecimal.ZERO }) {
                        Text(
                            text = "لا توجد مصروفات مسجلة لهذا الشهر حتى الآن",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        summary.departmentSummaries.filter { it.totalAmount > BigDecimal.ZERO }.forEach { deptSummary ->
                            DepartmentProgressRow(
                                department = deptSummary.department,
                                amount = deptSummary.totalAmount,
                                percentage = deptSummary.percentageOfTotal
                            )
                        }
                    }
                }
            }
        }

        // Recent Transactions Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "أحدث العمليات المسجلة",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = BlackPrimary
                    )
                )
                TextButton(onClick = onNavigateToExpenses) {
                    Text("عرض الكل", color = GoldDark, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Recent Transactions List
        if (summary.recentTransactions.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "لا توجد عمليات مسجلة في هذا الشهر. اضغط على أزرار الإجراءات السريعة بالأعلى لإضافة أول عملية.",
                    modifier = Modifier.testTag("empty_transactions")
                )
            }
        } else {
            items(summary.recentTransactions, key = { "${it.type}_${it.id}" }) { item ->
                TransactionRowCard(
                    transaction = item,
                    onEdit = {
                        // Routing based on transaction type
                        when (item.type) {
                            TransactionType.EXPENSE -> onNavigateToExpenses()
                            TransactionType.REVENUE -> onNavigateToRevenues()
                            TransactionType.VEHICLE_LOG -> onNavigateToVehicles()
                            else -> onNavigateToExpenses()
                        }
                    },
                    onDelete = {
                        transactionToDelete = item
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Delete Confirmation Dialog
    transactionToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = {
                Text("تأكيد الحذف", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("هل أنت متأكد من رغبتك في حذف العملية \"${tx.title}\" بقيمة ${MoneyUtils.format(tx.amount)}؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (tx.type) {
                            TransactionType.EXPENSE -> viewModel.deleteExpense(tx.id)
                            TransactionType.REVENUE -> viewModel.deleteRevenue(tx.id)
                            else -> viewModel.deleteExpense(tx.id)
                        }
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialRed)
                ) {
                    Text("حذف نهائي")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { transactionToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = BlackPrimary,
                    fontSize = 12.sp
                ),
                maxLines = 1
            )
        }
    }
}
