package com.example

import com.example.core.MoneyUtils
import com.example.core.DateUtils
import com.example.data.local.entities.ExpenseEntity
import com.example.data.local.entities.RevenueEntity
import com.example.data.local.entities.VehicleEntity
import com.example.data.local.entities.VehicleFuelLogEntity
import com.example.data.local.entities.VehicleMaintenanceLogEntity
import com.example.domain.usecase.FinancialCalculationUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class FinancialCalculationsTest {

    private val useCase = FinancialCalculationUseCase()

    @Test
    fun `test money parsing handles arabic and english decimals correctly`() {
        assertEquals(BigDecimal("1250.50"), MoneyUtils.parseBigDecimal("1250.50"))
        assertEquals(BigDecimal("1250.50"), MoneyUtils.parseBigDecimal("1250،50"))
        assertEquals(BigDecimal("1250.50"), MoneyUtils.parseBigDecimal("١٢٥٠.٥٠"))
        assertNull(MoneyUtils.parseBigDecimal("invalid-amount"))
        assertNull(MoneyUtils.parseBigDecimal("-50.00"))
        assertNull(MoneyUtils.parseBigDecimal("0"))
    }

    @Test
    fun `test piastres conversion avoids floating point errors`() {
        val parsedA = MoneyUtils.parseBigDecimal("100.25")
        val parsedB = MoneyUtils.parseBigDecimal("200.75")
        assertNotNull(parsedA)
        assertNotNull(parsedB)
        val piastresA = MoneyUtils.egpToPiastres(parsedA!!)
        val piastresB = MoneyUtils.egpToPiastres(parsedB!!)
        assertEquals(10025L, piastresA)
        assertEquals(20075L, piastresB)
        val sum = piastresA + piastresB
        assertEquals(30100L, sum)
        assertEquals("301.00", MoneyUtils.formatPiastresPlain(sum))
    }

    @Test
    fun `test financial summary calculation adheres to constitutional rules`() {
        // 1000.00 EGP = 100,000 Piastres
        // 2500.00 EGP = 250,000 Piastres
        val expenses = listOf(
            ExpenseEntity(
                expenseId = 1,
                amountPiastres = 100000L,
                date = 1000L,
                department = "PERSONAL",
                subcategory = "عناية",
                title = "مشتريات"
            ),
            ExpenseEntity(
                expenseId = 2,
                amountPiastres = 250000L,
                date = 2000L,
                department = "EDUCATION",
                subcategory = "رسوم",
                title = "قسط مدرسة"
            )
        )

        // 10,000.00 EGP = 1,000,000 Piastres
        val revenues = listOf(
            RevenueEntity(
                revenueId = 1,
                amountPiastres = 1000000L,
                date = 5000L,
                sourceType = "SALARY",
                title = "راتب وظيفي"
            )
        )

        val fuelLogs = listOf(
            VehicleFuelLogEntity(
                fuelLogId = 1,
                vehicleId = 1,
                date = 3000L,
                odometerKm = 10000L,
                quantityLiters = 50.0,
                pricePerLiterPiastres = 1000L,
                totalCostPiastres = 50000L, // 500.00 EGP
                isFullTank = true,
                linkedExpenseId = 101L
            )
        )

        val maintenanceLogs = listOf(
            VehicleMaintenanceLogEntity(
                maintenanceLogId = 1,
                vehicleId = 1,
                date = 4000L,
                odometerKm = 10000L,
                maintenanceType = "PERIODIC_SERVICE",
                description = "صيانة دورية",
                totalCostPiastres = 150000L, // 1500.00 EGP
                linkedExpenseId = 102L
            )
        )

        val summary = useCase.calculateSummary(
            currentBalancePiastres = 500000L,
            expenses = expenses,
            revenues = revenues,
            fuelLogs = fuelLogs,
            maintenanceLogs = maintenanceLogs
        )

        // Total direct expenses = 100,000 + 250,000 = 350,000 Piastres (3,500.00 EGP)
        assertEquals(350000L, summary.totalExpensesPiastres)
        // Total revenues = 1,000,000 Piastres (10,000.00 EGP)
        assertEquals(1000000L, summary.totalRevenuesPiastres)
        // Net Income = 1,000,000 - 350,000 = 650,000 Piastres (6,500.00 EGP)
        assertEquals(650000L, summary.netIncomePiastres)
    }

    @Test
    fun `test vehicle summary calculation and full to full efficiency`() {
        val vehicle = VehicleEntity(
            vehicleId = 1,
            name = "تويوتا",
            plateNumber = "123",
            initialOdometerKm = 10000L,
            currentOdometerKm = 10500L
        )

        val fuelLogs = listOf(
            VehicleFuelLogEntity(
                fuelLogId = 1,
                vehicleId = 1,
                date = 1000L,
                odometerKm = 10000L,
                quantityLiters = 40.0,
                pricePerLiterPiastres = 1000L,
                totalCostPiastres = 40000L, // 400 EGP
                isFullTank = true,
                linkedExpenseId = 1L
            ),
            VehicleFuelLogEntity(
                fuelLogId = 2,
                vehicleId = 1,
                date = 2000L,
                odometerKm = 10500L, // 500 km delta
                quantityLiters = 50.0, // 50 liters consumed
                pricePerLiterPiastres = 1000L,
                totalCostPiastres = 50000L, // 500 EGP
                isFullTank = true,
                linkedExpenseId = 2L
            )
        )

        val maintenanceLogs = listOf(
            VehicleMaintenanceLogEntity(
                maintenanceLogId = 1,
                vehicleId = 1,
                date = 3000L,
                odometerKm = 10500L,
                maintenanceType = "PERIODIC_SERVICE",
                description = "تغيير زيت",
                totalCostPiastres = 80000L, // 800 EGP
                linkedExpenseId = 3L
            )
        )

        val summary = useCase.calculateVehicleSummary(
            vehicle = vehicle,
            expenses = emptyList(),
            fuelLogs = fuelLogs,
            maintenanceLogs = maintenanceLogs
        )

        // Fuel expenses = 90,000 Piastres (900 EGP), Maintenance = 80,000 Piastres (800 EGP)
        assertEquals(90000L, summary.fuelExpensesPiastres)
        assertEquals(80000L, summary.maintenanceExpensesPiastres)
        assertEquals(170000L, summary.totalExpensesPiastres)

        // Efficiency: 500 km / 50 liters = 10.0 km/L
        assertNotNull(summary.averageFuelConsumptionKmPerLiter)
        assertEquals(10.0, summary.averageFuelConsumptionKmPerLiter!!, 0.01)
    }

    @Test
    fun `test empty inputs return zero balances safely`() {
        val summary = useCase.calculateSummary(
            currentBalancePiastres = 0L,
            expenses = emptyList(),
            revenues = emptyList()
        )
        assertEquals(0L, summary.totalExpensesPiastres)
        assertEquals(0L, summary.totalRevenuesPiastres)
        assertEquals(0L, summary.netIncomePiastres)
    }

    @Test
    fun `monthly reports use selected year and exclude uncollected accrual revenue`() {
        val year = 2025
        val expenses = listOf(
            ExpenseEntity(
                amountPiastres = 1_000L,
                date = DateUtils.getMonthStartEpoch(year, 1) + 5_000L,
                department = "HOME",
                subcategory = "بقالة",
                title = "مصروف يناير"
            ),
            ExpenseEntity(
                amountPiastres = 500L,
                date = DateUtils.getMonthStartEpoch(year, 2) + 5_000L,
                department = "HOME",
                subcategory = "فواتير",
                title = "مصروف فبراير"
            ),
            ExpenseEntity(
                amountPiastres = 99_000L,
                date = DateUtils.getMonthStartEpoch(year - 1, 1) + 5_000L,
                department = "HOME",
                subcategory = "قديم",
                title = "مصروف سنة سابقة"
            )
        )
        val revenues = listOf(
            RevenueEntity(
                amountPiastres = 3_000L,
                date = DateUtils.getMonthStartEpoch(year, 1) + 8_000L,
                sourceType = "SALARY",
                title = "دخل يناير"
            ),
            RevenueEntity(
                amountPiastres = 9_000L,
                date = DateUtils.getMonthStartEpoch(year, 1) + 9_000L,
                sourceType = "OTHER_INCOME",
                title = "دخل مستحق غير محصل",
                isAccrualCollection = true
            ),
            RevenueEntity(
                amountPiastres = 88_000L,
                date = DateUtils.getMonthStartEpoch(year - 1, 1) + 8_000L,
                sourceType = "SALARY",
                title = "دخل سنة سابقة"
            )
        )

        val reports = useCase.calculateMonthlyReports(year, expenses, revenues)

        assertEquals(12, reports.size)
        assertEquals(year, reports[0].year)
        assertEquals(1_000L, reports[0].totalExpensesPiastres)
        assertEquals(3_000L, reports[0].totalRevenuesPiastres)
        assertEquals(2_000L, reports[0].netBalancePiastres)
        assertEquals(500L, reports[1].totalExpensesPiastres)
        assertEquals(0L, reports[1].totalRevenuesPiastres)
        assertEquals(-500L, reports[1].netBalancePiastres)
        assertEquals(0L, reports[2].totalExpensesPiastres)
    }

}
