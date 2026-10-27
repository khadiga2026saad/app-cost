package com.example.data.repository

import androidx.room.withTransaction
import com.example.core.DateUtils
import com.example.core.MoneyUtils
import com.example.data.local.AppDatabase
import com.example.data.local.entities.CertificatePayoutScheduleEntity
import com.example.data.local.entities.CurrentAccountEntity
import com.example.data.local.entities.ExpenseEntity
import com.example.data.local.entities.FinancialCertificateEntity
import com.example.data.local.entities.GamaeyaEntity
import com.example.data.local.entities.GamaeyaInstallmentEntity
import com.example.data.local.entities.RevenueEntity
import com.example.data.local.entities.StudentEntity
import com.example.data.local.entities.TreasuryMovementEntity
import com.example.data.local.entities.VehicleAccidentLogEntity
import com.example.data.local.entities.VehicleEntity
import com.example.data.local.entities.VehicleFuelLogEntity
import com.example.data.local.entities.VehicleMaintenanceLogEntity
import com.example.data.local.entities.VehicleOilLogEntity
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface FinanceRepository {
    // Treasury
    fun getAccountFlow(): Flow<CurrentAccountEntity?>
    suspend fun getAccountDirect(): CurrentAccountEntity?
    suspend fun recordTreasuryMovement(type: String, amountPiastres: Long, reason: String): Long

    // Expenses
    fun getAllExpenses(): Flow<List<ExpenseEntity>>
    fun getExpensesBetween(startEpoch: Long, endEpoch: Long): Flow<List<ExpenseEntity>>
    fun getExpensesByDepartmentBetween(department: String, startEpoch: Long, endEpoch: Long): Flow<List<ExpenseEntity>>
    fun getExpensesByStudent(studentId: Long): Flow<List<ExpenseEntity>>
    fun getExpensesByStudentBetween(studentId: Long, startEpoch: Long, endEpoch: Long): Flow<List<ExpenseEntity>>
    fun getExpensesByVehicle(vehicleId: Long): Flow<List<ExpenseEntity>>
    suspend fun getExpenseById(id: Long): ExpenseEntity?
    suspend fun insertExpense(expense: ExpenseEntity): Long
    suspend fun updateExpense(oldExpense: ExpenseEntity, newExpense: ExpenseEntity)
    suspend fun deleteExpense(expense: ExpenseEntity)
    suspend fun deleteExpenseById(id: Long)

    // Revenues
    fun getAllRevenues(): Flow<List<RevenueEntity>>
    fun getRevenuesBetween(startEpoch: Long, endEpoch: Long): Flow<List<RevenueEntity>>
    suspend fun getRevenueById(id: Long): RevenueEntity?
    suspend fun insertRevenue(revenue: RevenueEntity): Long
    suspend fun updateRevenue(oldRevenue: RevenueEntity, newRevenue: RevenueEntity)
    suspend fun deleteRevenue(revenue: RevenueEntity)
    suspend fun deleteRevenueById(id: Long)

    // Students
    fun getAllStudents(): Flow<List<StudentEntity>>
    fun getActiveStudents(): Flow<List<StudentEntity>>
    suspend fun getStudentById(id: Long): StudentEntity?
    suspend fun insertStudent(student: StudentEntity): Long
    suspend fun updateStudent(student: StudentEntity)
    suspend fun deleteStudent(student: StudentEntity)

    // Vehicles
    fun getAllVehicles(): Flow<List<VehicleEntity>>
    fun getActiveVehicles(): Flow<List<VehicleEntity>>
    suspend fun getVehicleById(id: Long): VehicleEntity?
    suspend fun insertVehicle(vehicle: VehicleEntity): Long
    suspend fun updateVehicle(vehicle: VehicleEntity)
    suspend fun deleteVehicle(vehicle: VehicleEntity)

    // Vehicle Logs
    fun getFuelLogsForVehicle(vehicleId: Long): Flow<List<VehicleFuelLogEntity>>
    fun getFuelLogsBetween(startEpoch: Long, endEpoch: Long): Flow<List<VehicleFuelLogEntity>>
    suspend fun insertFuelLogWithExpense(log: VehicleFuelLogEntity, vehicleName: String): Long
    fun getMaintenanceLogsForVehicle(vehicleId: Long): Flow<List<VehicleMaintenanceLogEntity>>
    fun getMaintenanceLogsBetween(startEpoch: Long, endEpoch: Long): Flow<List<VehicleMaintenanceLogEntity>>
    suspend fun insertMaintenanceLogWithExpense(log: VehicleMaintenanceLogEntity, vehicleName: String): Long
    fun getOilLogsForVehicle(vehicleId: Long): Flow<List<VehicleOilLogEntity>>
    fun getOilLogsBetween(startEpoch: Long, endEpoch: Long): Flow<List<VehicleOilLogEntity>>
    suspend fun insertOilLogWithExpense(log: VehicleOilLogEntity, vehicleName: String): Long
    fun getAccidentLogsForVehicle(vehicleId: Long): Flow<List<VehicleAccidentLogEntity>>
    suspend fun insertAccidentLogWithExpense(log: VehicleAccidentLogEntity, vehicleName: String): Long
    suspend fun deleteVehicleLogById(id: Long, logType: String = "")

    // Gamaeyat
    fun getAllGamaeyat(): Flow<List<GamaeyaEntity>>
    fun getActiveGamaeyat(): Flow<List<GamaeyaEntity>>
    suspend fun getGamaeyaById(id: Long): GamaeyaEntity?
    suspend fun createGamaeyaWithInstallments(gamaeya: GamaeyaEntity): Long
    fun getInstallmentsForGamaeya(gamaeyaId: Long): Flow<List<GamaeyaInstallmentEntity>>
    fun getAllUnpaidInstallments(): Flow<List<GamaeyaInstallmentEntity>>
    suspend fun payGamaeyaInstallment(installmentId: Long, paidDate: Long)
    suspend fun collectGamaeyaPayout(gamaeyaId: Long, collectedDate: Long)

    // Certificates
    fun getAllCertificates(): Flow<List<FinancialCertificateEntity>>
    fun getActiveCertificates(): Flow<List<FinancialCertificateEntity>>
    suspend fun getCertificateById(id: Long): FinancialCertificateEntity?
    suspend fun createCertificateWithSchedules(cert: FinancialCertificateEntity): Long
    suspend fun deleteCertificate(cert: FinancialCertificateEntity)
    fun getSchedulesForCertificate(certId: Long): Flow<List<CertificatePayoutScheduleEntity>>
    fun getDueUncollectedSchedules(now: Long): Flow<List<CertificatePayoutScheduleEntity>>
    suspend fun collectCertificatePayout(scheduleId: Long, collectedDate: Long)
}

