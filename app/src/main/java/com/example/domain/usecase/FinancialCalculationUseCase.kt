package com.example.domain.usecase

import com.example.core.DateUtils
import com.example.core.MoneyUtils
import com.example.data.local.entities.ExpenseEntity
import com.example.data.local.entities.FinancialCertificateEntity
import com.example.data.local.entities.GamaeyaEntity
import com.example.data.local.entities.GamaeyaInstallmentEntity
import com.example.data.local.entities.RevenueEntity
import com.example.data.local.entities.StudentEntity
import com.example.data.local.entities.VehicleEntity
import com.example.data.local.entities.VehicleFuelLogEntity
import com.example.data.local.entities.VehicleMaintenanceLogEntity
import com.example.domain.model.CertificateSummary
import com.example.domain.model.Department
import com.example.domain.model.DepartmentSummary
import com.example.domain.model.FinancialSummary
import com.example.domain.model.GamaeyaSummary
import com.example.domain.model.MonthlyReportItem
import com.example.domain.model.StudentSummary
import com.example.domain.model.TransactionItem
import com.example.domain.model.TransactionType
import com.example.domain.model.VehicleSummary
import java.math.BigDecimal
import java.math.RoundingMode

class FinancialCalculationUseCase {

    /**
     * Calculates the comprehensive executive summary adhering strictly to Section 12.1 of the Constitution:
     * - Total Monthly Expenses = sum(expenses.amountPiastres)
     * - Total Monthly Earned Income = sum(revenues.amountPiastres where isAccrualCollection == false)
     * - Net Income = Total Earned Income - Total Expenses
     * - Total Cash Outflow = Total Expenses + Paid Gamaeya Installments
     * - Total Cash Inflow = Total Earned Income + Collected Gamaeya Payouts
     */
    fun calculateSummary(
        currentBalancePiastres: Long = 0L,
        expenses: List<ExpenseEntity>,
        revenues: List<RevenueEntity>,
        paidInstallments: List<GamaeyaInstallmentEntity> = emptyList(),
        collectedGamaeyat: List<GamaeyaEntity> = emptyList(),
        fuelLogs: List<VehicleFuelLogEntity> = emptyList(),
        maintenanceLogs: List<VehicleMaintenanceLogEntity> = emptyList()
    ): FinancialSummary {
        var totalExpensesPiastres = 0L
        val deptTotals = mutableMapOf<Department, Long>()
        Department.entries.forEach { dept ->
            deptTotals[dept] = 0L
        }

        // Sum expenses and bucket by department
        for (expense in expenses) {
            val amount = expense.amountPiastres
            totalExpensesPiastres += amount

            val dept = Department.fromId(expense.department)
            val current = deptTotals[dept] ?: 0L
            deptTotals[dept] = current + amount
        }

        // Fuel and maintenance totals (for tracking)
        val fuelTotalPiastres = fuelLogs.sumOf { it.totalCostPiastres }
        val maintenanceTotalPiastres = maintenanceLogs.sumOf { it.totalCostPiastres }

        // Sum earned revenues (excluding accrual collection to prevent double counting)
        val totalRevenuesPiastres = revenues
            .filter { !it.isAccrualCollection }
            .sumOf { it.amountPiastres }

        // Net income (Surplus or Deficit)
        val netIncomePiastres = totalRevenuesPiastres - totalExpensesPiastres

        // Outflows and Inflows
        val gamaeyaOutflowPiastres = paidInstallments.sumOf { it.amountPiastres }
        val gamaeyaInflowPiastres = collectedGamaeyat.sumOf { it.totalPoolValuePiastres }

        val totalCashOutflowPiastres = totalExpensesPiastres + gamaeyaOutflowPiastres
        val totalCashInflowPiastres = totalRevenuesPiastres + gamaeyaInflowPiastres

        // Department breakdown percentages
        val deptSummaries = deptTotals.map { (dept, sum) ->
            val percentage = if (totalExpensesPiastres > 0L) {
                BigDecimal(sum)
                    .multiply(BigDecimal(100))
                    .divide(BigDecimal(totalExpensesPiastres), 2, RoundingMode.HALF_UP)
                    .toFloat()
            } else {
                0f
            }
            DepartmentSummary(
                department = dept,
                totalAmountPiastres = sum,
                percentageOfTotal = percentage
            )
        }.sortedByDescending { it.totalAmountPiastres }

        // Compile unified recent transactions
        val transactions = mutableListOf<TransactionItem>()
        expenses.forEach { exp ->
            transactions.add(
                TransactionItem(
                    id = exp.expenseId,
                    title = exp.title,
                    amountPiastres = exp.amountPiastres,
                    date = exp.date,
                    type = TransactionType.EXPENSE,
                    subtitle = "${Department.fromId(exp.department).titleAr} • ${exp.subcategory}",
                    department = Department.fromId(exp.department),
                    rawDepartment = exp.department
                )
            )
        }

        revenues.forEach { rev ->
            transactions.add(
                TransactionItem(
                    id = rev.revenueId,
                    title = rev.title,
                    amountPiastres = rev.amountPiastres,
                    date = rev.date,
                    type = TransactionType.REVENUE,
                    subtitle = rev.payerOrInstitution ?: "إيراد مكتسب"
                )
            )
        }

        paidInstallments.forEach { inst ->
            transactions.add(
                TransactionItem(
                    id = inst.installmentId,
                    title = "قسط جمعية مسدد #${inst.installmentNumber}",
                    amountPiastres = inst.amountPiastres,
                    date = inst.paidDate ?: inst.dueDate,
                    type = TransactionType.GAMAEYA_INSTALLMENT,
                    subtitle = "سداد التزام نقدي"
                )
            )
        }

        transactions.sortByDescending { it.date }

        return FinancialSummary(
            currentLiquidBalancePiastres = currentBalancePiastres,
            totalExpensesPiastres = totalExpensesPiastres,
            totalRevenuesPiastres = totalRevenuesPiastres,
            netIncomePiastres = netIncomePiastres,
            totalCashOutflowPiastres = totalCashOutflowPiastres,
            totalCashInflowPiastres = totalCashInflowPiastres,
            departmentSummaries = deptSummaries,
            recentTransactions = transactions.take(15),
            fuelTotalPiastres = fuelTotalPiastres,
            maintenanceTotalPiastres = maintenanceTotalPiastres
        )
    }

