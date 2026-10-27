package com.example.domain.model

import com.example.core.MoneyUtils
import java.math.BigDecimal

data class FinancialSummary(
    val currentLiquidBalancePiastres: Long = 0L,
    val totalExpensesPiastres: Long = 0L,
    val totalRevenuesPiastres: Long = 0L,
    val netIncomePiastres: Long = 0L,
    val totalCashOutflowPiastres: Long = 0L,
    val totalCashInflowPiastres: Long = 0L,
    val departmentSummaries: List<DepartmentSummary> = emptyList(),
    val recentTransactions: List<TransactionItem> = emptyList(),
    val fuelTotalPiastres: Long = 0L,
    val maintenanceTotalPiastres: Long = 0L
) {
    val currentLiquidBalance: BigDecimal get() = MoneyUtils.piastresToEgp(currentLiquidBalancePiastres)
    val totalExpenses: BigDecimal get() = MoneyUtils.piastresToEgp(totalExpensesPiastres)
    val totalRevenues: BigDecimal get() = MoneyUtils.piastresToEgp(totalRevenuesPiastres)
    val netBalance: BigDecimal get() = MoneyUtils.piastresToEgp(netIncomePiastres)
    val totalCashOutflow: BigDecimal get() = MoneyUtils.piastresToEgp(totalCashOutflowPiastres)
    val totalCashInflow: BigDecimal get() = MoneyUtils.piastresToEgp(totalCashInflowPiastres)
    val fuelTotal: BigDecimal get() = MoneyUtils.piastresToEgp(fuelTotalPiastres)
    val maintenanceTotal: BigDecimal get() = MoneyUtils.piastresToEgp(maintenanceTotalPiastres)
}

data class DepartmentSummary(
    val department: Department,
    val totalAmountPiastres: Long,
    val percentageOfTotal: Float
) {
    val totalAmount: BigDecimal get() = MoneyUtils.piastresToEgp(totalAmountPiastres)
}

data class StudentSummary(
    val studentId: Long,
    val fullName: String,
    val academicStage: String,
    val totalMonthlyPiastres: Long,
    val totalAnnualPiastres: Long,
    val monthlyBudgetPiastres: Long?
) {
    val totalMonthly: BigDecimal get() = MoneyUtils.piastresToEgp(totalMonthlyPiastres)
    val totalAnnual: BigDecimal get() = MoneyUtils.piastresToEgp(totalAnnualPiastres)
}

data class VehicleSummary(
    val vehicleId: Long,
    val vehicleName: String,
    val plateNumber: String,
    val directExpensesPiastres: Long = 0L,
    val fuelExpensesPiastres: Long = 0L,
    val maintenanceExpensesPiastres: Long = 0L,
    val oilExpensesPiastres: Long = 0L,
    val accidentExpensesPiastres: Long = 0L,
    val totalExpensesPiastres: Long = 0L,
    val totalRevenuesPiastres: Long = 0L,
    val netProfitOrLossPiastres: Long = 0L,
    val fuelLiters: Double = 0.0,
    val averageFuelConsumptionKmPerLiter: Double? = null
) {
    val directExpenses: BigDecimal get() = MoneyUtils.piastresToEgp(directExpensesPiastres)
    val fuelExpenses: BigDecimal get() = MoneyUtils.piastresToEgp(fuelExpensesPiastres)
    val maintenanceExpenses: BigDecimal get() = MoneyUtils.piastresToEgp(maintenanceExpensesPiastres)
    val totalExpenses: BigDecimal get() = MoneyUtils.piastresToEgp(totalExpensesPiastres)
    val totalRevenues: BigDecimal get() = MoneyUtils.piastresToEgp(totalRevenuesPiastres)
    val netProfitOrLoss: BigDecimal get() = MoneyUtils.piastresToEgp(netProfitOrLossPiastres)
    val fuelEfficiencyDisplay: String
        get() = averageFuelConsumptionKmPerLiter?.let { String.format("%.2f كم/لتر", it) } ?: "غير متاح"
}

data class VehicleLogItem(
    val id: Long,
    val vehicleId: Long,
    val logType: String, // FUEL or MAINTENANCE
    val amount: String,
    val date: Long,
    val odometer: Int? = null,
    val liters: Double? = null,
    val serviceType: String? = null,
    val serviceCenter: String? = null,
    val notes: String = ""
)

data class GamaeyaSummary(
    val gamaeyaId: Long,
    val name: String,
    val totalPoolValuePiastres: Long,
    val numberOfMembers: Int,
    val monthlyInstallmentPiastres: Long,
    val myTurnOrder: Int,
    val totalPaidPiastres: Long,
    val totalReceivedPiastres: Long,
    val remainingObligationsPiastres: Long,
    val netPositionPiastres: Long,
    val isPayoutReceived: Boolean
) {
    val totalPoolValue: BigDecimal get() = MoneyUtils.piastresToEgp(totalPoolValuePiastres)
    val monthlyInstallment: BigDecimal get() = MoneyUtils.piastresToEgp(monthlyInstallmentPiastres)
    val totalPaid: BigDecimal get() = MoneyUtils.piastresToEgp(totalPaidPiastres)
    val totalReceived: BigDecimal get() = MoneyUtils.piastresToEgp(totalReceivedPiastres)
    val remainingObligations: BigDecimal get() = MoneyUtils.piastresToEgp(remainingObligationsPiastres)
    val netPosition: BigDecimal get() = MoneyUtils.piastresToEgp(netPositionPiastres)
}

data class CertificateSummary(
    val certificateId: Long,
    val name: String,
    val bankName: String,
    val principalPiastres: Long,
    val annualRatePercent: String,
    val payoutFrequency: String,
    val expectedTotalYieldPiastres: Long,
    val dueUncollectedYieldPiastres: Long,
    val collectedYieldPiastres: Long
) {
    val principal: BigDecimal get() = MoneyUtils.piastresToEgp(principalPiastres)
    val expectedTotalYield: BigDecimal get() = MoneyUtils.piastresToEgp(expectedTotalYieldPiastres)
    val dueUncollectedYield: BigDecimal get() = MoneyUtils.piastresToEgp(dueUncollectedYieldPiastres)
    val collectedYield: BigDecimal get() = MoneyUtils.piastresToEgp(collectedYieldPiastres)
}

enum class TransactionType {
    EXPENSE,
    REVENUE,
    VEHICLE_LOG,
    GAMAEYA_INSTALLMENT,
    CERTIFICATE_PAYOUT
}

data class TransactionItem(
    val id: Long,
    val title: String,
    val amountPiastres: Long,
    val date: Long,
    val type: TransactionType,
    val subtitle: String,
    val department: Department? = null,
    val rawDepartment: String? = null,
    val logType: String? = null
) {
    val amount: BigDecimal get() = MoneyUtils.piastresToEgp(amountPiastres)
}

data class MonthlyReportItem(
    val year: Int,
    val month: Int,
    val monthName: String,
    val totalExpensesPiastres: Long,
    val totalRevenuesPiastres: Long,
    val netBalancePiastres: Long
) {
    val totalExpenses: BigDecimal get() = MoneyUtils.piastresToEgp(totalExpensesPiastres)
    val totalRevenues: BigDecimal get() = MoneyUtils.piastresToEgp(totalRevenuesPiastres)
    val netBalance: BigDecimal get() = MoneyUtils.piastresToEgp(netBalancePiastres)
}