class FinanceRepositoryImpl(private val database: AppDatabase) : FinanceRepository {
    private val currentAccountDao = database.currentAccountDao()
    private val expenseDao = database.expenseDao()
    private val revenueDao = database.revenueDao()
    private val studentDao = database.studentDao()
    private val vehicleDao = database.vehicleDao()
    private val vehicleLogDao = database.vehicleLogDao()
    private val gamaeyaDao = database.gamaeyaDao()
    private val certificateDao = database.certificateDao()
    private val treasuryMovementDao = database.treasuryMovementDao()

    override fun getAccountFlow(): Flow<CurrentAccountEntity?> = currentAccountDao.getAccountFlow()

    override suspend fun getAccountDirect(): CurrentAccountEntity? = currentAccountDao.getAccountDirect()

    override suspend fun recordTreasuryMovement(type: String, amountPiastres: Long, reason: String): Long =
        database.withTransaction {
            val account = currentAccountDao.getAccountDirect()
                ?: CurrentAccountEntity(accountId = 1L, currentBalancePiastres = 0L)
            val current = account.currentBalancePiastres
            val newBalance = when (type) {
                "DEPOSIT" -> current + amountPiastres
                "WITHDRAWAL" -> current - amountPiastres
                else -> amountPiastres
            }
            val now = DateUtils.currentEpochMillis()
            currentAccountDao.updateBalance(newBalance, now)

            treasuryMovementDao.insertMovement(
                TreasuryMovementEntity(
                    movementType = type,
                    amountPiastres = amountPiastres,
                    date = now,
                    reason = reason,
                    balanceBeforePiastres = current,
                    balanceAfterPiastres = newBalance
                )
            )
        }

    override fun getAllExpenses(): Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    override fun getExpensesBetween(startEpoch: Long, endEpoch: Long): Flow<List<ExpenseEntity>> =
        expenseDao.getExpensesBetween(startEpoch, endEpoch)