    /**
     * Calculates Full-to-Full fuel efficiency (Section 7 and 12.4 of Constitution).
     * Returns null if two consecutive full tanks are not present or liters added <= 0.
     */
    fun calculateFullToFullEfficiency(
        logs: List<VehicleFuelLogEntity>
    ): Double? {
        val sortedLogs = logs.sortedBy { it.odometerKm }
        val fullTankIndices = sortedLogs.indices.filter { sortedLogs[it].isFullTank }
        if (fullTankIndices.size < 2) return null

        val startIndex = fullTankIndices.first()
        val endIndex = fullTankIndices.last()
        val distanceKm = sortedLogs[endIndex].odometerKm - sortedLogs[startIndex].odometerKm
        if (distanceKm <= 0L) return null

        // Sum liters added after the start full tank up to and including the end full tank
        val liters = sortedLogs.subList(startIndex + 1, endIndex + 1).sumOf { it.quantityLiters }
        if (liters <= 0.0) return null

        val kmPerLiter = distanceKm.toDouble() / liters
        return BigDecimal(kmPerLiter).setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    /**
     * Calculates Vehicle Summary.
     */
    fun calculateVehicleSummary(
        vehicle: VehicleEntity,
        expenses: List<ExpenseEntity>,
        fuelLogs: List<VehicleFuelLogEntity>,
        maintenanceLogs: List<VehicleMaintenanceLogEntity>
    ): VehicleSummary {
        val fuelSum = fuelLogs.sumOf { it.totalCostPiastres }
        val maintenanceSum = maintenanceLogs.sumOf { it.totalCostPiastres }
        val directSum = expenses.sumOf { it.amountPiastres }
        val totalSum = directSum + fuelSum + maintenanceSum

        val totalLiters = fuelLogs.sumOf { it.quantityLiters }
        val efficiency = calculateFullToFullEfficiency(fuelLogs)

        return VehicleSummary(
            vehicleId = vehicle.vehicleId,
            vehicleName = vehicle.name,
            plateNumber = vehicle.plateNumber,
            directExpensesPiastres = directSum,
            fuelExpensesPiastres = fuelSum,
            maintenanceExpensesPiastres = maintenanceSum,
            totalExpensesPiastres = totalSum,
            totalRevenuesPiastres = 0L,
            netProfitOrLossPiastres = -totalSum,
            fuelLiters = totalLiters,
            averageFuelConsumptionKmPerLiter = efficiency
        )
    }

    /**
     * Calculates Student Education Summary.
     */
    fun calculateStudentSummary(
        student: StudentEntity,
        monthlyExpenses: List<ExpenseEntity>,
        allAnnualExpenses: List<ExpenseEntity>
    ): StudentSummary {
        val monthlyTotal = monthlyExpenses.sumOf { it.amountPiastres }
        val annualTotal = allAnnualExpenses.sumOf { it.amountPiastres }
        return StudentSummary(
            studentId = student.studentId,
            fullName = student.fullName,
            academicStage = student.academicStage,
            totalMonthlyPiastres = monthlyTotal,
            totalAnnualPiastres = annualTotal,
            monthlyBudgetPiastres = student.monthlyBudgetPiastres
        )
    }

    /**
     * Calculates Gamaeya Summary and Net Financial Position (Section 12.5 of Constitution).
     */
    fun calculateGamaeyaSummary(
        gamaeya: GamaeyaEntity,
        installments: List<GamaeyaInstallmentEntity>
    ): GamaeyaSummary {
        val paidSum = installments.filter { it.isPaid }.sumOf { it.amountPiastres }
        val totalObligation = gamaeya.numberOfMembers.toLong() * gamaeya.monthlyInstallmentPiastres
        val remainingObligation = maxOf(0L, totalObligation - paidSum)

        val totalReceived = if (gamaeya.isPayoutReceived) gamaeya.totalPoolValuePiastres else 0L
        val netPosition = totalReceived - paidSum

        return GamaeyaSummary(
            gamaeyaId = gamaeya.gamaeyaId,
            name = gamaeya.name,
            totalPoolValuePiastres = gamaeya.totalPoolValuePiastres,
            numberOfMembers = gamaeya.numberOfMembers,
            monthlyInstallmentPiastres = gamaeya.monthlyInstallmentPiastres,
            myTurnOrder = gamaeya.myTurnOrder,
            totalPaidPiastres = paidSum,
            totalReceivedPiastres = totalReceived,
            remainingObligationsPiastres = remainingObligation,
            netPositionPiastres = netPosition,
            isPayoutReceived = gamaeya.isPayoutReceived
        )
    }

    /**
     * Calculates Fixed Simple Annual Return for Certificates (Section 8.4 and 12.6 of Constitution).
     */
    fun calculateCertificateAnnualProfitPiastres(
        principalPiastres: Long,
        annualRatePercent: BigDecimal
    ): Long {
        val principalBd = BigDecimal(principalPiastres)
        val rateBd = annualRatePercent.divide(BigDecimal(100), 10, RoundingMode.HALF_UP)
        return principalBd.multiply(rateBd).setScale(0, RoundingMode.HALF_UP).toLong()
    }

    fun calculateCertificatePeriodicYieldPiastres(
        principalPiastres: Long,
        annualRatePercent: BigDecimal,
        frequency: String,
        durationYears: Int
    ): Long {
        val annualProfit = calculateCertificateAnnualProfitPiastres(principalPiastres, annualRatePercent)
        val annualProfitBd = BigDecimal(annualProfit)
        return when (frequency.uppercase()) {
            "MONTHLY" -> annualProfitBd.divide(BigDecimal(12), 0, RoundingMode.HALF_UP).toLong()
            "QUARTERLY" -> annualProfitBd.divide(BigDecimal(4), 0, RoundingMode.HALF_UP).toLong()
            "SEMI_ANNUALLY" -> annualProfitBd.divide(BigDecimal(2), 0, RoundingMode.HALF_UP).toLong()
            "ANNUALLY" -> annualProfit
            "AT_MATURITY" -> annualProfitBd.multiply(BigDecimal(durationYears)).setScale(0, RoundingMode.HALF_UP).toLong()
            else -> annualProfitBd.divide(BigDecimal(12), 0, RoundingMode.HALF_UP).toLong()
        }
    }
}
