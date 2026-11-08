package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entities.CurrentAccountEntity
import com.example.data.local.entities.ExpenseEntity
import com.example.data.local.entities.FinancialCertificateEntity
import com.example.data.local.entities.RevenueEntity
import com.example.data.local.entities.VehicleAccidentLogEntity
import com.example.data.local.entities.VehicleEntity
import com.example.data.local.entities.VehicleFuelLogEntity
import com.example.data.repository.FinanceRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Settlement tests for the repository rules defined by the Financial Constitution.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FinanceRepositorySettlementTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: FinanceRepositoryImpl

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = FinanceRepositoryImpl(db)
    }

    @After
    fun closeDb() {
        db.close()
    }

    private suspend fun seedTreasury(initialBalancePiastres: Long) {
        db.currentAccountDao().insertOrUpdate(
            CurrentAccountEntity(accountId = 1L, currentBalancePiastres = initialBalancePiastres)
        )
    }

    private suspend fun balance(): Long =
        repository.getAccountDirect()?.currentBalancePiastres ?: 0L

    @Test
    fun treasuryBalanceUpdateCreatesMissingAccountRow() = runBlocking {
        assertNull(repository.getAccountDirect())

        repository.insertExpense(
            ExpenseEntity(
                amountPiastres = 12_500L,
                date = 1_000L,
                department = "HOME",
                subcategory = "مستلزمات",
                title = "مصروف اختبار"
            )
        )

        val account = repository.getAccountDirect()
        assertNotNull(account)
        assertEquals(-12_500L, account!!.currentBalancePiastres)
    }

    private suspend fun insertVehicle(): Long =
        db.vehicleDao().insertVehicle(
            VehicleEntity(
                name = "تويوتا كورولا",
                plateNumber = "أ ب ج 123",
                initialOdometerKm = 1000L,
                currentOdometerKm = 1000L
            )
        )

    @Test
    fun deleteFuelLogRefundsTreasuryAndRemovesLinkedExpense() = runBlocking {
        seedTreasury(100_000L)
        val vehicleId = insertVehicle()
        val logId = repository.insertFuelLogWithExpense(
            VehicleFuelLogEntity(
                vehicleId = vehicleId,
                date = 1000L,
                odometerKm = 1500L,
                quantityLiters = 20.0,
                pricePerLiterPiastres = 1500L,
                totalCostPiastres = 30_000L,
                isFullTank = true,
                linkedExpenseId = 0L
            ),
            "تويوتا كورولا"
        )
        assertTrue(logId > 0)
        assertEquals(70_000L, balance())

        repository.deleteVehicleLogById(logId, "FUEL")

        assertEquals(100_000L, balance())
        assertTrue(db.expenseDao().getAllExpenses().first().isEmpty())
        assertTrue(db.vehicleLogDao().getFuelLogsForVehicle(vehicleId).first().isEmpty())
    }

    @Test
    fun deleteUnknownLogTypeThrowsClearExceptionWithoutChangingBalances() = runBlocking {
        seedTreasury(100_000L)
        val vehicleId = insertVehicle()
        repository.insertFuelLogWithExpense(
            VehicleFuelLogEntity(
                vehicleId = vehicleId,
                date = 1000L,
                odometerKm = 1500L,
                quantityLiters = 20.0,
                pricePerLiterPiastres = 1500L,
                totalCostPiastres = 30_000L,
                isFullTank = true,
                linkedExpenseId = 0L
            ),
            "تويوتا كورولا"
        )
        assertEquals(70_000L, balance())

        var failure: IllegalArgumentException? = null
        try {
            repository.deleteVehicleLogById(1L, "NOT_A_TYPE")
        } catch (exception: IllegalArgumentException) {
            failure = exception
        }

        assertNotNull(failure)
        assertTrue(failure!!.message.orEmpty().contains("NOT_A_TYPE"))
        assertEquals(70_000L, balance())
        assertEquals(1, db.vehicleLogDao().getFuelLogsForVehicle(vehicleId).first().size)
    }

    @Test
    fun accidentValidationRejectsInventedNetCost() = runBlocking {
        seedTreasury(100_000L)
        val vehicleId = insertVehicle()
        val invalid = VehicleAccidentLogEntity(
            vehicleId = vehicleId,
            accidentDate = 1000L,
            description = "حادث بسيط",
            estimatedDamagePiastres = 50_000L,
            actualRepairCostPiastres = 0L,
            insuranceCompensationPiastres = 0L,
            netUserCostPiastres = 12_000L,
            compensationStatus = "PENDING_CLAIM"
        )

        var rejected = false
        try {
            repository.insertAccidentLogWithExpense(invalid, "تويوتا كورولا")
        } catch (e: IllegalArgumentException) {
            rejected = true
        }
        assertTrue(rejected)
        assertEquals(100_000L, balance())

        val validId = repository.insertAccidentLogWithExpense(
            invalid.copy(netUserCostPiastres = 0L),
            "تويوتا كورولا"
        )
        assertTrue(validId > 0)
        assertEquals(100_000L, balance())
        assertTrue(db.expenseDao().getAllExpenses().first().isEmpty())
        assertEquals(1, db.vehicleLogDao().getAccidentLogsForVehicle(vehicleId).first().size)

        repository.deleteVehicleLogById(validId, "ACCIDENT")
        assertEquals(100_000L, balance())
        assertTrue(db.vehicleLogDao().getAccidentLogsForVehicle(vehicleId).first().isEmpty())
    }

    @Test
    fun redeemCertificateAtMaturityReturnsPrincipalAndCancelsPayouts() = runBlocking {
        seedTreasury(1_200_000L)
        val certId = repository.createCertificateWithSchedules(
            FinancialCertificateEntity(
                name = "شهادة القمة",
                bankName = "البنك الأهلي",
                principalPiastres = 1_000_000L,
                annualRatePercent = "24.00",
                payoutFrequency = "MONTHLY",
                durationYears = 1,
                issueDate = 1000L,
                maturityDate = 31_536_000_000L
            )
        )
        assertTrue(certId > 0)
        assertEquals(200_000L, balance())
        assertFalse(db.certificateDao().getSchedulesForCertificate(certId).first().isEmpty())

        repository.redeemCertificate(
            certId,
            redemptionDate = 31_536_000_000L,
            isEarlyBreak = false,
            penaltyPiastres = 0L
        )

        assertEquals(1_200_000L, balance())
        val redeemed = repository.getCertificateById(certId)
        assertNotNull(redeemed)
        assertFalse(redeemed!!.isActive)
        assertFalse(redeemed.isRedeemedEarly)
        assertEquals(0L, redeemed.redemptionPenaltyPiastres)
        assertTrue(db.certificateDao().getSchedulesForCertificate(certId).first().isEmpty())
    }

    @Test
    fun redeemCertificateEarlyAppliesBankPenalty() = runBlocking {
        seedTreasury(1_200_000L)
        val certId = repository.createCertificateWithSchedules(
            FinancialCertificateEntity(
                name = "شهادة القمة",
                bankName = "البنك الأهلي",
                principalPiastres = 1_000_000L,
                annualRatePercent = "24.00",
                payoutFrequency = "MONTHLY",
                durationYears = 1,
                issueDate = 1000L,
                maturityDate = 31_536_000_000L
            )
        )
        assertEquals(200_000L, balance())

        repository.redeemCertificate(
            certId,
            redemptionDate = 1_000_000_000L,
            isEarlyBreak = true,
            penaltyPiastres = 50_000L
        )

        assertEquals(1_150_000L, balance())
        val redeemed = repository.getCertificateById(certId)!!
        assertTrue(redeemed.isRedeemedEarly)
        assertEquals(50_000L, redeemed.redemptionPenaltyPiastres)
        assertTrue(db.certificateDao().getSchedulesForCertificate(certId).first().isEmpty())
    }

    @Test
    fun deleteCertificateReversesPurchaseAndCollectedPayouts() = runBlocking {
        seedTreasury(1_200_000L)
        val certId = repository.createCertificateWithSchedules(
            FinancialCertificateEntity(
                name = "شهادة القمة",
                bankName = "البنك الأهلي",
                principalPiastres = 1_000_000L,
                annualRatePercent = "24.00",
                payoutFrequency = "MONTHLY",
                durationYears = 1,
                issueDate = 1000L,
                maturityDate = 31_536_000_000L
            )
        )
        assertEquals(200_000L, balance())

        val schedule = db.certificateDao().getSchedulesForCertificateDirect(certId).first()
        repository.collectCertificatePayout(schedule.scheduleId, collectedDate = 2_000L)
        assertEquals(220_000L, balance())
        assertEquals(1, db.revenueDao().getAllRevenues().first().size)

        val cert = repository.getCertificateById(certId)!!
        repository.deleteCertificate(cert)

        assertEquals(1_200_000L, balance())
        assertTrue(db.revenueDao().getAllRevenues().first().isEmpty())
        assertNull(repository.getCertificateById(certId))
    }

    @Test
    fun updateRevenueReconcilesTreasuryAcrossAccrualCollectionTransitions() = runBlocking {
        seedTreasury(100_000L)

        val accrual = RevenueEntity(
            amountPiastres = 10_000L,
            date = 1_000L,
            sourceType = "OTHER_INCOME",
            title = "دخل مستحق",
            isAccrualCollection = true
        )
        val accrualId = repository.insertRevenue(accrual)
        assertEquals(100_000L, balance())

        val collected = accrual.copy(
            revenueId = accrualId,
            amountPiastres = 15_000L,
            isAccrualCollection = false
        )
        repository.updateRevenue(accrual.copy(revenueId = accrualId), collected)
        assertEquals(115_000L, balance())

        val changedBackToAccrual = collected.copy(
            amountPiastres = 7_000L,
            isAccrualCollection = true
        )
        repository.updateRevenue(collected, changedBackToAccrual)
        assertEquals(100_000L, balance())

        val updatedAccrual = changedBackToAccrual.copy(amountPiastres = 9_000L)
        repository.updateRevenue(changedBackToAccrual, updatedAccrual)
        assertEquals(100_000L, balance())
    }


    @Test
    fun deletingMaturedCertificateDoesNotReturnPrincipalTwice() = runBlocking {
        seedTreasury(1_200_000L)
        val certId = repository.createCertificateWithSchedules(
            FinancialCertificateEntity(
                name = "شهادة مستحقة",
                bankName = "بنك الاختبار",
                principalPiastres = 1_000_000L,
                annualRatePercent = "20.00",
                payoutFrequency = "AT_MATURITY",
                durationYears = 1,
                issueDate = 1_000L,
                maturityDate = 31_536_000_000L
            )
        )
        val staleActiveCopy = repository.getCertificateById(certId)!!
        repository.redeemCertificate(
            certId,
            redemptionDate = 31_536_000_000L,
            isEarlyBreak = false,
            penaltyPiastres = 0L
        )
        assertEquals(1_200_000L, balance())

        repository.deleteCertificate(staleActiveCopy)

        assertEquals(1_200_000L, balance())
        assertNull(repository.getCertificateById(certId))
    }

    @Test
    fun deletingEarlyRedeemedCertificateReversesOnlyItsPenalty() = runBlocking {
        seedTreasury(1_200_000L)
        val certId = repository.createCertificateWithSchedules(
            FinancialCertificateEntity(
                name = "شهادة مكسورة",
                bankName = "بنك الاختبار",
                principalPiastres = 1_000_000L,
                annualRatePercent = "20.00",
                payoutFrequency = "MONTHLY",
                durationYears = 1,
                issueDate = 1_000L,
                maturityDate = 31_536_000_000L
            )
        )
        repository.redeemCertificate(
            certId,
            redemptionDate = 1_000_000_000L,
            isEarlyBreak = true,
            penaltyPiastres = 50_000L
        )
        assertEquals(1_150_000L, balance())

        repository.deleteCertificate(repository.getCertificateById(certId)!!)

        assertEquals(1_200_000L, balance())
        assertNull(repository.getCertificateById(certId))
    }

}