    override fun getExpensesByDepartmentBetween(
        department: String,
        startEpoch: Long,
        endEpoch: Long
    ): Flow<List<ExpenseEntity>> =
        expenseDao.getExpensesByDepartmentBetween(department, startEpoch, endEpoch)

    override fun getExpensesByStudent(studentId: Long): Flow<List<ExpenseEntity>> =
        expenseDao.getExpensesByStudent(studentId)

    override fun getExpensesByStudentBetween(
        studentId: Long,
        startEpoch: Long,
        endEpoch: Long
    ): Flow<List<ExpenseEntity>> =
        expenseDao.getExpensesByStudentBetween(studentId, startEpoch, endEpoch)

    override fun getExpensesByVehicle(vehicleId: Long): Flow<List<ExpenseEntity>> =
        expenseDao.getExpensesByVehicle(vehicleId)

    override suspend fun getExpenseById(id: Long): ExpenseEntity? = expenseDao.getExpenseById(id)

    override suspend fun insertExpense(expense: ExpenseEntity): Long = database.withTransaction {
        require(expense.amountPiastres > 0) { "Expense amount must be greater than zero" }
        val id = expenseDao.insertExpense(expense)
        // Deduct from Treasury atomically
        val account = currentAccountDao.getAccountDirect()
            ?: CurrentAccountEntity(accountId = 1L, currentBalancePiastres = 0L)
        val newBalance = account.currentBalancePiastres - expense.amountPiastres
        currentAccountDao.updateBalance(newBalance, DateUtils.currentEpochMillis())
        id
    }

    override suspend fun updateExpense(oldExpense: ExpenseEntity, newExpense: ExpenseEntity) =
        database.withTransaction {
            require(newExpense.amountPiastres > 0) { "Updated expense amount must be greater than zero" }
            expenseDao.updateExpense(newExpense)
            val delta = newExpense.amountPiastres - oldExpense.amountPiastres
            if (delta != 0L) {
                val account = currentAccountDao.getAccountDirect()
                    ?: CurrentAccountEntity(accountId = 1L, currentBalancePiastres = 0L)
                val newBalance = account.currentBalancePiastres - delta
                currentAccountDao.updateBalance(newBalance, DateUtils.currentEpochMillis())
            }
        }

    override suspend fun deleteExpense(expense: ExpenseEntity) = database.withTransaction {
        expenseDao.deleteExpense(expense)
        // Refund to Treasury atomically
        val account = currentAccountDao.getAccountDirect()
            ?: CurrentAccountEntity(accountId = 1L, currentBalancePiastres = 0L)
        val newBalance = account.currentBalancePiastres + expense.amountPiastres
        currentAccountDao.updateBalance(newBalance, DateUtils.currentEpochMillis())
    }

    override suspend fun deleteExpenseById(id: Long) {
        val expense = expenseDao.getExpenseById(id)
        if (expense != null) {
            deleteExpense(expense)
        }
    }

    override fun getAllRevenues(): Flow<List<RevenueEntity>> = revenueDao.getAllRevenues()

    override fun getRevenuesBetween(startEpoch: Long, endEpoch: Long): Flow<List<RevenueEntity>> =
        revenueDao.getRevenuesBetween(startEpoch, endEpoch)

    override suspend fun getRevenueById(id: Long): RevenueEntity? = revenueDao.getRevenueById(id)

    override suspend fun insertRevenue(revenue: RevenueEntity): Long = database.withTransaction {
        require(revenue.amountPiastres > 0) { "Revenue amount must be greater than zero" }
        val id = revenueDao.insertRevenue(revenue)
        // Add to Treasury atomically if not an accrual realization
        if (!revenue.isAccrualCollection) {
            val account = currentAccountDao.getAccountDirect()
                ?: CurrentAccountEntity(accountId = 1L, currentBalancePiastres = 0L)
            val newBalance = account.currentBalancePiastres + revenue.amountPiastres
            currentAccountDao.updateBalance(newBalance, DateUtils.currentEpochMillis())
        }
        id
    }

