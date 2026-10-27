package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.CertificatePayoutScheduleEntity
import com.example.data.local.entities.FinancialCertificateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CertificateDao {

    // --- Certificates ---
    @Query("SELECT * FROM financial_certificates ORDER BY issueDate DESC")
    fun getAllCertificates(): Flow<List<FinancialCertificateEntity>>

    @Query("SELECT * FROM financial_certificates WHERE isActive = 1 ORDER BY issueDate DESC")
    fun getActiveCertificates(): Flow<List<FinancialCertificateEntity>>

    @Query("SELECT * FROM financial_certificates WHERE certificateId = :id LIMIT 1")
    suspend fun getCertificateById(id: Long): FinancialCertificateEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCertificate(cert: FinancialCertificateEntity): Long

    @Update
    suspend fun updateCertificate(cert: FinancialCertificateEntity)

    @Delete
    suspend fun deleteCertificate(cert: FinancialCertificateEntity)

    // --- Payout Schedules ---
    @Query("SELECT * FROM certificate_payout_schedules WHERE certificateId = :certId ORDER BY cycleNumber ASC")
    fun getSchedulesForCertificate(certId: Long): Flow<List<CertificatePayoutScheduleEntity>>

    @Query("SELECT * FROM certificate_payout_schedules WHERE isCollected = 0 AND expectedDueDate <= :now ORDER BY expectedDueDate ASC")
    fun getDueUncollectedSchedules(now: Long): Flow<List<CertificatePayoutScheduleEntity>>

    @Query("SELECT * FROM certificate_payout_schedules WHERE scheduleId = :id LIMIT 1")
    suspend fun getScheduleById(id: Long): CertificatePayoutScheduleEntity?

    @Query("SELECT * FROM certificate_payout_schedules WHERE payoutKey = :key LIMIT 1")
    suspend fun getScheduleByPayoutKey(key: String): CertificatePayoutScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSchedules(schedules: List<CertificatePayoutScheduleEntity>)

    @Update
    suspend fun updateSchedule(schedule: CertificatePayoutScheduleEntity)

    @Query("UPDATE certificate_payout_schedules SET isCollected = 1, actualCollectedDate = :collectedDate, linkedRevenueId = :revenueId WHERE scheduleId = :id")
    suspend fun markScheduleCollected(id: Long, collectedDate: Long, revenueId: Long)
}
