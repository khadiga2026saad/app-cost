package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {

    @Query("SELECT * FROM vehicles ORDER BY vehicleId DESC")
    fun getAllVehicles(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveVehicles(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE vehicleId = :vehicleId LIMIT 1")
    suspend fun getVehicleById(vehicleId: Long): VehicleEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertVehicle(vehicle: VehicleEntity): Long

    @Update
    suspend fun updateVehicle(vehicle: VehicleEntity)

    @Delete
    suspend fun deleteVehicle(vehicle: VehicleEntity)

    @Query("UPDATE vehicles SET currentOdometerKm = :newOdometer WHERE vehicleId = :vehicleId AND :newOdometer > currentOdometerKm")
    suspend fun updateOdometerIfHigher(vehicleId: Long, newOdometer: Long)

    @Query("UPDATE vehicles SET lastOilChangeKm = :odometer WHERE vehicleId = :vehicleId")
    suspend fun updateLastOilChange(vehicleId: Long, odometer: Long)
}
