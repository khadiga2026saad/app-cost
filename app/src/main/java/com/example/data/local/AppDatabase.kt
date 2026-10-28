package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.core.DateUtils
import com.example.core.MoneyUtils
import com.example.data.local.dao.CertificateDao
import com.example.data.local.dao.CurrentAccountDao
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.GamaeyaDao
import com.example.data.local.dao.RevenueDao
import com.example.data.local.dao.StudentDao
import com.example.data.local.dao.TreasuryMovementDao
import com.example.data.local.dao.VehicleDao
import com.example.data.local.dao.VehicleLogDao
import com.example.data.local.dao.UserDao
import com.example.data.local.dao.ProfileDao
import com.example.data.local.entities.UserProfile
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
import com.example.data.local.entities.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CurrentAccountEntity::class,
        StudentEntity::class,
        VehicleEntity::class,
        ExpenseEntity::class,
        VehicleFuelLogEntity::class,
        VehicleMaintenanceLogEntity::class,
        VehicleOilLogEntity::class,
        VehicleAccidentLogEntity::class,
        GamaeyaEntity::class,
        GamaeyaInstallmentEntity::class,
        FinancialCertificateEntity::class,
        CertificatePayoutScheduleEntity::class,
        RevenueEntity::class,
        TreasuryMovementEntity::class,
        UserEntity::class,
        UserProfile::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao
    abstract fun userDao(): UserDao
    abstract fun currentAccountDao(): CurrentAccountDao
    abstract fun studentDao(): StudentDao
    abstract fun vehicleDao(): VehicleDao
    abstract fun vehicleLogDao(): VehicleLogDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun revenueDao(): RevenueDao
    abstract fun gamaeyaDao(): GamaeyaDao
    abstract fun certificateDao(): CertificateDao
    abstract fun treasuryMovementDao(): TreasuryMovementDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `user_profiles` (`accountKey` TEXT NOT NULL, `fullName` TEXT NOT NULL, `nickname` TEXT NOT NULL, `birthDate` TEXT, `phone` TEXT, `photoPath` TEXT, `currency` TEXT NOT NULL, `dateFormat` TEXT NOT NULL, `numberLanguage` TEXT NOT NULL, PRIMARY KEY(`accountKey`))")
            }
        }
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `users` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `username` TEXT NOT NULL, `email` TEXT NOT NULL, `passwordHash` TEXT NOT NULL, `salt` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_username` ON `users` (`username`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_email` ON `users` (`email`)")
            }
        }
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_cost_pro_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val now = DateUtils.currentEpochMillis()
            val dayMillis = 86_400_000L

            // 1. Initial Liquid Treasury (Opening Balance 25,000.00 EGP = 2,500,000 Piastres)
            val initialBalancePiastres = 2_500_000L
            database.currentAccountDao().insertOrUpdate(
                CurrentAccountEntity(
                    accountId = 1L,
                    accountName = "الخزينة الرئيسية",
                    currentBalancePiastres = initialBalancePiastres,
                    openingBalancePiastres = initialBalancePiastres,
                    lastReconciledAt = now
                )
            )

            // 2. Starter Students
            val student1Id = database.studentDao().insertStudent(
                StudentEntity(
                    fullName = "أحمد محمد",
                    academicStage = "المرحلة الابتدائية",
                    schoolOrInstitution = "مدرسة النور الخاصة",
                    academicYear = "2025/2026",
                    monthlyBudgetPiastres = 250_000L // 2,500.00 EGP
                )
            )

            val student2Id = database.studentDao().insertStudent(
                StudentEntity(
                    fullName = "سارة محمد",
                    academicStage = "المرحلة الإعدادية",
                    schoolOrInstitution = "مدرسة الأوائل الرسمية",
                    academicYear = "2025/2026",
                    monthlyBudgetPiastres = 200_000L // 2,000.00 EGP
                )
            )

            // 3. Starter Vehicle
            val vehicleId = database.vehicleDao().insertVehicle(
                VehicleEntity(
                    name = "تويوتا كورولا 2023",
                    plateNumber = "أ ب ج 4589",
                    initialOdometerKm = 35400L,
                    currentOdometerKm = 35950L,
                    fuelTankCapacityLiters = 50.0,
                    oilChangeIntervalKm = 5000L,
                    lastOilChangeKm = 35000L
                )
            )

            // 4. Starter Certificate (100,000.00 EGP = 10,000,000 Piastres at 24.0% yield)
            val certPrincipalPiastres = 10_000_000L
            val certId = database.certificateDao().insertCertificate(
                FinancialCertificateEntity(
                    name = "شهادة القمة الثلاثية",
                    bankName = "البنك الأهلي المصري",
                    principalPiastres = certPrincipalPiastres,
                    annualRatePercent = "24.00",
                    payoutFrequency = "MONTHLY",
                    durationYears = 3,
                    issueDate = now - (dayMillis * 30),
                    maturityDate = now + (dayMillis * 365 * 3)
                )
            )

            // Certificate Schedule Generation (12 months * 3 = 36 cycles; monthly yield = 200,000 Piastres = 2,000.00 EGP)
            val monthlyYieldPiastres = 200_000L
            val schedules = (1..36).map { cycle ->
                CertificatePayoutScheduleEntity(
                    certificateId = certId,
                    cycleNumber = cycle,
                    expectedDueDate = now + (dayMillis * 30 * cycle),
                    expectedAmountPiastres = monthlyYieldPiastres,
                    payoutKey = "CERT_${certId}_CYC_${cycle}_${DateUtils.formatDate(now + (dayMillis * 30 * cycle))}"
                )
            }
            database.certificateDao().insertSchedules(schedules)

            // 5. Starter Gamaeya (Total 100,000.00 EGP = 10,000,000 Piastres, 10 members, turn 5)
            val poolValuePiastres = 10_000_000L
            val membersCount = 10
            val monthlyInstallmentPiastres = 1_000_000L // 10,000.00 EGP
            val gamaeyaId = database.gamaeyaDao().insertGamaeya(
                GamaeyaEntity(
                    name = "جمعية الزملاء 2026",
                    totalPoolValuePiastres = poolValuePiastres,
                    numberOfMembers = membersCount,
                    monthlyInstallmentPiastres = monthlyInstallmentPiastres,
                    myTurnOrder = 5,
                    startDate = now - (dayMillis * 60),
                    expectedPayoutDate = now + (dayMillis * 90)
                )
            )

            // Generate installments using Penny Distribution Algorithm
            val installmentAmounts = MoneyUtils.allocatePennyRemainder(
                monthlyInstallmentPiastres * membersCount,
                membersCount
            )
            val installments = installmentAmounts.mapIndexed { index, amt ->
                val instNum = index + 1
                val due = now - (dayMillis * 60) + (dayMillis * 30 * index)
                val isAlreadyPaid = instNum <= 2
                GamaeyaInstallmentEntity(
                    gamaeyaId = gamaeyaId,
                    installmentNumber = instNum,
                    dueDate = due,
                    paidDate = if (isAlreadyPaid) due else null,
                    amountPiastres = amt,
                    isPaid = isAlreadyPaid,
                    idempotencyKey = "GAM_${gamaeyaId}_INST_$instNum"
                )
            }
            database.gamaeyaDao().insertInstallments(installments)

            // 6. Starter Expenses (all in Long piastres)
            val initialExpenses = listOf(
                ExpenseEntity(
                    amountPiastres = 145_000L, // 1,450.00 EGP
                    date = now - (dayMillis * 1),
                    department = "HOME",
                    subcategory = "تموين ومستلزمات",
                    title = "مشتريات غذائية شهرية",
                    notes = "سوبرماركت الهايبر"
                ),
                ExpenseEntity(
                    amountPiastres = 65_000L, // 650.00 EGP
                    date = now - (dayMillis * 2),
                    department = "HOME",
                    subcategory = "فواتير ومرافق",
                    title = "فاتورة الكهرباء والغاز",
                    notes = "سداد تطبيق فوري"
                ),
                ExpenseEntity(
                    amountPiastres = 45_000L, // 450.00 EGP
                    date = now - (dayMillis * 3),
                    department = "MEDICAL",
                    subcategory = "أدوية وصيدليات",
                    title = "شراء أدوية وفيتامينات",
                    notes = "صيدلية الحي"
                ),
                ExpenseEntity(
                    amountPiastres = 35_000L, // 350.00 EGP
                    date = now - (dayMillis * 4),
                    department = "PERSONAL",
                    subcategory = "عناية ومظهر شخصي",
                    title = "مستلزمات شخصية",
                    notes = ""
                ),
                ExpenseEntity(
                    amountPiastres = 85_000L, // 850.00 EGP
                    date = now - (dayMillis * 5),
                    department = "EDUCATION",
                    subcategory = "رسوم دراسية",
                    title = "قسط اشتراك الباص والملازم",
                    notes = "الفصل الدراسي الثاني",
                    studentId = student1Id
                ),
                ExpenseEntity(
                    amountPiastres = 50_000L, // 500.00 EGP
                    date = now - (dayMillis * 6),
                    department = "EDUCATION",
                    subcategory = "كتب وأدوات مدرسية",
                    title = "كتب خارجية وأدوات رسم",
                    notes = "مكتبة النهضة",
                    studentId = student2Id
                )
            )

            for (expense in initialExpenses) {
                database.expenseDao().insertExpense(expense)
            }

            // 7. Starter Vehicle Fuel with linked expense
            val fuelCostPiastres = 42_000L // 420.00 EGP
            val fuelExpenseId = database.expenseDao().insertExpense(
                ExpenseEntity(
                    amountPiastres = fuelCostPiastres,
                    date = now - (dayMillis * 2),
                    department = "VEHICLES",
                    subcategory = "وقود وتعبئات",
                    title = "وقود 95 كامل - تويوتا كورولا",
                    vehicleId = vehicleId
                )
            )

            database.vehicleLogDao().insertFuelLog(
                VehicleFuelLogEntity(
                    vehicleId = vehicleId,
                    date = now - (dayMillis * 2),
                    odometerKm = 35950L,
                    quantityLiters = 35.0,
                    pricePerLiterPiastres = 1_200L, // 12.00 EGP / L
                    totalCostPiastres = fuelCostPiastres,
                    isFullTank = true,
                    stationName = "محطة وطنية",
                    linkedExpenseId = fuelExpenseId
                )
            )

            // 8. Starter Revenues (Salary + Freelance)
            database.revenueDao().insertRevenue(
                RevenueEntity(
                    amountPiastres = 2_200_000L, // 22,000.00 EGP
                    date = now - (dayMillis * 10),
                    sourceType = "SALARY",
                    title = "الراتب الشهري الأساسي",
                    payerOrInstitution = "الشركة الدولية للحلول التقنية"
                )
            )

            database.revenueDao().insertRevenue(
                RevenueEntity(
                    amountPiastres = 400_000L, // 4,000.00 EGP
                    date = now - (dayMillis * 4),
                    sourceType = "ADDITIONAL_FREELANCE",
                    title = "مشروع استشاري تقني مستقل",
                    payerOrInstitution = "مكتب المستشار الهندسي"
                )
            )
        }
    }
}
