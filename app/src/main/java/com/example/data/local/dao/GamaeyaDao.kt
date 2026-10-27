package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.GamaeyaEntity
import com.example.data.local.entities.GamaeyaInstallmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GamaeyaDao {

    // --- Gamaeya Headers ---
    @Query("SELECT * FROM gamaeyat ORDER BY startDate DESC")
    fun getAllGamaeyat(): Flow<List<GamaeyaEntity>>

    @Query("SELECT * FROM gamaeyat WHERE status = 'ACTIVE' ORDER BY startDate DESC")
    fun getActiveGamaeyat(): Flow<List<GamaeyaEntity>>

    @Query("SELECT * FROM gamaeyat WHERE gamaeyaId = :id LIMIT 1")
    suspend fun getGamaeyaById(id: Long): GamaeyaEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertGamaeya(gamaeya: GamaeyaEntity): Long

    @Update
    suspend fun updateGamaeya(gamaeya: GamaeyaEntity)

    @Delete
    suspend fun deleteGamaeya(gamaeya: GamaeyaEntity)

    // --- Installments ---
    @Query("SELECT * FROM gamaeya_installments WHERE gamaeyaId = :gamaeyaId ORDER BY installmentNumber ASC")
    fun getInstallmentsForGamaeya(gamaeyaId: Long): Flow<List<GamaeyaInstallmentEntity>>

    @Query("SELECT * FROM gamaeya_installments WHERE isPaid = 0 ORDER BY dueDate ASC")
    fun getAllUnpaidInstallments(): Flow<List<GamaeyaInstallmentEntity>>

    @Query("SELECT * FROM gamaeya_installments WHERE installmentId = :id LIMIT 1")
    suspend fun getInstallmentById(id: Long): GamaeyaInstallmentEntity?

    @Query("SELECT * FROM gamaeya_installments WHERE idempotencyKey = :key LIMIT 1")
    suspend fun getInstallmentByIdempotencyKey(key: String): GamaeyaInstallmentEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertInstallments(installments: List<GamaeyaInstallmentEntity>)

    @Update
    suspend fun updateInstallment(installment: GamaeyaInstallmentEntity)

    @Query("UPDATE gamaeya_installments SET isPaid = 1, paidDate = :paidDate WHERE installmentId = :id")
    suspend fun markInstallmentPaid(id: Long, paidDate: Long)

    @Query("UPDATE gamaeya_installments SET isPaid = 0, paidDate = NULL WHERE installmentId = :id")
    suspend fun markInstallmentUnpaid(id: Long)
}