    override suspend fun updateRevenue(oldRevenue: RevenueEntity, newRevenue: RevenueEntity) =
        database.withTransaction {
            require(newRevenue.amountPiastres > 0) { "Revenue amount must be greater than zero" }
            revenueDao.updateRevenue(newRevenue)
            val delta = newRevenue.amountPiastres - oldRevenue.amountPiastres
            if (delta != 0L && !newRevenue.isAccrualCollection) {
                val account = currentAccountDao.getAccountDirect()
                    ?: CurrentAccountEntity(accountId = 1L, currentBalancePiastres = 0L)
                val newBalance = account.currentBalancePiastres + delta
                currentAccountDao.updateBalance(newBalance, DateUtils.currentEpochMillis())
            }
        }

    override suspend fun deleteRevenue(revenue: RevenueEntity) = database.withTransaction {
        revenueDao.deleteRevenue(revenue)
        if (!revenue.isAccrualCollection) {
            val account = currentAccountDao.getAccountDirect()
                ?: CurrentAccountEntity(accountId = 1L, currentBalancePiastres = 0L)
            val newBalance = account.currentBalancePiastres - revenue.amountPiastres
            currentAccountDao.updateBalance(newBalance, DateUtils.currentEpochMillis())
        }
    }

    override suspend fun deleteRevenueById(id: Long) {
        val rev = revenueDao.getRevenueById(id)
        if (rev != null) {
            deleteRevenue(rev)
        }
    }

    override fun getAllStudents(): Flow<List<StudentEntity>> = studentDao.getAllStudents()

    override fun getActiveStudents(): Flow<List<StudentEntity>> = studentDao.getActiveStudents()

    override suspend fun getStudentById(id: Long): StudentEntity? = studentDao.getStudentById(id)

    override suspend fun insertStudent(student: StudentEntity): Long = studentDao.insertStudent(student)

    override suspend fun updateStudent(student: StudentEntity) = studentDao.updateStudent(student)

    override suspend fun deleteStudent(student: StudentEntity) = studentDao.deleteStudent(student)

    override fun getAllVehicles(): Flow<List<VehicleEntity>> = vehicleDao.getAllVehicles()

    override fun getActiveVehicles(): Flow<List<VehicleEntity>> = vehicleDao.getActiveVehicles()

    override suspend fun getVehicleById(id: Long): VehicleEntity? = vehicleDao.getVehicleById(id)

    override suspend fun insertVehicle(vehicle: VehicleEntity): Long = vehicleDao.insertVehicle(vehicle)

    override suspend fun updateVehicle(vehicle: VehicleEntity) = vehicleDao.updateVehicle(vehicle)

    override suspend fun deleteVehicle(vehicle: VehicleEntity) = vehicleDao.deleteVehicle(vehicle)

    override fun getFuelLogsForVehicle(vehicleId: Long): Flow<List<VehicleFuelLogEntity>> =
        vehicleLogDao.getFuelLogsForVehicle(vehicleId)

    override fun getFuelLogsBetween(startEpoch: Long, endEpoch: Long): Flow<List<VehicleFuelLogEntity>> =
        vehicleLogDao.getFuelLogsBetween(startEpoch, endEpoch)

    override suspend fun insertFuelLogWithExpense(log: VehicleFuelLogEntity, vehicleName: String): Long =
        database.withTransaction {
            // 1. Create linked expense under VEHICLES department
            val expense = ExpenseEntity(
                amountPiastres = log.totalCostPiastres,
                date = log.date,
                department = "VEHICLES",
                subcategory = "وقود وتعبئات",
                title = "وقود $vehicleName (${log.fuelType})",
                vehicleId = log.vehicleId
            )
            val expenseId = expenseDao.insertExpense(expense)

            // 2. Insert fuel log linked to expense
            val fuelLogWithExpense = log.copy(linkedExpenseId = expenseId)
            val logId = vehicleLogDao.insertFuelLog(fuelLogWithExpense)

            // 3. Update vehicle odometer if higher
            vehicleDao.updateOdometerIfHigher(log.vehicleId, log.odometerKm)

            // 4. Deduct cost from treasury
            val account = currentAccountDao.getAccountDirect()
                ?: CurrentAccountEntity(accountId = 1L, currentBalancePiastres = 0L)
            currentAccountDao.updateBalance(
                account.currentBalancePiastres - log.totalCostPiastres,
                DateUtils.currentEpochMillis()
            )

            logId
        }

    override fun getMaintenanceLogsForVehicle(vehicleId: Long): Flow<List<VehicleMaintenanceLogEntity>> =
        vehicleLogDao.getMaintenanceLogsForVehicle(vehicleId)

