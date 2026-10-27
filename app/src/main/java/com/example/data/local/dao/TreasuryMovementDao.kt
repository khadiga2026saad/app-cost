package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.TreasuryMovementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TreasuryMovementDao {

    @Query("SELECT * FROM treasury_movements ORDER BY date DESC, movementId DESC")
    fun getAllMovements(): Flow<List<TreasuryMovementEntity>>

    @Query("SELECT * FROM treasury_movements WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getMovementsBetween(start: Long, end: Long): Flow<List<TreasuryMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMovement(movement: TreasuryMovementEntity): Long
}
