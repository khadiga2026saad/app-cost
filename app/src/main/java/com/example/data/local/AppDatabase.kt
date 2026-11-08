package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
import com.example.data.local.dao.HouseholdExpenseDao
import com.example.data.local.dao.MedicalExpenseDao
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
import com.example.data.local.entities.HouseholdExpenseEntity
import com.example.data.local.entities.MedicalExpenseEntity

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
        UserProfile::class,
        HouseholdExpenseEntity::class,
        MedicalExpenseEntity::class
    ],
    version = 4,
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
    abstract fun householdExpenseDao(): HouseholdExpenseDao
    abstract fun medicalExpenseDao(): MedicalExpenseDao

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
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS household_expenses (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, category TEXT NOT NULL, amountPiastres INTEGER NOT NULL, date INTEGER NOT NULL, expenseType TEXT NOT NULL, notes TEXT NOT NULL, attachmentsJson TEXT NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_household_expenses_date ON household_expenses(date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_household_expenses_category ON household_expenses(category)")
                db.execSQL("CREATE TABLE IF NOT EXISTS medical_expenses (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, patientName TEXT NOT NULL, facilityType TEXT NOT NULL, facilityName TEXT NOT NULL, conditionDescription TEXT NOT NULL, doctorDiagnosis TEXT NOT NULL, checkupCostPiastres INTEGER NOT NULL, medicationCostPiastres INTEGER NOT NULL, date INTEGER NOT NULL, notes TEXT NOT NULL, attachmentsJson TEXT NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_medical_expenses_date ON medical_expenses(date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_medical_expenses_patientName ON medical_expenses(patientName)")
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }

    }
}
