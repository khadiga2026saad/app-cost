package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.VehicleAccidentLogEntity
import com.example.data.local.entities.VehicleFuelLogEntity
import com.example.data.local.entities.VehicleMaintenanceLogEntity
import com.example.data.local.entities.VehicleOilLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleLogDao {

    // --- Fuel Logs ---
    @Query("SELECT * FROM vehicle_fuel_logs WHERE vehicleId = :vehicleId ORDER BY odometerKm DESC, date DESC")
    fun getFuelLogsForVehicle(vehicleId: Long): Flow<List<VehicleFuelLogEntity>>

    @Query("SELECT * FROM vehicle_fuel_logs WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getFuelLogsBetween(start: Long, end: Long): Flow<List<VehicleFuelLogEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFuelLog(log: VehicleFuelLogEntity): Long

    @Update
    suspend fun updateFuelLog(log: VehicleFuelLogEntity)

    @Delete
    suspend fun deleteFuelLog(log: VehicleFuelLogEntity)

    // --- Maintenance Logs ---
    @Query("SELECT * FROM vehicle_maintenance_logs WHERE vehicleId = :vehicleId ORDER BY date DESC")
    fun getMaintenanceLogsForVehicle(vehicleId: Long): Flow<List<VehicleMaintenanceLogEntity>>

    @Query("SELECT * FROM vehicle_maintenance_logs WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getMaintenanceLogsBetween(start: Long, end: Long): Flow<List<VehicleMaintenanceLogEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMaintenanceLog(log: VehicleMaintenanceLogEntity): Long

    @Update
    suspend fun updateMaintenanceLog(log: VehicleMaintenanceLogEntity)

    @Delete
    suspend fun deleteMaintenanceLog(log: VehicleMaintenanceLogEntity)

    // --- Oil Logs ---
    @Query("SELECT * FROM vehicle_oil_logs WHERE vehicleId = :vehicleId ORDER BY odometerAtChangeKm DESC, date DESC")
    fun getOilLogsForVehicle(vehicleId: Long): Flow<List<VehicleOilLogEntity>>

    @Query("SELECT * FROM vehicle_oil_logs WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getOilLogsBetween(start: Long, end: Long): Flow<List<VehicleOilLogEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertOilLog(log: VehicleOilLogEntity): Long

    @Update
    suspend fun updateOilLog(log: VehicleOilLogEntity)

    @Delete
    suspend fun deleteOilLog(log: VehicleOilLogEntity)

    // --- Accident Logs ---
    @Query("SELECT * FROM vehicle_accident_logs WHERE vehicleId = :vehicleId ORDER BY accidentDate DESC")
    fun getAccidentLogsForVehicle(vehicleId: Long): Flow<List<VehicleAccidentLogEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAccidentLog(log: VehicleAccidentLogEntity): Long

    @Update
    suspend fun updateAccidentLog(log: VehicleAccidentLogEntity)

    @Delete
    suspend fun deleteAccidentLog(log: VehicleAccidentLogEntity)

    @Query("DELETE FROM vehicle_fuel_logs WHERE fuelLogId = :id")
    suspend fun deleteFuelLogById(id: Long)

    @Query("DELETE FROM vehicle_maintenance_logs WHERE maintenanceLogId = :id")
    suspend fun deleteMaintenanceLogById(id: Long)
}