    override fun getMaintenanceLogsBetween(startEpoch: Long, endEpoch: Long): Flow<List<VehicleMaintenanceLogEntity>> =
        vehicleLogDao.getMaintenanceLogsBetween(startEpoch, endEpoch)

    override suspend fun insertMaintenanceLogWithExpense(
        log: VehicleMaintenanceLogEntity,
        vehicleName: String
    ): Long = database.withTransaction {
        val expense = ExpenseEntity(
            amountPiastres = log.totalCostPiastres,
            date = log.date,
            department = "VEHICLES",
            subcategory = "صيانة دورية وإصلاحات",
            title = "صيانة $vehicleName - ${log.description}",
            vehicleId = log.vehicleId
        )
        val expenseId = expenseDao.insertExpense(expense)
        val logId = vehicleLogDao.insertMaintenanceLog(log.copy(linkedExpenseId = expenseId))

        vehicleDao.updateOdometerIfHigher(log.vehicleId, log.odometerKm)

        val account = currentAccountDao.getAccountDirect()
            ?: CurrentAccountEntity(accountId = 1L, currentBalancePiastres = 0L)
        currentAccountDao.updateBalance(
            account.currentBalancePiastres - log.totalCostPiastres,
            DateUtils.currentEpochMillis()
        )

        logId
    }

    override fun getOilLogsForVehicle(vehicleId: Long): Flow<List<VehicleOilLogEntity>> =
        vehicleLogDao.getOilLogsForVehicle(vehicleId)

    override fun getOilLogsBetween(startEpoch: Long, endEpoch: Long): Flow<List<VehicleOilLogEntity>> =
        vehicleLogDao.getOilLogsBetween(startEpoch, endEpoch)

    override suspend fun insertOilLogWithExpense(log: VehicleOilLogEntity, vehicleName: String): Long =
        database.withTransaction {
            val expense = ExpenseEntity(
                amountPiastres = log.totalCostPiastres,
                date = log.date,
                department = "VEHICLES",
                subcategory = "زيوت وفلاتر",
                title = "تغيير زيت $vehicleName (${log.oilBrandAndGrade})",
                vehicleId = log.vehicleId
            )
            val expenseId = expenseDao.insertExpense(expense)
            val logId = vehicleLogDao.insertOilLog(log.copy(linkedExpenseId = expenseId))

            vehicleDao.updateOdometerIfHigher(log.vehicleId, log.odometerAtChangeKm)
            vehicleDao.updateLastOilChange(log.vehicleId, log.odometerAtChangeKm)

            val account = currentAccountDao.getAccountDirect()
                ?: CurrentAccountEntity(accountId = 1L, currentBalancePiastres = 0L)
            currentAccountDao.updateBalance(
                account.currentBalancePiastres - log.totalCostPiastres,
                DateUtils.currentEpochMillis()
            )

            logId
        }

    override fun getAccidentLogsForVehicle(vehicleId: Long): Flow<List<VehicleAccidentLogEntity>> =
        vehicleLogDao.getAccidentLogsForVehicle(vehicleId)

    override suspend fun insertAccidentLogWithExpense(
        log: VehicleAccidentLogEntity,
        vehicleName: String
    ): Long = database.withTransaction {
        var expenseId: Long? = null
        if (log.netUserCostPiastres > 0L) {
            val expense = ExpenseEntity(
                amountPiastres = log.netUserCostPiastres,
                date = log.accidentDate,
                department = "VEHICLES",
                subcategory = "حوادث وتلفيات",
                title = "إصلاح تلفيات حادث $vehicleName",
                vehicleId = log.vehicleId
            )
            expenseId = expenseDao.insertExpense(expense)

            val account = currentAccountDao.getAccountDirect()
                ?: CurrentAccountEntity(accountId = 1L, currentBalancePiastres = 0L)
            currentAccountDao.updateBalance(
                account.currentBalancePiastres - log.netUserCostPiastres,
                DateUtils.currentEpochMillis()
            )
        }
        vehicleLogDao.insertAccidentLog(log.copy(linkedExpenseId = expenseId))
    }

    override suspend fun deleteVehicleLogById(id: Long, logType: String): Unit = database.withTransaction {
        if (logType.equals("MAINTENANCE", ignoreCase = true)) {
            vehicleLogDao.deleteMaintenanceLogById(id)
        } else {
            vehicleLogDao.deleteFuelLogById(id)
            vehicleLogDao.deleteMaintenanceLogById(id)
        }
    }

