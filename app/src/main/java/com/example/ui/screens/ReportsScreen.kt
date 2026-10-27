package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.DateUtils
import com.example.core.MoneyUtils
import com.example.domain.model.Department
import com.example.domain.model.MonthlyReportItem
import com.example.ui.components.DepartmentProgressRow
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.MonthPickerBar
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
fun ReportsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.currentMonthSummary.collectAsStateWithLifecycle()
    val monthFilter by viewModel.currentMonthFilter.collectAsStateWithLifecycle()
    val monthlyReports by viewModel.monthlyReports.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            MonthPickerBar(
                year = monthFilter.year,
                month = monthFilter.month,
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() }
            )
        }

        // Performance Overview for Selected Month
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_report_performance"),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Assessment, contentDescription = null, tint = GoldAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "الأداء المالي لشهر ${DateUtils.getMonthName(monthFilter.month)} ${monthFilter.year}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = BlackPrimary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReportMetricBox(
                            title = "الإيرادات",
                            amount = summary.totalRevenues,
                            color = FinancialGreen,
                            bgColor = FinancialGreenContainer,
                            modifier = Modifier.weight(1f)
                        )
                        ReportMetricBox(
                            title = "المصروفات",
                            amount = summary.totalExpenses,
                            color = FinancialRed,
                            bgColor = FinancialRedContainer,
                            modifier = Modifier.weight(1f)
                        )
                        ReportMetricBox(
                            title = "الصافي",
                            amount = summary.netBalance,
                            color = if (summary.netBalance >= BigDecimal.ZERO) GoldDark else FinancialRed,
                            bgColor = GoldLight,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Fuel & Maintenance sub-totals if any
                    val hasVehiclesCosts = summary.fuelTotal > BigDecimal.ZERO || summary.maintenanceTotal > BigDecimal.ZERO
                    if (hasVehiclesCosts) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("منها وقود: ${MoneyUtils.format(summary.fuelTotal)}", style = MaterialTheme.typography.bodySmall)
                            Text("منها صيانة: ${MoneyUtils.format(summary.maintenanceTotal)}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        // Department Breakdown Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_report_departments"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Category, contentDescription = null, tint = GoldAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "تفصيل المصروفات حسب الأقسام الخمسة",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = BlackPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (summary.departmentSummaries.none { it.totalAmount > BigDecimal.ZERO }) {
                        Text(
                            "لا توجد مصروفات لهذا الشهر",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        summary.departmentSummaries.forEach { item ->
                            DepartmentProgressRow(
                                department = item.department,
                                amount = item.totalAmount,
                                percentage = item.percentageOfTotal
                            )
                        }
                    }
                }
            }
        }

        // Month-by-Month Comparative Analysis
        item {
            Text(
                text = "المقارنة التاريخية بين الأشهر",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = BlackPrimary)
            )
        }

        if (monthlyReports.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "لا توجد بيانات تاريخية كافية بعد لإجراء المقارنات الشهرية.",
                    icon = Icons.Default.CompareArrows
                )
            }
        } else {
            items(monthlyReports) { report ->
                MonthlyComparisonCard(report = report)
            }
        }

        item {
            Spacer(modifier = Modifier.height(88.dp))
        }
    }
}

@Composable
private fun ReportMetricBox(
    title: String,
    amount: BigDecimal,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray, fontSize = 11.sp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            MoneyUtils.format(amount),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold, color = color, fontSize = 13.sp)
        )
    }
}

@Composable
private fun MonthlyComparisonCard(
    report: MonthlyReportItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("report_month_${report.year}_${report.month}"),
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
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "${report.monthName} ${report.year}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = BlackPrimary)
                    )
                }

                val isPositive = report.netBalance >= BigDecimal.ZERO
                Text(
                    text = "الصافي: " + MoneyUtils.format(report.netBalance),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isPositive) FinancialGreen else FinancialRed
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "الإيرادات: ${MoneyUtils.format(report.totalRevenues)}",
                    style = MaterialTheme.typography.bodySmall.copy(color = FinancialGreen, fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "المصروفات: ${MoneyUtils.format(report.totalExpenses)}",
                    style = MaterialTheme.typography.bodySmall.copy(color = FinancialRed, fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}