    override fun getAllGamaeyat(): Flow<List<GamaeyaEntity>> = gamaeyaDao.getAllGamaeyat()

    override fun getActiveGamaeyat(): Flow<List<GamaeyaEntity>> = gamaeyaDao.getActiveGamaeyat()

    override suspend fun getGamaeyaById(id: Long): GamaeyaEntity? = gamaeyaDao.getGamaeyaById(id)

    override suspend fun createGamaeyaWithInstallments(gamaeya: GamaeyaEntity): Long = database.withTransaction {
        val gamaeyaId = gamaeyaDao.insertGamaeya(gamaeya)
        val dayMillis = 86_400_000L
        val totalExpected = gamaeya.monthlyInstallmentPiastres * gamaeya.numberOfMembers

        // Generate installments using Penny Distribution Algorithm
        val installmentAmounts = MoneyUtils.allocatePennyRemainder(totalExpected, gamaeya.numberOfMembers)
        val installments = installmentAmounts.mapIndexed { index, amount ->
            val instNumber = index + 1
            GamaeyaInstallmentEntity(
                gamaeyaId = gamaeyaId,
                installmentNumber = instNumber,
                dueDate = gamaeya.startDate + (dayMillis * 30 * index),
                amountPiastres = amount,
                isPaid = false,
                idempotencyKey = "GAM_${gamaeyaId}_INST_$instNumber"
            )
        }
        gamaeyaDao.insertInstallments(installments)
        gamaeyaId
    }

    override fun getInstallmentsForGamaeya(gamaeyaId: Long): Flow<List<GamaeyaInstallmentEntity>> =
        gamaeyaDao.getInstallmentsForGamaeya(gamaeyaId)

    override fun getAllUnpaidInstallments(): Flow<List<GamaeyaInstallmentEntity>> =
        gamaeyaDao.getAllUnpaidInstallments()

    override suspend fun payGamaeyaInstallment(installmentId: Long, paidDate: Long) = database.withTransaction {
        val installment = gamaeyaDao.getInstallmentById(installmentId) ?: return@withTransaction
        if (!installment.isPaid) {
            gamaeyaDao.markInstallmentPaid(installmentId, paidDate)

            // Deduct installment from Treasury (Cash Outflow, NOT an expense)
            val account = currentAccountDao.getAccountDirect()
                ?: CurrentAccountEntity(accountId = 1L, currentBalancePiastres = 0L)
            currentAccountDao.updateBalance(
                account.currentBalancePiastres - installment.amountPiastres,
                DateUtils.currentEpochMillis()
            )
        }
    }

    override suspend fun collectGamaeyaPayout(gamaeyaId: Long, collectedDate: Long) = database.withTransaction {
        val gamaeya = gamaeyaDao.getGamaeyaById(gamaeyaId) ?: return@withTransaction
        if (!gamaeya.isPayoutReceived) {
            gamaeyaDao.updateGamaeya(
                gamaeya.copy(
                    isPayoutReceived = true,
                    actualPayoutDate = collectedDate
                )
            )

            // Add full payout pool to Treasury (Cash Inflow, NOT earned income)
            val account = currentAccountDao.getAccountDirect()
                ?: CurrentAccountEntity(accountId = 1L, currentBalancePiastres = 0L)
            currentAccountDao.updateBalance(
                account.currentBalancePiastres + gamaeya.totalPoolValuePiastres,
                DateUtils.currentEpochMillis()
            )
        }
    }

    override fun getAllCertificates(): Flow<List<FinancialCertificateEntity>> = certificateDao.getAllCertificates()

    override fun getActiveCertificates(): Flow<List<FinancialCertificateEntity>> =
        certificateDao.getActiveCertificates()

    override suspend fun getCertificateById(id: Long): FinancialCertificateEntity? =
        certificateDao.getCertificateById(id)

    override suspend fun createCertificateWithSchedules(cert: FinancialCertificateEntity): Long =
        database.withTransaction {
            val certId = certificateDao.insertCertificate(cert)
            val rate = BigDecimal(cert.annualRatePercent)
            val dayMillis = 86_400_000L

            val cycleCount = when (cert.payoutFrequency.uppercase()) {
                "MONTHLY" -> cert.durationYears * 12
                "QUARTERLY" -> cert.durationYears * 4
                "SEMI_ANNUALLY" -> cert.durationYears * 2
                "ANNUALLY" -> cert.durationYears
                "AT_MATURITY" -> 1
                else -> cert.durationYears * 12
            }

            val cycleIntervalDays = when (cert.payoutFrequency.uppercase()) {
                "MONTHLY" -> 30
                "QUARTERLY" -> 90
                "SEMI_ANNUALLY" -> 180
                "ANNUALLY" -> 365
                "AT_MATURITY" -> cert.durationYears * 365
                else -> 30
            }

            val annualProfitPiastres = BigDecimal(cert.principalPiastres)
                .multiply(rate.divide(BigDecimal(100), 10, java.math.RoundingMode.HALF_UP))
                .setScale(0, java.math.RoundingMode.HALF_UP)
                .toLong()

            val periodicYieldPiastres = when (cert.payoutFrequency.uppercase()) {
                "MONTHLY" -> (BigDecimal(annualProfitPiastres).divide(BigDecimal(12), 0, java.math.RoundingMode.HALF_UP)).toLong()
                "QUARTERLY" -> (BigDecimal(annualProfitPiastres).divide(BigDecimal(4), 0, java.math.RoundingMode.HALF_UP)).toLong()
                "SEMI_ANNUALLY" -> (BigDecimal(annualProfitPiastres).divide(BigDecimal(2), 0, java.math.RoundingMode.HALF_UP)).toLong()
                "ANNUALLY" -> annualProfitPiastres
                "AT_MATURITY" -> annualProfitPiastres * cert.durationYears
                else -> (BigDecimal(annualProfitPiastres).divide(BigDecimal(12), 0, java.math.RoundingMode.HALF_UP)).toLong()
            }

            val schedules = (1..cycleCount).map { cycle ->
                val dueDate = cert.issueDate + (dayMillis * cycleIntervalDays * cycle)
                CertificatePayoutScheduleEntity(
                    certificateId = certId,
                    cycleNumber = cycle,
                    expectedDueDate = dueDate,
                    expectedAmountPiastres = periodicYieldPiastres,
                    payoutKey = "CERT_${certId}_CYC_${cycle}_${DateUtils.formatDate(dueDate)}"
                )
            }
            certificateDao.insertSchedules(schedules)

            // Deduct principal from Treasury as investment outflow (NOT an expense)
            val account = currentAccountDao.getAccountDirect()
                ?: CurrentAccountEntity(accountId = 1L, currentBalancePiastres = 0L)
            currentAccountDao.updateBalance(
                account.currentBalancePiastres - cert.principalPiastres,
                DateUtils.currentEpochMillis()
            )

            certId
        }

    override fun getSchedulesForCertificate(certId: Long): Flow<List<CertificatePayoutScheduleEntity>> =
        certificateDao.getSchedulesForCertificate(certId)

    override fun getDueUncollectedSchedules(now: Long): Flow<List<CertificatePayoutScheduleEntity>> =
        certificateDao.getDueUncollectedSchedules(now)

    override suspend fun collectCertificatePayout(scheduleId: Long, collectedDate: Long) = database.withTransaction {
        val schedule = certificateDao.getScheduleById(scheduleId) ?: return@withTransaction
        if (!schedule.isCollected) {
            val cert = certificateDao.getCertificateById(schedule.certificateId)

            // Record as earned revenue
            val revenue = RevenueEntity(
                amountPiastres = schedule.expectedAmountPiastres,
                date = collectedDate,
                sourceType = "CERTIFICATE_YIELD",
                title = "عائد دوري: ${cert?.name ?: "شهادة بنكية"}",
                payerOrInstitution = cert?.bankName,
                certificateId = schedule.certificateId,
                cycleKey = schedule.payoutKey
            )
            val revenueId = revenueDao.insertRevenue(revenue)

            certificateDao.markScheduleCollected(scheduleId, collectedDate, revenueId)

            // Add yield to Treasury (Earned income cash inflow)
            val account = currentAccountDao.getAccountDirect()
                ?: CurrentAccountEntity(accountId = 1L, currentBalancePiastres = 0L)
            currentAccountDao.updateBalance(
                account.currentBalancePiastres + schedule.expectedAmountPiastres,
                DateUtils.currentEpochMillis()
            )
        }
    }

    override suspend fun deleteCertificate(cert: FinancialCertificateEntity) {
        certificateDao.deleteCertificate(cert)
    }
}
